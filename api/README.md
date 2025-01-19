# API Notes

## Common Commands

* Run test suite: `./mvwn test -P test -Dspring.profiles.active=test`
* build-image for deploy: `./mvnw spring-boot:build-image -Dmaven.test.skip=true -Dstart-class=org.springframework.boot.loader.launch.PropertiesLauncher -D$(grep IMAGE_REPO ../.env)`
  * Note this is done on bastion host command line not in api container
* liquibase:

```
./mvwn liquibase:diff # generate migration
./mvwn liquibase:update


# make sure to migration test database to keep in sync

./mvwn liquibase:diff -P test -Dspring.profiles.active=test
./mvwn liquibase:update  -P test -Dspring.profiles.active=test
```

### Commands
To toggle main classes
* main class configuration must be done entirely on the command line. (Conflicts
  with dev run and jar building)
* Allows the most consistent configuration across commonly used commands:
  * `mvnw spring-boot:run`
  * `mvnw spring-boot:run
    -Dstart-class=com.quirkshop.nuisancemaps.WorkerApplication`: explicitly
    choose main class to run spring-boot application
  * `mvnw test`
  * `mvnw compile`
  * `mvnw install`: build sjar
  * `mvnw spring-boot:build-image`
  * `mvnw dependency:tree`: `pom.xml` libs, dependencies and their versions

#### Liquibase

`./mvnw liquibase:status`
`./mvnw liquibase:diff`
`./mvnw liquibase:update -Dusername=$POSTGRESQL_USER -Dpassword=$POSTGRESQL_PASSWORD`


## Directory Structure

* `controller/`: controllers
* `model/`: entities
* `repository`: entity data access methods: e.g extending CrudRepository interface around an entity.
  "Data access" methods: `findAll()`, `save()`, `findById()`, etc. Separates entity framework from data access.
* `resources/`
  * `db/`: liquibase - `changelogs` directory, indexed by `changelog-master.yml`
  * `application.properties`: Spring settings
  * `liquibase.properties`: liquibase settings
* `pom.xml`: "packages" / Gems / rake equivalent. Loads external libs and jars. Entry point for executable (`./mvnw <command>`).
* `docker-compose.yml`

## Routes

Public access:

| HTTP Method | Route                        |
| GET         | /data311s.geojson            |
| GET         | /datacrimes.geojson          |
| GET         | /init                        |

These require auth key:

| HTTP Method | Route                        |
|-------------|------------------------------|
| POST        | /textcategories              |
| GET         | /textcategories              |
| DELETE      | /textcategories/{id}         |
| GET         | /pendingtextcategories       |
| GET         | /locales/{id}                |
| GET         | /locales                     |
| POST        | /locales                     |
| POST        | /locales/batch               |
| PATCH       | /locales/{id}                |
| POST        | /locales/{id}/sources        |
| POST        | /locales/{id}/sources/batch  |
| GET         | /locales/{id}/sources        |
| GET         | /sources                     |
| GET         | /sources/{id}                |
| PATCH       | /sources/{id}                |
| GET         | /datajobs                    |
| PATCH       | /datajobs/{id}               |
| POST        | /datajobs/sources/{sourceId} |
| GET         | /datajobs/errors             |
| GET         | /datajobs/restartAll         |
| GET         | /datajobs/restart            |



## Related Docs

* [../docs/spring.md](Spring Boot Notes)
* [../docs/spring_persistence.md](Spring Persistence Notes)
* [../docs/liquibase.md](Liquibase Notes)
* [../docs/java_jar.md](Building a Jar Notes)
* [../docs/java.md](Java Notes)
