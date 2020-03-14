/******************************************************************************
 * Copyright (C) 2020 Jan Kubovy (jan@kubovy.eu)                              *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/
package com.poterion.bluetooth.tester.data

import com.poterion.communication.serial.communicator.Channel

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
		var bluetoothChannel: Int? = null,

		var dataPart: Int? = null,
		var dataBlockSize: Int? = null,

		var usbDevice: String? = null,
		//var usbName: String? = null,

		var hasBluetooth: Boolean = true,
		var hasUSB: Boolean = true,
		var hasTemp: Boolean = false,
		var hasLcd: Boolean = false,
		var hasRegistry: Boolean = false,
		var hasGpio: Boolean = false,
		var hasRgb: Boolean = false,
		var hasIndicators: Boolean = false,
		var hasLight: Boolean = false,

		var lcdLine: Int? = null,

		var registryAddress: String? = null)