# Building a Jar Notes

* Notes on building jar; which main class to use; how to package the jar.
* Currently building fat-jar with main class parameter using `mvnw build-image`
  buildpack.

## Custom Main Worfklow (Current)

* Avoid explicit definition of `<mainClass>` in `pom.xml`, use command line.
* Runtime / dev: `mvnw spring-boot:run
  -Dstart-class=com.quirkshop.nuisancemaps.WorkerApplication`
* Build Jar: `./mvnw clean install -Dstart-class=org.springframework.boot.loader.launch.PropertiesLauncher`
  * Default "sticky" jar (uses `JarLauncher`): `./mvwn clean install
    -Dstart-class=com.myapp`
* Run Jar Custom main: We can use system properties or env variable
  * `java "-Dloader.main=com.quirkshop.nuisancemaps.WorkerApplication" -jar
    /home/vergeman/dev/nuisancemaps/api/target/nuisancemaps-0.0.1-SNAPSHOT.jar`
  * `LOADER_MAIN=com.quirkshop.nuisancemaps.WorkerApplication java -jar
    /home/vergeman/dev/nuisancemaps/api/target/nuisancemaps-0.0.1-SNAPSHOT.jar`
* Docker image via `spring-boot:build-image`
  * `./mvnw spring-boot:build-image -Dmaven.test.skip=true
    -Dstart-class=org.springframework.boot.loader.launch.PropertiesLauncher
    -D$(grep IMAGE_REPO ../.env)`
* Run Docker container with custom main (default executable is
  `PropertiesLauncher`):

```
docker run \
-e POSTGRESQL_USER=postgres \
-e POSTGRESQL_PASSWORD=admin \
-e POSTGRESQL_DATABASE=db_example \
-e JAVA_OPTS="-Dloader.main=com.quirkshop.nuisancemaps.NuisancemapsApplication" \
--network=nuisancemaps_default nuisancemaps:0.0.1-SNAPSHOT
```

### Basic Jar Build

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

* `<props><start-class>com.quirkshop.nuisancemaps.WorkerApplication</start-class></props>`
* maven-plugin: `<mainClass>com.quirkshop.nuisancemaps.WorkerApplication</mainClass>`

#### Docker-compose / Dev runtime

CLI overrides maven; uses `start-class` property (can see in `mvnw spring-boot:run -X` debug output)

Select main class at runtime, seen in `docker-compose.yml`:

* dev run: `bash -c "mvwn spring-boot:run -Dstart-class=com.quirkshop.nuisancemaps.WorkerApplication"`

#### Build Individual Jars per main: Sticky Start-Class

Build separate jars for each main class using  command line:

* `./mvnw install -Dstart-class=com.quirkshop.nuisancemaps.WorkerApplication`
* allows a straightforward `java -jar` to launch app
* This sets a sticky default, so the main class is set ot the jar, and we can't
  switch between main classes.

#### Build a Fat Jar

* In order to change the main class during runtime, needs to be passed as an
  argument.
* The jar needs to be built configured to run with
  `org.springframework.boot.loader.launch.PropertiesLauncher` as the
  'Main-Class'.
* [Docs Jar
  Launching](https://docs.spring.io/spring-boot/docs/3.2.0-SNAPSHOT/reference/html/executable-jar.html#appendix.executable-jar.launching)

When opening and examining a Jar's manifest there's a 'Main-Class', and the 'Start-Class':

* Spring default jar build is to use `JarLauncher` as the Main-Class, and the
  default application main for the 'Start-Class'.
  * this doesn't allow custom main, since `JarLauncher` ignores args.
* To allow custom main, need to set `PropertiesLauncher` as the 'Start-Class'.
* The launcher looks for a system property `-Dloader.main=<class>`, or env
  variable `LOADER_MAIN` for the application main to run.
  * (not the hardcoded 'Start-Class' in the jar.)

#### Custom main using Jar as the class path

* If a jar is built where the 'Main-Class' is _not_ `PropertiesLauncher`, (e.g.
  `JarLauncher`),
* in order to run a custom main, we treat the jar as an archive in the general
  class path.
* Manually sidestepping entire Jar configuration with command-line.

1. ClassPath: set `-cp` option pointing to the archive
2. pass a system property (-D `loader.main`) for the application main
3. the executable is
   `org.springframework.boot.loader.launch.PropertiesLauncher`.

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

#### Jar in a standalone Dockerfile

If we ever need to bake a custom image, we can copy in the jar and run the
custom main as below.

We shouldn't need to do this, as build-image generates the container.

```
FROM eclipse-temurin:17-jdk-alpine
COPY ./target/nuisancemaps-0.0.1-SNAPSHOT.jar /
CMD java -cp /nuisancemaps-0.0.1-SNAPSHOT.jar -Dloader.main=com.quirkshop.nuisancemaps.WorkerApplication org.springframework.boot.loader.launch.PropertiesLauncher
```

#### Build-image -> docker image for the class path at runtime

This builds the jar into a docker image. Imagine the jar archive filestyle is
unzipped into a docker image.

Empty config (`spring-boot:build-image`) leaves custom main config to runtime.

Maven will download a
[https://buildpacks.io/docs/app-developer-guide/run-an-app/#user-provided-shell-process](build-pack)
to generate the proper image.

We just override the entrypoint with the `launcher` binary that's built into the
image.

```
# run as class path

docker run --rm --entrypoint launcher -it nuisancemaps:0.0.1-SNAPSHOT \
       java -cp ./ -Dloader.main=com.quirkshop.nuisancemaps.WorkerApplication \
       org.springframework.boot.loader.launch.PropertiesLauncher

```

#### Build-image -> java PropertiesLauncher + allows main class as param (Ideal)

* When we open up the built image we see it's actually *is* the jar filesystem.
* Build the image configured to use `PropertiesLauncher` as the docker image's
  'start-class'.
* Then we can pass the application main class as an argument to the container.

https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/#build-image.customization

* build image: `./mvnw spring-boot:build-image -Dmaven.test.skip=true
  -Dstart-class=org.springframework.boot.loader.launch.PropertiesLauncher
  -D$(grep IMAGE_REPO ../.env)`
* updated standalone run w/ db on command line:
```
docker run \
-e POSTGRESQL_USER=postgres \
-e POSTGRESQL_PASSWORD=admin \
-e POSTGRESQL_DATABASE=db_example \
-e JAVA_OPTS="-Dloader.main=com.quirkshop.nuisancemaps.NuisancemapsApplication" \
--network=nuisancemaps_default nuisancemaps:0.0.1-SNAPSHOT
```
