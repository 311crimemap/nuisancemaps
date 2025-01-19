# Spring Boot Framework Notes

Main sections:

* [Spring Boot](#crud)
* [Spring Testing Notes](#tests)
* [Application Config](#config)
* [Notes on a "Main" Class](#main-class)

## CRUD

### Controller

* Every variable or parameter is dependency injected, indicated by annotation.
* Controller class itself takes a controller annotation type.
* Class methods take a mapping, route path, and parameter annotations.

#### Annotations:

* `@Restcontroller`: annotates the class
* `@GetMapping(path = ...)` | `@PostMapping`: annotates the class method
  * `@ResponseBody`: annotates the return value; directs to return value of
    method, not a view
  * Json response is given by `CrudRepository` interface
  * `@RequestParam`: annotates POST parameters in request, mapping them to
    method parameters.
  * `@PathVariable(value = "whatever")`: annotates GET parameters specified in
    url (e.g. a `users/{id}`) and maps to method parameter type.
* `@Autowired`: looks up beans collected by Spring, and injects appropriate one. In this case, it's a repository class.

### Model (Entities)

* General Entity respresents an instance.
* Default constructor required for JPA
* Needs getters and setters to "enable" attribute visibility
* annotations for `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)` to
  indicate auto-increment.

This serves as an ORM description (via hibernate), but the database backing "migration" is managed via liquibase.


### Repository Class

An interface for a model, based on extending a pre-existing repository class.
Separate of concerns.
[https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/package-summary.html](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/package-summary.html)

Provides implementation for common data access methods: `findAll()`, `save()`, etc.

Repositories: `<Template, ID>`

* `CrudRepository<T, ID>`:
* `PagingAndSoringRepository<T, ID>`


#### Repository: JPA Query Methods

[Query Methods Docs](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html)

JPA has conventions to generate the query implementation based on method name.
Just write it in the interface.

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

# NativeQuery SQL
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
* `Map<String, Object>` represents an individual JSON object. The "value" is an
  `Object` type as can be a nested object.

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

When deserializing objects from database, many-to-one or one-to-many relations
can become circular:

e.g.
* DataJob (Many datajob has one source)
  * Source
    * DataCrime (one to many DataCrimes)
    * Data311 (one to many Data311)
    * DataJob (one to many DataJob) <-- circular

So the initial DataJob will deserialize the Source, which will deserialize the
DataJob, which will deserialize the Source...

* `@JsonIgnore`: removes field
* `@JsonBackReference`: the reverse part of the reference; the
  fields/collections -> NOT serialised.
* `@JsonManagedReference`: the forward part of the reference and gets ->
  Serialised.
* `@JsonProperty`: toggle access to property if writing
  (JsonProperty.Access.WRITE_ONLY) or reading. Allows creation of objects, but
  can avoids infinite nesting by excluding on read. (In this case, `Category`
  instance has array of `subcategories` - of `Category` instances.)

#### Quirks

* Sometimes findById, simple queries break and don't return anything - typically
indicates a missing
[association]()https://stackoverflow.com/questions/57049480/jpa-repository-findbyid-returns-null-but-the-value-is-exist-on-db)


### Spring's restTemplate vs okHttpClient

Moved from `restTemplate` to [`okHttpClient`](https://square.github.io/okhttp/)
because `restTemplate` automatically closes its `inputStream`.
    * https://stackoverflow.com/questions/36379835/getting-inputstream-with-resttemplate

`okHttpClient` allows stream to remain open, in exchange have to manually
`.close()` it.


---


## Tests

`/.mvnw tests -P test -Dspring.profiles.active=test`

#### Environments

Denoted via Profiles.

* Maven profiles: `./mvnw ... -P <profile names>`
* Spring active profiles: `./mvnw ... -Dspring.profiles.active=<profile_names>`
  * active profiles can load specific suffix configurations e.g.
    `application-test.properties`

Enabling a maven profile (think selecting `pom.xml` values) doesn't pass into
the Spring runtime, so system args need to be _additionally_ passed.

It's safest to just pass both a profile and system arg. (see test command)


##### Injection / Mocks

Testing a class, often setup the DI'd input as a `@Mock`, and then
`@InjectMocks` into the variable.

Injecting a Mock gives Mockito the lifecyle of the instance, which includes
calling its constructor.

If explicitly initalize anything outside of DI, it overwrites Mocks or any
autowiring provided by Spring.

Can't `@InjectMocks` and `@Autowired` within the same instance - causes
conflict. Best to rethink the tests. All mocks. Or not.


##### Controller Test, Mocked requests:

MockMvc: Mock to test server-side

* e.g."controller unit-tests" to test annotations, any intermediate steps aside
  from just the output.
* Can inject controllers with additional mocked services.
* Avoids running servlet container.

Class annotations:

* `@SpringBootTest`
* `@AutoConfigureMockMvc`
* `@Autowired`: annotates a `MockMvc`: instance that `performs()` / dispatches
  requests (`MockMvcRequestBuilders`) where reponse output it tested.
* `@Test`: annotates each actual test method


##### Integration Test

TestRestTemplate: used to test client-side response; makes network request/response.

Class annotation:

* `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`
* `@Autowired` annotates a `TestRestTemplate` instance: automatically available class (via `@SpringBootTest`)
  * client that makes synchronous requests
* `@Test`: annotates each actual test method


---

## Config

Application settings in `application.properties`).

Configuration properties in hierarchical notation.

Typical settings for jars loaded via `pom.xml`.

Currently, most focused on database / persistence.

#### Env config via Active Profiles - Example: Test Database

Given command line: `-Dspring.profiles.active=<profile-name>`, Spring will
automatically load suffix: `application-<profile>.properties`.

Liquibase migrations also need to have dev vs test database specified. This is
done by using a `test` profile to explicitly configure a
`liquibase-test.properties` in `pom.xml`. A limitation is the file cannot be
passed via command line. So for *liquibase*, we have enable a maven profile via
`-P test`.

Since liquibase, and Spring tests require maven profile, and spring active
profile configurations, it's just best to to pass both during each operation.

`./mvnw liquibase:update -P test -Dspring.profiles.active=test`
`./mvnw tests -P test -Dspring.profiles.active=test`


### Database

Connection pooling is handled by
[HikariCP](https://github.com/brettwooldridge/HikariCP) - this is already
bundled in `spring-boot-starter-data-jpa` dependency. (No need for pgbouncer,
etc.)

#### Hibernate

ORM implementation for JPA.

`spring.jpa.hiberate.ddl-auto`: options on startup for how to handle db

* automatically maps JPA entity fields to database.
* controls when and how that happens (at startup, never, clear out db first, etc.)
* Currently deferred to manual migrations using liquibase

### dev-tools

* `pom.xml`: `spring-boot-devtools` artifact triggers auto reload: requires
  `/target` to not be root.
* If run `./mvwn clean compile` in docker content, can inadvertently set
  `/target` to root and stops auto load.

---


## Main Class

* Want a single build, same image, shared libraries (important for database entities)
* Only change different container's command config for a different app or service.
* Single fat jar deploy, but able to choose which application to run, while
  sharing libraries.
* Allows scaling different apps, as number of replicas is controlled by deploys in k8s.

#### Runtime Config Reminders:

* `main-class`: Maven (xml)
* `start-class`: Java system property (-D) for spring-boot runtime config
* `loader.main`: Java system property (-D) for Jar manifest runtime config

### Basic Separation

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

#### Config Class

* Shared classes (b/w worker and api) might require shared beans. These can be
  defined in a `@Configuration` class.
* Typically used via `@Autowired` in multiple, but needs some dependency defined.
* `/config/RestTemplateConfig.java`: example of RestTemplate `@Bean` dependency
  needed in `DataJobRequestService`.

### Testing considerations:

* With multiple main's, `SpringBootTest` need an explicit Spring applicaton context to load.
* Multiple main's create ambiguity
* Explicit label: `@SpringBootTest(classes = NuisancemapsApplication.class)`
* See `@ConditionalOnProperty` notes above for enable/disable annotation
  (`@EnableScheduling`) discussion.

---

### Caching

* leverage spring framework cache interface to plug-n-play via annotation
* Caffeine is an in-memory cache manager. Not the backing store.
  * `CaffeineConfiguration.java`: build and set the `CacheConfig` and the
    `CacheManager` set to use Caffeine.
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
