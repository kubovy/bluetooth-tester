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
import com.poterion.bluetooth.tester.select
import com.poterion.bluetooth.tester.updateCount
import com.poterion.communication.serial.communicator.BluetoothCommunicator
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.USBCommunicator
import com.poterion.communication.serial.extensions.TempCommunicatorExtension
import com.poterion.communication.serial.listeners.TempCommunicatorListener
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.stage.Stage

/**
 * Temperature controller.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class TemperatureController : ModuleControllerInterface, TempCommunicatorListener {
	companion object {
		internal fun getRoot(primaryStage: Stage,
							 configController: ConfigController,
							 deviceConfiguration: DeviceConfiguration,
							 usbCommunicator: USBCommunicator,
							 btCommunicator: BluetoothCommunicator,
							 selectedChannel: () -> Channel?): TemperatureController =
				FXMLLoader(TemperatureController::class.java.getResource("temperature.fxml"))
						.also { it.load<GridPane>() }
						.getController<TemperatureController>()
						.also { controller ->
							controller.primaryStage = primaryStage
							controller.configController = configController
							controller.deviceConfiguration = deviceConfiguration
							controller.usbCommunicator = TempCommunicatorExtension(usbCommunicator)
							controller.btCommunicator = TempCommunicatorExtension(btCommunicator)
							controller.selectedChannel = selectedChannel
							controller.startup()
						}
	}

	@FXML private lateinit var labelTitle: Label

	@FXML private lateinit var hboxContent: HBox
	@FXML private lateinit var comboTempIndex: ComboBox<String>
	@FXML private lateinit var textTemperature: TextField
	@FXML private lateinit var textHumidity: TextField

	@FXML private lateinit var buttonTemperatureGet: Button

	private lateinit var primaryStage: Stage
	private lateinit var configController: ConfigController
	private lateinit var deviceConfiguration: DeviceConfiguration

	private lateinit var usbCommunicator: TempCommunicatorExtension<USBCommunicator.Descriptor>
	private lateinit var btCommunicator: TempCommunicatorExtension<BluetoothCommunicator.Descriptor>
	private lateinit var selectedChannel: () -> Channel?

	private val communicator: TempCommunicatorExtension<*>?
		get() = when (selectedChannel()) {
			Channel.USB -> usbCommunicator
			Channel.BLUETOOTH -> btCommunicator
			else -> null
		}

	override var enabled: Boolean = false
		set(value) {
			field = value
			comboTempIndex.isDisable = !value
			textTemperature.isDisable = !value
			textHumidity.isDisable = !value
			buttonTemperatureGet.isDisable = !value
		}

	override val rows: List<Triple<Node?, Node?, Node?>>
		get() = listOf(Triple(labelTitle, hboxContent, buttonTemperatureGet))

	@FXML
	fun initialize() {
		// noop
	}

	private fun startup() {
		usbCommunicator.register(this)
		btCommunicator.register(this)
	}

	@FXML
	fun onTemperatureGet() {
		//dht11Enabled = false
		val num = comboTempIndex.value?.toIntOrNull() ?: 0
		communicator?.sendTempRequest(num)
	}

	override fun onTempCountReceived(channel: Channel, count: Int) = Platform.runLater {
		comboTempIndex.updateCount(count)
	}

	override fun onTempReceived(channel: Channel, num: Int, temp: Double, humidity: Double) = Platform.runLater {
		// dht11Enabled = true
		comboTempIndex.select(num)
		textTemperature.text = "%.2f".format(temp)
		textHumidity.text = "%.0f".format(humidity)
	}
}
