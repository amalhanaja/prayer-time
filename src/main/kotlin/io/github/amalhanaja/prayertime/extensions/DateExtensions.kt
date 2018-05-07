package io.github.amalhanaja.prayertime.extensions

import io.github.amalhanaja.prayertime.entity.DateComponents
import java.util.*
import java.util.Calendar.*
import kotlin.math.floor
import kotlin.math.round

fun Date.isLeapYear(): Boolean {
    return year % 4 == 0 && !(year % 100 == 0 && year % 400 != 0)
}

fun Int.isLeapYear(): Boolean {
    return this % 4 == 0 && !(this % 100 == 0 && this % 400 != 0)
}

fun Date.roundedMinute(): Date {
    val cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.time = this
    val min = cal[MINUTE]
    val sec = cal[SECOND]
    cal.set(MINUTE, floor(min + round(sec / 60.0)).toInt())
    cal.set(SECOND, 0)
    return cal.time
}

fun Date.toDateComponents(): DateComponents {
    val cal = GregorianCalendar.getInstance()
    cal.time = this
    return DateComponents(
            year = cal[YEAR],
            month = cal[MONTH] + 1,
            day = cal[DAY_OF_MONTH]
    )
}

fun DateComponents.resolveTime(): Date {
    return resolveTime(year = year, month = month, day = day)
}

fun resolveTime(year: Int, month: Int, day: Int): Date {
    val cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.set(year, month - 1, day, 0, 0, 0)
    return cal.time
}

fun Date.add(field: Int, value: Int): Date {
    val cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.time = this
    cal.add(field, value)
    return cal.time
}