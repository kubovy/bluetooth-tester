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
import com.poterion.bluetooth.tester.getHexInt
import com.poterion.communication.serial.bools2Byte
import com.poterion.communication.serial.byte2Bools
import com.poterion.communication.serial.communicator.BluetoothCommunicator
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.USBCommunicator
import com.poterion.communication.serial.extensions.RegistryCommunicatorExtension
import com.poterion.communication.serial.listeners.RegistryCommunicatorListener
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.stage.Stage

/**
 * Registry controller.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class RegistryController : ModuleControllerInterface, RegistryCommunicatorListener {
	companion object {
		internal fun getRoot(primaryStage: Stage,
							 configController: ConfigController,
							 deviceConfiguration: DeviceConfiguration,
							 usbCommunicator: USBCommunicator,
							 btCommunicator: BluetoothCommunicator,
							 selectedChannel: () -> Channel?): RegistryController =
				FXMLLoader(RegistryController::class.java.getResource("registry.fxml"))
						.also { it.load<GridPane>() }
						.getController<RegistryController>()
						.also { controller ->
							controller.primaryStage = primaryStage
							controller.configController = configController
							controller.deviceConfiguration = deviceConfiguration
							controller.usbCommunicator = RegistryCommunicatorExtension(usbCommunicator)
							controller.btCommunicator = RegistryCommunicatorExtension(btCommunicator)
							controller.selectedChannel = selectedChannel
							controller.startup()
						}
	}

	@FXML private lateinit var labelTitle: Label

	@FXML private lateinit var gridContent: GridPane
	@FXML private lateinit var textRegistryAddress: TextField
	@FXML private lateinit var textRegistryRegistry: TextField
	@FXML private lateinit var labelRegistry00: Label
	@FXML private lateinit var labelRegistry01: Label
	@FXML private lateinit var labelRegistry02: Label
	@FXML private lateinit var labelRegistry03: Label
	@FXML private lateinit var labelRegistry04: Label
	@FXML private lateinit var labelRegistry05: Label
	@FXML private lateinit var labelRegistry06: Label
	@FXML private lateinit var labelRegistry07: Label
	@FXML private lateinit var checkboxRegistry00: CheckBox
	@FXML private lateinit var checkboxRegistry01: CheckBox
	@FXML private lateinit var checkboxRegistry02: CheckBox
	@FXML private lateinit var checkboxRegistry03: CheckBox
	@FXML private lateinit var checkboxRegistry04: CheckBox
	@FXML private lateinit var checkboxRegistry05: CheckBox
	@FXML private lateinit var checkboxRegistry06: CheckBox
	@FXML private lateinit var checkboxRegistry07: CheckBox

	@FXML private lateinit var hboxButtons: HBox
	@FXML private lateinit var buttonRegistryGet: Button
	@FXML private lateinit var buttonRegistrySet: Button

	private lateinit var primaryStage: Stage
	private lateinit var configController: ConfigController
	private lateinit var deviceConfiguration: DeviceConfiguration

	private lateinit var usbCommunicator: RegistryCommunicatorExtension<USBCommunicator.Descriptor>
	private lateinit var btCommunicator: RegistryCommunicatorExtension<BluetoothCommunicator.Descriptor>
	private lateinit var selectedChannel: () -> Channel?

	private val communicator: RegistryCommunicatorExtension<*>?
		get() = when (selectedChannel()) {
			Channel.USB -> usbCommunicator
			Channel.BLUETOOTH -> btCommunicator
			else -> null
		}

	override var enabled: Boolean = false
		set(value) {
			field = value
			textRegistryAddress.isDisable = !value
			textRegistryRegistry.isDisable = !value
			labelRegistry00.isDisable = !value
			labelRegistry01.isDisable = !value
			labelRegistry02.isDisable = !value
			labelRegistry03.isDisable = !value
			labelRegistry04.isDisable = !value
			labelRegistry05.isDisable = !value
			labelRegistry06.isDisable = !value
			labelRegistry07.isDisable = !value
			checkboxRegistry00.isDisable = !value
			checkboxRegistry01.isDisable = !value
			checkboxRegistry02.isDisable = !value
			checkboxRegistry03.isDisable = !value
			checkboxRegistry04.isDisable = !value
			checkboxRegistry05.isDisable = !value
			checkboxRegistry06.isDisable = !value
			checkboxRegistry07.isDisable = !value
			buttonRegistryGet.isDisable = !value
			buttonRegistrySet.isDisable = !value
		}

	override val rows: List<Triple<Node?, Node?, Node?>>
		get() = listOf(Triple(labelTitle, gridContent, hboxButtons))

	@FXML
	fun initialize() {
		textRegistryAddress.focusedProperty().addListener { _, _, focus ->
			if (!focus) {
				deviceConfiguration.registryAddress = textRegistryAddress.text.takeUnless { it.isNullOrEmpty() }
				configController.save()
			}
		}
	}

	private fun startup() {
		usbCommunicator.register(this)
		btCommunicator.register(this)
	}

	@FXML
	fun onRegistryGet() {
		//mcp23017Enabled = false
		val address = textRegistryAddress.getHexInt()
		val registry = textRegistryAddress.getHexInt()
		if (address != null && registry != null) communicator?.sendRegistryRequest(address, registry)
	}

	@FXML
	fun onRegistrySet() {
		val address = textRegistryAddress.getHexInt()
		val registry = textRegistryAddress.getHexInt()
		if (address != null && registry != null) communicator?.sendRegistryWrite(address, registry, bools2Byte(
				checkboxRegistry07.isSelected,
				checkboxRegistry06.isSelected,
				checkboxRegistry05.isSelected,
				checkboxRegistry04.isSelected,
				checkboxRegistry03.isSelected,
				checkboxRegistry02.isSelected,
				checkboxRegistry01.isSelected,
				checkboxRegistry00.isSelected))
	}

	override fun onRegistryValue(channel: Channel, address: Int, registry: Int,
								 vararg values: Int) = Platform.runLater {
		//registryEnabled = true
		textRegistryAddress.text = "0x%02X".format(address)
		textRegistryRegistry.text = "0x02X".format(registry)
		checkboxRegistry00.isIndeterminate = false
		checkboxRegistry01.isIndeterminate = false
		checkboxRegistry02.isIndeterminate = false
		checkboxRegistry03.isIndeterminate = false
		checkboxRegistry04.isIndeterminate = false
		checkboxRegistry05.isIndeterminate = false
		checkboxRegistry06.isIndeterminate = false
		checkboxRegistry07.isIndeterminate = false

		byte2Bools(values[0]).also {
			checkboxRegistry00.isSelected = it[0]
			checkboxRegistry01.isSelected = it[1]
			checkboxRegistry02.isSelected = it[2]
			checkboxRegistry03.isSelected = it[3]
			checkboxRegistry04.isSelected = it[4]
			checkboxRegistry05.isSelected = it[5]
			checkboxRegistry06.isSelected = it[6]
			checkboxRegistry07.isSelected = it[7]
			labelRegistry00.text = if (it[0]) "1" else "0"
			labelRegistry01.text = if (it[1]) "1" else "0"
			labelRegistry02.text = if (it[2]) "1" else "0"
			labelRegistry03.text = if (it[3]) "1" else "0"
			labelRegistry04.text = if (it[4]) "1" else "0"
			labelRegistry05.text = if (it[5]) "1" else "0"
			labelRegistry06.text = if (it[6]) "1" else "0"
			labelRegistry07.text = if (it[7]) "1" else "0"
		}
	}
}
