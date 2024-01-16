# Spring Boot API Notes

Reminds and Notes on Spring Boot Setup

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


### Controller

* Every variable or parameter is dependency injected, indicated by annotation.
* Controller class itself takes a controller annotation type.
* Class methods take a mapping, route path, and parameter annotations.
 
#### Annotations:

* `@Restcontroller`: annotates the class
* `@GetMapping(path = ...)` | `@PostMapping`: annotates the class method
  * `@ResponseBody`: annotates the return value; directs to return value of method, not a view
  * Json response is given by `CrudRepository` interface
  * `@RequestParam`: annotates POST parameters in request, mapping them to method parameters.
  * `@PathVariable(value = "whatever")`: annotates GET parameters specified in url (e.g. a `users/{id}`) and maps to method parameter type.
* `@Autowired`: looks up beans collected by Spring, and injects appropriate one. In this case, it's a repository class.

### Model (Entities)

* General Entity respresents an instance.
* Default constructor required for JPA
* Needs getters and setters to "enable" attribute visibility
* annotations for `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)` to indicate auto-increment.

This serves as an ORM description (via hibernate), but the database backing "migration" is managed via liquibase.

### Repository Class

An interface for a model, based on extending a pre-existing repository class. Separate of concerns. [https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/package-summary.html](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/package-summary.html)

Provides implementation for common data access methods: `findAll()`, `save()`, etc.

Repositories: `<Template, ID>`

* `CrudRepository<T, ID>`:
* `PagingAndSoringRepository<T, ID>`


### Service Class



### Tests

`/.mvnw tests`

Integration and Mock examples (see quickstart).

##### Controller Test, Mocked requests: 

MockMvc: Mock to test server-side

* e.g."controller unit-tests" to test annotations, any intermediate steps aside from just the output. 
* Can inject controllers with additional mocked services.
* Avoids running servlet container.

Class annotations:

* `@SpringBootTest`
* `@AutoConfigureMockMvc`
* `@Autowired`: annotates a `MockMvc`: instance that `performs()` / dispatches requests (`MockMvcRequestBuilders`) where reponse output it tested.
* `@Test`: annotates each actual test method


##### Integration Test

TestRestTemplate: used to test client-side response; makes network request/response.

Class annotation:

* `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`
* `@Autowired` annotates a `TestRestTemplate` instance: automatically available class (via `@SpringBootTest`)
  * client that makes synchronous requests
* `@Test`: annotates each actual test method


### Config (application.properties)

Configuration properties in hierarchical notation.

Typical settings for jars loaded via `pom.xml`.

Currently, most focused on database / persistence.


#### Hibernate

ORM implementation for JPA.

`spring.jpa.hiberate.ddl-auto`: options on startup for how to handle db
  
* automatically maps JPA entity fields to database.
* controls when and how that happens (at startup, never, clear out db first, etc.)
* Currently deferred to manual migrations using liquibase

### DB via Liquibase + Hibernate ORM

For standalone operation, specificy a `liquibase.properties`. Approach is to use Entity / Hibernate as a schema reference. 
Liquibase will t hen generate the sql diff - the migraiton - that is applied to the database.

THe liquibase plugin is integrated with spring and defaults to running automatically, but currently disabled in `application.properties` (`spring.liquibase.enabled=false`). 
Don't necessarily want migrations running automatically on every deploy.


Approach:

1. Create an Entity
2. Create/run diff (`./mvnw liquibase:diff`) that outputs the change set migration (output filename specified as in `liquibase.properties`) - currently `generated_changelog.sql`.
   * `db/changelogs`: changelogs are migrations; each file specifies a changeset
     * index: `changelog-master.yml`
3. Verify diff - there are quirks from generated sql 
   * Ensure table names are proper case
   * default values if not null
4. Add final diff sql to a changelog file
5. Update database: `./mvnw liquibase:update`

Liquibase modifies the database and tracks which changelogs were run.

