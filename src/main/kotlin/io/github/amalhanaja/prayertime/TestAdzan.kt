package io.github.amalhanaja.prayertime

import io.github.amalhanaja.prayertime.entity.CalculationMethod
import io.github.amalhanaja.prayertime.entity.Coordinate
import io.github.amalhanaja.prayertime.extensions.toDateComponents
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

fun main(args: Array<String>) {
    val prayerTime = PrayerTime(Coordinate(-6.2559582,106.7887902), Date().toDateComponents(), CalculationMethod.SINGAPORE)
    println("${prayerTime.fajr}\n" +
            "${prayerTime.sunrise}\n" +
            "${prayerTime.dhuhr}\n" +
            "${prayerTime.asr}\n" +
            "${prayerTime.maghrib}\n" +
            "${prayerTime.isha}")
    println(ZonedDateTime.now(ZoneOffset.UTC))
}