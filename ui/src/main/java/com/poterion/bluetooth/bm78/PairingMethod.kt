package com.poterion.bluetooth.bm78

enum class PairingMethod(val byte: Byte) {
    PIN(0x00),
    JUST_WORK(0x01),
    PASSKEY(0x02),
    USER_CONFIRM(0x03)
}