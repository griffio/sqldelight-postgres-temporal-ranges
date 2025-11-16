package griffio

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import griffio.queries.Sample

import org.postgresql.ds.PGSimpleDataSource
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random

private fun getSqlDriver() = PGSimpleDataSource().apply {
    setURL("jdbc:postgresql://localhost:5432/appointments")
    applicationName = "App Main"
}.asJdbcDriver()

fun main() {

    val driver = getSqlDriver()
    val sample = Sample(driver)

    // random appointment day
    val year = (2000..2025).random()
    val month = (1..12).random()
    val day = (1..28).random()

    var slotBegin = LocalDateTime.of(year, month, day, 9, 0).atOffset(ZoneOffset.UTC)
    // Book appointments from 9am to 6pm, randomly choose whether to book or not
    while(slotBegin.hour < 18) {
        val slotEnd = slotBegin.plusMinutes(30)
        if (Random.nextBoolean()) sample.appointmentsQueries.insert("[$slotBegin, $slotEnd)")
        slotBegin = slotEnd
    }

    println("Appointment slots booked ---")
    sample.appointmentsQueries.appointments().executeAsList().forEach { println(it) }
    // user is available for appointments from 9am to 6pm on the day of the appointment
    slotBegin = LocalDateTime.of(year, month, day, 9, 0).atOffset(ZoneOffset.UTC)
    val slotEnd = LocalDateTime.of(year, month, day, 18, 0).atOffset(ZoneOffset.UTC)

    println("Available Appointment slots ---")
    // User's availability, as a multirange, is overlapped on free slots
    sample.appointmentsQueries.selectAvailableAppointments("{[$slotBegin, $slotEnd)}", "[$slotBegin, $slotEnd)").executeAsList().forEach { println(it) }
}
