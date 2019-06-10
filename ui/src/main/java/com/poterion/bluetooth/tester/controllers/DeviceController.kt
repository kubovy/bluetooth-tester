package com.poterion.bluetooth.tester.controllers

import com.poterion.bluetooth.tester.data.DeviceConfiguration
import com.poterion.monitor.api.communication.*
import com.poterion.monitor.api.data.*
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.StringConverter
import jssc.SerialPortList
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min


/**
 * Bluetooth tester's controller.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class DeviceController : CommunicatorListener {
	companion object {
		internal fun getRoot(primaryStage: Stage,
							 configController: ConfigController,
							 deviceConfiguration: DeviceConfiguration): Pair<Parent, DeviceController> =
				FXMLLoader(DeviceController::class.java.getResource("device.fxml"))
						.let { it.load<Parent>() to it.getController<DeviceController>() }
						.also { (_, controller) ->
							controller.primaryStage = primaryStage
							controller.configController = configController
							controller.deviceConfiguration = deviceConfiguration
						}
						.also { (_, controller) -> controller.startup() }
	}

	@FXML private lateinit var connection: ToggleGroup

	@FXML private lateinit var radioUSB: RadioButton
	@FXML private lateinit var choiceUSBPort: ChoiceBox<Pair<String, Boolean>>
	@FXML private lateinit var labelUSBStatus: Label
	@FXML private lateinit var progressUSB: ProgressBar
	@FXML private lateinit var buttonUSBConnect: Button

	@FXML private lateinit var radioBluetooth: RadioButton
	@FXML private lateinit var textBluetoothAddress: TextField
	@FXML private lateinit var textBluetoothChannel: TextField
	@FXML private lateinit var labelBluetoothStatus: Label
	@FXML private lateinit var progressBluetooth: ProgressBar
	@FXML private lateinit var buttonBluetoothConnect: Button

	@FXML private lateinit var textBluetoothName: TextField
	@FXML private lateinit var textBluetoothPin: TextField
	@FXML private lateinit var comboBluetoothPairingMethod: ComboBox<PairingMethod>
	@FXML private lateinit var buttonSettingsGet: Button
	@FXML private lateinit var buttonSettingsSet: Button

	@FXML private lateinit var textBluetoothEepromBlockSize: TextField
	@FXML private lateinit var buttonBluetoothEepromDownload: Button
	@FXML private lateinit var buttonBluetoothEepromUpload: Button

	@FXML private lateinit var textDHT11Temperature: TextField
	@FXML private lateinit var textDHT11Humidity: TextField
	@FXML private lateinit var buttonDHT11Get: Button

	@FXML private lateinit var checkboxLCDBacklight: CheckBox
	@FXML private lateinit var textLCDLine: TextField
	@FXML private lateinit var areaLCD: TextArea
	@FXML private lateinit var buttonLCDGet: Button
	@FXML private lateinit var buttonLCDSet: Button
	@FXML private lateinit var buttonLCDClear: Button
	@FXML private lateinit var buttonLCDReset: Button

	@FXML private lateinit var textMCP23017Address: TextField
	@FXML private lateinit var checkboxMCP23017PortA: CheckBox
	@FXML private lateinit var checkboxMCP23017PortB: CheckBox
	@FXML private lateinit var labelMCP23017A00: Label
	@FXML private lateinit var labelMCP23017A01: Label
	@FXML private lateinit var labelMCP23017A02: Label
	@FXML private lateinit var labelMCP23017A03: Label
	@FXML private lateinit var labelMCP23017A04: Label
	@FXML private lateinit var labelMCP23017A05: Label
	@FXML private lateinit var labelMCP23017A06: Label
	@FXML private lateinit var labelMCP23017A07: Label
	@FXML private lateinit var labelMCP23017B00: Label
	@FXML private lateinit var labelMCP23017B01: Label
	@FXML private lateinit var labelMCP23017B02: Label
	@FXML private lateinit var labelMCP23017B03: Label
	@FXML private lateinit var labelMCP23017B04: Label
	@FXML private lateinit var labelMCP23017B05: Label
	@FXML private lateinit var labelMCP23017B06: Label
	@FXML private lateinit var labelMCP23017B07: Label
	@FXML private lateinit var checkboxMCP23017A00: CheckBox
	@FXML private lateinit var checkboxMCP23017A01: CheckBox
	@FXML private lateinit var checkboxMCP23017A02: CheckBox
	@FXML private lateinit var checkboxMCP23017A03: CheckBox
	@FXML private lateinit var checkboxMCP23017A04: CheckBox
	@FXML private lateinit var checkboxMCP23017A05: CheckBox
	@FXML private lateinit var checkboxMCP23017A06: CheckBox
	@FXML private lateinit var checkboxMCP23017A07: CheckBox
	@FXML private lateinit var checkboxMCP23017B00: CheckBox
	@FXML private lateinit var checkboxMCP23017B01: CheckBox
	@FXML private lateinit var checkboxMCP23017B02: CheckBox
	@FXML private lateinit var checkboxMCP23017B03: CheckBox
	@FXML private lateinit var checkboxMCP23017B04: CheckBox
	@FXML private lateinit var checkboxMCP23017B05: CheckBox
	@FXML private lateinit var checkboxMCP23017B06: CheckBox
	@FXML private lateinit var checkboxMCP23017B07: CheckBox
	@FXML private lateinit var buttonMCP23017Get: Button
	@FXML private lateinit var buttonMCP23017Set: Button

	@FXML private lateinit var labelPIR: Label
	@FXML private lateinit var buttonPIRGet: Button

	@FXML private lateinit var comboRGBItem: ComboBox<String>
	@FXML private lateinit var textRGBListSize: TextField
	@FXML private lateinit var comboRGBPattern: ComboBox<RGBPattern>
	@FXML private lateinit var colorRGB: ColorPicker
	@FXML private lateinit var textRGBDelay: TextField
	@FXML private lateinit var textRGBMin: TextField
	@FXML private lateinit var textRGBMax: TextField
	@FXML private lateinit var textRGBTimeout: TextField
	@FXML private lateinit var buttonRGBGet: Button
	@FXML private lateinit var buttonRGBAdd: Button
	@FXML private lateinit var buttonRGBSet: Button

	@FXML private lateinit var textWS281xLED: TextField
	@FXML private lateinit var comboWS281xPattern: ComboBox<WS281xPattern>
	@FXML private lateinit var colorWS281x: ColorPicker
	@FXML private lateinit var textWS281xDelay: TextField
	@FXML private lateinit var textWS281xMin: TextField
	@FXML private lateinit var textWS281xMax: TextField
	@FXML private lateinit var circleWS281x00: Circle
	@FXML private lateinit var circleWS281x01: Circle
	@FXML private lateinit var circleWS281x02: Circle
	@FXML private lateinit var circleWS281x03: Circle
	@FXML private lateinit var circleWS281x04: Circle
	@FXML private lateinit var circleWS281x05: Circle
	@FXML private lateinit var circleWS281x06: Circle
	@FXML private lateinit var circleWS281x07: Circle
	@FXML private lateinit var circleWS281x08: Circle
	@FXML private lateinit var circleWS281x09: Circle
	@FXML private lateinit var circleWS281x10: Circle
	@FXML private lateinit var circleWS281x11: Circle
	@FXML private lateinit var circleWS281x12: Circle
	@FXML private lateinit var circleWS281x13: Circle
	@FXML private lateinit var circleWS281x14: Circle
	@FXML private lateinit var circleWS281x15: Circle
	@FXML private lateinit var circleWS281x16: Circle
	@FXML private lateinit var circleWS281x17: Circle
	@FXML private lateinit var circleWS281x18: Circle
	@FXML private lateinit var circleWS281x19: Circle
	@FXML private lateinit var circleWS281x20: Circle
	@FXML private lateinit var circleWS281x21: Circle
	@FXML private lateinit var circleWS281x22: Circle
	@FXML private lateinit var circleWS281x23: Circle
	@FXML private lateinit var circleWS281x24: Circle
	@FXML private lateinit var circleWS281x25: Circle
	@FXML private lateinit var circleWS281x26: Circle
	@FXML private lateinit var circleWS281x27: Circle
	@FXML private lateinit var circleWS281x28: Circle
	@FXML private lateinit var circleWS281x29: Circle
	@FXML private lateinit var circleWS281x30: Circle
	@FXML private lateinit var circleWS281x31: Circle
	@FXML private lateinit var circleWS281x32: Circle
	@FXML private lateinit var circleWS281x33: Circle
	@FXML private lateinit var circleWS281x34: Circle
	@FXML private lateinit var circleWS281x35: Circle
	@FXML private lateinit var circleWS281x36: Circle
	@FXML private lateinit var circleWS281x37: Circle
	@FXML private lateinit var circleWS281x38: Circle
	@FXML private lateinit var circleWS281x39: Circle
	@FXML private lateinit var circleWS281x40: Circle
	@FXML private lateinit var circleWS281x41: Circle
	@FXML private lateinit var circleWS281x42: Circle
	@FXML private lateinit var circleWS281x43: Circle
	@FXML private lateinit var circleWS281x44: Circle
	@FXML private lateinit var circleWS281x45: Circle
	@FXML private lateinit var circleWS281x46: Circle
	@FXML private lateinit var circleWS281x47: Circle
	@FXML private lateinit var circleWS281x48: Circle
	@FXML private lateinit var circleWS281x49: Circle
	@FXML private lateinit var buttonWS281xGet: Button
	@FXML private lateinit var buttonWS281xSet: Button

	@FXML private lateinit var comboWS281xLightItem: ComboBox<String>
	@FXML private lateinit var textWS281xLightListSize: TextField
	@FXML private lateinit var choiceWS281xLightRainbow: ChoiceBox<String>
	@FXML private lateinit var comboWS281xLightPattern: ComboBox<WS281xLightPattern>
	@FXML private lateinit var colorWS281xLight1: ColorPicker
	@FXML private lateinit var colorWS281xLight2: ColorPicker
	@FXML private lateinit var colorWS281xLight3: ColorPicker
	@FXML private lateinit var colorWS281xLight4: ColorPicker
	@FXML private lateinit var colorWS281xLight5: ColorPicker
	@FXML private lateinit var colorWS281xLight6: ColorPicker
	@FXML private lateinit var colorWS281xLight7: ColorPicker
	@FXML private lateinit var textWS281xLightDelay: TextField
	@FXML private lateinit var textWS281xLightWidth: TextField
	@FXML private lateinit var textWS281xLightFading: TextField
	@FXML private lateinit var textWS281xLightMin: TextField
	@FXML private lateinit var textWS281xLightMax: TextField
	@FXML private lateinit var textWS281xLightTimeout: TextField
	@FXML private lateinit var buttonWS281xLightGet: Button
	@FXML private lateinit var buttonWS281xLightAdd: Button
	@FXML private lateinit var buttonWS281xLightSet: Button

	@FXML private lateinit var textTxMessageType: TextField
	@FXML private lateinit var textTxCRC: TextField
	@FXML private lateinit var areaTxMessage: TextArea
	@FXML private lateinit var areaRxMessage: TextArea

	private lateinit var primaryStage: Stage
	private lateinit var configController: ConfigController
	private lateinit var deviceConfiguration: DeviceConfiguration
	private val usbCommunicator = USBCommunicator()
	private val bluetoothCommunicator = BluetoothCommunicator()
	private val rgbConfigs = mutableMapOf<Int, RGBConfig>()
	private val ws281xPatterns = mutableMapOf<Int, WS281xPattern?>()
	private val ws281xColors = mutableMapOf<Int, Color>()
	private val ws281xDelays = mutableMapOf<Int, Int>()
	private val ws281xMinMax = mutableMapOf<Int, Pair<Int, Int>>()
	private val ws281xLights = mutableMapOf<Int, WS281xLightConfig>()
	private var bluetoothEepromFile: File? = null
	private val bluetoothEepromBuffer = ByteArray(8192)
	private var bluetoothEepromChunkSize = 0

	private var usbEnabled: Boolean = true
		set(value) {
			field = value
			radioUSB.isDisable = !value
			choiceUSBPort.isDisable = !value
			buttonUSBConnect.isDisable = !value
		}

	private var bluetoothEnabled: Boolean = true
		set(value) {
			field = value
			radioBluetooth.isDisable = !value
			textBluetoothAddress.isDisable = !value
			textBluetoothChannel.isDisable = !value
			buttonBluetoothConnect.isDisable = !value
		}

	private var bluetoothSettingsEnabled: Boolean = false
		set(value) {
			field = value
			textBluetoothName.isDisable = !value
			textBluetoothPin.isDisable = !value
			comboBluetoothPairingMethod.isDisable = !value
			buttonSettingsGet.isDisable = !value
			buttonSettingsSet.isDisable = !value
		}

	private var bluetoothEepromEnabled: Boolean = false
		set(value) {
			field = value && radioUSB.isSelected
			textBluetoothEepromBlockSize.isDisable = !value || !radioUSB.isSelected
			buttonBluetoothEepromDownload.isDisable = !value || !radioUSB.isSelected
			buttonBluetoothEepromUpload.isDisable = !value || !radioUSB.isSelected
		}

	private var dht11Enabled: Boolean = false
		set(value) {
			field = value
			textDHT11Temperature.isDisable = !value
			textDHT11Humidity.isDisable = !value
			buttonDHT11Get.isDisable = !value
		}

	private var lcdEnabled: Boolean = false
		set(value) {
			field = value
			checkboxLCDBacklight.isDisable = !value
			textLCDLine.isDisable = !value
			areaLCD.isDisable = !value
			buttonLCDGet.isDisable = !value
			buttonLCDSet.isDisable = !value
			buttonLCDClear.isDisable = !value
			buttonLCDReset.isDisable = !value
		}

	private var mcp23017Enabled: Boolean = false
		set(value) {
			field = value
			textMCP23017Address.isDisable = !value
			checkboxMCP23017PortA.isDisable = !value
			checkboxMCP23017PortB.isDisable = !value
			labelMCP23017A00.isDisable = !value
			labelMCP23017A01.isDisable = !value
			labelMCP23017A02.isDisable = !value
			labelMCP23017A03.isDisable = !value
			labelMCP23017A04.isDisable = !value
			labelMCP23017A05.isDisable = !value
			labelMCP23017A06.isDisable = !value
			labelMCP23017A07.isDisable = !value
			labelMCP23017B00.isDisable = !value
			labelMCP23017B01.isDisable = !value
			labelMCP23017B02.isDisable = !value
			labelMCP23017B03.isDisable = !value
			labelMCP23017B04.isDisable = !value
			labelMCP23017B05.isDisable = !value
			labelMCP23017B06.isDisable = !value
			labelMCP23017B07.isDisable = !value
			checkboxMCP23017A00.isDisable = !value
			checkboxMCP23017A01.isDisable = !value
			checkboxMCP23017A02.isDisable = !value
			checkboxMCP23017A03.isDisable = !value
			checkboxMCP23017A04.isDisable = !value
			checkboxMCP23017A05.isDisable = !value
			checkboxMCP23017A06.isDisable = !value
			checkboxMCP23017A07.isDisable = !value
			checkboxMCP23017B00.isDisable = !value
			checkboxMCP23017B01.isDisable = !value
			checkboxMCP23017B02.isDisable = !value
			checkboxMCP23017B03.isDisable = !value
			checkboxMCP23017B04.isDisable = !value
			checkboxMCP23017B05.isDisable = !value
			checkboxMCP23017B06.isDisable = !value
			checkboxMCP23017B07.isDisable = !value
			buttonMCP23017Get.isDisable = !value
			buttonMCP23017Set.isDisable = !value
		}

	private var pirEnabled: Boolean = false
		set(value) {
			field = value
			buttonPIRGet.isDisable = !value
		}

	private var rgbEnabled: Boolean = false
		set(value) {
			field = value
			comboRGBItem.isDisable = !value
			textRGBListSize.isDisable = !value
			comboRGBPattern.isDisable = !value
			colorRGB.isDisable = !value
			textRGBDelay.isDisable = !value
			textRGBMin.isDisable = !value
			textRGBMax.isDisable = !value
			textRGBTimeout.isDisable = !value
			buttonRGBGet.isDisable = !value
			buttonRGBAdd.isDisable = !value
			buttonRGBSet.isDisable = !value
		}

	private var ws281xEnabled: Boolean = false
		set(value) {
			field = value
			textWS281xLED.isDisable = !value
			comboWS281xPattern.isDisable = !value
			colorWS281x.isDisable = !value
			textWS281xDelay.isDisable = !value
			textWS281xMin.isDisable = !value
			textWS281xMax.isDisable = !value
			buttonWS281xGet.isDisable = !value
			buttonWS281xSet.isDisable = !value
		}

	private var ws281xLightEnabled: Boolean = false
		set(value) {
			field = value
			comboWS281xLightItem.isDisable = !value
			textWS281xLightListSize.isDisable = !value
			choiceWS281xLightRainbow.isDisable = !value
			comboWS281xLightPattern.isDisable = !value
			colorWS281xLight1.isDisable = !value || choiceWS281xLightRainbow.selectionModel.selectedIndex > 0
			colorWS281xLight2.isDisable = !value || choiceWS281xLightRainbow.selectionModel.selectedIndex > 0
			colorWS281xLight3.isDisable = !value || choiceWS281xLightRainbow.selectionModel.selectedIndex > 0
			colorWS281xLight4.isDisable = !value || choiceWS281xLightRainbow.selectionModel.selectedIndex > 0
			colorWS281xLight5.isDisable = !value || choiceWS281xLightRainbow.selectionModel.selectedIndex > 0
			colorWS281xLight6.isDisable = !value || choiceWS281xLightRainbow.selectionModel.selectedIndex > 0
			colorWS281xLight7.isDisable = !value || choiceWS281xLightRainbow.selectionModel.selectedIndex > 0
			textWS281xLightDelay.isDisable = !value || comboWS281xLightPattern.selectionModel.selectedItem?.delay == null
			textWS281xLightWidth.isDisable = !value || comboWS281xLightPattern.selectionModel.selectedItem?.width == null
			textWS281xLightFading.isDisable = !value || comboWS281xLightPattern.selectionModel.selectedItem?.fading == null
			textWS281xLightMin.isDisable = !value || comboWS281xLightPattern.selectionModel.selectedItem?.min == null
			textWS281xLightMax.isDisable = !value || comboWS281xLightPattern.selectionModel.selectedItem?.max == null
			textWS281xLightTimeout.isDisable = !value || comboWS281xLightPattern.selectionModel.selectedItem?.timeout == null
			buttonWS281xLightGet.isDisable = !value
			buttonWS281xLightAdd.isDisable = !value
			buttonWS281xLightSet.isDisable = !value
		}

	private var txEnabled: Boolean = false
		set(value) {
			field = value
			textTxMessageType.isDisable = !value
			textTxCRC.isDisable = !value
			areaTxMessage.isDisable = !value
		}

	private var enabled: Boolean = false
		set(value) {
			field = value
			usbEnabled = deviceConfiguration.hasUSB
			bluetoothSettingsEnabled = deviceConfiguration.hasBluetooth
			bluetoothEepromEnabled = usbCommunicator.isConnected && value && deviceConfiguration.hasUSB && deviceConfiguration.hasBluetooth
			dht11Enabled = value && deviceConfiguration.hasDHT11
			lcdEnabled = value && deviceConfiguration.hasLCD
			mcp23017Enabled = value && deviceConfiguration.hasMCP23017
			pirEnabled = value && deviceConfiguration.hasPIR
			rgbEnabled = value && deviceConfiguration.hasRGB
			ws281xEnabled = value && deviceConfiguration.hasWS281xIndicators
			ws281xLightEnabled = value && deviceConfiguration.hasWS281xLight
			txEnabled = value
			areaRxMessage.isDisable = !value
			//buttonRxClear.isDisable = !value
		}

	private val bluetoothScannerListener = object : ScannerListener<BluetoothCommunicator.Descriptor> {
		override fun onAvailableDevicesChanged(channel: Channel, devices: Collection<BluetoothCommunicator.Descriptor>) {
			println(devices)
		}
	}

	private val usbScannerListener = object : ScannerListener<USBCommunicator.Descriptor> {
		override fun onAvailableDevicesChanged(channel: Channel, devices: Collection<USBCommunicator.Descriptor>) {
			updateSerialPorts(devices)
		}
	}

	@FXML
	fun initialize() {
		/* USB */
		choiceUSBPort.converter = object : StringConverter<Pair<String, Boolean>>() {
			override fun toString(pair: Pair<String, Boolean>?): String? = pair
					?.let { (port, available) -> "${port}${if (!available) " (disconnected)" else ""}" }

			override fun fromString(string: String?): Pair<String, Boolean>? = string
					?.let { "(.*)( \\(disconnected\\))?".toRegex() }
					?.matchEntire(string)
					?.groupValues
					?.takeIf { it.isNotEmpty() }
					?.let { it[0] to (it.size <= 1 || it[1] != " (disconnected)") }
		}
		choiceUSBPort.selectionModel.selectedItemProperty().addListener { _, _, value ->
			deviceConfiguration.usbDevice = value.first
			configController.save()
		}

		/* BLUETOOTH */
		textBluetoothAddress.focusedProperty().addListener { _, _, focus ->
			if (!focus) {
				deviceConfiguration.bluetoothAddress = textBluetoothAddress.text
				configController.save()
			}
		}

		textBluetoothChannel.focusedProperty().addListener { _, _, focus ->
			if (!focus) {
				deviceConfiguration.bluetoothChannel = textBluetoothChannel.text
				configController.save()
			}
		}

		textBluetoothEepromBlockSize.focusedProperty().addListener { _, _, focus ->
			if (!focus) {
				deviceConfiguration.bluetoothEepromBlockSize = textBluetoothEepromBlockSize.text.toIntOrNull()
				configController.save()
			}
		}

		comboBluetoothPairingMethod.items.addAll(PairingMethod.values())
		comboBluetoothPairingMethod.selectionModel.select(0)

		/* LCD */
		textLCDLine.focusedProperty().addListener { _, _, focus ->
			if (!focus) {
				deviceConfiguration.lcdLine = textLCDLine.text.toIntOrNull()
				configController.save()
			}
		}

		/* MCP23017 */
		textMCP23017Address.focusedProperty().addListener { _, _, focus ->
			if (!focus) {
				deviceConfiguration.mcp23017I2CAddress = textMCP23017Address.text.takeUnless { it.isNullOrEmpty() }
				configController.save()
			}
		}

		/* RGB */
		comboRGBPattern.items.addAll(RGBPattern.values())
		comboRGBPattern.selectionModel.selectedItemProperty().addListener { _, _, value ->
			textRGBDelay.isDisable = value.delay == null
			textRGBMin.isDisable = value.min == null
			textRGBMax.isDisable = value.max == null
			textRGBTimeout.isDisable = value.timeout == null

			textRGBDelay.text = ""
			textRGBMin.text = ""
			textRGBMax.text = ""
			textRGBTimeout.text = ""

			value.delay?.also { textRGBDelay.promptText = "${it}" }
			value.min?.also { textRGBMin.promptText = "${it}" }
			value.max?.also { textRGBMax.promptText = "${it}" }
			value.timeout?.also { textRGBTimeout.promptText = "${it}" }
		}
		comboRGBPattern.selectionModel.select(0)
		colorRGB.value = Color.BLACK
		colorRGB.customColors.clear()
		colorRGB.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		comboRGBItem.selectionModel.selectedItemProperty()
				.addListener { _, _, value -> value?.toIntOrNull()?.let { rgbConfigs[it] }?.also { setRGB(it) } }
		comboRGBItem.valueProperty()
				.addListener { _, _, value -> value?.toIntOrNull()?.let { rgbConfigs[it] }?.also { setRGB(it) } }

		/* WS281x INDICATORS */
		circleWS281x00.fill = Color.TRANSPARENT
		circleWS281x01.fill = Color.TRANSPARENT
		circleWS281x02.fill = Color.TRANSPARENT
		circleWS281x03.fill = Color.TRANSPARENT
		circleWS281x04.fill = Color.TRANSPARENT
		circleWS281x05.fill = Color.TRANSPARENT
		circleWS281x06.fill = Color.TRANSPARENT
		circleWS281x07.fill = Color.TRANSPARENT
		circleWS281x08.fill = Color.TRANSPARENT
		circleWS281x09.fill = Color.TRANSPARENT
		circleWS281x10.fill = Color.TRANSPARENT
		circleWS281x11.fill = Color.TRANSPARENT
		circleWS281x12.fill = Color.TRANSPARENT
		circleWS281x13.fill = Color.TRANSPARENT
		circleWS281x14.fill = Color.TRANSPARENT
		circleWS281x15.fill = Color.TRANSPARENT
		circleWS281x16.fill = Color.TRANSPARENT
		circleWS281x17.fill = Color.TRANSPARENT
		circleWS281x18.fill = Color.TRANSPARENT
		circleWS281x19.fill = Color.TRANSPARENT
		circleWS281x20.fill = Color.TRANSPARENT
		circleWS281x21.fill = Color.TRANSPARENT
		circleWS281x22.fill = Color.TRANSPARENT
		circleWS281x23.fill = Color.TRANSPARENT
		circleWS281x24.fill = Color.TRANSPARENT
		circleWS281x25.fill = Color.TRANSPARENT
		circleWS281x26.fill = Color.TRANSPARENT
		circleWS281x27.fill = Color.TRANSPARENT
		circleWS281x28.fill = Color.TRANSPARENT
		circleWS281x29.fill = Color.TRANSPARENT
		circleWS281x30.fill = Color.TRANSPARENT
		circleWS281x31.fill = Color.TRANSPARENT
		circleWS281x32.fill = Color.TRANSPARENT
		circleWS281x33.fill = Color.TRANSPARENT
		circleWS281x34.fill = Color.TRANSPARENT
		circleWS281x35.fill = Color.TRANSPARENT
		circleWS281x36.fill = Color.TRANSPARENT
		circleWS281x37.fill = Color.TRANSPARENT
		circleWS281x38.fill = Color.TRANSPARENT
		circleWS281x39.fill = Color.TRANSPARENT
		circleWS281x40.fill = Color.TRANSPARENT
		circleWS281x41.fill = Color.TRANSPARENT
		circleWS281x42.fill = Color.TRANSPARENT
		circleWS281x43.fill = Color.TRANSPARENT
		circleWS281x44.fill = Color.TRANSPARENT
		circleWS281x45.fill = Color.TRANSPARENT
		circleWS281x46.fill = Color.TRANSPARENT
		circleWS281x47.fill = Color.TRANSPARENT
		circleWS281x48.fill = Color.TRANSPARENT
		circleWS281x49.fill = Color.TRANSPARENT
		comboWS281xPattern.items.addAll(WS281xPattern.values())
		comboWS281xPattern.selectionModel.select(0)
		colorWS281x.value = Color.BLACK
		colorWS281x.customColors.clear()
		colorWS281x.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)

		/* WS281x LIGHT */
		choiceWS281xLightRainbow.items.addAll("Colors", "Ranbow Row", "Rainbow Row Circle", "Rainbow", "Rainbow Circle")
		choiceWS281xLightRainbow.selectionModel.selectedIndexProperty().addListener { _, _, value ->
			colorWS281xLight1.isDisable = value.toInt() > 0
			colorWS281xLight2.isDisable = value.toInt() > 0
			colorWS281xLight3.isDisable = value.toInt() > 0
			colorWS281xLight4.isDisable = value.toInt() > 0
			colorWS281xLight5.isDisable = value.toInt() > 0
			colorWS281xLight6.isDisable = value.toInt() > 0
			colorWS281xLight7.isDisable = value.toInt() > 0
		}
		choiceWS281xLightRainbow.selectionModel.select(0)
		comboWS281xLightPattern.items.addAll(WS281xLightPattern.values())
		comboWS281xLightPattern.selectionModel.selectedItemProperty().addListener { _, _, value ->
			textWS281xLightDelay.isDisable = value.delay == null
			textWS281xLightWidth.isDisable = value.width == null
			textWS281xLightFading.isDisable = value.fading == null
			textWS281xLightMin.isDisable = value.min == null
			textWS281xLightMax.isDisable = value.max == null
			textWS281xLightTimeout.isDisable = value.timeout == null

			textWS281xLightDelay.text = ""
			textWS281xLightWidth.text = ""
			textWS281xLightFading.text = ""
			textWS281xLightMin.text = ""
			textWS281xLightMax.text = ""
			textWS281xLightTimeout.text = ""

			value.delay?.also { textWS281xLightDelay.promptText = "${it}" }
			value.width?.also { textWS281xLightWidth.promptText = "${it}" }
			value.fading?.also { textWS281xLightFading.promptText = "${it}" }
			value.min?.also { textWS281xLightMin.promptText = "${it}" }
			value.max?.also { textWS281xLightMax.promptText = "${it}" }
			value.timeout?.also { textWS281xLightTimeout.promptText = "${it}" }
		}

		comboWS281xLightPattern.selectionModel.select(WS281xLightPattern.WS281x_LIGHT_OFF)
		colorWS281xLight1.value = Color.BLACK
		colorWS281xLight1.customColors.clear()
		colorWS281xLight1.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		colorWS281xLight2.value = Color.BLACK
		colorWS281xLight2.customColors.clear()
		colorWS281xLight2.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		colorWS281xLight3.value = Color.BLACK
		colorWS281xLight3.customColors.clear()
		colorWS281xLight3.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		colorWS281xLight4.value = Color.BLACK
		colorWS281xLight4.customColors.clear()
		colorWS281xLight4.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		colorWS281xLight5.value = Color.BLACK
		colorWS281xLight5.customColors.clear()
		colorWS281xLight5.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		colorWS281xLight6.value = Color.BLACK
		colorWS281xLight6.customColors.clear()
		colorWS281xLight6.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		colorWS281xLight7.value = Color.BLACK
		colorWS281xLight7.customColors.clear()
		colorWS281xLight7.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
		comboWS281xLightItem.selectionModel.selectedItemProperty()
				.addListener { _, _, value -> value?.toIntOrNull()?.let { ws281xLights[it] }?.also { setWS281xLight(it) } }
		comboWS281xLightItem.valueProperty()
				.addListener { _, _, value -> value?.toIntOrNull()?.let { ws281xLights[it] }?.also { setWS281xLight(it) } }


		BluetoothScanner.register(bluetoothScannerListener)
		USBScanner.register(usbScannerListener)

		bluetoothCommunicator.register(this)
		usbCommunicator.register(this)
	}

	private fun startup() {
		when (deviceConfiguration.selectedChannel) {
			Channel.USB -> radioUSB.isSelected = true
			Channel.BLUETOOTH -> radioBluetooth.isSelected = true
		}
		radioBluetooth.selectedProperty().addListener { _, _, value ->
			if (value) deviceConfiguration.selectedChannel = Channel.BLUETOOTH
			configController.save()
		}
		radioUSB.selectedProperty().addListener { _, _, value ->
			bluetoothEepromEnabled = usbCommunicator.isConnected && value
			if (value) deviceConfiguration.selectedChannel = Channel.USB
			configController.save()
		}

		updateSerialPorts(SerialPortList.getPortNames().map { USBCommunicator.Descriptor(it) })

		textBluetoothAddress.text = deviceConfiguration.bluetoothAddress ?: ""
		textBluetoothChannel.text = deviceConfiguration.bluetoothChannel ?: ""
		textBluetoothEepromBlockSize.text = deviceConfiguration.bluetoothEepromBlockSize?.toString() ?: ""
		textLCDLine.text = deviceConfiguration.lcdLine?.toString() ?: ""
		textMCP23017Address.text = deviceConfiguration.mcp23017I2CAddress ?: ""

		enabled = usbCommunicator.isConnected || bluetoothCommunicator.isConnected
	}

	fun shutdown() {
		usbCommunicator.shutdown()
		bluetoothCommunicator.shutdown()
	}

	@FXML
	fun onUSBConnectionToggle() {
		if (usbCommunicator.isDisconnected) {
			choiceUSBPort.value
					?.takeIf { it.second }
					?.let { USBCommunicator.Descriptor(it.first) }
					?.let { usbCommunicator.connect(it) }
					?.takeIf { it }
					?.also { buttonUSBConnect.text = "Cancel" }
		} else {
			usbCommunicator.disconnect()
			buttonUSBConnect.text = "Connect"
		}
	}

	@FXML
	fun onBluetoothConnectionToggle() {
		if (bluetoothCommunicator.isDisconnected) {
			val descriptor = BluetoothCommunicator.Descriptor(
					textBluetoothAddress.text,
					textBluetoothChannel.text.toIntOrNull() ?: 6)

			if (bluetoothCommunicator.connect(descriptor)) {
				buttonBluetoothConnect.text = "Cancel"
			}
		} else {
			bluetoothCommunicator.disconnect()
			buttonBluetoothConnect.text = "Connect"
		}
	}

	@FXML
	fun onBluetoothSettingsGet() {
		//bluetoothSettingsEnabled = false
		MessageKind.BT_SETTINGS.sendGetRequest()
	}

	@FXML
	fun onBluetoothSettingsSet() {
		deviceConfiguration.bluetoothName = textBluetoothName.text
		configController.save()
		MessageKind.BT_SETTINGS.sendConfiguration(
				*(listOf(comboBluetoothPairingMethod.selectionModel.selectedIndex)
						+ textBluetoothPin.text.padEnd(6, 0.toChar()).substring(0, 6).map { it.toInt() }
						+ textBluetoothName.text.padEnd(16, 0.toChar()).substring(0, 16).map { it.toInt() }).toTypedArray())
	}

	@FXML
	fun onBluetoothEepromDownload() {
		bluetoothEepromChunkSize = textBluetoothEepromBlockSize.text.toIntOrNull() ?: 255
		if (bluetoothEepromChunkSize > 0) {
			bluetoothEepromFile = FileChooser()
					.apply { title = "Choose file to download the EEPROM to ..." }
					.showSaveDialog(primaryStage)
			if (bluetoothEepromFile != null) {
				bluetoothEepromEnabled = false
				progressUSB.progress = -1.0
				(0 until bluetoothEepromBuffer.size).forEach { bluetoothEepromBuffer[it] = 0xFF.toByte() }
				MessageKind.BT_EEPROM.sendGetRequest()
			}
		}
	}

	@FXML
	fun onBluetoothEepromUpload() {
		val fileChooser = FileChooser()
		fileChooser.title = "Choose file to upload to the EEPROM ..."
		bluetoothEepromFile = fileChooser.showOpenDialog(primaryStage)
		if (bluetoothEepromFile != null) {
			bluetoothSettingsEnabled = false
		}
	}

	@FXML
	fun onDHT11Get() {
		//dht11Enabled = false
		MessageKind.DHT11.sendGetRequest()
	}

	@FXML
	fun onLCDGet() {
		//lcdEnabled = false
		MessageKind.LCD.sendGetRequest(textLCDLine.text.toIntOrNull())
	}

	@FXML
	fun onLCDSet() {
		areaLCD.text.split("\n")
				.asSequence()
				.filterIndexed { line, _ -> line < 4 }
				.map { it.substring(0, min(it.length, 20)) }
				.map { string -> string.toCharArray().map { it.toInt() }.toTypedArray() }
				.mapIndexed { line, data -> line to data }
				.filter { (line, _) -> line == (textLCDLine.text.toIntOrNull() ?: line) }
				.toList()
				.forEach { (line, data) -> MessageKind.LCD.sendConfiguration(line, data.size, *data) }
	}

	@FXML
	fun onLCDBacklight() {
		MessageKind.LCD.sendConfiguration(if (checkboxLCDBacklight.isSelected) 0x7E else 0x7D)
	}

	@FXML
	fun onLCDClear() {
		MessageKind.LCD.sendConfiguration(0x7B)
	}

	@FXML
	fun onLCDReset() {
		MessageKind.LCD.sendConfiguration(0x7C)
	}

	@FXML
	fun onMCP23017Get() {
		//mcp23017Enabled = false
		"0x(\\d+)".toRegex()
				.matchEntire(textMCP23017Address.text)
				?.takeIf { it.groupValues.size == 2 }
				?.groupValues
				?.get(1)
				?.toInt(16)
				.also { MessageKind.MCP23017.sendGetRequest(it ?: 0x20) }
	}

	@FXML
	fun onMCP23017Set() {
		val ports = (if (checkboxMCP23017PortA.isSelected) 1 else 0) + (if (checkboxMCP23017PortB.isSelected) 2 else 0)
		if (ports > 0) {
			val params = mutableListOf("0x(\\d+)".toRegex()
					.matchEntire(textMCP23017Address.text)
					?.takeIf { it.groupValues.size == 2 }
					?.groupValues
					?.get(1)
					?.toInt(16) ?: 0x20,
					ports)
			if (checkboxMCP23017PortA.isSelected) params.add(bools2Byte(
					checkboxMCP23017A07.isSelected,
					checkboxMCP23017A06.isSelected,
					checkboxMCP23017A05.isSelected,
					checkboxMCP23017A04.isSelected,
					checkboxMCP23017A03.isSelected,
					checkboxMCP23017A02.isSelected,
					checkboxMCP23017A01.isSelected,
					checkboxMCP23017A00.isSelected))
			if (checkboxMCP23017PortB.isSelected) params.add(bools2Byte(
					checkboxMCP23017B07.isSelected,
					checkboxMCP23017B06.isSelected,
					checkboxMCP23017B05.isSelected,
					checkboxMCP23017B04.isSelected,
					checkboxMCP23017B03.isSelected,
					checkboxMCP23017B02.isSelected,
					checkboxMCP23017B01.isSelected,
					checkboxMCP23017B00.isSelected))

			MessageKind.MCP23017.sendConfiguration(*params.toTypedArray())
		}
	}

	@FXML
	fun onPIRGet() {
		//pirEnabled = false
		MessageKind.PIR.sendGetRequest()
	}

	@FXML
	fun onRGBGet() {
		//rgbEnabled = false
		val item = comboRGBItem.value?.toIntOrNull()
		MessageKind.RGB.sendGetRequest(item)
	}

	@FXML
	fun onRGBAdd() {
		val pattern = comboRGBPattern.selectionModel.selectedItem
		MessageKind.RGB.sendConfiguration(
				pattern.code,
				colorRGB.value.red * 255.0,
				colorRGB.value.green * 255.0,
				colorRGB.value.blue * 255.0,
				(textRGBDelay.text.toIntOrNull() ?: pattern.delay ?: 100) / 256.0,
				(textRGBDelay.text.toIntOrNull() ?: pattern.delay ?: 100) % 256.0,
				textRGBMin.text.toIntOrNull() ?: pattern.min ?: 0,
				textRGBMax.text.toIntOrNull() ?: pattern.max ?: 255,
				textRGBTimeout.text.toIntOrNull() ?: pattern.timeout ?: INDEFINED)
	}

	@FXML
	fun onRGBSet() {
		val pattern = comboRGBPattern.selectionModel.selectedItem
		MessageKind.RGB.sendConfiguration(
				pattern.code + 0x80,
				colorRGB.value.red * 255.0,
				colorRGB.value.green * 255.0,
				colorRGB.value.blue * 255.0,
				(textRGBDelay.text.toIntOrNull() ?: pattern.delay ?: 100) / 256.0,
				(textRGBDelay.text.toIntOrNull() ?: pattern.delay ?: 100) % 256.0,
				textRGBMin.text.toIntOrNull() ?: pattern.min ?: 0,
				textRGBMax.text.toIntOrNull() ?: pattern.max ?: 255,
				textRGBTimeout.text.toIntOrNull() ?: pattern.timeout ?: INDEFINED)
	}

	@FXML
	fun onWS281xGet() {
		//ws281xEnabled = false
		val led = textWS281xLED.text.toIntOrNull()
		MessageKind.WS281x.sendGetRequest(led)
	}

	@FXML
	fun onWS281xSet() {
		val led = textWS281xLED.text.toIntOrNull()
		if (led == null) {
			MessageKind.WS281x.sendConfiguration(
					colorWS281x.value.red * 255.0,
					colorWS281x.value.green * 255.0,
					colorWS281x.value.blue * 255.0)
			(0 until 50).forEach { i ->
				this::class.java.declaredFields
						.find { it.name == "circleWS281x%02d".format(i) }
						?.get(this)
						?.let { it as? Circle }
						?.fill = colorWS281x.value
			}
		} else {
			MessageKind.WS281x.sendConfiguration(
					led,
					comboWS281xPattern.selectionModel.selectedItem.code,
					colorWS281x.value.red * 255.0,
					colorWS281x.value.green * 255.0,
					colorWS281x.value.blue * 255.0,
					(textWS281xDelay.text.toIntOrNull() ?: 50) / 256.0,
					(textWS281xDelay.text.toIntOrNull() ?: 50) % 256.0,
					textWS281xMin.text.toIntOrNull() ?: 0,
					textWS281xMax.text.toIntOrNull() ?: 255)
			this::class.java.declaredFields
					.find { it.name == "circleWS281x%02d".format(led) }
					?.get(this)
					?.let { it as? Circle }
					?.fill = colorWS281x.value
		}
	}

	@FXML
	fun onWS281xLED(event: MouseEvent) = event.source.let { it as? Circle }?.id
			?.replace("circleWS281x", "")
			?.toIntOrNull()
			?.also { led ->
				println(led)
				textWS281xLED.text = "${led}"
				ws281xPatterns[led]?.also { comboWS281xPattern.selectionModel.select(it) }
				colorWS281x.value = ws281xColors[led] ?: Color.BLACK
				textWS281xDelay.text = "${ws281xDelays[led] ?: ""}"
				textWS281xMin.text = "${ws281xMinMax[led]?.first ?: ""}"
				textWS281xMax.text = "${ws281xMinMax[led]?.second ?: ""}"
			}

	@FXML
	fun onWS281xLightGet() {
		//ws281xLightEnabled = false
		val item = comboWS281xLightItem.value?.toIntOrNull()
		MessageKind.WS281xLIGHT.sendGetRequest(item)
	}

	@FXML
	fun onWS281xLightAdd() {
		val pattern = comboWS281xLightPattern.selectionModel.selectedItem
		val rainbow = choiceWS281xLightRainbow.selectionModel.selectedIndex
		MessageKind.WS281xLIGHT.sendConfiguration(
				pattern.code,
				if (rainbow == 0) colorWS281xLight1.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight1.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight1.value.blue * 255.0 else rainbow,
				if (rainbow == 0) colorWS281xLight2.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight2.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight2.value.blue * 255.0 else rainbow,
				if (rainbow == 0) colorWS281xLight3.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight3.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight3.value.blue * 255.0 else rainbow,
				if (rainbow == 0) colorWS281xLight4.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight4.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight4.value.blue * 255.0 else rainbow,
				if (rainbow == 0) colorWS281xLight5.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight5.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight5.value.blue * 255.0 else rainbow,
				if (rainbow == 0) colorWS281xLight6.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight6.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight6.value.blue * 255.0 else rainbow,
				if (rainbow == 0) colorWS281xLight7.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight7.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight7.value.blue * 255.0 else rainbow,
				(textWS281xLightDelay.text.toIntOrNull() ?: pattern.delay ?: 10) / 256.0,
				(textWS281xLightDelay.text.toIntOrNull() ?: pattern.delay ?: 10) % 256.0,
				textWS281xLightWidth.text.toIntOrNull() ?: pattern.width ?: 3,
				textWS281xLightFading.text.toIntOrNull() ?: pattern.fading ?: 0,
				textWS281xLightMin.text.toIntOrNull() ?: pattern.min ?: 0,
				textWS281xLightMax.text.toIntOrNull() ?: pattern.max ?: 255,
				textWS281xLightTimeout.text.toIntOrNull() ?: pattern.timeout ?: INDEFINED)
	}

	@FXML
	fun onWS281xLightSet() {
		val pattern = comboWS281xLightPattern.selectionModel.selectedItem
		val rainbow = choiceWS281xLightRainbow.selectionModel.selectedIndex
		MessageKind.WS281xLIGHT.sendConfiguration(
				pattern.code + 0x80,
				if (rainbow == 0) colorWS281xLight1.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight1.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight1.value.blue * 255.0 else rainbow,
				if (rainbow == 0) colorWS281xLight2.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight2.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight2.value.blue * 255.0 else rainbow,
				if (rainbow == 0) colorWS281xLight3.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight3.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight3.value.blue * 255.0 else rainbow,
				if (rainbow == 0) colorWS281xLight4.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight4.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight4.value.blue * 255.0 else rainbow,
				if (rainbow == 0) colorWS281xLight5.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight5.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight5.value.blue * 255.0 else rainbow,
				if (rainbow == 0) colorWS281xLight6.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight6.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight6.value.blue * 255.0 else rainbow,
				if (rainbow == 0) colorWS281xLight7.value.red * 255.0 else 0x01,
				if (rainbow == 0) colorWS281xLight7.value.green * 255.0 else 0x02,
				if (rainbow == 0) colorWS281xLight7.value.blue * 255.0 else rainbow,
				(textWS281xLightDelay.text.toIntOrNull() ?: pattern.delay ?: 10) / 256.0,
				(textWS281xLightDelay.text.toIntOrNull() ?: pattern.delay ?: 10) % 256.0,
				textWS281xLightWidth.text.toIntOrNull() ?: pattern.width ?: 3,
				textWS281xLightFading.text.toIntOrNull() ?: pattern.fading ?: 0,
				textWS281xLightMin.text.toIntOrNull() ?: pattern.min ?: 0,
				textWS281xLightMax.text.toIntOrNull() ?: pattern.max ?: 255,
				textWS281xLightTimeout.text.toIntOrNull() ?: pattern.timeout ?: INDEFINED)
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

//	@FXML
//	fun onTxSend() {
//		"0x([0-9A-Fa-f]{1,4})".toRegex()
//				.matchEntire(textTxMessageType.text)
//				?.groups
//				?.get(0)
//				?.value
//				?.toIntOrNull()
//				?.let { code -> MessageKind.values().find { it.code == code } }
//				?.also { messageType ->
//					bluetoothCommunicator.send(messageType, areaTxMessage.text
//							.replace("[ \t\n\r]".toRegex(), "")
//							.replace("0x([0-9A-Fa-f]{1,4})".toRegex()) {
//								"${it.groups[1]?.value?.toIntOrNull(16)?.toChar() ?: ""}"
//							}
//							.toByteArray())
//				}
//	}

	override fun onConnecting(channel: Channel) {
		when (channel) {
			Channel.USB -> {
				labelUSBStatus.text = "Connecting ..."
				progressUSB.progress = -1.0
			}
			Channel.BLUETOOTH -> {
				labelBluetoothStatus.text = "Connecting ..."
				progressBluetooth.progress = -1.0
			}
		}
	}

	override fun onConnect(channel: Channel) {
		when (channel) {
			Channel.USB -> {
				labelUSBStatus.text = "Connected"
				buttonUSBConnect.text = "Disconnect"
				progressUSB.progress = 0.0
			}
			Channel.BLUETOOTH -> {
				labelBluetoothStatus.text = "Connected"
				buttonBluetoothConnect.text = "Disconnect"
				progressBluetooth.progress = 0.0
			}
		}
		enabled = usbCommunicator.isConnected || bluetoothCommunicator.isConnected
	}

	override fun onDisconnect(channel: Channel) {
		when (channel) {
			Channel.USB -> {
				labelUSBStatus.text = "Not Connected"
				buttonUSBConnect.text = "Connect"
				progressUSB.progress = 0.0
			}
			Channel.BLUETOOTH -> {
				labelBluetoothStatus.text = "Not Connected"
				buttonBluetoothConnect.text = "Connect"
				progressBluetooth.progress = 0.0
			}
		}
		//labelPIR.text = "No update yet."
		enabled = usbCommunicator.isConnected || bluetoothCommunicator.isConnected
	}

	override fun onMessageReceived(channel: Channel, message: IntArray) {
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

		areaRxMessage.text += "\n%s> %s [CRC: 0x%02X]: %s (0x%02X) with %d bytes: \n%s"
				.format(channel,
						timestamp,
						message[0],
						MessageKind.values().find { it.code == message[1] } ?: "",
						message[1],
						message.size,
						sb.toString())
		//areaRxMessage.scrollTop = Double.MAX_VALUE
		areaRxMessage.selectPositionCaret(areaRxMessage.length)
		areaRxMessage.deselect()

		when (message[1]) {
			MessageKind.IDD.code -> {
				if (message.size > 3) when (message[3]) {
					0x00 -> { // Capabilities
						if (message.size > 4) byte2Bools(message[4]).also {
							deviceConfiguration.hasBluetooth = it[0]
							deviceConfiguration.hasUSB = it[1]
							deviceConfiguration.hasDHT11 = it[2]
							deviceConfiguration.hasLCD = it[3]
							deviceConfiguration.hasMCP23017 = it[4]
							deviceConfiguration.hasPIR = it[5]
						}
						if (message.size > 5) byte2Bools(message[5]).also {
							deviceConfiguration.hasRGB = it[0]
							deviceConfiguration.hasWS281xIndicators = it[1]
							deviceConfiguration.hasWS281xLight = it[2]
						}
						configController.save()
						enabled = enabled // refresh capabilities
					}
					0x01 -> { // Name
						if (message.size > 4) {
							deviceConfiguration.bluetoothName = message
									.toList()
									.subList(4, message.size)
									.map { it.toChar() }
									.toCharArray()
									.let { String(it) }
							configController.save()
						}
					}
				}
			}
			MessageKind.DHT11.code -> {
				if (message.size == 4) {
					textDHT11Temperature.text = "${message[2]}"
					textDHT11Humidity.text = "${message[3]}"
					dht11Enabled = true
				}
			}
			MessageKind.LCD.code -> {
				if (message.size > 4) {
					val backlight = message[2] > 0
					val line = message[3]
					val columns = message[4]
					val string = (0 until columns)
							.map { message[it + 5] }
							.map { it.toChar() }
							.toCharArray()
							.let { String(it) }
					val content = areaLCD.text
							.split("\n".toRegex())
							.mapIndexed { l, c -> l to c }
							.toMap()
							.toMutableMap()
					content[line] = string
					checkboxLCDBacklight.isIndeterminate = false
					checkboxLCDBacklight.isSelected = backlight
					areaLCD.text = (0 until 4).joinToString("\n") { content[it] ?: "" }
					lcdEnabled = true
				}
			}
			MessageKind.MCP23017.code -> {
				if (message.size == 5) {
					checkboxMCP23017A00.isIndeterminate = false
					checkboxMCP23017A01.isIndeterminate = false
					checkboxMCP23017A02.isIndeterminate = false
					checkboxMCP23017A03.isIndeterminate = false
					checkboxMCP23017A04.isIndeterminate = false
					checkboxMCP23017A05.isIndeterminate = false
					checkboxMCP23017A06.isIndeterminate = false
					checkboxMCP23017A07.isIndeterminate = false
					checkboxMCP23017B00.isIndeterminate = false
					checkboxMCP23017B01.isIndeterminate = false
					checkboxMCP23017B02.isIndeterminate = false
					checkboxMCP23017B03.isIndeterminate = false
					checkboxMCP23017B04.isIndeterminate = false
					checkboxMCP23017B05.isIndeterminate = false
					checkboxMCP23017B06.isIndeterminate = false
					checkboxMCP23017B07.isIndeterminate = false

					byte2Bools(message[3]).also {
						checkboxMCP23017A00.isSelected = it[0]
						checkboxMCP23017A01.isSelected = it[1]
						checkboxMCP23017A02.isSelected = it[2]
						checkboxMCP23017A03.isSelected = it[3]
						checkboxMCP23017A04.isSelected = it[4]
						checkboxMCP23017A05.isSelected = it[5]
						checkboxMCP23017A06.isSelected = it[6]
						checkboxMCP23017A07.isSelected = it[7]
						labelMCP23017A00.text = if (it[0]) "1" else "0"
						labelMCP23017A01.text = if (it[1]) "1" else "0"
						labelMCP23017A02.text = if (it[2]) "1" else "0"
						labelMCP23017A03.text = if (it[3]) "1" else "0"
						labelMCP23017A04.text = if (it[4]) "1" else "0"
						labelMCP23017A05.text = if (it[5]) "1" else "0"
						labelMCP23017A06.text = if (it[6]) "1" else "0"
						labelMCP23017A07.text = if (it[7]) "1" else "0"
					}
					byte2Bools(message[4]).also {
						checkboxMCP23017B00.isSelected = it[0]
						checkboxMCP23017B01.isSelected = it[1]
						checkboxMCP23017B02.isSelected = it[2]
						checkboxMCP23017B03.isSelected = it[3]
						checkboxMCP23017B04.isSelected = it[4]
						checkboxMCP23017B05.isSelected = it[5]
						checkboxMCP23017B06.isSelected = it[6]
						checkboxMCP23017B07.isSelected = it[7]
						labelMCP23017B00.text = if (it[0]) "1" else "0"
						labelMCP23017B01.text = if (it[1]) "1" else "0"
						labelMCP23017B02.text = if (it[2]) "1" else "0"
						labelMCP23017B03.text = if (it[3]) "1" else "0"
						labelMCP23017B04.text = if (it[4]) "1" else "0"
						labelMCP23017B05.text = if (it[5]) "1" else "0"
						labelMCP23017B06.text = if (it[6]) "1" else "0"
						labelMCP23017B07.text = if (it[7]) "1" else "0"
					}

					mcp23017Enabled = true
				}
			}
			MessageKind.PIR.code -> {
				if (message.size == 3) {
					labelPIR.text = if (message[2] > 0) "Motion detected!" else "Nothing is moving."
				}
			}
			MessageKind.RGB.code -> {
				if (message.size == 13) {
					textRGBListSize.text = "${message[2]}"

					val index = message[3]
					val pattern = RGBPattern.values().find { it.code == message[4] }
					if (pattern != null) {
						rgbConfigs[index] = RGBConfig(
								pattern,
								message[5], message[6], message[7],
								(message[8] * 256) + message[9],
								message[10],
								message[11],
								message[12])
						comboRGBItem.items.clear()
						comboRGBItem.items.addAll(rgbConfigs.keys.sorted().map { "${it}" })
						comboRGBItem.selectionModel.select("${index}")
					}
					rgbEnabled = true
				}
			}
			MessageKind.WS281x.code -> {
				if (message.size == 12) {
					//val count = message[2]
					val led = message[3]
					ws281xPatterns[led] = WS281xPattern.values().find { it.code == message[4] }
					ws281xColors[led] = Color.rgb(message[5], message[6], message[7])
					ws281xDelays[led] = (message[8] * 256) + message[9]
					ws281xMinMax[led] = message[10] to message[11]

					textWS281xLED.text = "${led}"
					ws281xPatterns[led]?.also { comboWS281xPattern.selectionModel.select(it) }
					colorWS281x.value = ws281xColors[led] ?: Color.BLACK
					textWS281xDelay.text = "${ws281xDelays[led] ?: ""}"
					textWS281xMin.text = "${ws281xMinMax[led]?.first ?: ""}"
					textWS281xMax.text = "${ws281xMinMax[led]?.second ?: ""}"

					this::class.java.declaredFields
							.find { it.name == "circleWS281x%02d".format(led) }
							?.get(this)
							?.let { it as? Circle }
							?.fill = ws281xColors[led]

					ws281xEnabled = true
				}
			}
			MessageKind.WS281xLIGHT.code -> {
				if (message.size == 33) {
					textWS281xLightListSize.text = "${message[2]}"

					val index = message[3]
					val pattern = WS281xLightPattern.values().find { it.code == message[4] }
					if (pattern != null) {
						ws281xLights[index] = WS281xLightConfig(
								pattern,
								message[5], message[6], message[7],
								message[8], message[9], message[10],
								message[11], message[12], message[13],
								message[14], message[15], message[16],
								message[17], message[18], message[19],
								message[20], message[21], message[22],
								message[23], message[24], message[25],
								(message[26] * 256) + message[27],
								message[28],
								message[29],
								message[30],
								message[31],
								message[32])
						comboWS281xLightItem.items.clear()
						comboWS281xLightItem.items.addAll(ws281xLights.keys.sorted().map { "${it}" })
						comboWS281xLightItem.selectionModel.select("${index}")
					}
					ws281xLightEnabled = true
				}
			}
			MessageKind.BT_SETTINGS.code -> {
				if (message.size == 25) {
					PairingMethod.values()
							.find { it.code == message[2] }
							?.also { comboBluetoothPairingMethod.selectionModel.select(it) }
					textBluetoothPin.text = message
							.toList()
							.subList(3, 9)
							.map { it.toChar() }
							.toCharArray()
							.let { String(it) }
					textBluetoothName.text = message
							.toList()
							.subList(9, 25)
							.map { it.toChar() }
							.toCharArray()
							.let { String(it) }

					deviceConfiguration.bluetoothName = textBluetoothName.text
					configController.save()

					bluetoothSettingsEnabled = true
				}
			}
			MessageKind.BT_EEPROM.code -> {
				if (message.size == 3 && message[2] == 0xFF) { // Download finished
					bluetoothEepromFile?.outputStream()?.bufferedWriter()?.use { writer ->
						bluetoothEepromBuffer
								.toList()
								.chunked(bluetoothEepromChunkSize)
								.mapIndexed { index, chunk -> index * bluetoothEepromChunkSize to chunk }
								.toMap()
								.forEach { (address, data) ->
									val dataAligned = data.map { it }.toMutableList()
									while (dataAligned.size < bluetoothEepromChunkSize) dataAligned.add(0xFF.toByte())
									val line = "    0x%04X: %s | %s"
											.format(address,
													dataAligned.joinToString("") { "%02X".format(it) },
													dataAligned.joinToString("") {
														when (it) {
															in 32..126 -> "${it.toChar()}"
															0xFF.toByte() -> "*"
															else -> "."
														}
													})
									println("EEPROM> ${line}")
									writer.write("${line}\n")
								}
					}

					progressUSB.progress = 0.0
					bluetoothEepromEnabled = true
					bluetoothEepromChunkSize = 0
				} else if (message.size > 6 && (message[4] * 256) + message[5] == message.size - 6) {
					bluetoothEepromEnabled = false
					if (progressUSB.progress == 0.0) progressUSB.progress = -1.0
					val address = (message[2] * 256) + message[3]
					val length = (message[4] * 256) + message[5]
					(0 until length).forEach { i ->
						bluetoothEepromBuffer[address + i] = message[i + 5].toByte()
					}
				}
			}
		}
		enabled = usbCommunicator.isConnected || bluetoothCommunicator.isConnected
	}

	override fun onMessageSent(channel: Channel, message: IntArray, remaining: Int) {
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

		areaTxMessage.text += "\n%s> %s [CRC: 0x%02X]: %s (0x%02X) with %d bytes: \n%s"
				.format(channel,
						timestamp,
						message[0],
						MessageKind.values().find { it.code == message[1] } ?: "",
						message[1],
						message.size,
						sb.toString())
		//areaTxMessage.scrollTop = Double.MAX_VALUE
		areaTxMessage.selectPositionCaret(areaTxMessage.length)
		areaTxMessage.deselect()

		if (remaining == 0) {
			when (channel) {
				Channel.USB -> {
					labelUSBStatus.text = "Connected"
					progressUSB.progress = 0.0
				}
				Channel.BLUETOOTH -> {
					labelBluetoothStatus.text = "Connected"
					progressBluetooth.progress = 0.0
				}
			}
			txEnabled = true
		} else {
			when (channel) {
				Channel.USB -> {
					labelUSBStatus.text = "Sending ..."
					progressUSB.progress = -1.0
				}
				Channel.BLUETOOTH -> {
					labelBluetoothStatus.text = "Sending ..."
					progressBluetooth.progress = -1.0
				}
			}
		}
	}

	private fun setRGB(rgb: RGBConfig) {
		comboRGBPattern.selectionModel.select(rgb.pattern)
		colorRGB.value = Color.rgb(rgb.red, rgb.green, rgb.blue)
		textRGBDelay.text = "${rgb.delay}"
		textRGBMin.text = "${rgb.min}"
		textRGBMax.text = "${rgb.max}"
		textRGBTimeout.text = "${rgb.timeout}"
	}

	private fun setWS281xLight(lightConfig: WS281xLightConfig) {
		comboWS281xLightPattern.selectionModel.select(lightConfig.pattern)
		colorWS281xLight1.value = Color.rgb(lightConfig.color1Red, lightConfig.color1Green, lightConfig.color1Blue)
		colorWS281xLight2.value = Color.rgb(lightConfig.color2Red, lightConfig.color2Green, lightConfig.color2Blue)
		colorWS281xLight3.value = Color.rgb(lightConfig.color3Red, lightConfig.color3Green, lightConfig.color3Blue)
		colorWS281xLight4.value = Color.rgb(lightConfig.color4Red, lightConfig.color4Green, lightConfig.color4Blue)
		colorWS281xLight5.value = Color.rgb(lightConfig.color5Red, lightConfig.color5Green, lightConfig.color5Blue)
		colorWS281xLight6.value = Color.rgb(lightConfig.color6Red, lightConfig.color6Green, lightConfig.color6Blue)
		colorWS281xLight7.value = Color.rgb(lightConfig.color7Red, lightConfig.color7Green, lightConfig.color7Blue)
		textWS281xLightDelay.text = "${lightConfig.delay}"
		textWS281xLightWidth.text = "${lightConfig.width}"
		textWS281xLightFading.text = "${lightConfig.fading}"
		textWS281xLightMin.text = "${lightConfig.min}"
		textWS281xLightMax.text = "${lightConfig.max}"
		textWS281xLightTimeout.text = "${lightConfig.timeout}"
	}

	private fun updateSerialPorts(devices: Collection<USBCommunicator.Descriptor>) {
		val portNames = devices
				.map { it.portName }
				.map { it to true }
				.toMutableList()

		if (choiceUSBPort.items.filtered { it.second }.size != portNames.size
				|| !choiceUSBPort.items.filter { it.second }.all { port -> portNames.any { port.first == it.first } }) {

			deviceConfiguration.usbDevice
					?.takeIf { device -> portNames.none { it.first == device } }
					?.also { portNames.add(it to false) }

			choiceUSBPort.items.clear()
			choiceUSBPort.items.addAll(portNames.sortedBy { it.first })

			val selected = choiceUSBPort.items.find { it.first == deviceConfiguration.usbDevice }

			if (selected != null) {
				choiceUSBPort.selectionModel.select(selected)
			} else {
				choiceUSBPort.selectionModel.select(0)
			}
		}
	}

	private fun MessageKind.sendGetRequest(param: Int? = null) {
		if (param == null) {
			if (radioUSB.isSelected) usbCommunicator.send(this)
			else if (radioBluetooth.isSelected) bluetoothCommunicator.send(this)
		} else {
			if (radioUSB.isSelected) usbCommunicator.send(this, byteArrayOf(param.toByte()))
			else if (radioBluetooth.isSelected) bluetoothCommunicator.send(this, byteArrayOf(param.toByte()))
		}
	}

	private fun MessageKind.sendConfiguration(vararg params: Number) {
		if (radioUSB.isSelected) usbCommunicator.send(this, params.map { it.toByte() }.toByteArray())
		else if (radioBluetooth.isSelected) bluetoothCommunicator.send(this, params.map { it.toByte() }.toByteArray())
	}


}
