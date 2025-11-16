# SqlDelight 2.2.x Postgresql TSRanges support 

https://github.com/cashapp/sqldelight

Example:

see https://www.postgresql.org/docs/current/rangetypes.html#RANGETYPES-CONSTRAINT

```sql

CREATE EXTENSION btree_gist;

CREATE TABLE Appointments(
  slot TSTZRANGE NOT NULL CHECK( date_part('minute', LOWER(slot)) IN (0, 30) AND date_part('minute', UPPER(slot)) IN (0, 30)),
  duration INT NOT NULL GENERATED ALWAYS AS ( EXTRACT (epoch FROM UPPER(slot) - LOWER(slot)) / 60 ) STORED CHECK(duration IN (30, 60, 90, 120)),
  EXCLUDE USING GIST(slot WITH &&)
);

```

```sql
selectAvailableAppointments:
SELECT tstzmultirange(:user_availability::TSTZMULTIRANGE) - range_agg(slot) AS available_appointments
FROM Appointments
WHERE slot && tstzrange(:appointments_range::TSTZRANGE);
```

----

```shell
createdb appointments &&
./gradlew build &&
./gradlew flywayMigrate
```

Flyway db migrations
https://documentation.red-gate.com/fd/gradle-task-184127407.html
