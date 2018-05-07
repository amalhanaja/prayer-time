package io.github.amalhanaja.prayertime.utils

import io.github.amalhanaja.prayertime.entity.Coordinate
import io.github.amalhanaja.prayertime.extensions.unwindAngle
import java.lang.StrictMath.toDegrees
import java.lang.StrictMath.toRadians
import kotlin.math.*

object Qibla {
    private val MAKKAH = Coordinate(21.4225241, 39.8261818)

    fun calculateQiblaDirection(coordinates: Coordinate): Double {
        // Equation from "Spherical Trigonometry For the use of colleges and schools" page 50
        val longitudeDelta = toRadians(MAKKAH.longitude) - toRadians(coordinates.longitude)
        val latitudeRadians = toRadians(coordinates.latitude)
        val term1 = sin(longitudeDelta)
        val term2 = cos(latitudeRadians) * tan(toRadians(MAKKAH.latitude))
        val term3 = sin(latitudeRadians) * cos(longitudeDelta)

        val angle = atan2(term1, term2 - term3)
        return (toDegrees(angle)).unwindAngle()
    }
}