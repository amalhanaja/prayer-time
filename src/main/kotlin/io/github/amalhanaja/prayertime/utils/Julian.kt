package io.github.amalhanaja.prayertime.utils

import java.util.*
import kotlin.math.floor

object Julian {
    fun day(date: Date): Double {
        val cal :Calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.time = date
        return day(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH),
                hour = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0
        )
    }

    fun day(year: Int, month: Int, day: Int, hour: Double): Double {
        val Y = when {
            month > 2 -> year
            else -> year -1
        }
        val M = when {
            month > 2 -> month
            else -> month + 12
        }
        val D = day + (hour / 24)
        val A =  Y/100
        val B = 2 - A + floor(A/4.0)
        val i0 = floor(365.25 * (Y + 4716))
        val i1 = floor(30.6001 * (M + 1))
        return i0 + i1 + D + B - 1524.5
    }

    fun century(julianDay: Double): Double {
        return (julianDay - 2451545.0) / 36525
    }
}