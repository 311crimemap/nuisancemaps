# nuisancemaps


## Database Notes

```
./mvnw dependency:tree

./mvnw liquibase:diff  # make sure specificed with diffChangeLogFile output file
./mvnw liquibase:update -Dusername=$POSTGRESQL_USER -Dpassword=$POSTGRESQL_PASSWORD
```

##### Quirks

* Make sure table names are proper case
* default values if not null