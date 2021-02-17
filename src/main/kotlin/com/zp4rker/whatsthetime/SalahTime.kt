package com.zp4rker.whatsthetime

import java.time.OffsetDateTime

/**
 * @author zp4rker
 */
data class SalahTime(val name: String, val time: OffsetDateTime) {
    companion object {
        val Schedule = mutableListOf<SalahTime>()
        val Ignored = arrayOf("Sunrise", "Sunset", "Imsak", "Midnight")
    }

    enum class Duration(val value: Long) {
        Fajr(30),
        Dhuhr(120),
        Asr(120),
        Maghrib(30),
        Isha(180)
    }
}
