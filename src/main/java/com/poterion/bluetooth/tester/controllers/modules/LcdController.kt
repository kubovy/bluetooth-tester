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
import com.poterion.communication.serial.extensions.LcdCommunicatorExtension
import com.poterion.communication.serial.listeners.LcdCommunicatorListener
import com.poterion.communication.serial.payload.LcdCommand
import com.poterion.utils.kotlin.noop
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import kotlin.math.min

/**
 * LCD controller.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class LcdController : ModuleControllerInterface, LcdCommunicatorListener {
	companion object {
		internal fun getRoot(primaryStage: Stage,
							 configController: ConfigController,
							 deviceConfiguration: DeviceConfiguration,
							 usbCommunicator: USBCommunicator,
							 btCommunicator: BluetoothCommunicator,
							 selectedChannel: () -> Channel?): LcdController =
				FXMLLoader(LcdController::class.java.getResource("lcd.fxml"))
						.also { it.load<GridPane>() }
						.getController<LcdController>()
						.also { controller ->
							controller.primaryStage = primaryStage
							controller.configController = configController
							controller.deviceConfiguration = deviceConfiguration
							controller.usbCommunicator = LcdCommunicatorExtension(usbCommunicator)
							controller.btCommunicator = LcdCommunicatorExtension(btCommunicator)
							controller.selectedChannel = selectedChannel
							controller.startup()
						}
	}

	@FXML private lateinit var labelTitle: Label

	@FXML private lateinit var gridContent: GridPane
	@FXML private lateinit var comboLcdIndex: ComboBox<String>
	@FXML private lateinit var checkboxLcdBacklight: CheckBox
	@FXML private lateinit var textLcdLine: TextField
	@FXML private lateinit var areaLcd: TextArea

	@FXML private lateinit var gridButtons: GridPane
	@FXML private lateinit var buttonLcdGet: Button
	@FXML private lateinit var buttonLcdSet: Button
	@FXML private lateinit var buttonLcdClear: Button
	@FXML private lateinit var buttonLcdReset: Button

	private lateinit var primaryStage: Stage
	private lateinit var configController: ConfigController
	private lateinit var deviceConfiguration: DeviceConfiguration

	private lateinit var usbCommunicator: LcdCommunicatorExtension<USBCommunicator.Descriptor>
	private lateinit var btCommunicator: LcdCommunicatorExtension<BluetoothCommunicator.Descriptor>
	private lateinit var selectedChannel: () -> Channel?

	private val communicator: LcdCommunicatorExtension<*>?
		get() = when (selectedChannel()) {
			Channel.USB -> usbCommunicator
			Channel.BLUETOOTH -> btCommunicator
			else -> null
		}

	override var enabled: Boolean = false
		set(value) {
			field = value
			comboLcdIndex.isDisable = !value
			checkboxLcdBacklight.isDisable = !value
			textLcdLine.isDisable = !value
			areaLcd.isDisable = !value
			buttonLcdGet.isDisable = !value
			buttonLcdSet.isDisable = !value
			buttonLcdClear.isDisable = !value
			buttonLcdReset.isDisable = !value
		}

	override val rows: List<Triple<Node?, Node?, Node?>>
		get() = listOf(Triple(labelTitle, gridContent, gridButtons))

	@FXML
	fun initialize() {
		textLcdLine.focusedProperty().addListener { _, _, focus ->
			if (!focus) {
				deviceConfiguration.lcdLine = textLcdLine.text.toIntOrNull()
				configController.save()
			}
		}
	}

	private fun startup() {
		usbCommunicator.register(this)
		btCommunicator.register(this)
		textLcdLine.text = deviceConfiguration.lcdLine?.toString() ?: ""
	}

	@FXML
	fun onLcdGet() {
		//lcdEnabled = false
		val num = comboLcdIndex.value?.toIntOrNull() ?: 0
		val line = textLcdLine.text.toIntOrNull()
		communicator?.sendLcdContentRequest(num, line)
	}

	@FXML
	fun onLcdSet() {
		val num = comboLcdIndex.value?.toIntOrNull() ?: 0
		val backlight = checkboxLcdBacklight.isSelected
		areaLcd.text.split("\n")
				.asSequence()
				.filterIndexed { line, _ -> line < 4 }
				.map { it.substring(0, min(it.length, 20)) }
				.mapIndexed { line, data -> line to data }
				.filter { (line, _) -> line == (textLcdLine.text.toIntOrNull() ?: line) }
				.toList()
				.forEach { (line, data) -> communicator?.sendLcdLine(num, line, data, backlight) }
	}

	@FXML
	fun onLcdBacklight() {
		val num = comboLcdIndex.value?.toIntOrNull() ?: 0
		val command = if (checkboxLcdBacklight.isSelected) LcdCommand.BACKLIGHT else LcdCommand.NO_BACKLIGHT
		communicator?.sendLcdCommand(num, command)
	}

	@FXML
	fun onLcdClear() {
		val num = comboLcdIndex.value?.toIntOrNull() ?: 0
		communicator?.sendLcdCommand(num, LcdCommand.CLEAR)
	}

	@FXML
	fun onLcdReset() {
		val num = comboLcdIndex.value?.toIntOrNull() ?: 0
		communicator?.sendLcdCommand(num, LcdCommand.RESET)
	}

	override fun onLcdCountReceived(channel: Channel, count: Int) = Platform.runLater {
		comboLcdIndex.updateCount(count)
	}

	override fun onLcdCommandReceived(channel: Channel, num: Int, command: LcdCommand) = Platform.runLater {
		comboLcdIndex.select(num)
		when (command) {
			LcdCommand.CLEAR -> areaLcd.text = "\n\n\n"
			LcdCommand.RESET -> noop()
			LcdCommand.BACKLIGHT -> {
				checkboxLcdBacklight.isIndeterminate = false
				checkboxLcdBacklight.isSelected = true
			}
			LcdCommand.NO_BACKLIGHT -> {
				checkboxLcdBacklight.isIndeterminate = false
				checkboxLcdBacklight.isSelected = false
			}
		}
	}

	override fun onLcdContentChanged(channel: Channel,
									 num: Int,
									 backlight: Boolean,
									 line: Int,
									 content: String) = Platform.runLater {
		// lcdEnabled = true
		comboLcdIndex.select(num)
		val cache = areaLcd.text
				.split("\n".toRegex())
				.mapIndexed { l, c -> l to c }
				.toMap()
				.toMutableMap()
		cache[line] = content
		checkboxLcdBacklight.isIndeterminate = false
		checkboxLcdBacklight.isSelected = backlight
		areaLcd.text = (0 until 4).joinToString("\n") { cache[it] ?: "" }
	}
}
