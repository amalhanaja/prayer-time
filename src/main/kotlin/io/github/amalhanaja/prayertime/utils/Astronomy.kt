package io.github.amalhanaja.prayertime.utils

import io.github.amalhanaja.prayertime.entity.Coordinate
import io.github.amalhanaja.prayertime.extensions.closestAngle
import io.github.amalhanaja.prayertime.extensions.normalize
import io.github.amalhanaja.prayertime.extensions.unwindAngle
import java.lang.StrictMath.toDegrees
import java.lang.StrictMath.toRadians
import kotlin.math.*

object Astronomy {
    fun meanSolarLongitude(julianCentury: Double): Double {
        val term1 = 280.4664567
        val term2 = 36000.76983 * julianCentury
        val term3 = 0.0003032 * julianCentury.pow(2)
        return (term1 + term2 + term3).unwindAngle()
    }

    fun meanLunarLatitude(julianCentury: Double): Double {
        val term1 = 218.3165
        val term2 = 481267.8813 * julianCentury
        return (term1 + term2).unwindAngle()
    }

    fun apparentSolarLongitude(julianCentury: Double, longitude: Double): Double {
        val lng = longitude + solarEquationOfTheCenter(julianCentury, meanSolarAnomaly(julianCentury))
        val omega = 125.04 - (1934.136 * julianCentury)
        val lambda = lng - 0.00569 - (0.00478 * sin(toRadians(omega)))
        return lambda.unwindAngle()
    }

    fun ascendingLunarNodeLongitude(julianCentury: Double): Double {
        val term1 = 125.04452
        val term2 = 1934.136261 * julianCentury
        val term3 = 0.0020708 * julianCentury.pow(2)
        val term4 = julianCentury.pow(3) / 450000
        val omega = term1 - term2 + term3 + term4
        return omega.unwindAngle()
    }

    fun meanSolarAnomaly(julianCentury: Double): Double {
        val term1 = 357.52911
        val term2 = 35999.05029 * julianCentury
        val term3 = 0.0001537 * julianCentury.pow(2)
        return (term1 + term2 - term3).unwindAngle()
    }

    fun solarEquationOfTheCenter(julianCentury: Double, meanAnomaly: Double): Double {
        val rad = toRadians(meanAnomaly)
        val term1 = (1.914602 - (0.004817 * julianCentury) - (0.000014 * julianCentury.pow(2))) * sin(rad)
        val term2 = (0.019993 - (0.000101 * julianCentury)) * sin(2 * rad)
        val term3 = 0.000289 * sin(3 * rad)
        return term1 + term2 + term3
    }

    fun meanObliquityEcliptic(julianCentury: Double): Double {
        val term1 = 23.439291
        val term2 = 0.013004167 * julianCentury
        val term3 = 0.0000001639 * julianCentury.pow(2)
        val term4 = 0.0000005036 * julianCentury.pow(3)
        return term1 - term2 - term3 + term4
    }

    fun apparentObliquityEcliptic(julianCentury: Double, meanObliquity: Double): Double {
        val obliquity = 125.04 - (1934.136 * julianCentury)
        return meanObliquity + (0.00256 * cos(toRadians(obliquity)))
    }

    fun meanSiderealTime(julianCentury: Double): Double {
        val jd = (julianCentury * 36525) + 2451545.0
        val term1 = 280.46061837
        val term2 = 360.98564736629 * (jd - 2451545)
        val term3 = 0.000387933 * julianCentury.pow(2)
        val term4 = julianCentury.pow(3) / 38710000
        val theta = term1 + term2 + term3 - term4
        return theta.unwindAngle()
    }

    fun nutationInLongitude(solarLongitude: Double, lunarLongitude: Double, ascendingNode: Double): Double {
        val term1 = (-17.2/3600) * sin(toRadians(ascendingNode))
        val term2 =  (1.32/3600) * sin(2 * toRadians(solarLongitude))
        val term3 =  (0.23/3600) * sin(2 * toRadians(lunarLongitude))
        val term4 =  (0.21/3600) * sin(2 * toRadians(ascendingNode))
        return term1 - term2 - term3 + term4
    }

