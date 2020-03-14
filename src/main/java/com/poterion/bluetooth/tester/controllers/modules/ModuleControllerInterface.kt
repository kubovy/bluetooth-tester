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
package com.poterion.bluetooth.tester.controllers.modules

import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.listeners.CommunicatorListener
import com.poterion.communication.serial.payload.DeviceCapabilities
import com.poterion.utils.kotlin.noop
import javafx.scene.Node

/**
 * Module controller interface.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
interface ModuleControllerInterface : CommunicatorListener {
	var enabled: Boolean

	val rows: List<Triple<Node?, Node?, Node?>>

	override fun onConnecting(channel: Channel) = noop()

	override fun onConnect(channel: Channel) = noop()

	override fun onConnectionReady(channel: Channel) = noop()

	override fun onDisconnect(channel: Channel) = noop()

	override fun onDeviceCapabilitiesChanged(channel: Channel, capabilities: DeviceCapabilities) = noop()

	override fun onDeviceNameChanged(channel: Channel, name: String) = noop()

	override fun onMessageReceived(channel: Channel, message: IntArray) = noop()

	override fun onMessagePrepare(channel: Channel) = noop()

	override fun onMessageSent(channel: Channel, message: IntArray, remaining: Int) = noop()
}