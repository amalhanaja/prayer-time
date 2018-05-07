package io.github.amalhanaja.prayertime.entity

data class PrayerOffset(
        var fajr: Int = 0,
        var sunrise: Int = 0,
        var dhuhr: Int = 0,
        var asr: Int = 0,
        var maghrib: Int = 0,
        var isha: Int = 0
)