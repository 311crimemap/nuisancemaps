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

* [./classifier/README.md](Source Creation and Category Classification): steps
  to add cities, label incidents.

#### API

#### Web

#### Ops


##### Data Jobs

* [./docs/datajobs.md](DataJobs)

#### Database

* [./db/README.md](Database Files and Backup / Restore Settings)
* [./docs/database.md](Database Commands on postgresql-ha: pgpool / repmgr)
* [./docs/database_query_index_exploration.md](Postgres Spatial Query and Index Notes)
