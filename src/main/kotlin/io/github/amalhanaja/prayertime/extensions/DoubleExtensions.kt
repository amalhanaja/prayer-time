package io.github.amalhanaja.prayertime.extensions
import kotlin.math.*

fun Double.normalize(max: Double): Double {
    return this - (max * floor(this / max))
}

fun Double.unwindAngle(): Double {
    return this.normalize(360.00)
}

fun Double.closestAngle(): Double {
    return when {
        this >= -180 && this <= 180 -> this
        else -> this - (360 * round(this / 360))
    }
}