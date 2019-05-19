package com.poterion.monitor.api.data

enum class RGBPattern(val code:Int) {
    LIGHT(0x00),
    BLINK(0x01),
    FADE_IN(0x02),
    FADE_OUT(0x03),
    FADE_TOGGLE(0x04);

    val byteCode = code.toByte()
}