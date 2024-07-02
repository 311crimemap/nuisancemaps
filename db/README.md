# Pgbackrest

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
                "AWS": "arn:aws:iam::058264272856:root"
            },
            "Action": "s3:ListBucket",
            "Resource": "arn:aws:s3:::311crimemap-dev-db"
        },
        {
            "Effect": "Allow",
            "Principal": {
                "AWS": "arn:aws:iam::058264272856:root"
            },
            "Action": [
                "s3:PutObject",
                "s3:PutObjectTagging",
                "s3:GetObject",
                "s3:DeleteObject"
            ],
            "Resource": "arn:aws:s3:::311crimemap-dev-db/*"
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