    fun nutationInObliquity(solarLongitude: Double, lunarLongitude: Double, ascendingNode: Double
    ): Double {
        val term1 =  (9.2/3600) * cos(toRadians(ascendingNode))
        val term2 = (0.57/3600) * cos(2 * toRadians(solarLongitude))
        val term3 = (0.10/3600) * cos(2 * toRadians(lunarLongitude))
        val term4 = (0.09/3600) * cos(2 * toRadians(ascendingNode))
        return term1 + term2 + term3 - term4
    }

    fun altitudeOfCelestialBody(observerLatitude: Double, declination: Double, localHourAngle: Double): Double {
        val term1 = sin(toRadians(observerLatitude)) * sin(toRadians(declination))
        val term2 = cos(toRadians(observerLatitude)) * cos(toRadians(declination)) * cos(toRadians(localHourAngle))
        return toDegrees(asin(term1 + term2))
    }

    fun approximateTransit(longitude: Double, siderealTime: Double, rightAscension: Double): Double {
        val lw = longitude * -1
        return ((rightAscension + lw - siderealTime) / 360).normalize(1.0)
    }

    fun correctedTransit(
            approximateTransit: Double,
            longitude: Double,
            siderealTime: Double,
            rightAscension: Double,
            prevAscension: Double,
            nextRightAscension: Double
    ): Double {
        val lw = longitude * -1
        val theta = (siderealTime + 360.985647 * approximateTransit).unwindAngle()
        val alpha = (interpolateAngles(rightAscension, prevAscension, nextRightAscension,  approximateTransit)).unwindAngle()
        val h = (theta - lw - alpha).closestAngle()
        val delta = h / -360
        return (approximateTransit + delta) * 24
    }


    fun correctedHourAngle(
            approximateTransit: Double,
            angle: Double,
            coordinate: Coordinate,
            afterTransit: Boolean,
            siderealTime: Double,
            rightAscension: Double,
            prevRightAscension: Double,
            nextRightAscension: Double,
            declination: Double,
            prevDeclination: Double,
            nextDeclination: Double
    ): Double {
        val lw = coordinate.longitude * -1
        val term1 = sin(toRadians(angle)) - sin(toRadians(coordinate.latitude)) * sin(toRadians(declination))
        val term2 = cos(toRadians(coordinate.latitude)) * cos(toRadians(declination))
        val h0 = toDegrees(acos(term1 / term2))
        val factor = if (afterTransit) approximateTransit + h0 / 360 else approximateTransit - h0 / 360
        val theta = (siderealTime + 360.985647 * factor).unwindAngle()
        val alpha = (interpolateAngles(rightAscension,prevRightAscension,nextRightAscension,factor)).unwindAngle()
        val delta = interpolate(declination, prevDeclination, nextDeclination, factor)
        val hh = theta - lw - alpha
        val h = altitudeOfCelestialBody(coordinate.latitude, delta, hh)
        val term3 = h - angle
        val term4 = 360.0 * cos(toRadians(delta)) *
                cos(toRadians(coordinate.latitude)) * sin(toRadians(hh))
        val deltaFactor = term3 / term4
        return (factor + deltaFactor) * 24
    }

    fun interpolate(value: Double, prev: Double, next: Double, factor: Double): Double {
        val a = value - prev
        val b = next - value
        val c = b - a
        return value + ((factor/2) * (a + b + (factor * c)))
    }

    fun interpolateAngles(value: Double, prev: Double, next: Double, factor: Double): Double {
        val a = (value - prev).unwindAngle()
        val b = (next - value).unwindAngle()
        val c = b - a
        return value + ((factor/2) * (a + b + (factor * c)))
    }
}