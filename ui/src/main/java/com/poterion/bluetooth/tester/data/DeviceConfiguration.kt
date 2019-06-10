package com.poterion.bluetooth.tester.data

import com.poterion.monitor.api.communication.Channel

/**
 * Device configuration.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
data class DeviceConfiguration(
		var selectedChannel: Channel = Channel.BLUETOOTH,
		var name: String? = null,

		var bluetoothAddress: String? = null,
		var bluetoothName: String? = null,
		var bluetoothChannel: String? = null,
		var bluetoothEepromBlockSize: Int? = null,
		//var bluetoothEepromContent: String? = null,

		var usbDevice: String? = null,
		//var usbName: String? = null,

		var hasBluetooth: Boolean = true,
		var hasUSB: Boolean = true,
		var hasDHT11: Boolean = false,
		var hasLCD: Boolean = false,
		var hasMCP23017: Boolean = false,
		var hasPIR: Boolean = false,
		var hasRGB: Boolean = false,
		var hasWS281xIndicators: Boolean = false,
		var hasWS281xLight: Boolean = false,

		var lcdLine: Int? = null,

		var mcp23017I2CAddress: String? = null)