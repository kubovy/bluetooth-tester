package com.poterion.monitor.api.communication

fun BluetoothMessageKind.sendGetRequest(param: Int? = null) = if (param == null) {
    BluetoothCommunicator.send(this)
} else {
    BluetoothCommunicator.send(this, byteArrayOf(param.toByte()))
}

fun BluetoothMessageKind.sendConfiguration(vararg params: Number) =
        BluetoothCommunicator.send(this, params.map { it.toByte() }.toByteArray())

fun bools2Byte(b7: Boolean, b6: Boolean, b5: Boolean, b4: Boolean, b3: Boolean, b2: Boolean, b1: Boolean, b0: Boolean) =
    (if (b7) 0b10000000 else 0b00000000) or
    (if (b6) 0b01000000 else 0b00000000) or
    (if (b5) 0b00100000 else 0b00000000) or
    (if (b4) 0b00010000 else 0b00000000) or
    (if (b3) 0b00001000 else 0b00000000) or
    (if (b2) 0b00000100 else 0b00000000) or
    (if (b1) 0b00000010 else 0b00000000) or
    (if (b0) 0b00000001 else 0b00000000)

fun ByteArray.calculateChecksum() = (map { it.toInt() }.takeIf { it.isNotEmpty() }?.reduce { acc, i -> acc + i }
        ?: 0) and 0xFF
