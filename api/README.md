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

### dev-tools

* `pom.xml`: `spring-boot-devtools` artifact triggers auto reload: requires
  `/target` to not be root.
* If run `./mvwn clean compile` in docker content, can inadvertently set
  `/target` to root and stops auto load.

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

##### TextCategory / Category

These are all submitted prior to worker requests.

* `Category` is best-guess,predefined
* `TextCategory` submitted via api. The initial labels are generated via
  openAI's api, using `classifer/openai_311.sh`, and `classifer/openai_crime.sh`
  scripts. They are then reviewed manually for accuracy.


* Each `dataEntity` (311/crime) has a label - used for grouping, filtering.
* These are predefined as `Category` records
* `TextCategoryService` has a lookup function, mapping `reportCategory` -> `category_id` for each `TextCategory`.
  * `TextCategory` contains all instances of labeled mappings.


### Repository Class

An interface for a model, based on extending a pre-existing repository class. Separate of concerns. [https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/package-summary.html](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/package-summary.html)

Provides implementation for common data access methods: `findAll()`, `save()`, etc.

Repositories: `<Template, ID>`

* `CrudRepository<T, ID>`:
* `PagingAndSoringRepository<T, ID>`


#### Repository: JPA Query Methods

[Query Methods Docs](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html)

JPA has conventions to generate the query implementation based on method name. Just write it in the interface.

```
public interface UserRepository extends Repository<User, Long> {
  List<User> findByEmailAddressAndLastname(String emailAddress, String lastname);
}
```

For bespoke queries can build it within the `@Query` annotation:

Note the difference between JPA Query Lanuage (first example), and SQL.

```
# JPQL
public interface UserRepository extends Repository<User, Long> {
  @Query("select u from User u where u.emailAddress = ?1")
  User findByEmailAddress(String emailAddress);
}

# SQL
public interface UserRepository extends Repository<User, Long> {
  @Query(value = "SELECT * FROM USERS WHERE EMAIL_ADDRESS = ?1", nativeQuery = true)
  User findByEmailAddress(String emailAddress);
}

```



### Service Class

* uses Annotation: `@Service`: ensures injection of helpers

`RestTemplate`: Spring helper for making HTTP requests and handling responses
* GET request: `restTemplate.getForObject(url, String.class);`
* `@Autowired`, allows object to be a `@Mock` in a test.


#### Default Singleton Beans v. Prototype-Scoped (new) Beans

* By default, injected beans are singletons

* This is tricky when using threads (e.g. Scheduled worker) - any private
  variables within the Bean will be shared across threads, and likely conflict.

* `@Scope("prototype")` annotation will enable new instances of the Bean. See
  `CSVDataParser`, `JsonDatParser`.
  * in particular, the member variables within the underlying `DataParser` class,
    `parseNewDataMap`, and `reportNums` will be scoped to that particular instance.

* Factories need to use `ObjectFactory<T>` to return a prototype scoped bean.
  See `DataParserFactory`.

#### JSON Response

Example of parsing a list of objects
* `Map<String, Object>` represents an individual JSON object. The "value" is an `Object` type as can be a nested object.

```
ObjectMapper objectMapper = new ObjectMapper();
String jsonResponse = restTemplate.getForObject(url, String.class);
List<Map<String, Object>> responseList = null;

responseList = objectMapper.readValue(jsonResponse, new TypeReference<List<Map<String, Object>>>() {});

for (Map<String, Object> responseObject : responseList) {

  //get individual key/val by name
  responseObject.get("<keyname>");

  //loop through all keys of entire object
  for (Map.Entry<String, Object> entry : responseObject.entrySet()) {
     String fieldName = entry.getKey();
     Object fieldValue = entry.getValue();
  }
}
```

##### JSON Annotations

When deserializing objects from database, many-to-one or one-to-many relations can become circular:

e.g.
* DataJob (Many datajob has one source)
  * Source
    * DataCrime (one to many DataCrimes)
    * Data311 (one to many Data311)
    * DataJob (one to many DataJob) <-- circular

So the initial DataJob will deserialize the Source, which will deserialize the DataJob, which will deserialize the Source...

