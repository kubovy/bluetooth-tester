package com.poterion.monitor.api.data

enum class WS281xPattern(val code: Int) {
    LIGHT(0x00),      // Simple light
    BLINK(0x01),      // Blink 50/50
    FADE_IN(0x02),    // Fade in 0>1
    FADE_OUT(0x03),   // Fade out 1>0
    FADE_TOGGLE(0x04);// Fade toggle 0>1>0

    val byteCode = code.toByte()
}