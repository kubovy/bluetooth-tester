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

import com.poterion.bluetooth.tester.*
import com.poterion.bluetooth.tester.controllers.ConfigController
import com.poterion.bluetooth.tester.data.DeviceConfiguration
import com.poterion.communication.serial.communicator.BluetoothCommunicator
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.USBCommunicator
import com.poterion.communication.serial.extensions.RgbStripCommunicatorExtension
import com.poterion.communication.serial.listeners.RgbStripCommunicatorListener
import com.poterion.communication.serial.payload.ColorOrder
import com.poterion.communication.serial.payload.RgbPattern
import com.poterion.communication.serial.payload.RgbStripConfiguration
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.stage.Stage

/**
 * RGB strip controller.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class RgbStripController : ModuleControllerInterface, RgbStripCommunicatorListener {
	companion object {
		internal fun getRoot(primaryStage: Stage,
							 configController: ConfigController,
							 deviceConfiguration: DeviceConfiguration,
							 usbCommunicator: USBCommunicator,
							 btCommunicator: BluetoothCommunicator,
							 selectedChannel: () -> Channel?): RgbStripController =
				FXMLLoader(RgbStripController::class.java.getResource("rgb-strip.fxml"))
						.also { it.load<GridPane>() }
						.getController<RgbStripController>()
						.also { controller ->
							controller.primaryStage = primaryStage
							controller.configController = configController
							controller.deviceConfiguration = deviceConfiguration
							controller.usbCommunicator = RgbStripCommunicatorExtension(usbCommunicator) {
								if (controller.radioRgbColorOrderGRB.isSelected) ColorOrder.GRB else ColorOrder.RGB
							}
							controller.btCommunicator = RgbStripCommunicatorExtension(btCommunicator) {
								if (controller.radioRgbColorOrderGRB.isSelected) ColorOrder.GRB else ColorOrder.RGB
							}
							controller.selectedChannel = selectedChannel
							controller.startup()
						}
	}

	@FXML private lateinit var labelTitle: Label

	@FXML private lateinit var gridContent: GridPane
	@FXML private lateinit var comboRgbIndex: ComboBox<String>
	@FXML private lateinit var comboRgbItem: ComboBox<String>
	@FXML private lateinit var textRgbListSize: TextField
	@FXML private lateinit var comboRgbPattern: ComboBox<RgbPattern>
	@FXML private lateinit var colorRgb: ColorPicker
	@FXML private lateinit var textRgbDelay: TextField
	@FXML private lateinit var textRgbMin: TextField
	@FXML private lateinit var textRgbMax: TextField
	@FXML private lateinit var textRgbTimeout: TextField
	@FXML private lateinit var radioRgbColorOrderRGB: RadioButton
	@FXML private lateinit var radioRgbColorOrderGRB: RadioButton

	@FXML private lateinit var gridButtons: GridPane
	@FXML private lateinit var buttonRgbGet: Button
	@FXML private lateinit var buttonRgbAdd: Button
	@FXML private lateinit var buttonRgbSet: Button

	private lateinit var primaryStage: Stage
	private lateinit var configController: ConfigController
	private lateinit var deviceConfiguration: DeviceConfiguration

	private lateinit var usbCommunicator: RgbStripCommunicatorExtension<USBCommunicator.Descriptor>
	private lateinit var btCommunicator: RgbStripCommunicatorExtension<BluetoothCommunicator.Descriptor>
	private lateinit var selectedChannel: () -> Channel?

	private val communicator: RgbStripCommunicatorExtension<*>?
		get() = when (selectedChannel()) {
			Channel.USB -> usbCommunicator
			Channel.BLUETOOTH -> btCommunicator
			else -> null
		}

	override var enabled: Boolean = false
		set(value) {
			field = value
			comboRgbIndex.isDisable = !value
			comboRgbItem.isDisable = !value
			textRgbListSize.isDisable = !value
			comboRgbPattern.isDisable = !value
			colorRgb.isDisable = !value
			textRgbDelay.isDisable = !value
			textRgbMin.isDisable = !value
			textRgbMax.isDisable = !value
			textRgbTimeout.isDisable = !value
			radioRgbColorOrderRGB.isDisable = !value
			radioRgbColorOrderGRB.isDisable = !value
			buttonRgbGet.isDisable = !value
			buttonRgbAdd.isDisable = !value
			buttonRgbSet.isDisable = !value
		}

	override val rows: List<Triple<Node?, Node?, Node?>>
		get() = listOf(Triple(labelTitle, gridContent, gridButtons))

	private val rgbConfigs: MutableMap<Int, MutableMap<Int, RgbStripConfiguration>> = mutableMapOf()

	@FXML
	fun initialize() {
		comboRgbPattern.items.addAll(RgbPattern.values())
		comboRgbPattern.selectionModel.selectedItemProperty().addListener { _, _, value ->
			textRgbDelay.isDisable = value.delay == null
			textRgbMin.isDisable = value.min == null
			textRgbMax.isDisable = value.max == null
			textRgbTimeout.isDisable = value.timeout == null

			textRgbDelay.text = ""
			textRgbMin.text = ""
			textRgbMax.text = ""
			textRgbTimeout.text = ""

			value.delay?.also { textRgbDelay.promptText = "${it}" }
			value.min?.also { textRgbMin.promptText = "${it}" }
			value.max?.also { textRgbMax.promptText = "${it}" }
			value.timeout?.also { textRgbTimeout.promptText = "${it}" }
		}
		comboRgbPattern.selectionModel.select(0)
		colorRgb.value = Color.BLACK
		colorRgb.customColors.clear()
		colorRgb.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		comboRgbIndex.selectionModel.selectedItemProperty().addListener { _, _, _ -> comboRgbItem.select(0) }
		comboRgbItem.selectionModel.selectedItemProperty().addListener { _, _, value ->
			val num = comboRgbIndex.value?.toIntOrNull() ?: 0
			val index = value?.toIntOrNull() ?: 0
			val rgbConfig = rgbConfigs
					.getOrPut(num) { mutableMapOf() }
					.getOrPut(index) { RgbStripConfiguration(RgbPattern.OFF, java.awt.Color.BLACK, 0, 0, 255, 1) }
			setRgb(rgbConfig)
		}
		comboRgbItem.valueProperty().addListener { _, _, value ->
			val num = comboRgbIndex.value?.toIntOrNull() ?: 0
			val index = value?.toIntOrNull() ?: 0
			val rgbConfig = rgbConfigs.getOrPut(num) { mutableMapOf() }[index]
			if (rgbConfig != null) setRgb(rgbConfig)
		}
	}

	private fun startup() {
		usbCommunicator.register(this)
		btCommunicator.register(this)
	}

	@FXML
	fun onRgbGet() {
		this::class.java.declaredFields
				.find { it.name == "buttonGpioGet%02d".format(it) }
				?.get(this)
				?.let { it as? Circle }
				?.fill = colorRgb.value
		//rgbEnabled = false
		val num = comboRgbIndex.value?.toIntOrNull() ?: 0
		val index = comboRgbItem.value?.toIntOrNull()
		communicator?.sendRgbStripConfigurationRequest(num, index)
	}

	@FXML
	fun onRgbAdd() {
		val num = comboRgbIndex.value?.toIntOrNull() ?: 0
		val pattern = comboRgbPattern.selectionModel.selectedItem
		val color = colorRgb.value.toAwtColor()
		communicator?.sendRgbStripConfiguration(
				num,
				pattern,
				color,
				textRgbDelay.text.toIntOrNull() ?: pattern.delay ?: 100,
				textRgbMin.text.toIntOrNull() ?: pattern.min ?: 0,
				textRgbMax.text.toIntOrNull() ?: pattern.max ?: 255,
				textRgbTimeout.text.toIntOrNull() ?: pattern.timeout ?: INDEFINED,
				false)
	}

	@FXML
	fun onRgbSet() {
		val num = comboRgbIndex.value?.toIntOrNull() ?: 0
		val pattern = comboRgbPattern.selectionModel.selectedItem
		val color = colorRgb.value.toAwtColor()
		communicator?.sendRgbStripConfiguration(
				num,
				pattern,
				color,
				textRgbDelay.text.toIntOrNull() ?: pattern.delay ?: 100,
				textRgbMin.text.toIntOrNull() ?: pattern.min ?: 0,
				textRgbMax.text.toIntOrNull() ?: 255,
				textRgbTimeout.text.toIntOrNull() ?: pattern.timeout ?: INDEFINED,
				true)
	}

	override fun onRgbStripCountChanged(channel: Channel, count: Int) = Platform.runLater {
		comboRgbIndex.updateCount(count)
	}

	override fun onRgbStripConfiguration(channel: Channel,
										 num: Int,
										 count: Int,
										 index: Int,
										 configuration: RgbStripConfiguration) = Platform.runLater {
		//rgbStripEnabled = true
		textRgbListSize.text = "${count}"
		rgbConfigs.getOrPut(num) { mutableMapOf() }[index] = configuration
		comboRgbItem.updateCount(count)
		comboRgbItem.selectionModel.select("${index}")
	}

	private fun setRgb(configuration: RgbStripConfiguration) {
		comboRgbPattern.selectionModel.select(configuration.pattern)
		colorRgb.value = configuration.color.toColor()
		textRgbDelay.text = "${configuration.delay}"
		textRgbMin.text = "${configuration.minimum}"
		textRgbMax.text = "${configuration.maximum}"
		textRgbTimeout.text = "${configuration.timeout}"
	}
}
