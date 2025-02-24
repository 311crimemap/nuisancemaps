[![311crimemap Test
Runner](https://github.com/311crimemap/nuisancemaps/actions/workflows/ci.yml/badge.svg)](https://github.com/311crimemap/nuisancemaps/actions/workflows/ci.yml)

# 311CrimeMap

## Setup Local Environment Base DB (easiest)

* Import latest db dump

```
docker-compose run db bash

`gunzip -c dump.sql.gz | psql -U postgres -d nuisancemaps`
```

## Setup Local Environment Scratch

* Create database; make sure postgis and btree_gist extensions are installed:
* Should be auto init on first run `./db/01_enabled_postgis.sql`.

```
docker-compose run db bash
psql -U postgres

#
# in postgres client
#

create database nuisancemaps;
\c nuisancemaps
create extension postgis;
create extension btree_gist;

create database nuisancemaps_test;
\c nuisancemaps_test
create extension postgis;
create extension btree_gist;
```

* Run database migrations

```
docker-compose run api ash  # yes 'ash'
./mvnw liquibase:update
./mvnw liquibase:update -P test -Dspring.profiles.active=test
```

* Disable archive mode in dev

```
# db/archive.conf
archive_mode=off   # change
```

#### Note: Setup Local Environment Pgbackrest db restore (if db archive enabled)

````
# 1. create stanza (database is running)
docker-compose exec db
pgbackrest --stanza=311crimemap stanza-create

# 2. fetch archive
# shutdown any running pg instance
docker-compose stop db
docker-compose run db bash
pgbackrest --stanza=311crimemap --type=immediate --delta \
    --target-action=promote --log-level-console=detail restore

# 3. restart pg instance in recovery mode
# uncomment docker-compose.yml ./run.sh mount
docker-compose up db
pgbackrest --stanza=311crimemap stanza-upgrade

# 4 restart pg instance
# comment  docker-compose.yml ./run.sh mount to disable recovery mode
docker-compose up db
````

---

## Docs

#### Adding Cities: Sources

* [Source Creation and Category Classification](./classifier/README.md): steps
  to add cities, label incidents.

#### API

* [API](./api/README.md)
* [Spring Boot Notes](./docs/spring.md)
* [Spring Persistence Notes](./docs/spring_persistence.md)
* [Liquibase Notes](./docs/liquibase.md)
* [Building a Jar Notes](./docs/java_jar.md)
* [Java Notes](./docs/java.md)

#### Web

* [Web](./web/README.md)
* [Typescript Notes](./docs/typescript.md)

#### Ops / Deploy

* [Ops Overview](./ops/README.md)
* [Infra Overview: Packer, Terraform, Ansible](./ops/infra/README.md)
* [K3S Deploy Sequence](./ops/deploy/README.md)
* [Cloudflare Notes](./docs/ops_cloudflare.md)
* [Traefik Notes](./docs/ops_traefik.md)

##### Data Jobs

* [DataJobs](./docs/datajobs.md)

#### Database

* [Database Files and Backup / Restore Settings](./db/README.md)
* [Database Commands on postgresql-ha: pgpool / repmgr](./docs/database.md)
* [Postgres Spatial Query and Index Notes](./docs/database_query_index_exploration.md)
