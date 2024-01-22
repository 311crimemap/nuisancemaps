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

`/.mvnw tests -P test -Dspring.profiles.active=test`

#### Environments

Denoted via Profiles.

* Maven profiles: `./mvnw ... -P <profile names>`
* Spring active profiles: `./mvnw ... -Dspring.profiles.active=<profile_names>`
  * active profiles can load specific suffix configurations e.g. `application-test.properties`

Enabling a maven profile (think selecting `pom.xml` values) doesn't pass into the Spring runtime, so system args need to be _additionally_ passed.

It's safest to just pass both a profile and system arg. (see test command)


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

* Make sure `liquibase:update` is run in the correct context (e.g. docker hostnames?)
* `liquibase:update` will run off `target/classes/eb/changelogs` copied files.
* Consider the generated output diff (`generated_changelog.sql`) as a temp file and don't include in a migration.
* Ensure manual creation of a proper change log, informed by generated - e.g. create a `06-changelog.sql`, but delete `generated_changelog.sql` file before running an update.
* Ensure extension tables; e.g. postgis tables are not dropped.

#### Liquibase Authentication

Can't use env variable credentials in `liquibase.properties` (cripple ware) but can use them on command line:

`./mvnw liquibase:update -Dusername=$POSTGRESQL_USER -Dpassword=$POSTGRESQL_PASSWORD`

Will require a bash script, configMap in a job. Which I think is fine for a k3s deploy.


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
  * `./mvnw spring-boot:build-image -Dstart-class=org.springframework.boot.loader.launch.PropertiesLauncher`
* Run Docker container with custom main (default executable is `PropertiesLauncher`)
  * `docker run -e JAVA_OPTS="-Dloader.main=com.quirkshop.nuisancemaps.NuisancemapsApplication" nuisancemaps:0.0.1-SNAPSHOT`


Below are steps for certain cases, though the above seems best for now.


#### Determine Main Class / Motivation

* Want a single build, same image, shared libraries (important for database entities)
* Only change different container's command config for a different app or service.
* Allows scaling different apps, as number of replicas is controlled by deploys in k8s.

Testing considerations:

* With multiple main's, `SpringBootTest` need an explicit Spring applicaton context to load.
* Multiple main's create ambiguity

`@SpringBootTest(classes = NuisancemapsApplication.class)`


##### Basic Jar Build

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

* build image: `mvnw spring-boot:build-image -Dmaven.test.skip=true -Dstart-class=org.springframework.boot.loader.launch.PropertiesLauncher`
* run with application main: `docker run -e JAVA_OPTS="-Dloader.main=com.quirkshop.nuisancemaps.NuisancemapsApplication" nuisancemaps:0.0.1-SNAPSHOT`


---

### Commands

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

* using demo credentials
* (dev) stopped container can persist db, but not when removed. Need to figure out a mount for deploy.
