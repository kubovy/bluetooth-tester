package com.poterion.monitor.api.communication

/**
 * Bluetooth message kind.
 *
 * @param code Code of the message type.
 * @author Jan Kubovy <jan@kubovy.eu>
 */
enum class BluetoothMessageKind(final override var code: Int, override val delay: Long? = null) : MessageKind {
	/** Cyclic redundancy check message */
	CRC(0x00),
	/** ID of device message */
	IDD(0x01),
	/** Plain message */
	PLAIN(0x02),
	SETTINGS(0x03),
	IO(0x10),
	DHT11(0x11),
	PIR(0x12),
	LCD(0x20),
	RGB(0x30),
	WS281x(0x40),
	SM_CONFIGURATION(0x80),
	SM_PULL(0x81),
	SM_PUSH(0x82),
	SM_GET_STATE(0x83),
	SM_SET_STATE(0x84),
	SM_ACTION(0x85),
	/** Unknown message */
	UNKNOWN(0xFF);

	override val byteCode = code.toByte()
}