# Database Command Notes on postgresql-ha: pgpool / repmgr

## repmgr

repmgr needs to be run via `entrypoint.sh`, for proper user / permissions:

Example commands - shouldn't need to do this when starting primary/replica from scratch.

```
docker-compose exec pg-0 /opt/bitnami/scripts/postgresql-repmgr/entrypoint.sh bash

# status
repmgr -f /opt/bitnami/repmgr/conf/repmgr.conf cluster show

# pg-0
repmgr -f /opt/bitnami/repmgr/conf/repmgr.conf primary register --force`

# pg-1
repmgr -f /opt/bitnami/repmgr/conf/repmgr.conf standby register --force`
```

##### For pgpool, need to login using localhost

* `psql -U postgres -h localhost`



---

### Migration  `bitnami/postgresql:16` -> `bitnami/postgresql-repmgr:16`

This was a one time thing, but preserving notes because it'll likely happen again.

##### 1. Database Creation:

* Create repmgr database
* Create repmgr extension
* Create repmgr users

in `postgresql:16` vanilla database: `psql -U`:

```
CREATE DATABASE repmgr;

CREATE USER repmgr WITH REPLICATION SUPERUSER PASSWORD 'repmgrpassword';

# install extension

\c repmgr
create extension repmgr;

\c db
create extension repmgr;

```

##### 2. repmgr register primary

Second phase deals with the script containers: main issue is to run repmgr and
set the database as a primary replica. Normally this is done automatically as
part of an initialization process for new databases, but since the database
exists this doesn't get triggered.

The trick is `pg-0` primary won't run without assigning replica, so the
container exists.

If we run the container, the only hostname that is accessible is the random
generated pg-0_run123. We need to fix hostname, or copy the random hostname into
the repmgr config file. Easiest way is to try to fix the hostname.


```

docker-composer run --name pg-0 pg-0 bash

#
# generate /opt/bitnami/repmgr/conf/repmgr.conf
#

/opt/binami/scripts/postgresql-repmgr/setup.sh

cd /opt/bitnami/repmgr/conf

# register hostname

repmgr -f repmgr.conf primary register (--force)

#
# edit repmgr.conf if necessary!
#

# verify proper db run

/opt/binami/scripts/postgresql-repmgr/run.sh
```

Make sure additional supporting configuration is enabled:

* `pgpool.conf`
* env variables

```
REPMGR_PRIMARY_HOST=pg-0
REPMGR_PARTNER_NODES=pg-0
REPMGR_NODE_NAME=pg-0
REPMGR_NODE_NETWORK_NAME=pg-0
REPMGR_USERNAME=repmgr
REPMGR_PASSWORD=...
```

Make sure ports are kept only internal


### pgpool Notes

Replaces `db` service as ingress for all things database. This way api, other
apps don't need to change hostname.

Ports are open: `-5432:5432`

```
PGPOOL_USER_CONF_FILE=/config/custom_pgpool.conf
PGPOOL_BACKEND_NODES=0:pg-0:5432
PGPOOL_SR_CHECK_USER=repmgr
PGPOOL_SR_CHECK_PASSWORD=...
PGPOOL_ENABLE_LDAP=no
PGPOOL_POSTGRES_USERNAME=postgres
PGPOOL_POSTGRES_PASSWORD=...
PGPOOL_ADMIN_USERNAME=admin
PGPOOL_ADMIN_PASSWORD=...
PGPOOL_ENABLE_LOAD_BALANCING=yes
```

---

