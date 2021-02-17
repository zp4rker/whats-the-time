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
}
