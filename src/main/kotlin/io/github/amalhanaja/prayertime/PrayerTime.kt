package io.github.amalhanaja.prayertime

import io.github.amalhanaja.prayertime.entity.*
import io.github.amalhanaja.prayertime.entity.CalculationMethod.*
import io.github.amalhanaja.prayertime.entity.Prayer.*
import io.github.amalhanaja.prayertime.extensions.add
import io.github.amalhanaja.prayertime.extensions.isLeapYear
import io.github.amalhanaja.prayertime.extensions.resolveTime
import io.github.amalhanaja.prayertime.utils.SolarTime
import java.util.*
import java.util.Calendar.*
import java.util.concurrent.TimeUnit
import kotlin.math.*


class PrayerTime internal constructor(coordinates: Coordinate, date: Date, calculationMethod: CalculationMethod = MWL) {
    val fajr: Date?
    val sunrise: Date?
    val dhuhr: Date?
    val asr: Date?
    val maghrib: Date?
    val isha: Date?
    val imsak: Date?

    /**
     * Calculate PrayerTimes
     * @param coordinates the coordinates of the location
     * @param date the date components for that location
     * @param params the parameters for the calculation
     */
    constructor(coordinates: Coordinate, date: DateComponents, calculationMethod: CalculationMethod = MWL) : this(coordinates, date.resolveTime(), calculationMethod) {

    }

    init {
        var tempFajr: Date? = null
        var tempSunrise: Date? = null
        var tempDhuhr: Date? = null
        var tempAsr: Date? = null
        var tempMaghrib: Date? = null
        var tempIsha: Date? = null
        val calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.time = date
        val year = calendar.get(YEAR)
        val dayOfYear = calendar.get(DAY_OF_YEAR)
        val solarTime = SolarTime(date, coordinates)
        var timeComponents = TimeComponents.from(solarTime.transit)
        val transit = timeComponents?.dateComponents(date)
        timeComponents = TimeComponents.from(solarTime.sunrise)
        val sunriseComponents = timeComponents?.dateComponents(date)
        timeComponents = TimeComponents.from(solarTime.sunset)
        val sunsetComponents = timeComponents?.dateComponents(date)
        val error = transit == null || sunriseComponents == null || sunsetComponents == null
        if (!error) {
            tempDhuhr = transit
            tempSunrise = sunriseComponents
            tempMaghrib = sunsetComponents

            timeComponents = TimeComponents.from(
                    solarTime.afternoon(calculationMethod.params.madhab))
            tempAsr = timeComponents?.dateComponents(date)

            // get night length
            val tomorrowSunrise = sunriseComponents?.add( DAY_OF_YEAR, 1)
            val night = tomorrowSunrise!!.time - sunsetComponents?.time!!

            timeComponents = TimeComponents
                    .from(solarTime.hourAngle(-calculationMethod.params.fajrAngle, false))
            tempFajr = timeComponents?.dateComponents(date)
            when {
                calculationMethod == MOON_SIGHTING_COMMITTEE && coordinates.latitude >= 55 -> {
                    tempFajr = sunriseComponents.add(SECOND, -1 * (night / 7000).toInt())
                }
            }

            val nightPortions = calculationMethod.params.nightPortions

            val safeFajr: Date = when {
                calculationMethod == MOON_SIGHTING_COMMITTEE -> {
                    seasonAdjustedMorningTwilight(coordinates.latitude, dayOfYear, year, sunriseComponents)
                }
                else -> {
                    val portion = nightPortions.fajr
                    val nightFraction = (portion * night / 1000).toLong()
                    sunriseComponents.add(SECOND, -1 * nightFraction.toInt())
                }
            }

            if (tempFajr == null || tempFajr.before(safeFajr)) {
                tempFajr = safeFajr
            }

            // Isha calculation with check against safe value
            if (calculationMethod.params.ishaInterval > 0) {
                tempIsha = tempMaghrib?.add(SECOND, calculationMethod.params.ishaInterval * 60)
            } else {
                timeComponents = TimeComponents.from(
                        solarTime.hourAngle(-calculationMethod.params.ishaAngle, true))
                tempIsha = timeComponents?.dateComponents(date)
                when {
                    calculationMethod == MOON_SIGHTING_COMMITTEE && coordinates.latitude >= 55 -> {
                        val nightFraction = night / 7000
                        tempIsha = sunsetComponents.add(SECOND, nightFraction.toInt())
                    }
                }

                val safeIsha: Date = when {
                    calculationMethod == MOON_SIGHTING_COMMITTEE -> {
                        seasonAdjustedEveningTwilight(
                                coordinates.latitude, dayOfYear, year, sunsetComponents)
                    }
                    else -> {
                        val portion = nightPortions.isha
                        val nightFraction = (portion * night / 1000).toLong()
                        sunsetComponents.add(SECOND, nightFraction.toInt())
                    }
                }
                if (tempIsha == null || tempIsha.after(safeIsha)) {
                    tempIsha = safeIsha
                }
            }
        }
        val dhuhrOffsetInMinutes: Int = when(calculationMethod) {
            // Moonsighting Committee requires 5 minutes for the sun to pass
            // the zenith and dhuhr to enter
            MOON_SIGHTING_COMMITTEE -> 5
            UMM_AL_QURA, GULF, QATAR -> 0
            else -> 1
        }
        val maghribOffsetInMinutes: Int = when (calculationMethod){
            // Moonsighting Committee adds 3 minutes to sunset time to account for light refraction
            MOON_SIGHTING_COMMITTEE -> 3
            else -> 0
        }
        if (error || tempAsr == null) {
            // if we don't have all prayer times then initialization failed
            this.fajr = null
            this.sunrise = null
            this.dhuhr = null
            this.asr = null
            this.maghrib = null
            this.isha = null
            this.imsak = null
        } else {
            // Assign final times to public struct members with all offsets
            this.fajr = tempFajr?.add(MINUTE, calculationMethod.params.offset.fajr)
            this.sunrise = tempSunrise?.add(MINUTE, calculationMethod.params.offset.sunrise)
            this.dhuhr = tempDhuhr?.add(MINUTE, calculationMethod.params.offset.dhuhr)
            this.asr = tempAsr.add(MINUTE, calculationMethod.params.offset.asr)
            this.maghrib = tempMaghrib?.add(MINUTE, calculationMethod.params.offset.maghrib)
            this.isha = tempIsha?.add(MINUTE, calculationMethod.params.offset.isha)
            this.imsak = Date(fajr!!.time - TimeUnit.MINUTES.toMillis(10))
        }
    }

