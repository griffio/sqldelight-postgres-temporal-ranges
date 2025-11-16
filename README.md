# SqlDelight 2.2.x Postgresql TSRanges support 

https://github.com/cashapp/sqldelight

Example:

see https://www.postgresql.org/docs/current/rangetypes.html#RANGETYPES-CONSTRAINT

You can use the `btree_gist` extension to define exclusion constraints on plain scalar data types, which can then be combined with range exclusions for maximum flexibility. 

Allow only appointments on the hour or at half past the hour.
Appointments are allowed in 30, 60, 90 and 120 minute durations.
Overlapping appointments are not allowed by the exclusion constraint.

```sql

CREATE EXTENSION btree_gist;

CREATE TABLE Appointments(
  slot TSTZRANGE NOT NULL CHECK( date_part('minute', LOWER(slot)) IN (0, 30) AND date_part('minute', UPPER(slot)) IN (0, 30)),
  duration INT NOT NULL GENERATED ALWAYS AS ( EXTRACT (epoch FROM UPPER(slot) - LOWER(slot)) / 60 ) STORED CHECK(duration IN (30, 60, 90, 120)),
  EXCLUDE USING GIST(slot WITH &&)
);

```

Return the empty time slots (as a multirange) by subtracting existing appointments from the user’s declared availability (as a multirange).

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
