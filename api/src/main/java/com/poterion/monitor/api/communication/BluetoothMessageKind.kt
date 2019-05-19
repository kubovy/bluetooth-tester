package com.poterion.monitor.api.communication

/**
 * Bluetooth message kind.
 *
 * @param code Code of the message type.
 * @author Jan Kubovy <jan@kubovy.eu>
 */
enum class BluetoothMessageKind(override var code: Int, override val delay: Long? = null) : MessageKind {
	/** Cyclic redundancy check message */
	CRC(0x00),
	/** ID of device message */
	IDD(0x01),
	/** Plain message */
	PLAIN(0x02),
	SETTINGS(0x03),
	IO(0x10),
	DHT11(0x11),
	LCD(0x12),
	MCP23017(0x13),
	PIR(0x14),
	RGB(0x15),
	WS281x(0x16),
	WS281xLIGHT(0x17),
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