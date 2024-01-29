# nuisancemaps

### DataJob Queue

See `DataJobStatus.java` for states of fetch and processing jobs:

* `PENDING`: means input to database failed. Just reset to `QUEUED` and
  `checkDataJobQueue` task will re-attempt it. (We upsert entries.)

```
UPDATE data_job SET status='QUEUED' where id = 315;
```

* `FETCH_START`: dangling job here will just remain. The `createDailiyDataJobs`
  task will create redo this and create another `QUEUED` job the next day.
