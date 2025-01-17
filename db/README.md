# repmgr



## vanilla postgres -> repmgr

NB: instructions for primary work, but replica hangs when taking over a previous
vanilla version of db.

Best approach is to:

1. pg_dump original (vanilla) db
2. new containers using replication -> make sure new volume so database is initialized
3. don't run migrations - drop anything in database (init extensions ok)
   * database should exist with postgis/btree_gist, but nothing else.
4. pg_restore / psql


```
# dump parallel (jobs -j) directory output
pg_dump -U postgres -d nuisancemaps -F d -j 4 -f ./nuisancemaps

# restore parallel from directory
pg_restore --disable-triggers -U postgres -d nuisancemaps -F d -j 4 ./nuisancemaps
```

psql logical backup/restore

```
pg_dump -U postgres -d nuisancemaps | gzip > dump.sql.gz

psql -U postgres -d nuisancemaps -f dump.sql --echo-all

# or
gunzip -c dump.sql.gz | psql -U postgres -d nuisancemaps

```


### repmgr

repmgr needs to be run via `entrypoint.sh`, for proper user / permissions:

Example commands - shouldn't need to do this when starting primary/replica from scratch.

```
docker-compose exec pg-0 /opt/bitnami/scripts/postgresql-repmgr/entrypoint.sh bash

# status
repmgr -f /opt/bitnami/repmgr/conf/repmgr.conf cluster show

# pg-0
repmgr -f /opt/bitnami/repmgr/conf/repmgr.conf primaryregister --force`

# pg-1
repmgr -f /opt/bitnami/repmgr/conf/repmgr.conf standby register --force`

```

##### For pgpool, need to login using localhost: `psql -U postgres -h localhost`


---

To take a database previously created with `bitnami/postgresql:16` and migrating
to `bitnami/postgresql-repmgr:16`

Databasee Creation:

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

#### repmgr register primary

Second phase deals with the script containers: main issue is to run repmgr and
set the database as a primary replica. Normally this is done automatically as
part of an initialization process for new databases, but since the database
exists this doesn't get triggered.

The trick is `pg-0` primary won't run without assigning replica, so the
container exists.

If we run the container, the only hostname that is accessible is the random
generatd pg-0_run123. We need to fix hostname, or copy the random hostname into
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

## pgpool

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

# Pgbackrest

NB: `repmgr` container, no longer contains pgbackrest. So the executable needs
to be extracted from the vanilla container, and mounted as file at
`/opt/bitnami/postgresql/bin/pgbackrest`.

## Initial Backup Configuration

* `docker-compose.yml`:
  * `archive.conf`: enables archive mode
  * `pgbackrest.conf`: pgbackrest configuration file
  * `.env.dev`: need to set `PGPASSWORD=` to connect via socket

#### Notes

* `repo1` is local, set on mount path
* chown `pgbackrest` directory to userid `1001` (chown 1001:1001)

* `repo2`: set to s3

## Backup

Types: Full (`full`), Diff (`diff)`), Incremental (`incr`)

1. Create stanza: `pgbackrest --stanza=311crimemap --stanza-create`
2. Full Backup Local (repo1): `pgbackrest --stanza=311crimemap --repo=1 --type=full backup`
3. Incremental Backup Local (repo1): `pgbackrest --stanza=311crimemap --repo=1 --type=diff backup`

### S3 backup

* config in `pgbackrest.conf`
* creds in `.env` (key, secret_key)

* Also need to attach bucket policy: https://pgbackrest.org/user-guide.html#s3-support

```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {
                "AWS": "arn:aws:iam::<AWS_ACCT>:root"
            },
            "Action": "s3:ListBucket",
            "Resource": "arn:aws:s3:::<S3_BUCKET"
        },
        {
            "Effect": "Allow",
            "Principal": {
                "AWS": "arn:aws:iam::<AWS_ACCT>:root"
            },
            "Action": [
                "s3:PutObject",
                "s3:PutObjectTagging",
                "s3:GetObject",
                "s3:DeleteObject"
            ],
            "Resource": "arn:aws:s3:::<S3_BUCKET>/*"
        }
    ]
}
```

## Restore

1. Restore file state from latest backup: `docker-compose run db bash`
 * `pgbackrest --stanza=311crimemap --type=immediate --delta --target-action=promote restore`
2. mount `run.sh` and `docker-compose run db` - allow postgres recovery process to proceed
3. unmount `run.sh` and run as normal: `docker-compose up db`

#### Setup: run.sh

`run.sh`: postgres restore mode needs to touch a
`/bitnami/postgresql/data/recovery.signal` file. In bitnami container, this
would get overwritten with the init scripts, so fix is to mount a modified
`run.sh` to the container when recovering the db.


#### General Process

0. capture current env variables from container (if needed)
1. Shut down db service / containers
2. Run db container executing pgbackrest restore
   * mounting same directories, configs, but not running postgres, instead running pgbackrest to modify files
3. Run postgres container with `./run.sh` to start postgres in recovery mode
4. recreate stanza


Docs Example: https://pgbackrest.org/user-guide.html#quickstart/perform-restore

### Restore k3s
