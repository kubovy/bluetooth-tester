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
import com.poterion.communication.serial.extensions.IOCommunicatorExtension
import com.poterion.communication.serial.listeners.IOCommunicatorListener
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.stage.Stage

/**
 * GPIO controller.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class IoController : ModuleControllerInterface, IOCommunicatorListener {
	companion object {
		internal fun getRoot(primaryStage: Stage,
							 configController: ConfigController,
							 deviceConfiguration: DeviceConfiguration,
							 usbCommunicator: USBCommunicator,
							 btCommunicator: BluetoothCommunicator,
							 selectedChannel: () -> Channel?): IoController =
				FXMLLoader(IoController::class.java.getResource("io.fxml"))
						.also { it.load<GridPane>() }
						.getController<IoController>()
						.also { controller ->
							controller.primaryStage = primaryStage
							controller.configController = configController
							controller.deviceConfiguration = deviceConfiguration
							controller.usbCommunicator = IOCommunicatorExtension(usbCommunicator)
							controller.btCommunicator = IOCommunicatorExtension(btCommunicator)
							controller.selectedChannel = selectedChannel
							controller.startup()
						}
	}

	@FXML private lateinit var labelTitle: Label

	@FXML private lateinit var gridContent: GridPane
	@FXML private lateinit var checkboxGpio00: CheckBox
	@FXML private lateinit var checkboxGpio01: CheckBox
	@FXML private lateinit var checkboxGpio02: CheckBox
	@FXML private lateinit var checkboxGpio03: CheckBox
	@FXML private lateinit var checkboxGpio04: CheckBox
	@FXML private lateinit var checkboxGpio05: CheckBox
	@FXML private lateinit var checkboxGpio06: CheckBox
	@FXML private lateinit var checkboxGpio07: CheckBox
	@FXML private lateinit var checkboxGpio08: CheckBox
	@FXML private lateinit var checkboxGpio09: CheckBox
	@FXML private lateinit var checkboxGpio10: CheckBox
	@FXML private lateinit var checkboxGpio11: CheckBox
	@FXML private lateinit var checkboxGpio12: CheckBox
	@FXML private lateinit var checkboxGpio13: CheckBox
	@FXML private lateinit var checkboxGpio14: CheckBox
	@FXML private lateinit var checkboxGpio15: CheckBox
	@FXML private lateinit var checkboxGpio16: CheckBox
	@FXML private lateinit var checkboxGpio17: CheckBox
	@FXML private lateinit var checkboxGpio18: CheckBox
	@FXML private lateinit var checkboxGpio19: CheckBox

	@FXML private lateinit var buttonGpioGet00: Button
	@FXML private lateinit var buttonGpioGet01: Button
	@FXML private lateinit var buttonGpioGet02: Button
	@FXML private lateinit var buttonGpioGet03: Button
	@FXML private lateinit var buttonGpioGet04: Button
	@FXML private lateinit var buttonGpioGet05: Button
	@FXML private lateinit var buttonGpioGet06: Button
	@FXML private lateinit var buttonGpioGet07: Button
	@FXML private lateinit var buttonGpioGet08: Button
	@FXML private lateinit var buttonGpioGet09: Button
	@FXML private lateinit var buttonGpioGet10: Button
	@FXML private lateinit var buttonGpioGet11: Button
	@FXML private lateinit var buttonGpioGet12: Button
	@FXML private lateinit var buttonGpioGet13: Button
	@FXML private lateinit var buttonGpioGet14: Button
	@FXML private lateinit var buttonGpioGet15: Button
	@FXML private lateinit var buttonGpioGet16: Button
	@FXML private lateinit var buttonGpioGet17: Button
	@FXML private lateinit var buttonGpioGet18: Button
	@FXML private lateinit var buttonGpioGet19: Button

	private lateinit var primaryStage: Stage
	private lateinit var configController: ConfigController
	private lateinit var deviceConfiguration: DeviceConfiguration

	private lateinit var usbCommunicator: IOCommunicatorExtension<USBCommunicator.Descriptor>
	private lateinit var btCommunicator: IOCommunicatorExtension<BluetoothCommunicator.Descriptor>
	private lateinit var selectedChannel: () -> Channel?

	private val communicator: IOCommunicatorExtension<*>?
		get() = when (selectedChannel()) {
			Channel.USB -> usbCommunicator
			Channel.BLUETOOTH -> btCommunicator
			else -> null
		}

	override var enabled: Boolean = false
		set(value) {
			field = value
			checkboxGpio00.isDisable = !value
			checkboxGpio01.isDisable = !value
			checkboxGpio02.isDisable = !value
			checkboxGpio03.isDisable = !value
			checkboxGpio04.isDisable = !value
			checkboxGpio05.isDisable = !value
			checkboxGpio06.isDisable = !value
			checkboxGpio07.isDisable = !value
			checkboxGpio08.isDisable = !value
			checkboxGpio09.isDisable = !value
			checkboxGpio10.isDisable = !value
			checkboxGpio11.isDisable = !value
			checkboxGpio12.isDisable = !value
			checkboxGpio13.isDisable = !value
			checkboxGpio14.isDisable = !value
			checkboxGpio15.isDisable = !value
			checkboxGpio16.isDisable = !value
			checkboxGpio17.isDisable = !value
			checkboxGpio18.isDisable = !value
			checkboxGpio19.isDisable = !value
			buttonGpioGet00.isDisable = !value
			buttonGpioGet01.isDisable = !value
			buttonGpioGet02.isDisable = !value
			buttonGpioGet03.isDisable = !value
			buttonGpioGet04.isDisable = !value
			buttonGpioGet05.isDisable = !value
			buttonGpioGet06.isDisable = !value
			buttonGpioGet07.isDisable = !value
			buttonGpioGet08.isDisable = !value
			buttonGpioGet09.isDisable = !value
			buttonGpioGet10.isDisable = !value
			buttonGpioGet11.isDisable = !value
			buttonGpioGet12.isDisable = !value
			buttonGpioGet13.isDisable = !value
			buttonGpioGet14.isDisable = !value
			buttonGpioGet15.isDisable = !value
			buttonGpioGet16.isDisable = !value
			buttonGpioGet17.isDisable = !value
			buttonGpioGet18.isDisable = !value
			buttonGpioGet19.isDisable = !value
		}

	override val rows: List<Triple<Node?, Node?, Node?>>
		get() = listOf(Triple(labelTitle, gridContent, null))

	@FXML
	fun initialize() {
		// noop
	}

	private fun startup() {
		usbCommunicator.register(this)
		btCommunicator.register(this)
	}

	@FXML
	fun onGpioGet(event: ActionEvent) {
		val port = (event.source as? Button)?.id?.let { "buttonGpioGet(\\d+)".toRegex().matchEntire(it) }
				?.groupValues
				?.takeIf { it.size == 2 }
				?.get(1)
				?.toIntOrNull()
		if (port != null) communicator?.sendIoRequest(port)
	}

	@FXML
	fun onGpioSet(event: ActionEvent) {
		val checkbox = (event.source as? CheckBox)
		val port = checkbox
				?.id
				?.let { "buttonGpioGet(\\d+)".toRegex().matchEntire(it) }
				?.groupValues
				?.takeIf { it.size == 2 }
				?.get(1)
				?.toIntOrNull()
		if (port != null) {
			checkbox.isIndeterminate = false
			communicator?.sendIoWrite(port, checkbox.isSelected)
		}
	}

	override fun onIoChanged(channel: Channel, port: Int, state: Boolean) = Platform.runLater {
		this::class.java.declaredFields
				.find { it.name == "checkboxGpio%02d".format(port) }
				?.get(this)
				?.let { it as? CheckBox }
				?.isSelected = state
	}
}
