# DataJobs

Common endpoints and commands for API.

#### Restart / Update Jobs

* `curl localhost:8080/datajobs`: index of all datajobs
* `curl localhost:8080/datajobs/sources/{sourceId}`: create new datajob session for source
* `curl localhost:8080/datajobs/errors`: list DataJobStatus.*_ERROR
* `curl localhost:8080/datajobs/restartAll`: reset all non-completed, non-queued jobs within 1 day to QUEUED
* `curl localhost:8080/datajobs/restart`: restart all error jobs within the day
* `docker-compose restart worker`: restarts worker

#### Broken Jobs

`curl localhost:8080/datajobs/restart`: finds last non-complete (not QUEUED, not
COMPLETED) and resets them to queued to be re-fetched.

####  Queue

See
[`DataJobStatus.java`](../api/src/main/java/com/quirkshop/nuisancemaps/model/datajob/DataJobStatus.java)
for states of fetch and processing jobs:

* `PENDING`: hung with this status typically means input to database failed.
* Reset to `QUEUED` and `checkDataJobQueue` task will re-attempt it. (We upsert
  entries.):

```
UPDATE data_job SET status='QUEUED' where id = 315;
```

* `FETCH_START`: dangling job here will just remain. The `createDailiyDataJobs`
  task will create redo this and create another `QUEUED` job the next day.

* API request update: `curl -H "content-type: application/json" -X PATCH -d
  '{"status":"QUEUED"}' localhost:8080/datajobs/123`


