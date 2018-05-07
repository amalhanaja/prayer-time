package io.github.amalhanaja.prayertime.entity

import java.util.*
import java.util.Calendar.*
import kotlin.math.floor

class TimeComponents internal constructor(
        val hour: Int,
        val minute: Int,
        val second: Int
){
    companion object {
        fun from(double: Double): TimeComponents? {
            if (double.isInfinite() || double.isNaN()){
                return null
            }
            val h = floor(double)
            val m = floor((double - h ) * 60)
            val s = floor((double - (h + m / 60)) * 3600 )
            return TimeComponents(h.toInt(), m.toInt(), s.toInt())
        }
    }

    fun dateComponents(date: Date): Date {
        val cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.time = date
        cal[HOUR_OF_DAY] = 0
        cal[MINUTE] = minute
        cal[SECOND] = second
        cal[HOUR_OF_DAY] = hour
        return cal.time
    }
}