package io.github.amalhanaja.prayertime.utils

import io.github.amalhanaja.prayertime.entity.Coordinate
import io.github.amalhanaja.prayertime.entity.Madhab
import java.util.*


class SolarTime(today: Date, private val observer: Coordinate) {

    val transit: Double
    val sunrise: Double
    val sunset: Double
    private val solar: SolarCoordinate
    private val prevSolar: SolarCoordinate
    private val nextSolar: SolarCoordinate
    private val approximateTransit: Double

    init {
        val calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.time = today
        calendar.add(Calendar.DATE, 1)
        val tomorrow = calendar.time
        calendar.add(Calendar.DATE, -2)
        val yesterday = calendar.time
        this.prevSolar = SolarCoordinate(Julian.day(yesterday))
        this.solar = SolarCoordinate(Julian.day(today))
        this.nextSolar = SolarCoordinate(Julian.day(tomorrow))
        this.approximateTransit = Astronomy.approximateTransit(observer.longitude,
                solar.apparentSiderealTime, solar.rightAscension)
        val solarAltitude = -50.0 / 60.0
        this.transit = Astronomy.correctedTransit(this.approximateTransit, observer.longitude,
                solar.apparentSiderealTime, solar.rightAscension, prevSolar.rightAscension,
                nextSolar.rightAscension)
        this.sunrise = Astronomy.correctedHourAngle(this.approximateTransit, solarAltitude,
                observer, false, solar.apparentSiderealTime, solar.rightAscension,
                prevSolar.rightAscension, nextSolar.rightAscension, solar.declination,
                prevSolar.declination, nextSolar.declination)
        this.sunset = Astronomy.correctedHourAngle(this.approximateTransit, solarAltitude,
                observer, true, solar.apparentSiderealTime, solar.rightAscension,
                prevSolar.rightAscension, nextSolar.rightAscension, solar.declination,
                prevSolar.declination, nextSolar.declination)
    }

    fun hourAngle(angle: Double, afterTransit: Boolean): Double {
        return Astronomy.correctedHourAngle(this.approximateTransit, angle, this.observer,
                afterTransit, this.solar.apparentSiderealTime, this.solar.rightAscension,
                this.prevSolar.rightAscension, this.nextSolar.rightAscension, this.solar.declination,
                this.prevSolar.declination, this.nextSolar.declination)
    }

    fun afternoon(madhab: Madhab): Double {
        val tangent = Math.abs(observer.latitude - solar.declination)
        val inverse = madhab.shadowLength + Math.tan(Math.toRadians(tangent))
        val angle = Math.toDegrees(Math.atan(1.0 / inverse))
        return hourAngle(angle, true)
    }
}