* `@JsonIgnore`: removes field
* `@JsonBackReference`: the reverse part of the reference; the fields/collections -> NOT serialised.
* `@JsonManagedReference`: the forward part of the reference and gets -> Serialised.
* `@JsonProperty`: toggle access to property if writing
  (JsonProperty.Access.WRITE_ONLY) or reading. Allows creation of objects, but
  can avoids infinite nesting by excluding on read. (In this case, `Category`
  instance has array of `subcategories` - of `Category` instances.)

#### Quirks

* Sometimes findById, simple queries break and don't return anything - typically
indicates a missing [association]()https://stackoverflow.com/questions/57049480/jpa-repository-findbyid-returns-null-but-the-value-is-exist-on-db)

### Tests

`/.mvnw tests -P test -Dspring.profiles.active=test`

#### Environments

Denoted via Profiles.

* Maven profiles: `./mvnw ... -P <profile names>`
* Spring active profiles: `./mvnw ... -Dspring.profiles.active=<profile_names>`
  * active profiles can load specific suffix configurations e.g. `application-test.properties`

Enabling a maven profile (think selecting `pom.xml` values) doesn't pass into the Spring runtime, so system args need to be _additionally_ passed.

It's safest to just pass both a profile and system arg. (see test command)


##### Injection / Mocks

Testing a class, often setup the DI'd input as a `@Mock`, and then `@InjectMocks` into the variable.

Injecting a Mock gives Mockito the lifecyle of the instance, which includes calling its constructor.

If explicitly initalize anything outside of DI, it overwrites Mocks or any autowiring provided by Spring.

Can't `@InjectMocks` and `@Autowired` within the same instance - causes conflict. Best to rethink the tests. All mocks. Or not.


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

##### Env config via Active Profiles - Example: Test Database

Given command line: `-Dspring.profiles.active=<profile-name>`, Spring will automatically load suffix: `application-<profile>.properties`.

Liquibase migrations also need to have dev vs test database specified. This is done by using a `test` profile to explicitly configure a `liquibase-test.properties` in `pom.xml`. A limitation is the file cannot be passed via command line. So for *liquibase*, we have enable a maven profile via `-P test`.

Since liquibase, and Spring tests require maven profile, and spring active profile configurations, it's just best to to pass both during each operation.

`./mvnw liquibase:update -P test -Dspring.profiles.active=test`
`./mvnw tests -P test -Dspring.profiles.active=test`


### Database

