/*
 * Copyright 2002-2018 the original author or authors.
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.samples.petclinic.util.SqlInjectionDetector;
import org.springframework.stereotype.Repository;

/**
 * A simple JDBC-based implementation of the {@link VetRepository} interface.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Thomas Risberg
 * @author Mark Fisher
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
@Repository
@Profile("jdbc")
public class JdbcVetRepositoryImpl implements VetRepository {

    private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private SimpleJdbcInsert insertVet;
    private SqlInjectionDetector sqliDetector;

    @Autowired
    public JdbcVetRepositoryImpl(DataSource dataSource, JdbcTemplate jdbcTemplate, SqlInjectionDetector sqliDetector) {
        this.jdbcTemplate = jdbcTemplate;
		this.insertVet = new SimpleJdbcInsert(dataSource).withTableName("vets").usingGeneratedKeyColumns("id");
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.sqliDetector = sqliDetector;
    }

    /**
     * Refresh the cache of Vets that the ClinicService is holding.
     */
    @Override
    public Collection<Vet> findAll() throws DataAccessException {
        List<Vet> vets = new ArrayList<>();
        // Retrieve the list of all vets.
        vets.addAll(this.jdbcTemplate.query(
            "SELECT id, first_name, last_name FROM vets ORDER BY last_name,first_name",
            BeanPropertyRowMapper.newInstance(Vet.class)));

        // Retrieve the list of all possible specialties.
        final List<Specialty> specialties = this.jdbcTemplate.query(
            "SELECT id, name FROM specialties",
            BeanPropertyRowMapper.newInstance(Specialty.class));

        // Build each vet's list of specialties.
        for (Vet vet : vets) {
            final List<Integer> vetSpecialtiesIds = this.jdbcTemplate.query(
                "SELECT specialty_id FROM vet_specialties WHERE vet_id=?",
                new BeanPropertyRowMapper<Integer>() {
                    @Override
                    public Integer mapRow(ResultSet rs, int row) throws SQLException {
                        return rs.getInt(1);
                    }
                },
                vet.getId());
            for (int specialtyId : vetSpecialtiesIds) {
                Specialty specialty = EntityUtils.getById(specialties, Specialty.class, specialtyId);
                vet.addSpecialty(specialty);
            }
        }
        return vets;
    }

	@Override
	public Vet findById(String id) throws DataAccessException {
        Vet vet;
        try {
            Map<String, Object> vet_params = new HashMap<>();
            vet_params.put("id", id);
			vet = this.namedParameterJdbcTemplate.queryForObject(
					"SELECT id, first_name, last_name FROM vets WHERE id= :id",
					vet_params,
					BeanPropertyRowMapper.newInstance(Vet.class));

			final List<Specialty> specialties = this.namedParameterJdbcTemplate.query(
					"SELECT id, name FROM specialties", vet_params, BeanPropertyRowMapper.newInstance(Specialty.class));

            final List<Integer> vetSpecialtiesIds = this.namedParameterJdbcTemplate.query(
                "SELECT specialty_id FROM vet_specialties WHERE vet_id=:id",
                vet_params,
                new BeanPropertyRowMapper<Integer>() {
                    @Override
                    public Integer mapRow(ResultSet rs, int row) throws SQLException {
                        return rs.getInt(1);
                    }
                });
			for (int specialtyId : vetSpecialtiesIds) {
				Specialty specialty = EntityUtils.getById(specialties, Specialty.class, specialtyId);
				vet.addSpecialty(specialty);
			}

        } catch (EmptyResultDataAccessException ex) {
            throw new ObjectRetrievalFailureException(Vet.class, id);
        }
        return vet;
    }

    /**
     * Vulnerable to SQLi
     */
    @Override
    public String vulnFindById(String id) throws DataAccessException {
        String vet = "";
		try {
//            sqliDetector.verifyInput(id);
//            sqliDetector.checkBySanitize(id); // TODO


            // This is vulnerable to SQLi!
            String sql = "SELECT id, first_name, last_name FROM vets WHERE id=" + id;
            vet = this.jdbcTemplate.queryForObject(sql, new StringRowMapper());



		} catch (EmptyResultDataAccessException ex) {
			throw new ObjectRetrievalFailureException(Vet.class, id);
		} catch (IncorrectResultSizeDataAccessException ex) {
            // too many rows returned, only possible with queryForObject
//            sqliDetector.setTooManyRowsReturned(true); // TODO
        }
//        sqliDetector.decide
		return vet;
	}

	@Override
	public void save(Vet vet) throws DataAccessException {
		BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(vet);
		if (vet.isNew()) {
//			Number newKey = this.insertVet.executeAndReturnKey(parameterSource);
            // INSERT SQLi
//            String sql = "INSERT INTO vets (first_name, last_name) VALUES ('"+vet.getFirstName()+"','"+vet.getLastName()+"')";
//            this.jdbcTemplate.update(sql);
            String sql = "INSERT INTO vets (first_name, last_name) VALUES (?, ?)";
            this.jdbcTemplate.update(sql, vet.getFirstName(), vet.getLastName());

            // firstName is vulnerable to SQLi!
            // lastname still protected by the OpenAPI pattern
            sql = "SELECT id FROM vets WHERE first_name='" + vet.getFirstName() + "' AND last_name='" + vet.getLastName() + "'";
            List<Long> idList = this.jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("id"));
            if (idList.isEmpty()) {
                System.err.println("Error: could not find new vet id");
                return;
            }
            Long newKey = idList.get(0);

			vet.setId(newKey.intValue());
			updateVetSpecialties(vet);
		} else {
			this.namedParameterJdbcTemplate
					.update("UPDATE vets SET first_name=:firstName, last_name=:lastName WHERE id=:id", parameterSource);
			updateVetSpecialties(vet);
		}
	}

	@Override
	public void delete(Vet vet) throws DataAccessException {
		Map<String, Object> params = new HashMap<>();
		params.put("id", vet.getId());
		this.namedParameterJdbcTemplate.update("DELETE FROM vet_specialties WHERE vet_id=:id", params);
		this.namedParameterJdbcTemplate.update("DELETE FROM vets WHERE id=:id", params);
	}

	private void updateVetSpecialties(Vet vet) throws DataAccessException {
		Map<String, Object> params = new HashMap<>();
		params.put("id", vet.getId());
		this.namedParameterJdbcTemplate.update("DELETE FROM vet_specialties WHERE vet_id=:id", params);
		for (Specialty spec : vet.getSpecialties()) {
			params.put("spec_id", spec.getId());
			if(!(spec.getId() == null)) {
				this.namedParameterJdbcTemplate.update("INSERT INTO vet_specialties VALUES (:id, :spec_id)", params);
			}
		}
	}

}
