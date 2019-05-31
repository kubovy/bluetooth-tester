package com.poterion.monitor.api.data

/**
 * Bluetooth pairing methods.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
enum class PairingMethod(val code: Int) {
    PIN(0x00),
    JUST_WORK(0x01),
    PASSKEY(0x02),
    USER_CONFIRM(0x03);
}