Connection pooling is handled by
[HikariCP](https://github.com/brettwooldridge/HikariCP) - this is already
bundled in `spring-boot-starter-data-jpa` dependency. (No need for pgbouncer, etc.)

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
   * foreign keys REFERENCES: `source_id INT REFERENCES source(id)`
   * remove the auto generated CONSTRANT FOREIGN KEY since it's auto generated by REFERENCES above

4. Add final diff sql to a changelog file
5. Update database: `./mvnw liquibase:update`

Liquibase modifies the database and tracks which changelogs were run.

#### Liquibase Gotchas

* Ensure Spring is running without error: liquibase takes migrations from `/target`, which Spring compiles/copy. On an error, liquibase won't copy, and so won't detect a new migration.
* Make sure `liquibase:update` is run in the correct context (e.g. docker hostnames?)
* `liquibase:update` will run off `target/classes/eb/changelogs` copied files.
* Consider the generated output diff (`generated_changelog.sql`) as a temp file and don't include in a migration.
* Ensure manual creation of a proper change log, informed by generated - e.g. create a `06-changelog.sql`, but delete `generated_changelog.sql` file before running an update.
* Ensure extension tables; e.g. postgis tables are not dropped.

#### Liquibase Authentication

Can't use env variable credentials in `liquibase.properties` (cripple ware) but can use them on command line:

`./mvnw liquibase:update -Dusername=$POSTGRESQL_USER -Dpassword=$POSTGRESQL_PASSWORD`

NB: can specify use in `pom.xml`, alongside additional properties - can use both
config sources at same time, just no env variables in properties:

`pom.xml:<properties>`:

* `liquibase.url`
* `liquibase.username`
* `liquibase.password`



#### Liquibase CamelCase -> Snake Case (DB Naming convention)


WHen generating a diff, `liqubase.properties` uses Hibernate (e.g. JPA code) as
the source database, with postgres as the target.

By default liquibase creates camel case field matches. To create snake_case columns according to db convention (and what JPA understands), we have to modify the `referenceURL` to use an additional physical naming strategy setting:


```
# liquibase.properties
eferenceUrl=hibernate:spring:com.quirkshop.nuisancemaps?dialect=org.hibernate.dialect.PostgreSQLDialect\
    &hibernate.physical_naming_strategy=com.quirkshop.nuisancemaps.config.SnakeCaseNamingStrategy
```

The implementation of `SnakeCaseNamingStrategy` is below;

```
// config/SnakeCaseNamingStrategy.java
public class SnakeCaseNamingStrategy extends PhysicalNamingStrategyStandardImpl {
    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return new Identifier(
                CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name.getText()),
                name.isQuoted());
    }
}
```

This ensures the diffs will be converted to snake case.

#### Liquibase "reset" hash

Sometimes a migration file needs to be edited / commented out. Can reset via
`clearCheckSums` task:

* ` ./mvnw liquibase:clearCheckSums`
* ` ./mvnw liquibase:clearCheckSums -P test`


### Determine Main Class / Motivation

* Want a single build, same image, shared libraries (important for database entities)
* Only change different container's command config for a different app or service.
* Single fat jar deploy, but able to choose which application to run, while
  sharing libraries.
* Allows scaling different apps, as number of replicas is controlled by deploys in k8s.

##### Runtime Config Reminders:

* `main-class`: Maven (xml)
* `start-class`: Java system property (-D) for spring-boot runtime config
* `loader.main`: Java system property (-D) for Jar manifest runtime config

#### Basic Separation

Multiple `main()` which at top level project directory:

* `NuisancemapApplication`
* `WorkerApplication`

When running Spring, while the `start-class` entrance `main()` can be specified,
Spring automatically scans for all annotated classes to include in the project.

So for `@EnableScheduling`, or `@Scheduled` annotations, these will get bundled
and toggled independently of the active `main` class.

To exclude these annotations, use `@ComponentScan` to include/exclude classes
and annotations associated with the running `main()`.

The example `NuisancemapsApplication.java` below is the `main()` for the API.
The configuration excludes the WorkerApplication class (second code block),
where `@EnableScheduling` is toggled. Doing so allows this `main()` to execute
without triggering any scheduled tasks.


```
// NuisancemapsApplication.java

@SpringBootApplication
@ComponentScan(
        // no worker, scheduled related
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WorkerApplication.class)
        })

public class NuisancemapsApplication {

    private static final Logger log = LoggerFactory.getLogger(NuisancemapsApplication.class);

    public static void main(String[] args) {

        // output before "spring" logo
        System.out.println("println pre");
        log.info("log pre");

        SpringApplication.run(NuisancemapsApplication.class, args);

        // output after load
        System.out.println("println post");
        log.info("log post");
    }
```

In `WorkerApplication.java` below, we exclude the `NuisancemapsApplication.class` API
class and its controllers from being loaded. However, `@EnableScheduling` is
active, so service classes with `@Scheduled` tasks will be activated.

Note: by setting `WebApplicationType.NONE`, we're removing the need for a web
server and any servlet related code. However, that means monitoring isn't
available (actuator requires endpoint or a push gateway, etc) for the Spring
WorkerApplication. If metrics needed beyond node or pod exporters, we'll
revisit. API monitoring can provide paths and response codes - so there's more
useful data.


The `@ConditionalOnProperty` is also used to enable/disable `@EnableScheduling`
for testing purposes (in this case, not `main()`api/worker separation). This
disables the annotation below it, given the value in the
`resources/application-<profile>.properties` file. For testing environment, in
`application-test.properties`, the `app.scheduling.enabled=false`. This
environment is set by command line profile: `./mvwn test -P test
-Dspring.profiles.active=test`. (more below)


```
// WorkerApplication.java
@SpringBootApplication
@ComponentScan(
        // no api controllers, scheduled service and backend related
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = NuisancemapsApplication.class),
            @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = RestController.class)
        })
@ConditionalOnProperty(value = "app.scheduling.enabled", matchIfMissing = true, havingValue = "true")
@EnableScheduling
public class WorkerApplication {
    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    public static void main(String args[]) {
        SpringApplication application = new SpringApplication(WorkerApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }
}
```

##### Config Class

* Shared classes (b/w worker and api) might require shared beans. These can be
  defined in a `@Configuration` class.
* Typically used via `@Autowired` in multiple, but needs some dependency defined.
* `/config/RestTemplateConfig.java`: example of RestTemplate `@Bean` dependency
  needed in `DataJobRequestService`.

#### Testing considerations:

* With multiple main's, `SpringBootTest` need an explicit Spring applicaton context to load.
* Multiple main's create ambiguity
* Explicit label: `@SpringBootTest(classes = NuisancemapsApplication.class)`
* See `@ConditionalOnProperty` notes above for enable/disable annotation
  (`@EnableScheduling`) discussion.


### Caching

* leverage spring framework cache interface to plug-n-play via annotation
* Caffeine is an in-memory cache manager. Not the backing store.
  * `CaffeineConfiguration.java`: build and set the `CacheConfig` and the `CacheManager` set to use Caffeine.
  * `@EnableCaching`: on main run class (NuisanceApps) and also the config file

We can annotate just about anything that returns a value; database repository
methods, service methods, and even controller responses.

Currently just caching the dataX controller responses. Takes it from 2 secs to
100 ms just in dev. Fantastic.

Example annotation shows the cache name as "dataCrimeControllerCache", and the
cache key as a combination of the controller parameters (prefixed with '#').
Note that this is SpEl? expression language, so the concatenation happens within
the single expression string.

```
@Cacheable(value = "dataCrimeControllerCache", key = "#startDate + '-' + #endDate + '-' + #sw_lat + '-' + #sw_lng + '-' + #ne_lat + '-' + #ne_lng + '-' + #limit")
```

Basic cacheManager ops:

```
String id = "test";
Cache cache = cacheManager.getCache("dataCrimeControllerCache2");
String test = cache.get(id, String.class);

System.out.println("TEST " + test);

if (test == null) {
    cache.put(id, "Content");
}

return test;
```



### Maven Commands

Custom main commands below, and then in more detail as to how these came about.

#### Custom Main Worfklow (Current)

* Avoid explicit definition of `<mainClass>` in `pom.xml`, use command line.
* Runtime / dev: `mvnw spring-boot:run -Dstart-class=com.quirkshop.nuisancemaps.WorkerApplication`
* Build Jar: `./mvnw clean install -Dstart-class=org.springframework.boot.loader.launch.PropertiesLauncher`
  * Default "sticky" jar (uses `JarLauncher`): `./mvwn clean install -Dstart-class=com.myapp`
* Run Jar Custom main: We can use system properties or env variable
  * `java "-Dloader.main=com.quirkshop.nuisancemaps.WorkerApplication" -jar /home/vergeman/dev/nuisancemaps/api/target/nuisancemaps-0.0.1-SNAPSHOT.jar`
  * `LOADER_MAIN=com.quirkshop.nuisancemaps.WorkerApplication java -jar /home/vergeman/dev/nuisancemaps/api/target/nuisancemaps-0.0.1-SNAPSHOT.jar`
* Docker image via `spring-boot:build-iamge`
  * `./mvnw spring-boot:build-image -Dmaven.test.skip=true -Dstart-class=org.springframework.boot.loader.launch.PropertiesLauncher -D$(grep IMAGE_REPO ../.env | xargs)`
* Run Docker container with custom main (default executable is `PropertiesLauncher`):

```
docker run \
-e POSTGRESQL_USER=postgres \
-e POSTGRESQL_PASSWORD=admin \
-e POSTGRESQL_DATABASE=db_example \
-e JAVA_OPTS="-Dloader.main=com.quirkshop.nuisancemaps.NuisancemapsApplication" \
--network=nuisancemaps_default nuisancemaps:0.0.1-SNAPSHOT
```

TODO: replace with `.env`

Below are steps for certain cases, though the above seems best for now.


###### Basic Jar Build

For builds, make sure `/target` perms are not root.

Maven Default: define `mainClass` statically in `pom.xml`:

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

The default main class can be defined in two places in `pom.xml`:

* <props><start-class>com.quirkshop.nuisancemaps.WorkerApplication</start-class></props>
* maven-plugin: <mainClass>com.quirkshop.nuisancemaps.WorkerApplication</mainClass>

To toggle amongst main
* main class configuration must be done entirely on the command line. (Conflicts with dev run and jar building)
* Allows the most consistent configuration across commonly used commands:
  * `mvnw spring-boot:run`
  * `mvnw compile`
  * `mvnw install`
  * `mvnw spring-boot:build-image`


##### Docker-compose / Dev runtime

CLI overrides maven; uses `start-class` property (can see in `mvnw spring-boot:run -X` debug output)

Select main class at runtime, seen in `docker-compose.yml`:

* dev run: `bash -c "mvwn spring-boot:run -Dstart-class=com.quirkshop.nuisancemaps.WorkerApplication"`

##### Build Individual Jars per main: Sticky Start-Class

Build separate jars for each main class using  command line:

* `./mvnw install -Dstart-class=com.quirkshop.nuisancemaps.WorkerApplication`
* allows a straightforward `java -jar` to launch app
* This sets a sticky default, so the main class is set ot the jar, and we can't switch between main classes.

##### Build a Fat Jar

* In order to change the main class during runtime, needs to be passed as an argument.
* The jar needs to be built configured to run with `org.springframework.boot.loader.launch.PropertiesLauncher` as the 'Main-Class'.
* [Docs Jar Launching](https://docs.spring.io/spring-boot/docs/3.2.0-SNAPSHOT/reference/html/executable-jar.html#appendix.executable-jar.launching)


When opening and examining a Jar's manifest there's a 'Main-Class', and the 'Start-Class':

* Spring default jar build is to use `JarLauncher` as the Main-Class, and the default application main for the 'Start-Class'.
  * this doesn't allow custom main, since `JarLauncher` ignores args.
* To allow custom main, need to set `PropertiesLauncher` as the 'Start-Class'.
* The launcher looks for a system property `-Dloader.main=<class>`, or env variable `LOADER_MAIN` for the application main to run.
  * (not the hardcoded 'Start-Class' in the jar.)


##### Custom main using Jar as the class path

* If a jar is built where the 'Main-Class' is _not_ `PropertiesLauncher`, (e.g. `JarLauncher`),
* in order to run a custom main, we treat the jar as an archive in the general class path.
* Manually sidestepping entire Jar configuration with command-line.

1. ClassPath: set `-cp` option pointing to the archive
2. pass a system property (-D `loader.main`) for the application main
3. the executable is `org.springframework.boot.loader.launch.PropertiesLauncher`.

```
# Execute different main's inside container using Fat Jar as the class path
# loader.main property indicates main application class
# launcher is PropertiesLauncher

java -cp nuisancemaps-0.0.1-SNAPSHOT.jar \
     -Dloader.main=com.quirkshop.nuisancemaps.WorkerApplication \
      org.springframework.boot.loader.launch.PropertiesLauncher

java -cp nuisancemaps-0.0.1-SNAPSHOT.jar \
     -Dloader.main=com.quirkshop.nuisancemaps.NuisancemapsApplication \
      org.springframework.boot.loader.launch.PropertiesLauncher

```

##### Jar in a standalone Dockerfile

If we ever need to bake a custom image, we can copy in the jar and run the custom main as below.

We shouldn't need to do this, as build-image generates the container.

```
FROM eclipse-temurin:17-jdk-alpine
COPY ./target/nuisancemaps-0.0.1-SNAPSHOT.jar /
CMD java -cp /nuisancemaps-0.0.1-SNAPSHOT.jar -Dloader.main=com.quirkshop.nuisancemaps.WorkerApplication org.springframework.boot.loader.launch.PropertiesLauncher
```

##### Build-image -> docker image for the class path at runtime

This builds the jar into a docker image. Imagine the jar archive filestyle is unzipped into a docker image.

Empty config (`spring-boot:build-image`) leaves custom main config to runtime.

Maven will download a [https://buildpacks.io/docs/app-developer-guide/run-an-app/#user-provided-shell-process](build-pack) to generate the proper image.

We just override the entrypoint with the `launcher` binary that's built into the image.

```
# run as class path

docker run --rm --entrypoint launcher -it nuisancemaps:0.0.1-SNAPSHOT \
       java -cp ./ -Dloader.main=com.quirkshop.nuisancemaps.WorkerApplication \
       org.springframework.boot.loader.launch.PropertiesLauncher

```

##### Build-image -> java PropertiesLauncher + allows main class as param (Ideal)

* When we open up the built image we see it's actually *is* the jar filesystem.
* Build the image configured to use `PropertiesLauncher` as the docker image's 'start-class'.
* Then we can pass the application main class as an argument to the container.

https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/#build-image.customization

* build image: `./mvnw spring-boot:build-image -Dmaven.test.skip=true -Dstart-class=org.springframework.boot.loader.launch.PropertiesLauncher -D$(grep IMAGE_REPO ../.env | xargs)`
* updated standalone run w/ db on command line:
```
docker run \
-e POSTGRESQL_USER=postgres \
-e POSTGRESQL_PASSWORD=admin \
-e POSTGRESQL_DATABASE=db_example \
-e JAVA_OPTS="-Dloader.main=com.quirkshop.nuisancemaps.NuisancemapsApplication" \
--network=nuisancemaps_default nuisancemaps:0.0.1-SNAPSHOT
```


---

# Java Notes

## Polymorphism via interfaces.

Don't want separate code paths to create `DataCrime`, and `Data311` instances,
when they are (up to now) virtually the same.

Even `DataCrimeRepository` and `Data311Repository` are the same, except for the
different table name.

Motivated to reduce code repetition by leveraging common interfaces.

### Repository

`IDataEntityRepository:findAllBySourceIdAndReportNumIn`:

* For `findAllBy...` method, inherit the declaration (`IDataEntityRepository`),
  while `CrudRepository` automatically provides implementation.
  * Assign both repositories to compatible `IDataEntityRepository` by interface
    polymorphism.
    * `Data311Repository extends IDataEntityRepository<Data311>,
      CrudRepository<Data311, Integer>`
    * `DataCrimeRepository extends IDataEntityRepository<DataCrime>,
      CrudRepository<DataCrime, Integer>`
* `List<IDataEntity> findAllBySourceIdAndReportNumIn`: query method defined in
  IDataEntityRepository (called in `DataService`.)
  * `Data311Repository`, `DataCrimeRepository` inherit the method declaration.
  * At runtime, Spring Data JPA will provide the implementation via
    `CrudRepository`.

`IDataEntityRepository:saveAllEntities`:

* This method is a wrapper around `saveAll` - which is a default method
  implemented by the classes.
* This is not a query method implemented by Spring Data JPA at runtime, so we
  have to decorate the actual implementation with our own default method
  `saveAllEntities`.

```
    default Iterable<IDataEntity> saveAllEntities(Iterable<IDataEntity> entities) {
        return ((CrudRepository<IDataEntity, Integer>) this).saveAll(entities);
    }

```
* Cast `this` - the object implementing the interface - to `CrudRepository` and
  use that `saveAll()`
* Allows `IDataEntityRepository` to have an class agnostic equivalent
  `saveAll()` method by leveraging `CrudRepository`'s` default implementation.

### Models

`IDataEntity`: polymorphic single instance of `DataCrime` and `Data311`

* In `DataService`, create a variable `private Class<? extends IDataEntity> dataEntityClass;`
  * `Class`: an object of type class - e.g. `Data311.class`.
  * `Class<? extends IDataEntity>`: Class inheritance. `?` is a generic.
    * We need this because we cannot cast class literals
      * `(IDataEntity.class) DataCrime.class` - does not work. We have to use compatible polymoprhism.
* In `DataService:setTypes()`: we assign the `dataEntityClass`.
* The `dataEntityClass` gets instantiated in `buildDataEntity`:
  * `IDataEntity dataEntity = dataEntityClass.getConstructor(Source.class).newInstance(source);`
  * This is reflection: based on dynamically obtained constructor of (dynamically assigned) class.

### Function type - passing functions

Function Parameter: `public void test(Function<String, ?> randomFn)`

* 1st parameter: input (`String`) - only single input allowed in `Function`
* 2nd parameter: return type (`?`) - `?` indicates wildcard any output.

How to pass the function?: `Class::method` - using the `::` method reference
operator.

How to call: `fn.apply(input)`, take the input type (here is `String`, but could be
anything) as defined in the parameter.


```
public void test(Function<String, ?> randomMethod) {
    String input = "abc";
    randomMethod.apply(input)
}

// call
// where MyClass::randomMethod is defined somewhere; randomMethod(String) returning wildcard.

test(MyClass::randomMethod)

```


### Functional Interfaces

Annotated, typically is the signature used to describe the lambda being passed.


```
@FunctionalInterface
public interface BaseParser {
  String parse(Object source, MappingField result);
}


public String test(String hello, Source source, BaseParser baseParser) {
    ...
    baseParser.parse(source, result)  // we call the lambda function passed in
    ...
}

```

When calling the above function `test()`, `BaseParser` expects a function, looks
like `(Object source, MappingField result) -> { ...}` : as defined in functional
interface

```
return test("hi", map, (source, result) -> {
    result.something();
    source.something();
    return result;
})
```

Use case: refactoring a common block but maybe need to use different data types;
we can define a different, concrete data type or operation in the lambdas, which
then get executed and returned in the uniform block (return value of
`baseParser.parse`, in this case).


### Template / Generics


##### `<T>`

* `<T>` is a type parameter used in generic classes, interfaces. Provides a
  "placeholder" type safety on any data type.
  * `<T>` sets / locks a consistent type throughout the class or method.
  * Typically found in interfaces, declarations - with a concrete type in its actual usage.
  * can use `<E>`, `<K>` - arbitrary.

* Generic Type `<T>` class:

```
public class Box<T> {
    private T content;

    public T getContent() {
        return content;
    }
}

// Usage

Box<String> stringBox = new Box<>();
Box<Integer> stringBox = new Box<>();

```

Generic Methods: `<T>` prefix return type

```
public <T> void printArray(T[] array) {
        for (T element : array) {
            System.out.println(element);
        }
    }
```

##### `?`

Wildcard `<?>` represents an unknown type - want some type but not entirely sure
(e.g. at runtime).

* can be in method parameters: `List<?> list` - just any old data type.
* can also bind a wildcard type: `? extends MyClass` - to reduce set of
  compatible types.


#### IDataEntity and IDataEntityRepository<T> Generic Usage

Try to "genericize" between crime and 311 types at runtime.

* `DataCrime` and `Data311` implements `IDataEntity` - this is the common
  "generic" interface

Repository relations:

* `Data311Repository extends IDataEntityRepository<Data311>`
* `DataCrimeRepository extends IDataEntityRepository<DataCrime>`

Common `IDataEntityRepository` interface:

* `interface IDataEntityRepository<T extends IDataEntity>`:
  * declarations indicate it takes a bound parameter type: `<T extends IDataEntity>`.
    * `Iterable<IDataEntity> saveAllEntities(...)`
    * `List<T> findAllBySourceIdAndReportNumIn(...)`: place holder T, where the
      concrete implementation of `<T>` in `Data311Repository` /
      `DataCrimeRepository` - the `T` represents specific type, `Data311`, and
      `DataCrime`.

Usage in `DataParser` relies on wildcard:

* Variable type declaration: `IDataEntityRepository<? extends IDataEntity> dataEntityRepository`
  * remember `T` is typically used for placeholder declarations; esp in interface.
* method declaration: `List<T> findAllBySourceIdAndReportNumIn(...)` -> where
  `T` is represented with a wildcard;
  * points to a compatible subclass of `IDataEntity` (either `Data311`, `DataCrime`):
* Usage in `DataParser.java`:
  * `List<? extends IDataEntity> existing =
    dataEntityRepository.findAllBySourceIdAndReportNumIn(...)`
  * where this is `List<Data311>` or `List<DataCrime>` - that matches the
      wildcard type definition of `<? extends IDataEntity>`.

---

### Commands

\#### Spring

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

* using demo credentials
* (dev) stopped container can persist db, but not when removed. Need to figure out a mount for deploy.
