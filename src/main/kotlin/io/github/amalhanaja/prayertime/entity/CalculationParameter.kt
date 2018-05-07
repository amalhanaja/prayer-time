package io.github.amalhanaja.prayertime.entity

import io.github.amalhanaja.prayertime.entity.HighLatitudeRule.*

data class CalculationParameter(
        val fajrAngle: Double = 0.toDouble(),

        val ishaAngle: Double = 0.toDouble(),

        val ishaInterval: Int = 0,

        val madhab: Madhab = Madhab.SHAFI,

        val highLatitudeRule: HighLatitudeRule = MIDDLE_OF_THE_NIGHT,

        val offset: PrayerOffset = PrayerOffset()
){

    val nightPortions = when (this.highLatitudeRule) {
            MIDDLE_OF_THE_NIGHT -> NightPortion(1.0 / 2.0, 1.0 / 2.0)
            SEVENTH_OF_THE_NIGHT -> NightPortion(1.0 / 7.0, 1.0 / 7.0)
            TWILIGHT_ANGLE -> NightPortion(this.fajrAngle / 60.0, this.ishaAngle / 60.0)
        }

}