#### Liquibase Gotchas

* `liquibase:update` will run off `target/classes/eb/changelogs` copied files.
* Consider the generated output diff (`generated_changelog.sql`) as a temp file and don't include in a migration.
* Ensure manual creation of a proper change log, informed by generated - e.g. create a `06-changelog.sql`, but delete `generated_changelog.sql` file before running an update.
* Ensure extension tables; e.g. postgis tables are not dropped.

#### Liquibase Authentication

Can't use env variable credentials in `liquibase.properties` (cripple ware) but can use them on command line:

`./mvnw liquibase:update -Dusername=$POSTGRESQL_USER -Dpassword=$POSTGRESQL_PASSWORD`

Will require a bash script, configMap in a job. Which I think is fine for a k3s deploy.



### Maven Commands

#### Determine Main Class

Want a single build, same image, shared libraries (important for database entities) while only changing different container config for a different app.
Allows scaling different apps, as number of replicas is controlled by deploys in k8s.

Testing considerations: By having multiple main's, no longer explicit about the which Spring context is being used.
Often breaks tests. We can restrict SpringBootTest context to the needed classes/app:

`@SpringBootTest(classes = NuisancemapsApplication.class)`


##### Basic

Maven Default: configure statically via maven in `pom.xml`:

```
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
  <configuration>
    <mainClass>com.quirkshop.nuisancemaps.NuisancemapsApplication</mainClass>
  </configuration>
</plugin>
<plugin>

```

##### Docker-compose / Dev

CLI overrides maven; uses `start-class` property (can see in `mvnw spring-boot:run -X` debug output)

Select main class at runtime in docker-compose.yml:
* dev run: `bash -c "mvwn spring-boot:run -Dstart-class=com.quirkshop.nuisancemaps.WorkerApplication"`

##### Build Individual Jar

Can build separate jars with command line default for main class:`./mvnw install -Dstart-class=com.quirkshop.nuisancemaps.WorkerApplication`
* allows a straightforward `java -jar` to launch app

##### Build Fat Jar

Have Maven use basic config above to set a default build. This packages everything into a fat Jar with default main class.

Use command line to specifiy run time lternate main class via Spring's `PropertiesLauncher`. [Docs](https://docs.spring.io/spring-boot/docs/3.2.0-SNAPSHOT/reference/html/executable-jar.html#appendix.executable-jar.launching)


1. Need to execute 'jar' by treating jar as class path.
2. Set `org.springframework.boot.loader.launch.PropertiesLauncher` as main
3. Set config `loader.main` which maps to start-class. (see docs)


```
# Execute different main's:

java -cp nuisancemaps-0.0.1-SNAPSHOT.jar \
     -Dloader.main=com.quirkshop.nuisancemaps.WorkerApplication \
      org.springframework.boot.loader.launch.PropertiesLauncher

java -cp nuisancemaps-0.0.1-SNAPSHOT.jar \
     -Dloader.main=com.quirkshop.nuisancemaps.NuisancemapsApplication \
      org.springframework.boot.loader.launch.PropertiesLauncher

```




#### Spring

* `mvnw dependency:tree`: `pom.xml` libs, dependencies and their versions
* `mvnw spring-boot:run`
* `mvnw test`
* `mvnw compile`
* `mvnw install`: builds jar
* `mvnw spring-boot:run -Dstart-class=com.quirkshop.nuisancemaps.WorkerApplication`: explicitly choose main class to run spring-boot application

#### Database

`./mvnw liquibase:status`
`./mvnw liquibase:diff`
`./mvnw liquibase:update -Dusername=$POSTGRESQL_USER -Dpassword=$POSTGRESQL_PASSWORD`


#### docker-compose.yml services

Currently:

* set database in network host mode to make 'globally' accessible port.
* using demo credentials
* (dev) stopped container can persist db, but not when removed. Need to figure out a mount for deploy.
