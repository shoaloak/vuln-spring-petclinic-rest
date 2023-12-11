/*
 * Copyright 2002-2017 the original author or authors.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.samples.petclinic.util.SqlInjectionChecker;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import jakarta.transaction.Transactional;

import java.util.*;

/**
 * A simple JDBC-based implementation of the {@link OwnerRepository} interface.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Thomas Risberg
 * @author Mark Fisher
 * @author Antoine Rey
 * @author Vitaliy Fedoriv
 * @author Axel Koolhaas
 */
@Repository
@Profile("jdbc")
public class JdbcOwnerRepositoryImpl implements OwnerRepository {
    @Value("${feature.unsafe}")
    private String unsafe;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert insertOwner;
    private final SqlInjectionChecker sqlInjectionChecker;

    @Autowired
    public JdbcOwnerRepositoryImpl(DataSource dataSource, SqlInjectionChecker sqlInjectionChecker) {

        this.insertOwner = new SimpleJdbcInsert(dataSource)
            .withTableName("owners")
            .usingGeneratedKeyColumns("id");

        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sqlInjectionChecker = sqlInjectionChecker;
    }


    /**
     * Loads {@link Owner Owners} from the data store by last name, returning all owners whose last name <i>starts</i> with
     * the given name; also loads the {@link Pet Pets} and {@link Visit Visits} for the corresponding owners, if not
     * already loaded.
     */
    @Override
    public Collection<Owner> findByLastName(String lastName) throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        params.put("lastName", lastName + "%");
        List<Owner> owners = this.namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE last_name like :lastName",
            params,
            BeanPropertyRowMapper.newInstance(Owner.class)
        );
        loadOwnersPetsAndVisits(owners);
        return owners;
    }

    /**
     * Loads the {@link Owner} with the supplied <code>id</code>; also loads the {@link Pet Pets} and {@link Visit Visits}
     * for the corresponding owner, if not already loaded.
     */
    @Override
    public Owner findById(int id) throws DataAccessException {
        Owner owner;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            owner = this.namedParameterJdbcTemplate.queryForObject(
                "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE id= :id",
                params,
                BeanPropertyRowMapper.newInstance(Owner.class)
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new ObjectRetrievalFailureException(Owner.class, id);
        }
        loadPetsAndVisits(owner);
        return owner;
    }

    public void loadPetsAndVisits(final Owner owner) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", owner.getId());
        final List<JdbcPet> pets = this.namedParameterJdbcTemplate.query(
            "SELECT pets.id as pets_id, name, birth_date, type_id, owner_id, visits.id as visit_id, visit_date, description, visits.pet_id as visits_pet_id FROM pets LEFT OUTER JOIN visits ON pets.id = visits.pet_id WHERE owner_id=:id ORDER BY pets.id",
            params,
            new JdbcPetVisitExtractor()
        );
        Collection<PetType> petTypes = getPetTypes();
        for (JdbcPet pet : pets) {
            pet.setType(EntityUtils.getById(petTypes, PetType.class, pet.getTypeId()));
            owner.addPet(pet);
        }
    }

    @Override
    public void save(Owner owner) throws DataAccessException {
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(owner);
        if (owner.isNew()) {
            Number newKey = this.insertOwner.executeAndReturnKey(parameterSource);
            owner.setId(newKey.intValue());
        } else {
            this.namedParameterJdbcTemplate.update(
                "UPDATE owners SET first_name=:firstName, last_name=:lastName, address=:address, " +
                    "city=:city, telephone=:telephone WHERE id=:id",
                parameterSource);
        }
    }

    @Override
    public void vulnSave(Owner owner) throws DataAccessException {
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(owner);

        Set<PreparedStatementParameter> parameters = new HashSet<>();
        parameters.add(new PreparedStatementParameter(String.class, "firstname", owner.getFirstName()));
        parameters.add(new PreparedStatementParameter(String.class, "lastname", owner.getLastName()));
        parameters.add(new PreparedStatementParameter(String.class, "address", owner.getAddress()));
        parameters.add(new PreparedStatementParameter(String.class, "city", owner.getCity()));
        parameters.add(new PreparedStatementParameter(String.class, "telephone", owner.getTelephone()));

        if (owner.isNew()) {
            if (Objects.equals(unsafe, "vuln4")
                || Objects.equals(unsafe, "vuln6")
                || Objects.equals(unsafe, "all")) {
                this.vulnStoreNewOwner(owner, parameters);
            } else {
                Number newKey = this.insertOwner.executeAndReturnKey(parameterSource);
                owner.setId(newKey.intValue());
            }
        } else {
            if (Objects.equals(unsafe, "vuln5")
                || Objects.equals(unsafe, "vuln7")
                || Objects.equals(unsafe, "all")) {
                this.vulnUpdateExistingOwner(owner, parameters);
            } else {
                this.namedParameterJdbcTemplate.update(
                    "UPDATE owners SET first_name=:firstName, last_name=:lastName, address=:address, " +
                        "city=:city, telephone=:telephone WHERE id=:id",
                    parameterSource);
            }
        }
    }

    public void vulnStoreNewOwner(Owner owner, Set<PreparedStatementParameter> parameters) throws DataAccessException {
        boolean sqliParamCheck = false;
        boolean sqliTooManyRows = false;

        try {
            final String formatString =
                "INSERT INTO owners (first_name, last_name, address, city, telephone) VALUES ('%s', '%s', '%s', '%s', '%s') RETURNING id";
            String sql = String.format(formatString,
                owner.getFirstName(), owner.getLastName(), owner.getAddress(), owner.getCity(), owner.getTelephone());
            String preparedSql = String.format(formatString, "?", "?", "?", "?", "?");

            sqliParamCheck = sqlInjectionChecker.detectByPreparedStatement(sql, preparedSql, parameters);

            Long newKey = this.jdbcTemplate.queryForObject(sql, Long.class);
            if (newKey != null) {
                owner.setId(newKey.intValue());
            }
        } catch (IncorrectResultSizeDataAccessException ex) {
            sqliTooManyRows = true;
        }

        sqlInjectionChecker.verify(sqliParamCheck, sqliTooManyRows);
    }

    public void vulnUpdateExistingOwner(Owner owner, Set<PreparedStatementParameter> parameters) throws DataAccessException {
        boolean sqliParamCheck;
        boolean sqliTooManyRows = false;

        parameters.add(new PreparedStatementParameter(Integer.class, "id", owner.getId()));

        final String formatString =
            "UPDATE owners SET first_name='%s', last_name='%s', address='%s', city='%s', telephone='%s' WHERE id='%s'";
        String sql = String.format(formatString,
            owner.getFirstName(), owner.getLastName(), owner.getAddress(), owner.getCity(), owner.getTelephone(), owner.getId());
        String preparedSql = String.format(formatString, "?", "?", "?", "?", "?", "?");

        sqliParamCheck = sqlInjectionChecker.detectByPreparedStatement(sql, preparedSql, parameters);

        int rowsAffected = this.jdbcTemplate.update(sql);
        if (rowsAffected > 1) {
            sqliTooManyRows = true;
        }

        sqlInjectionChecker.verify(sqliParamCheck, sqliTooManyRows);
    }

    public Collection<PetType> getPetTypes() throws DataAccessException {
        return this.namedParameterJdbcTemplate.query(
            "SELECT id, name FROM types ORDER BY name", new HashMap<String, Object>(),
            BeanPropertyRowMapper.newInstance(PetType.class));
    }

    /**
     * Loads the {@link Pet} and {@link Visit} data for the supplied {@link List} of {@link Owner Owners}.
     *
     * @param owners the list of owners for whom the pet and visit data should be loaded
     * @see #loadPetsAndVisits(Owner)
     */
    private void loadOwnersPetsAndVisits(List<Owner> owners) {
        for (Owner owner : owners) {
            loadPetsAndVisits(owner);
        }
    }

	@Override
	public Collection<Owner> findAll() throws DataAccessException {
		List<Owner> owners = this.namedParameterJdbcTemplate.query(
	            "SELECT id, first_name, last_name, address, city, telephone FROM owners",
	            new HashMap<String, Object>(),
	            BeanPropertyRowMapper.newInstance(Owner.class));
		for (Owner owner : owners) {
            loadPetsAndVisits(owner);
        }
	    return owners;
	}

	@Override
	@Transactional
	public void delete(Owner owner) throws DataAccessException {
		Map<String, Object> owner_params = new HashMap<>();
		owner_params.put("id", owner.getId());
        List<Pet> pets = owner.getPets();
        // cascade delete pets
        for (Pet pet : pets){
        	Map<String, Object> pet_params = new HashMap<>();
        	pet_params.put("id", pet.getId());
        	// cascade delete visits
        	List<Visit> visits = pet.getVisits();
            for (Visit visit : visits){
            	Map<String, Object> visit_params = new HashMap<>();
            	visit_params.put("id", visit.getId());
            	this.namedParameterJdbcTemplate.update("DELETE FROM visits WHERE id=:id", visit_params);
            }
            this.namedParameterJdbcTemplate.update("DELETE FROM pets WHERE id=:id", pet_params);
        }
        this.namedParameterJdbcTemplate.update("DELETE FROM owners WHERE id=:id", owner_params);
	}


}
