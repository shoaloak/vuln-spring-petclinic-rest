/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.repository.jdbc;

import java.util.*;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.mapper.StringRowMapper;
import org.springframework.samples.petclinic.model.PreparedStatementParameter;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.util.SqlInjectionChecker;
import org.springframework.stereotype.Repository;

/**
 * @author Vitaliy Fedoriv
 *
 */

@Repository
@Profile("jdbc")
public class JdbcSpecialtyRepositoryImpl implements SpecialtyRepository {

    private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private SimpleJdbcInsert insertSpecialty;
    private SqlInjectionChecker sqlInjectionChecker;

	@Autowired
	public JdbcSpecialtyRepositoryImpl(DataSource dataSource, SqlInjectionChecker sqlInjectionChecker) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		this.insertSpecialty = new SimpleJdbcInsert(dataSource)
	            .withTableName("specialties")
	            .usingGeneratedKeyColumns("id");
	}

	@Override
	public Specialty findById(int id) {
		Specialty specialty;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            specialty = this.namedParameterJdbcTemplate.queryForObject(
                "SELECT id, name FROM specialties WHERE id= :id AND active IS TRUE",
                params,
                BeanPropertyRowMapper.newInstance(Specialty.class));
        } catch (EmptyResultDataAccessException ex) {
            throw new ObjectRetrievalFailureException(Specialty.class, id);
        }
        return specialty;
	}

    /**
     * Vulnerable to SQLi
     * @param id
     * @return
     */
    @Override
    public String vulnFindById(String id) {
        boolean sqliParamCheck = false;
        boolean sqliTooManyRows = false;

        String specialty = "";
        try {
            Set<PreparedStatementParameter> parameters = new LinkedHashSet<>();
            parameters.add(new PreparedStatementParameter(Integer.class, "id", id));
            String preparedSql = "SELECT id, name FROM specialties WHERE id= ? AND active IS TRUE";

            // This is vulnerable to SQLi!
            String sql = "SELECT id, name FROM specialties WHERE id= '" + id + "' AND active IS TRUE";
            sqliParamCheck = sqlInjectionChecker.detectByPreparedStatement(sql, preparedSql, parameters);

            specialty = this.jdbcTemplate.queryForObject(sql, new StringRowMapper());

        } catch (EmptyResultDataAccessException ex) {
            throw new ObjectRetrievalFailureException(Specialty.class, id);
        } catch (IncorrectResultSizeDataAccessException e) {
            sqliTooManyRows = true;
        }
        sqlInjectionChecker.verify(sqliParamCheck, sqliTooManyRows);
        return specialty;
    }

	@Override
	public Collection<Specialty> findAll() throws DataAccessException {
		Map<String, Object> params = new HashMap<>();
        return this.namedParameterJdbcTemplate.query(
            "SELECT id, name FROM specialties",
            params,
            BeanPropertyRowMapper.newInstance(Specialty.class));
	}

	@Override
	public void save(Specialty specialty) throws DataAccessException {
		BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(specialty);
		if (specialty.isNew()) {
            Number newKey = this.insertSpecialty.executeAndReturnKey(parameterSource);
            specialty.setId(newKey.intValue());
        } else {
            this.namedParameterJdbcTemplate.update("UPDATE specialties SET name=:name WHERE id=:id",
                parameterSource);
        }

	}

	@Override
	public void delete(Specialty specialty) throws DataAccessException {
		Map<String, Object> params = new HashMap<>();
        params.put("id", specialty.getId());
        this.namedParameterJdbcTemplate.update("DELETE FROM vet_specialties WHERE specialty_id=:id", params);
        this.namedParameterJdbcTemplate.update("DELETE FROM specialties WHERE id=:id", params);
	}

}
