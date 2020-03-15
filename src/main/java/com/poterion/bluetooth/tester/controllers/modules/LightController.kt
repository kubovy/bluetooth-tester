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

import com.poterion.bluetooth.tester.INDEFINED
import com.poterion.bluetooth.tester.controllers.ConfigController
import com.poterion.bluetooth.tester.data.DeviceConfiguration
import com.poterion.bluetooth.tester.select
import com.poterion.bluetooth.tester.updateCount
import com.poterion.communication.serial.communicator.BluetoothCommunicator
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.USBCommunicator
import com.poterion.communication.serial.extensions.RgbLightCommunicatorExtension
import com.poterion.communication.serial.listeners.RgbLightCommunicatorListener
import com.poterion.communication.serial.payload.*
import com.poterion.communication.serial.toColor
import com.poterion.communication.serial.toRGBColor
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.stage.Stage

/**
 * Light controller.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class LightController : ModuleControllerInterface, RgbLightCommunicatorListener {
	companion object {
		internal fun getRoot(primaryStage: Stage,
							 configController: ConfigController,
							 deviceConfiguration: DeviceConfiguration,
							 usbCommunicator: USBCommunicator,
							 btCommunicator: BluetoothCommunicator,
							 selectedChannel: () -> Channel?): LightController =
				FXMLLoader(LightController::class.java.getResource("light.fxml"))
						.also { it.load<GridPane>() }
						.getController<LightController>()
						.also { controller ->
							controller.primaryStage = primaryStage
							controller.configController = configController
							controller.deviceConfiguration = deviceConfiguration
							controller.usbCommunicator = RgbLightCommunicatorExtension(usbCommunicator) {
								if (controller.radioLightColorOrderGRB.isSelected) ColorOrder.GRB else ColorOrder.RGB
							}
							controller.btCommunicator = RgbLightCommunicatorExtension(btCommunicator) {
								if (controller.radioLightColorOrderGRB.isSelected) ColorOrder.GRB else ColorOrder.RGB
							}
							controller.selectedChannel = selectedChannel
							controller.startup()
						}
	}

	@FXML private lateinit var labelTitle: Label

	@FXML private lateinit var gridContent: GridPane
	@FXML private lateinit var comboLightIndex: ComboBox<String>
	@FXML private lateinit var comboLightItem: ComboBox<String>
	@FXML private lateinit var textLightListSize: TextField
	@FXML private lateinit var choiceLightRainbow: ChoiceBox<String>
	@FXML private lateinit var comboLightPattern: ComboBox<RgbLightPattern>
	@FXML private lateinit var colorLight1: ColorPicker
	@FXML private lateinit var colorLight2: ColorPicker
	@FXML private lateinit var colorLight3: ColorPicker
	@FXML private lateinit var colorLight4: ColorPicker
	@FXML private lateinit var colorLight5: ColorPicker
	@FXML private lateinit var colorLight6: ColorPicker
	@FXML private lateinit var colorLight7: ColorPicker
	@FXML private lateinit var textLightDelay: TextField
	@FXML private lateinit var textLightWidth: TextField
	@FXML private lateinit var textLightFading: TextField
	@FXML private lateinit var textLightMin: TextField
	@FXML private lateinit var textLightMax: TextField
	@FXML private lateinit var textLightTimeout: TextField
	@FXML private lateinit var radioLightColorOrderRGB: RadioButton
	@FXML private lateinit var radioLightColorOrderGRB: RadioButton

	@FXML private lateinit var gridButtons: GridPane
	@FXML private lateinit var buttonLightGet: Button
	@FXML private lateinit var buttonLightAdd: Button
	@FXML private lateinit var buttonLightSet: Button

	private lateinit var primaryStage: Stage
	private lateinit var configController: ConfigController
	private lateinit var deviceConfiguration: DeviceConfiguration

	private lateinit var usbCommunicator: RgbLightCommunicatorExtension<USBCommunicator.Descriptor>
	private lateinit var btCommunicator: RgbLightCommunicatorExtension<BluetoothCommunicator.Descriptor>
	private lateinit var selectedChannel: () -> Channel?

	private val communicator: RgbLightCommunicatorExtension<*>?
		get() = when (selectedChannel()) {
			Channel.USB -> usbCommunicator
			Channel.BLUETOOTH -> btCommunicator
			else -> null
		}

	override var enabled: Boolean = false
		set(value) {
			field = value
			comboLightIndex.isDisable = !value
			comboLightItem.isDisable = !value
			textLightListSize.isDisable = !value
			choiceLightRainbow.isDisable = !value
			comboLightPattern.isDisable = !value
			colorLight1.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
			colorLight2.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
			colorLight3.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
			colorLight4.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
			colorLight5.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
			colorLight6.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
			colorLight7.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
			textLightDelay.isDisable = !value || comboLightPattern.selectionModel.selectedItem?.delay == null
			textLightWidth.isDisable = !value || comboLightPattern.selectionModel.selectedItem?.width == null
			textLightFading.isDisable = !value || comboLightPattern.selectionModel.selectedItem?.fading == null
			textLightMin.isDisable = !value || comboLightPattern.selectionModel.selectedItem?.min == null
			textLightMax.isDisable = !value || comboLightPattern.selectionModel.selectedItem?.max == null
			textLightTimeout.isDisable = !value || comboLightPattern.selectionModel.selectedItem?.timeout == null
			radioLightColorOrderRGB.isDisable = !value
			radioLightColorOrderGRB.isDisable = !value
			buttonLightGet.isDisable = !value
			buttonLightAdd.isDisable = !value
			buttonLightSet.isDisable = !value
		}

	override val rows: List<Triple<Node?, Node?, Node?>>
		get() = listOf(Triple(labelTitle, gridContent, gridButtons))

	private val lightsCache: MutableMap<Int, MutableMap<Int, RgbLightConfiguration>> = mutableMapOf()

	@FXML
	fun initialize() {
		choiceLightRainbow.items.addAll("Colors", "Rainbow Row", "Rainbow Row Circle", "Rainbow", "Rainbow Circle")
		choiceLightRainbow.selectionModel.selectedIndexProperty().addListener { _, _, value ->
			colorLight1.isDisable = value.toInt() > 0
			colorLight2.isDisable = value.toInt() > 0
			colorLight3.isDisable = value.toInt() > 0
			colorLight4.isDisable = value.toInt() > 0
			//colorLight5.isDisable = value.toInt() > 0
			//colorLight6.isDisable = value.toInt() > 0
			colorLight7.isDisable = value.toInt() > 0
		}
		choiceLightRainbow.selectionModel.select(0)
		comboLightPattern.items.addAll(RgbLightPattern.values())
		comboLightPattern.selectionModel.selectedItemProperty().addListener { _, _, value ->
			textLightDelay.isDisable = value.delay == null
			textLightWidth.isDisable = value.width == null
			textLightFading.isDisable = value.fading == null
			textLightMin.isDisable = value.min == null
			textLightMax.isDisable = value.max == null
			textLightTimeout.isDisable = value.timeout == null

			textLightDelay.text = ""
			textLightWidth.text = ""
			textLightFading.text = ""
			textLightMin.text = ""
			textLightMax.text = ""
			textLightTimeout.text = ""

			value.delay?.also { textLightDelay.promptText = "${it}" }
			value.width?.also { textLightWidth.promptText = "${it}" }
			value.fading?.also { textLightFading.promptText = "${it}" }
			value.min?.also { textLightMin.promptText = "${it}" }
			value.max?.also { textLightMax.promptText = "${it}" }
			value.timeout?.also { textLightTimeout.promptText = "${it}" }
		}

		comboLightPattern.selectionModel.select(RgbLightPattern.OFF)
		colorLight1.value = Color.BLACK
		colorLight1.customColors.clear()
		colorLight1.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		colorLight2.value = Color.BLACK
		colorLight2.customColors.clear()
		colorLight2.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		colorLight3.value = Color.BLACK
		colorLight3.customColors.clear()
		colorLight3.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		colorLight4.value = Color.BLACK
		colorLight4.customColors.clear()
		colorLight4.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		colorLight5.value = Color.BLACK
		colorLight5.customColors.clear()
		colorLight5.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		colorLight6.value = Color.BLACK
		colorLight6.customColors.clear()
		colorLight6.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		colorLight7.value = Color.BLACK
		colorLight7.customColors.clear()
		colorLight7.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		comboLightIndex.selectionModel.selectedItemProperty().addListener { _, _, _ -> comboLightItem.select(0) }
		comboLightItem.selectionModel.selectedItemProperty().addListener { _, _, value ->
			val num = comboLightIndex.value?.toIntOrNull() ?: 0
			val index = value?.toIntOrNull() ?: 0
			val lightConfig = lightsCache
					.getOrPut(num) { mutableMapOf() }
					.getOrPut(index) {
						RgbLightConfiguration(RgbLightPattern.OFF,
								RgbColor(), RgbColor(), RgbColor(), RgbColor(), RgbColor(), RgbColor(), RgbColor(),
								100, 3, 0, 0, 255, 1,
								Rainbow.NO_RAINBOW.code)
					}
			setLight(lightConfig)
		}
		comboLightItem.valueProperty().addListener { _, _, value ->
			val num = comboLightIndex.value?.toIntOrNull() ?: 0
			val index = value?.toIntOrNull() ?: 0
			val lightConfig = lightsCache.getOrPut(num) { mutableMapOf() }[index]
			if (lightConfig != null) setLight(lightConfig)
		}
	}

	private fun startup() {
		usbCommunicator.register(this)
		btCommunicator.register(this)
	}

	@FXML
	fun onLightGet() {
		//ws281xLightEnabled = false
		val num = comboLightIndex.value?.toIntOrNull() ?: 0
		val item = comboLightItem.value?.toIntOrNull()
		communicator?.sendRgbLightItemRequest(num, item)
	}

	@FXML
	fun onLightAdd() {
		val pattern = comboLightPattern.selectionModel.selectedItem
		communicator?.sendRgbLightSet(
				comboLightIndex.value?.toIntOrNull() ?: 0,
				comboLightPattern.value,
				colorLight1.value.toRGBColor(),
				colorLight2.value.toRGBColor(),
				colorLight3.value.toRGBColor(),
				colorLight4.value.toRGBColor(),
				colorLight5.value.toRGBColor(),
				colorLight6.value.toRGBColor(),
				colorLight7.value.toRGBColor(),
				textLightDelay.text.toIntOrNull() ?: pattern.delay ?: 10,
				textLightWidth.text.toIntOrNull() ?: pattern.width ?: 3,
				textLightFading.text.toIntOrNull() ?: pattern.fading ?: 0,
				textLightMin.text.toIntOrNull() ?: pattern.min ?: 0,
				textLightMax.text.toIntOrNull() ?: pattern.max ?: 255,
				textLightTimeout.text.toIntOrNull() ?: pattern.timeout ?: INDEFINED,
				choiceLightRainbow.selectionModel.selectedIndex,
				false)
	}

	@FXML
	fun onLightSet() {
		val pattern = comboLightPattern.selectionModel.selectedItem
		communicator?.sendRgbLightSet(
				comboLightIndex.value?.toIntOrNull() ?: 0,
				pattern,
				colorLight1.value.toRGBColor(),
				colorLight2.value.toRGBColor(),
				colorLight3.value.toRGBColor(),
				colorLight4.value.toRGBColor(),
				colorLight5.value.toRGBColor(),
				colorLight6.value.toRGBColor(),
				colorLight7.value.toRGBColor(),
				textLightDelay.text.toIntOrNull() ?: pattern.delay ?: 10,
				textLightWidth.text.toIntOrNull() ?: pattern.width ?: 3,
				textLightFading.text.toIntOrNull() ?: pattern.fading ?: 0,
				textLightMin.text.toIntOrNull() ?: pattern.min ?: 0,
				textLightMax.text.toIntOrNull() ?: pattern.max ?: 255,
				textLightTimeout.text.toIntOrNull() ?: pattern.timeout ?: INDEFINED,
				choiceLightRainbow.selectionModel.selectedIndex,
				true)
	}

	override fun onRgbLightCountChanged(channel: Channel, count: Int) = Platform.runLater {
		comboLightIndex.updateCount(count)
	}

	override fun onRgbLightConfiguration(channel: Channel,
										 num: Int,
										 count: Int,
										 index: Int,
										 configuration: RgbLightConfiguration) = Platform.runLater {
		//rgbLightEnabled = true
		textLightListSize.text = "${count}"
		lightsCache.getOrPut(num) { mutableMapOf() }[index] = configuration
		comboLightItem.updateCount(count)
		comboLightItem.selectionModel.select("${index}")
	}

	private fun setLight(configuration: RgbLightConfiguration) {
		comboLightPattern.selectionModel.select(configuration.pattern)
		colorLight1.value = configuration.color1.toColor()
		colorLight2.value = configuration.color2.toColor()
		colorLight3.value = configuration.color3.toColor()
		colorLight4.value = configuration.color4.toColor()
		colorLight5.value = configuration.color5.toColor()
		colorLight6.value = configuration.color6.toColor()
		colorLight7.value = configuration.color7.toColor()
		textLightDelay.text = "${configuration.delay}"
		textLightWidth.text = "${configuration.width}"
		textLightFading.text = "${configuration.fading}"
		textLightMin.text = "${configuration.minimum}"
		textLightMax.text = "${configuration.maximum}"
		textLightTimeout.text = "${configuration.timeout}"
	}
}
