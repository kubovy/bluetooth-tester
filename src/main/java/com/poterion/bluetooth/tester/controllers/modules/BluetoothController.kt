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
import com.poterion.communication.serial.communicator.BluetoothCommunicator
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.USBCommunicator
import com.poterion.communication.serial.extensions.BluetoothCommunicatorExtension
import com.poterion.communication.serial.listeners.BluetoothCommunicatorListener
import com.poterion.communication.serial.payload.BluetoothPairingMode
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.stage.Stage

/**
 * Bluetooth settings controller.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class BluetoothController : ModuleControllerInterface, BluetoothCommunicatorListener {
	companion object {
		internal fun getRoot(primaryStage: Stage,
							 configController: ConfigController,
							 deviceConfiguration: DeviceConfiguration,
							 usbCommunicator: USBCommunicator,
							 btCommunicator: BluetoothCommunicator,
							 selectedChannel: () -> Channel?): BluetoothController =
				FXMLLoader(BluetoothController::class.java.getResource("bluetooth.fxml"))
						.also { it.load<GridPane>() }
						.getController<BluetoothController>()
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

	@FXML private lateinit var labelTitle: Label

	@FXML private lateinit var gridContent: GridPane
	@FXML private lateinit var textBluetoothName: TextField
	@FXML private lateinit var textBluetoothPin: TextField
	@FXML private lateinit var comboBluetoothPairingMethod: ComboBox<BluetoothPairingMode>

	@FXML private lateinit var vboxButtons: VBox
	@FXML private lateinit var buttonSettingsGet: Button
	@FXML private lateinit var buttonSettingsSet: Button

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
			textBluetoothName.isDisable = !value
			textBluetoothPin.isDisable = !value
			comboBluetoothPairingMethod.isDisable = !value
			buttonSettingsGet.isDisable = !value
			buttonSettingsSet.isDisable = !value
		}

	override val rows: List<Triple<Node?, Node?, Node?>>
		get() = listOf(Triple(labelTitle, gridContent, vboxButtons))

	@FXML
	fun initialize() {
		comboBluetoothPairingMethod.items.addAll(BluetoothPairingMode.values())
		comboBluetoothPairingMethod.selectionModel.select(0)
	}

	private fun startup() {
		usbCommunicator.register(this)
		btCommunicator.register(this)
	}

	@FXML
	fun onBluetoothSettingsGet() {
		//bluetoothSettingsEnabled = false
		communicator?.sendBluetoothSettingsRequest()
	}

	@FXML
	fun onBluetoothSettingsSet() {
		deviceConfiguration.bluetoothName = textBluetoothName.text
		configController.save()
		communicator?.sendBluetoothSettings(comboBluetoothPairingMethod.value, textBluetoothPin.text,
				textBluetoothName.text)
	}

	override fun onDeviceNameChanged(channel: Channel, name: String) = Platform.runLater {
		textBluetoothName.text = name
	}

	override fun onBluetoothSettingsUpdated(channel: Channel,
											pairingMode: BluetoothPairingMode,
											pin: String,
											name: String) = Platform.runLater {
		//bluetoothSettingsEnabled = true
		comboBluetoothPairingMethod.selectionModel.select(pairingMode)
		textBluetoothPin.text = pin
		textBluetoothName.text = name
		deviceConfiguration.bluetoothName = name
		configController.save()
	}
}
