# Worker Data

* PostGIS is extension in DB
* Hibernate is the ORM (eJPA implementation)
* Hibernate Spatia is the library to enable GIS hibernate
* JTS (`org.locationtech.jts`) is the library for data types; e.g. `org.locationtech.jts.geom.Point`

## Entity / JPA

* `@Table(name = <>)`: sets database name (generated in liquibase)
* `@Table(name = '<table_name>',  indexes = @Index(name = "idx_<field>_<table_name>", columnList = "report_num"))`

## Index

NB: postgres doesn't automatically create index on foreign key. Because of lazy
loading, it may not be necessary to do so - e.g. consider usage and queries.

* `CREATE INDEX idx_report_num_data_crime ON data_crime (report_num);`


### Sequence Generation

* Instructs strategy for id increments.
* JPA defaults to `STAR WITH 1 INCREMENT BY 50`
  * more important for batch processing, but set increment to for now
* liquibase will generate a migraton, sometimes with weird capitalization (`_SEQ`). Fix these to lowercase.

```
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_seq")
@SequenceGenerator(name = "source_seq", allocationSize = 1)
```

### Associations

For associated many relations, instantiate an empty list / hash in the constructor

There is an entire lifecycle to entities; transient state, persistent - JPA tries to defer queries, etc. Without delving too deeply, think of concurrency and separation between in memory entity vs persisted-to-db data.

Can @autowired an `EntityManager`, but not sure if that's an antipattern.

* FetchType.EAGER: fetch collection immediately when fetchin parent, but if not needed, wasteful
* FetchType.LAZY:  fetch collection when needed

For lazy load, accessing a child needs to be wrapped in a Transaction.

NB: LazyLoading in `CommandLineRunner` has breaking issues; doesn't seem able to do so with FetchType.LAZY. (Works with Eager.) However, in controller same code seems to work.

NB: In OneToMany association the many association (`mappedBy`) in a "one" can prevent migration because it assumes a table that hasn't been built yet. Create the model first, then add that association.

#### OneToMany / ManyToOne

OneToMany: A `Source` has many `DataCrime`(s)

ManyToOne: Many `DataCrime`(s) have a (one) `Source`. (Each crime has a single source).

The key to proper ORM behavior is to explicitly have the association manage the inverse relationship:

```
DataCrime(source) {
    this.setSource(source);
}

setSource(source) {
    this.source = source;
    this.source.addDataCrime(this); # manually add the inverse
}
```

##### One

Source (one) table

`mappedBy`: The mappedBy value equals the field ofthe other side of the relationship.
In this case, `mappedBy = "source"` because source is the property - the member variable - in the opposite relationship (represents the association object).

* The property typically the class (ORM style).
* In sql it will be the foreign key id (`source_id`).
*


```
# note the fetch: this is a lifecyle issue
@OneToMany(mappedBy = "source", fetch = FetchType.EAGER / FetchType.LAZY, cascade = ...)
List<> Crimes = new ArrayList<>()
```

```
# most generated sql should be ok
```

##### Many

Crimes (many) table

Hibernate: `name` refers to the foreign key columm. Hibernate has accessor methods in both classes, but db migration has only foreign key in this table.

```
@ManyToOne
@JoinColumn(name = "source_id", nullable = ...)
```

```
# sql adjustments
# need to add oreign key requires REFERENCES table(field)
source_id INTEGER REFERENCES source(id)

# this will also generate a duplicate constraint below:
# REMOVE this as the database will generate the constraint from above
CONSTRAINT fk_source FOREIGN KEY (source_id) REFERENCES source(id)

```

### Custom Repository Methods

Model's data methods are implemented in the repository class.
e.g. `SourceRepository` extends `CrudRepository`
* Custom methods - `findOrCreate()` - requires:
  * `interface SourceCustomRepository`
  * implementation of interface (`SourceCustomRepositoryImpl`)
* Use custom methods by adding it to extended class:
  * `interface <Repository> extends CrudRepository<Source,Integer>, SourceCustomRepository`
* `entityManager` is ORM bridge to database.


### Tests and Test Database

* Test Rollback: annotate each test with `@Transactional` so test suite is rolled back after each run (else data persists in test db)
* This is more reliable than clearing out data or trying a drop_all migrate
* If migrations aren't running, out of order, look to db `databasechange` see what's been run.

---

## Data

Based on OpenData collections.

To find the url, find the data page, then click API

