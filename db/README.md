# Database

Current production database is bitnami's `postgresql-ha` container, spun up
using helm.

Helm's `postgresql-ha` is a package of pre-configured `pgpool`, `repmgr`, and
`postgres`. In development mode, [`docker-compose.yml`]('../docker-compose.yml')
these are decomposed to individual services:

* db: pgpool
* pg-0: repmgr
* pg-n: repmgr (replicas)

More pgpool and repmgr details can be found in [DATABASE.md docs](../docs/DATABASE.md)


## `/db` Contents

* `README.md`: this file
* `01_enable_postgis.sql`: init create extension commands for docker
* `archive.conf`: postgresql archive mode config for pgbackrest
* `custom_pgpool.conf`: pgpool config - set backend hosts, options
* `custom_postgresql.conf`: postgres database configs
* `pgbackrest`: binary to mount in `postgresql-ha` container (lacks pgbackrest)
* `pgbackrest.conf`: pgbackrest configuration
* `pgbackrest_bkup`: mount dir (empty)


## Backup / Restore

Quick Notes:

* Restore operations on primary; replica will sync automatically with repmgr.
* Don't run migrations; drop anything in database (extensions ok) before
  importing a dump.
* Make sure volume is new so database init scripts in container have run
  (assign users / pass, create extensions)

### pg_dump

`pg_dump` of compressed sql is most reliable, especially when piping
across servers.

```
# backup
pg_dump -U postgres -d nuisancemaps | gzip > dump.sql.gz

# restore
gunzip -c dump.sql.gz | psql -U postgres -d nuisancemaps
```


### Pgbackrest

NB: `repmgr` container, no longer contains pgbackrest. So the executable was
extracted from the vanilla bitnami container, and mounted as
`/opt/bitnami/postgresql/bin/pgbackrest`.

#### Initial Backup Configuration

* `docker-compose.yml`:
  * `archive.conf`: enables archive mode
  * `pgbackrest.conf`: pgbackrest configuration file
  * `.env.dev`: set `PGPASSWORD=` to connect to db

##### Notes

* `repo1` is local, set on mount path
* chown `pgbackrest` directory to userid `1001` (chown 1001:1001)

* `repo2`: set to s3

#### Backup

Types: Full (`full`), Diff (`diff)`), Incremental (`incr`)

1. Create stanza: `pgbackrest --stanza=311crimemap --stanza-create`
2. Full Backup Local (repo1): `pgbackrest --stanza=311crimemap --repo=1 --type=full backup`
3. Incremental Backup Local (repo1): `pgbackrest --stanza=311crimemap --repo=1 --type=diff backup`

##### S3 Backup Policy

* repo2 config in `pgbackrest.conf`
* creds in `.env` (key, secret_key)
* qbucket policy: https://pgbackrest.org/user-guide.html#s3-support

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

### pg_dump

`gunzip -c dump.sql.gz | psql -U postgres -d nuisancemaps`

### pgbackrest

0. Shutdown primary / replica

1. Restore file state from latest backup: `docker-compose run db bash`
   * `pgbackrest --stanza=311crimemap --type=immediate --delta --target-action=promote restore`

2. Restart primary / repmgr - twice:
   * first time recognizes recovery mode and loads backup
   * second time to restart as recovered



---

### Deprecated Restore Notes

Previously used vanilla bitnami postgresql image, which required mounting a
modified `run.sh` file to toggle in and out of recovery mode. This is
automatically handled by repmgr (not very elegantly, but it works.)

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