    fun currentPrayer(date: Date = Date()): Prayer {
        val millis = date.time
        return when {
            this.isha!!.time - millis <= 0 -> ISHA
            this.maghrib!!.time - millis <= 0 -> MAGHRIB
            this.asr!!.time - millis <= 0 -> ASR
            this.dhuhr!!.time - millis <= 0 -> DHUHR
            this.sunrise!!.time - millis <= 0 -> SUNRISE
            this.fajr!!.time - millis <= 0 -> FAJR
            else -> IMSAK
        }
    }

    fun nextPrayer(date: Date = Date()): Prayer {
        val millis = date.time
        return when {
            this.isha!!.time - millis <= 0 -> IMSAK
            this.maghrib!!.time - millis <= 0 -> ISHA
            this.asr!!.time - millis <= 0 -> MAGHRIB
            this.dhuhr!!.time - millis <= 0 -> ASR
            this.sunrise!!.time - millis <= 0 -> DHUHR
            this.fajr!!.time - millis <= 0 -> SUNRISE
            else -> FAJR
        }
    }

    fun timeForPrayer(prayer: Prayer): Date? {
        return when (prayer) {
            FAJR -> this.fajr
            SUNRISE -> this.sunrise
            DHUHR -> this.dhuhr
            ASR -> this.asr
            MAGHRIB -> this.maghrib
            ISHA -> this.isha
            IMSAK -> this.imsak
        }
    }

    companion object {

        private fun seasonAdjustedMorningTwilight(
                latitude: Double, day: Int, year: Int, sunrise: Date): Date {
            val a = 75 + 28.65 / 55.0 * abs(latitude)
            val b = 75 + 19.44 / 55.0 * abs(latitude)
            val c = 75 + 32.74 / 55.0 * abs(latitude)
            val d = 75 + 48.10 / 55.0 * abs(latitude)

            val adjustment: Double
            val dyy = daysSinceSolstice(day, year, latitude)
            adjustment = when {
                dyy < 91 -> a + (b - a) / 91.0 * dyy
                dyy < 137 -> b + (c - b) / 46.0 * (dyy - 91)
                dyy < 183 -> c + (d - c) / 46.0 * (dyy - 137)
                dyy < 229 -> d + (c - d) / 46.0 * (dyy - 183)
                dyy < 275 -> c + (b - c) / 46.0 * (dyy - 229)
                else -> b + (a - b) / 91.0 * (dyy - 275)
            }

            return sunrise.add(SECOND, -round(adjustment * 60.0).toInt())
        }

        private fun seasonAdjustedEveningTwilight(
                latitude: Double, day: Int, year: Int, sunset: Date): Date {
            val a = 75 + 25.60 / 55.0 * abs(latitude)
            val b = 75 + 2.050 / 55.0 * abs(latitude)
            val c = 75 - 9.210 / 55.0 * abs(latitude)
            val d = 75 + 6.140 / 55.0 * abs(latitude)

            val adjustment: Double
            val dyy = daysSinceSolstice(day, year, latitude)
            if (dyy < 91) {
                adjustment = a + (b - a) / 91.0 * dyy
            } else if (dyy < 137) {
                adjustment = b + (c - b) / 46.0 * (dyy - 91)
            } else if (dyy < 183) {
                adjustment = c + (d - c) / 46.0 * (dyy - 137)
            } else if (dyy < 229) {
                adjustment = d + (c - d) / 46.0 * (dyy - 183)
            } else if (dyy < 275) {
                adjustment = c + (b - c) / 46.0 * (dyy - 229)
            } else {
                adjustment = b + (a - b) / 91.0 * (dyy - 275)
            }

            return sunset.add(SECOND, round(adjustment * 60.0).toInt())
        }

        private fun daysSinceSolstice(dayOfYear: Int, year: Int, latitude: Double): Int {
            var daysSinceSolistice: Int
            val northernOffset = 10
            val isLeapYear = year.isLeapYear()
            val southernOffset = if (isLeapYear) 173 else 172
            val daysInYear = if (isLeapYear) 366 else 365

            if (latitude >= 0) {
                daysSinceSolistice = dayOfYear + northernOffset
                if (daysSinceSolistice >= daysInYear) {
                    daysSinceSolistice -= daysInYear
                }
            } else {
                daysSinceSolistice = dayOfYear - southernOffset
                if (daysSinceSolistice < 0) {
                    daysSinceSolistice += daysInYear
                }
            }
            return daysSinceSolistice
        }
    }
}