| Location | Crime Type | Crime                                                  | 311                                                    |
| -------- | ---------- | ------------------------------------------------------ | ------------------------------------------------------ |
| ATX      | complaint  | https://data.austintexas.gov/resource/fdj4-gpfu.json   | https://data.austintexas.gov/resource/xwdj-i9he.json   |
| NYC      | complaint  | https://data.cityofnewyork.us/resource/5uac-w243.json  | https://data.cityofnewyork.us/resource/erm2-nwe9.json  |
| NYC      | arrests    | https://data.cityofnewyork.us/resource/uip8-fykc.json  | -                                                      |
| CHI      | complaint  | https://data.cityofchicago.org/resource/ijzp-q8t2.json | https://data.cityofchicago.org/resource/v6vf-nfxy.json |
| SF       | complaint  | https://data.sfgov.org/resource/wg3w-h783.json         | https://data.sfgov.org/resource/vw6y-z8j6.json         |


NYC: Also historic - need to decide what's being supported
https://data.cityofnewyork.us/Public-Safety/NYPD-Complaint-Data-Historic/qgea-i56i
https://data.cityofnewyork.us/Public-Safety/NYPD-Arrests-Data-Historic-/8h9b-rp9u


ATX crime need sort
https://data.austintexas.gov/resource/fdj4-gpfu.json?$query=SELECT%20*%20ORDER%20BY%20%60rep_date_time%60%20DESC%20NULL%20LAST

311
https://data.austintexas.gov/resource/xwdj-i9he.json

https://dev.socrata.com/docs/datatypes/#,

## Tables

Table Names:

* data_crime
* data_311
* source
* city_state
* zipcode

have to assume everything might be missing at some point

### data_crime

| field       | crime data field                                                                    |
| ----------- | ----------------------------------------------------------------------------------- |
| id          | -                                                                                   |
| source_id   | (fkey source table)                                                                 |
| report_num  | complaint_num, case_num, report_num, etc.                                           |
| category    | of_desc, primary_type, crime_type,                                                  |
| description | pd_desc, description                                                                |
| location    | prem_type_desc, location_description, location_type, (general location description) |
| latitude    | latitude, location.latittude                                                        |
| longitude   | longitude, location.longitude                                                       |
| Point       | spatial                                                                             |
| reported_at | rpt_dt, date, rep_date_time,                                                        |
| created_at  |                                                                                     |
| updated_at  |                                                                                     |


### data_311

| field                | 311 data field                                                              |
| -------------------- | --------------------------------------------------------------------------- |
| id                   | -                                                                           |
| report_num           | sr_num, unique_key, service_request_id, sr_number                           |
| source_id            | (fkey source table)                                                         |
| incident type / name | sr_type_desc, complaint_type, service_name, sr_type                         |
| incident description | descriptor, service_detail, detail (may not exist, be external to response) |

These require double crawls to check for update - skip? except for date created
query by last_update and update fields?
311 app filters by new / open / closed but no one gives a shit?

| status                | status, sr_status_desc, status_description                                  | necessary?                                |
| date reported created | created_date, sr_created_date, requested_date_time                          | necessary?                                |
| last update?          | updated_datetime, sr_updated_date -- now we have to check on this?          | necessary?                                |
| date reported closed  | closed_date,                                                                | necessary?                                |

what use is this - address is good to display
somehwat redundant, except on global level - just do it

| street_address_raw    | street_address                                                              | raw                                       |
| street_address        | generated street address compound                                           | should this be method or created on input? |
| city_state_id         | fkey city_state                                                             | necessary?                                |
| zipcode_id            | fkey zipcode                                                                | necessary?                                |

| latitude              | latitude, location.latittude                                                |
| longitude             | longitude, location.longitude                                               |
| Point                 | spatial                                                                     |
| created_at            |                                                                             |
| updated_at            |                                                                             |


### source (311 or crime data source meta)

| field           | 311 data field    |
| --------------- | ----------------- |
| id              | -                 |
| report_category | (311, crime)      |
| description     | (nyc, atx, detc.) |
| url             |                   |
| created_at      |                   |
| updated_at      |                   |

field config - premature; just do hardcode in code for now, see where refactor can happen


### city_state (from 311)

| field | data field        |
| ----- | ----------------- |
| id    | -                 |
| city  | city              |
| state | state  (hardcode) |


#### zipcode (from 311)

| field   | data field                              |
| ------- | --------------------------------------- |
| id      | -                                       |
| zipcode | incident_zip, zip_code, (regex extract) |


Some places don't use addresses, just general spots on the road

- query limited, ingest limited
- skip saving primary raw data, don't want to deal with redoing all the fields
- Save raw json to s3? postgres json


---

## Entity Notes

Process:

1. design entity
2. design data -> input code
3. local file to test Entity then curl
4. refactor for DI
