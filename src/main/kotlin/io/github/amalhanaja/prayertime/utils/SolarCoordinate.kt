package io.github.amalhanaja.prayertime.utils

import io.github.amalhanaja.prayertime.extensions.unwindAngle
import java.lang.StrictMath.toDegrees
import java.lang.StrictMath.toRadians
import kotlin.math.*

internal class SolarCoordinate(julianDay: Double) {

    /**
     * The declination of the sun, the angle between
     * the rays of the Sun and the plane of the Earth's
     * equator, in degrees.
     */
    val declination: Double

    /**
     * Right ascension of the Sun, the angular distance on the
     * celestial equator from the vernal equinox to the hour circle,
     * in degrees.
     */
    val rightAscension: Double

    /**
     * Apparent sidereal time, the hour angle of the vernal
     * equinox, in degrees.
     */
    val apparentSiderealTime: Double

    init {
        val julianCentury = Julian.century(julianDay)
        val solarLongitude = Astronomy.meanSolarLongitude(julianCentury)
        val lunarLongitude = Astronomy.meanLunarLatitude(julianCentury)
        val ascendingNode = Astronomy.ascendingLunarNodeLongitude(julianCentury)
        val apparentSolarLongitude = toRadians(
                Astronomy.apparentSolarLongitude(julianCentury, solarLongitude))

        val siderealTime = Astronomy.meanSiderealTime(julianCentury)
        val nutationInLongitude= Astronomy.nutationInLongitude(solarLongitude, lunarLongitude, ascendingNode)
        val nutationInObliquity = Astronomy.nutationInObliquity(solarLongitude, lunarLongitude, ascendingNode)

        val meanObliquityEcliptic= Astronomy.meanObliquityEcliptic(julianCentury)
        val apparentObliquityEcliptic= toRadians(Astronomy.apparentObliquityEcliptic(julianCentury, meanObliquityEcliptic))

        this.declination = toDegrees(asin(sin(apparentObliquityEcliptic) * sin(apparentSolarLongitude)))

        this.rightAscension = toDegrees(atan2(cos(apparentObliquityEcliptic) * sin(apparentSolarLongitude), cos(apparentSolarLongitude)))
                .unwindAngle()

        this.apparentSiderealTime = siderealTime + nutationInLongitude* 3600 * cos(toRadians(meanObliquityEcliptic + nutationInObliquity)) / 3600
    }
}