package io.github.amalhanaja.prayertime.entity

sealed class CalculationMethod(val params: CalculationParameter) {
    object MWL : CalculationMethod(
            params = CalculationParameter(
                    fajrAngle = 18.0,
                    ishaAngle = 17.0
            )
    )

    object EGYPTIAN : CalculationMethod(
            params = CalculationParameter(
                    fajrAngle = 20.0,
                    ishaAngle = 18.0
            )
    )

    object KIRACHI : CalculationMethod(
            params = CalculationParameter(
                    fajrAngle = 18.0,
                    ishaAngle = 18.0
            )
    )

    object UMM_AL_QURA : CalculationMethod(
            params = CalculationParameter(
                    fajrAngle = 18.5,
                    ishaInterval = 90
            )
    )

    object GULF : CalculationMethod(
            params = CalculationParameter(
                    fajrAngle = 19.5,
                    ishaInterval= 90
            )
    )

    object MOON_SIGHTING_COMMITTEE : CalculationMethod(
            params = CalculationParameter(
                    fajrAngle = 18.0,
                    ishaAngle = 18.0
            )
    )

    object NORTH_AMERICA : CalculationMethod(
            params = CalculationParameter(
                    fajrAngle = 15.0,
                    ishaAngle = 15.0
            )
    )

    object KUWAIT : CalculationMethod(
            params = CalculationParameter(
                    fajrAngle = 18.0,
                    ishaAngle = 17.5
            )
    )

    object QATAR : CalculationMethod(
            params = CalculationParameter(
                    fajrAngle = 18.0,
                    ishaInterval= 90
            )
    )

    object SINGAPORE : CalculationMethod(
            params = CalculationParameter(
                    fajrAngle = 20.0,
                    ishaAngle = 18.0
            )
    )

    class OTHER(params: CalculationParameter) : CalculationMethod(params)
}