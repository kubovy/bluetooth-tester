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

import com.poterion.bluetooth.tester.controllers.ConfigController
import com.poterion.bluetooth.tester.data.DeviceConfiguration
import com.poterion.communication.serial.MessageKind
import com.poterion.communication.serial.communicator.BluetoothCommunicator
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.USBCommunicator
import com.poterion.communication.serial.extensions.BluetoothCommunicatorExtension
import com.poterion.communication.serial.payload.DeviceCapabilities
import com.poterion.utils.kotlin.noop
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.stage.Stage
import java.text.SimpleDateFormat
import java.util.*

/**
 * Bluetooth settings controller.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class RawController : ModuleControllerInterface {
	companion object {
		internal fun getRoot(primaryStage: Stage,
							 configController: ConfigController,
							 deviceConfiguration: DeviceConfiguration,
							 usbCommunicator: USBCommunicator,
							 btCommunicator: BluetoothCommunicator,
							 selectedChannel: () -> Channel?): RawController =
				FXMLLoader(RawController::class.java.getResource("raw.fxml"))
						.also { it.load<GridPane>() }
						.getController<RawController>()
						.also { controller ->
							controller.primaryStage = primaryStage
							controller.configController = configController
							controller.deviceConfiguration = deviceConfiguration
							controller.usbCommunicator = BluetoothCommunicatorExtension(usbCommunicator)
							controller.btCommunicator = BluetoothCommunicatorExtension(btCommunicator)
							controller.selectedChannel = selectedChannel
							controller.startup()
						}
	}

	@FXML private lateinit var labelTx: Label

	@FXML private lateinit var hboxContent: HBox
	@FXML private lateinit var textTxMessageType: TextField
	@FXML private lateinit var textTxCRC: TextField
	@FXML private lateinit var areaTxMessage: TextArea
	@FXML private lateinit var areaRxMessage: TextArea

	@FXML private lateinit var buttonTxClear: Button

	@FXML private lateinit var labelRx: Label

	@FXML private lateinit var buttonRxClear: Button

	private lateinit var primaryStage: Stage
	private lateinit var configController: ConfigController
	private lateinit var deviceConfiguration: DeviceConfiguration

	private lateinit var usbCommunicator: BluetoothCommunicatorExtension<USBCommunicator.Descriptor>
	private lateinit var btCommunicator: BluetoothCommunicatorExtension<BluetoothCommunicator.Descriptor>
	private lateinit var selectedChannel: () -> Channel?

	private val communicator: BluetoothCommunicatorExtension<*>?
		get() = when (selectedChannel()) {
			Channel.USB -> usbCommunicator
			Channel.BLUETOOTH -> btCommunicator
			else -> null
		}

	override var enabled: Boolean = false
		set(value) {
			field = value
			textTxMessageType.isDisable = !value
			textTxCRC.isDisable = !value
			areaTxMessage.isDisable = !value
			areaRxMessage.isDisable = !value
		}

	override val rows: List<Triple<Node?, Node?, Node?>>
		get() = listOf(
				Triple(labelTx, hboxContent, null),
				Triple(null, areaTxMessage, buttonTxClear),
				Triple(labelRx, areaRxMessage, buttonRxClear))

	@FXML
	fun initialize() {
		// noop
	}

	private fun startup() {
		usbCommunicator.register(this)
		btCommunicator.register(this)
	}

	@FXML
	fun onTxClear() {
		textTxMessageType.text = ""
		textTxCRC.text = ""
		areaTxMessage.text = ""
	}

	@FXML
	fun onRxClear() {
		areaRxMessage.text = ""
	}

	override fun onConnecting(channel: Channel) = noop()

	override fun onConnect(channel: Channel) = noop()

	override fun onConnectionReady(channel: Channel) = noop()

	override fun onDisconnect(channel: Channel) = noop()

	override fun onDeviceCapabilitiesChanged(channel: Channel, capabilities: DeviceCapabilities) = noop()

	override fun onDeviceNameChanged(channel: Channel, name: String) = noop()

	override fun onMessageReceived(channel: Channel, message: IntArray) = Platform.runLater {
		val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
		var chars = ""
		val sb = StringBuilder()
		//sb.append("ADDR: 0x00 0x01 0x02 0x03 0x04 0x05 0x06 0x07  ASCII\n\n")

		message.forEachIndexed { i, b ->
			if (i % 8 == 0) sb.append("%04x: ".format(i))
			chars += if (b in 32..126) b.toChar() else '.'
			sb.append("0x%02X%s".format(b, if (b > 255) "!" else " "))
			if (i % 8 == 7) {
				sb.append(" ${chars} |\n")
				chars = ""
			}
		}

		((message.size % 8) until 8).forEach { i ->
			if (i % 8 == 0) sb.append("%04x: ".format(i))
			sb.append("     ")
			if (i % 8 == 7) sb.append("          |\n")
		}

		areaRxMessage.text += "\n%s> %s [CRC: 0x%02X]: %s (0x%02X) with %d bytes: \n%s".format(channel, timestamp,
				message[0], MessageKind.values().find { it.code == message[1] } ?: "", message[1], message.size,
				sb.toString())
		//areaRxMessage.scrollTop = Double.MAX_VALUE
		areaRxMessage.selectPositionCaret(areaRxMessage.length)
		areaRxMessage.deselect()
	}

	override fun onMessagePrepare(channel: Channel) = noop()

	override fun onMessageSent(channel: Channel, message: IntArray, remaining: Int) = Platform.runLater {
		val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

		var chars = ""
		val sb = StringBuilder()

		message.forEachIndexed { i, b ->
			if (i % 8 == 0) sb.append("%04x: ".format(i))
			chars += if (b in 32..126) b.toChar() else '.'
			sb.append("0x%02X%s".format(b, if (b > 255) "!" else " "))
			if (i % 8 == 7) {
				sb.append(" ${chars} |\n")
				chars = ""
			}
		}

		((message.size % 8) until 8).forEach { i ->
			if (i % 8 == 0) sb.append("%04x: ".format(i))
			sb.append("     ")
			if (i % 8 == 7) sb.append("          |\n")
		}

		textTxCRC.text = "0x%02X".format(message[0])
		textTxMessageType.text = if (message.isNotEmpty()) "%s [0x%02X]"
				.format(MessageKind.values().find { it.code == message[1] } ?: "", message[1]) else ""

		areaTxMessage.text += "\n%s> %s [CRC: 0x%02X]: %s (0x%02X) with %d bytes: \n%s".format(channel, timestamp,
				message[0], MessageKind.values().find { it.code == message[1] } ?: "", message[1], message.size,
				sb.toString())
		//areaTxMessage.scrollTop = Double.MAX_VALUE
		areaTxMessage.selectPositionCaret(areaTxMessage.length)
		areaTxMessage.deselect()
	}
}
