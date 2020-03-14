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
import com.poterion.bluetooth.tester.updateCount
import com.poterion.communication.serial.communicator.BluetoothCommunicator
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.USBCommunicator
import com.poterion.communication.serial.extensions.RgbIndicatorCommunicatorExtension
import com.poterion.communication.serial.listeners.RgbIndicatorCommunicatorListener
import com.poterion.communication.serial.payload.ColorOrder
import com.poterion.communication.serial.payload.RgbColor
import com.poterion.communication.serial.payload.RgbIndicatorConfiguration
import com.poterion.communication.serial.payload.RgbPattern
import com.poterion.communication.serial.toColor
import com.poterion.communication.serial.toRGBColor
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.stage.Stage

/**
 * Indicators controller.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class IndicatorsController : ModuleControllerInterface, RgbIndicatorCommunicatorListener {
	companion object {
		internal fun getRoot(primaryStage: Stage,
							 configController: ConfigController,
							 deviceConfiguration: DeviceConfiguration,
							 usbCommunicator: USBCommunicator,
							 btCommunicator: BluetoothCommunicator,
							 selectedChannel: () -> Channel?): IndicatorsController =
				FXMLLoader(IndicatorsController::class.java.getResource("indicators.fxml"))
						.also { it.load<GridPane>() }
						.getController<IndicatorsController>()
						.also { controller ->
							controller.primaryStage = primaryStage
							controller.configController = configController
							controller.deviceConfiguration = deviceConfiguration
							controller.usbCommunicator = RgbIndicatorCommunicatorExtension(usbCommunicator) {
								if (controller.radioIndicatorsColorOrderGRB.isSelected) ColorOrder.GRB else ColorOrder.RGB
							}
							controller.btCommunicator = RgbIndicatorCommunicatorExtension(btCommunicator) {
								if (controller.radioIndicatorsColorOrderGRB.isSelected) ColorOrder.GRB else ColorOrder.RGB
							}
							controller.selectedChannel = selectedChannel
							controller.startup()
						}
	}

	@FXML private lateinit var labelTitle: Label

	@FXML private lateinit var gridContent: GridPane
	@FXML private lateinit var comboIndicatorsIndex: ComboBox<String>
	@FXML private lateinit var textIndicatorsLed: TextField
	@FXML private lateinit var comboIndicatorsPattern: ComboBox<RgbPattern>
	@FXML private lateinit var colorIndicators: ColorPicker
	@FXML private lateinit var textIndicatorsDelay: TextField
	@FXML private lateinit var textIndicatorsMin: TextField
	@FXML private lateinit var textIndicatorsMax: TextField
	@FXML private lateinit var radioIndicatorsColorOrderRGB: RadioButton
	@FXML private lateinit var radioIndicatorsColorOrderGRB: RadioButton

	@FXML private lateinit var vboxButtons: VBox
	@FXML private lateinit var buttonIndicatorsGet: Button
	@FXML private lateinit var buttonIndicatorsSet: Button

	@FXML private lateinit var gridState: GridPane
	@FXML private lateinit var circleIndicators00: Circle
	@FXML private lateinit var circleIndicators01: Circle
	@FXML private lateinit var circleIndicators02: Circle
	@FXML private lateinit var circleIndicators03: Circle
	@FXML private lateinit var circleIndicators04: Circle
	@FXML private lateinit var circleIndicators05: Circle
	@FXML private lateinit var circleIndicators06: Circle
	@FXML private lateinit var circleIndicators07: Circle
	@FXML private lateinit var circleIndicators08: Circle
	@FXML private lateinit var circleIndicators09: Circle
	@FXML private lateinit var circleIndicators10: Circle
	@FXML private lateinit var circleIndicators11: Circle
	@FXML private lateinit var circleIndicators12: Circle
	@FXML private lateinit var circleIndicators13: Circle
	@FXML private lateinit var circleIndicators14: Circle
	@FXML private lateinit var circleIndicators15: Circle
	@FXML private lateinit var circleIndicators16: Circle
	@FXML private lateinit var circleIndicators17: Circle
	@FXML private lateinit var circleIndicators18: Circle
	@FXML private lateinit var circleIndicators19: Circle
	@FXML private lateinit var circleIndicators20: Circle
	@FXML private lateinit var circleIndicators21: Circle
	@FXML private lateinit var circleIndicators22: Circle
	@FXML private lateinit var circleIndicators23: Circle
	@FXML private lateinit var circleIndicators24: Circle
	@FXML private lateinit var circleIndicators25: Circle
	@FXML private lateinit var circleIndicators26: Circle
	@FXML private lateinit var circleIndicators27: Circle
	@FXML private lateinit var circleIndicators28: Circle
	@FXML private lateinit var circleIndicators29: Circle
	@FXML private lateinit var circleIndicators30: Circle
	@FXML private lateinit var circleIndicators31: Circle
	@FXML private lateinit var circleIndicators32: Circle
	@FXML private lateinit var circleIndicators33: Circle
	@FXML private lateinit var circleIndicators34: Circle
	@FXML private lateinit var circleIndicators35: Circle
	@FXML private lateinit var circleIndicators36: Circle
	@FXML private lateinit var circleIndicators37: Circle
	@FXML private lateinit var circleIndicators38: Circle
	@FXML private lateinit var circleIndicators39: Circle
	@FXML private lateinit var circleIndicators40: Circle
	@FXML private lateinit var circleIndicators41: Circle
	@FXML private lateinit var circleIndicators42: Circle
	@FXML private lateinit var circleIndicators43: Circle
	@FXML private lateinit var circleIndicators44: Circle
	@FXML private lateinit var circleIndicators45: Circle
	@FXML private lateinit var circleIndicators46: Circle
	@FXML private lateinit var circleIndicators47: Circle
	@FXML private lateinit var circleIndicators48: Circle
	@FXML private lateinit var circleIndicators49: Circle

	private lateinit var primaryStage: Stage
	private lateinit var configController: ConfigController
	private lateinit var deviceConfiguration: DeviceConfiguration

	private lateinit var usbCommunicator: RgbIndicatorCommunicatorExtension<USBCommunicator.Descriptor>
	private lateinit var btCommunicator: RgbIndicatorCommunicatorExtension<BluetoothCommunicator.Descriptor>
	private lateinit var selectedChannel: () -> Channel?

	private val communicator: RgbIndicatorCommunicatorExtension<*>?
		get() = when (selectedChannel()) {
			Channel.USB -> usbCommunicator
			Channel.BLUETOOTH -> btCommunicator
			else -> null
		}

	override var enabled: Boolean = false
		set(value) {
			field = value
			comboIndicatorsIndex.isDisable = !value
			textIndicatorsLed.isDisable = !value
			comboIndicatorsPattern.isDisable = !value
			colorIndicators.isDisable = !value
			textIndicatorsDelay.isDisable = !value
			textIndicatorsMin.isDisable = !value
			textIndicatorsMax.isDisable = !value
			radioIndicatorsColorOrderRGB.isDisable = !value
			radioIndicatorsColorOrderGRB.isDisable = !value
			buttonIndicatorsGet.isDisable = !value
			buttonIndicatorsSet.isDisable = !value
		}

	override val rows: List<Triple<Node?, Node?, Node?>>
		get() = listOf(
				Triple(labelTitle, gridContent, vboxButtons),
				Triple(null, gridState, null))

	private val patternCache: MutableMap<Int, MutableMap<Int, RgbPattern>> = mutableMapOf()
	private val colorCache: MutableMap<Int, MutableMap<Int, RgbColor>> = mutableMapOf()
	private val delayCache: MutableMap<Int, MutableMap<Int, Int?>> = mutableMapOf()
	private val minMaxCache: MutableMap<Int, MutableMap<Int, Pair<Int?, Int?>>> = mutableMapOf()

	@FXML
	fun initialize() {
		circleIndicators00.fill = Color.TRANSPARENT
		circleIndicators01.fill = Color.TRANSPARENT
		circleIndicators02.fill = Color.TRANSPARENT
		circleIndicators03.fill = Color.TRANSPARENT
		circleIndicators04.fill = Color.TRANSPARENT
		circleIndicators05.fill = Color.TRANSPARENT
		circleIndicators06.fill = Color.TRANSPARENT
		circleIndicators07.fill = Color.TRANSPARENT
		circleIndicators08.fill = Color.TRANSPARENT
		circleIndicators09.fill = Color.TRANSPARENT
		circleIndicators10.fill = Color.TRANSPARENT
		circleIndicators11.fill = Color.TRANSPARENT
		circleIndicators12.fill = Color.TRANSPARENT
		circleIndicators13.fill = Color.TRANSPARENT
		circleIndicators14.fill = Color.TRANSPARENT
		circleIndicators15.fill = Color.TRANSPARENT
		circleIndicators16.fill = Color.TRANSPARENT
		circleIndicators17.fill = Color.TRANSPARENT
		circleIndicators18.fill = Color.TRANSPARENT
		circleIndicators19.fill = Color.TRANSPARENT
		circleIndicators20.fill = Color.TRANSPARENT
		circleIndicators21.fill = Color.TRANSPARENT
		circleIndicators22.fill = Color.TRANSPARENT
		circleIndicators23.fill = Color.TRANSPARENT
		circleIndicators24.fill = Color.TRANSPARENT
		circleIndicators25.fill = Color.TRANSPARENT
		circleIndicators26.fill = Color.TRANSPARENT
		circleIndicators27.fill = Color.TRANSPARENT
		circleIndicators28.fill = Color.TRANSPARENT
		circleIndicators29.fill = Color.TRANSPARENT
		circleIndicators30.fill = Color.TRANSPARENT
		circleIndicators31.fill = Color.TRANSPARENT
		circleIndicators32.fill = Color.TRANSPARENT
		circleIndicators33.fill = Color.TRANSPARENT
		circleIndicators34.fill = Color.TRANSPARENT
		circleIndicators35.fill = Color.TRANSPARENT
		circleIndicators36.fill = Color.TRANSPARENT
		circleIndicators37.fill = Color.TRANSPARENT
		circleIndicators38.fill = Color.TRANSPARENT
		circleIndicators39.fill = Color.TRANSPARENT
		circleIndicators40.fill = Color.TRANSPARENT
		circleIndicators41.fill = Color.TRANSPARENT
		circleIndicators42.fill = Color.TRANSPARENT
		circleIndicators43.fill = Color.TRANSPARENT
		circleIndicators44.fill = Color.TRANSPARENT
		circleIndicators45.fill = Color.TRANSPARENT
		circleIndicators46.fill = Color.TRANSPARENT
		circleIndicators47.fill = Color.TRANSPARENT
		circleIndicators48.fill = Color.TRANSPARENT
		circleIndicators49.fill = Color.TRANSPARENT
		comboIndicatorsPattern.items.addAll(RgbPattern.values())
		comboIndicatorsPattern.selectionModel.select(0)
		colorIndicators.value = Color.BLACK
		colorIndicators.customColors.clear()
		colorIndicators.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
	}

	private fun startup() {
		usbCommunicator.register(this)
		btCommunicator.register(this)
	}

	@FXML
	fun onIndicatorsGet() {
//		//ws281xEnabled = false
		val num = comboIndicatorsIndex.value?.toIntOrNull() ?: 0
		val leds = textIndicatorsLed.text.toIntOrNull()?.let { listOf(it) } ?: (0 until 50)
		for (led in leds) {
			communicator?.sendRgbIndicatorLedRequest(num, led)
		}
	}

	@FXML
	fun onIndicatorsSet() {
		val num = comboIndicatorsIndex.value?.toIntOrNull() ?: 0
		val led = textIndicatorsLed.text.toIntOrNull()
		val color = colorIndicators.value.toRGBColor()
		if (led == null) {
			communicator?.sendRgbIndicatorSetAll(num, color)
			(0 until 50).forEach { i ->
				this::class.java.declaredFields
						.find { it.name == "circleIndicators%02d".format(i) }
						?.get(this)
						?.let { it as? Circle }
						?.fill = colorIndicators.value
			}
		} else {
			communicator?.sendRgbIndicatorSet(
					num,
					led,
					comboIndicatorsPattern.value,
					color,
					textIndicatorsDelay.text.toIntOrNull() ?: 50,
					textIndicatorsMin.text.toIntOrNull() ?: 0,
					textIndicatorsMax.text.toIntOrNull() ?: 255,
					true)
			this::class.java.declaredFields
					.find { it.name == "circleIndicators%02d".format(led) }
					?.get(this)
					?.let { it as? Circle }
					?.fill = colorIndicators.value
		}
	}

	@FXML
	fun onIndicatorsClick(event: MouseEvent) = event.source.let { it as? Circle }?.id
			?.replace("circleIndicators", "")
			?.toIntOrNull()
			?.also { led ->
				val num = comboIndicatorsIndex.value?.toIntOrNull() ?: 0
				textIndicatorsLed.text = "${led}"
				val pattern = patternCache
						.getOrPut(num) { mutableMapOf() }
						.getOrPut(led) { RgbPattern.OFF }
				comboIndicatorsPattern.selectionModel.select(pattern)
				colorIndicators.value = colorCache
						.getOrPut(num) { mutableMapOf() }
						.getOrPut(led) { RgbColor() }
						.toColor()
				textIndicatorsDelay.text = delayCache
						.getOrPut(num) { mutableMapOf() }
						.getOrPut(led) { pattern.delay }
						?.toString()
						?: ""
				textIndicatorsMin.text = minMaxCache
						.getOrPut(num) { mutableMapOf() }
						.getOrPut(led) { pattern.min to pattern.max }
						.first
						?.toString()
						?: ""
				textIndicatorsMax.text = minMaxCache
						.getOrPut(num) { mutableMapOf() }
						.getOrPut(led) { pattern.min to pattern.max }
						.second
						?.toString()
						?: ""
			}

	override fun onRgbIndicatorCountChanged(channel: Channel, count: Int) = Platform.runLater {
		comboIndicatorsIndex.updateCount(count)
	}

	override fun onRgbIndicatorConfiguration(channel: Channel,
											 num: Int,
											 count: Int,
											 index: Int,
											 configuration: RgbIndicatorConfiguration) = Platform.runLater {
		//rgbIndicatorsEnabled = true
		val color = configuration.color
		patternCache.getOrPut(num) { mutableMapOf() }[index] = configuration.pattern
		colorCache.getOrPut(num) { mutableMapOf() }[index] = color
		delayCache.getOrPut(num) { mutableMapOf() }[index] = configuration.delay
		minMaxCache.getOrPut(num) { mutableMapOf() }[index] = configuration.minimum to configuration.maximum

		textIndicatorsLed.text = "${index}"
		comboIndicatorsPattern.selectionModel.select(configuration.pattern)
		colorIndicators.value = color.toColor()
		textIndicatorsDelay.text = "${configuration.delay}"
		textIndicatorsMin.text = "${configuration.minimum}"
		textIndicatorsMax.text = "${configuration.maximum}"

		(0 until 50).forEach { led ->
			this::class.java.declaredFields
					.find { it.name == "circleWS281x%02d".format(led) }
					?.get(this)
					?.let { it as? Circle }
					?.fill = colorCache.getOrPut(num) { mutableMapOf() }[led]?.toColor() ?: Color.TRANSPARENT
		}
	}
}
