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
package com.poterion.bluetooth.tester.controllers

import com.poterion.bluetooth.tester.controllers.modules.*
import com.poterion.bluetooth.tester.data.DeviceConfiguration
import com.poterion.communication.serial.communicator.BluetoothCommunicator
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.USBCommunicator
import com.poterion.communication.serial.listeners.CommunicatorListener
import com.poterion.communication.serial.payload.DeviceCapabilities
import com.poterion.communication.serial.scanner.BluetoothScanner
import com.poterion.communication.serial.scanner.ScannerListener
import com.poterion.communication.serial.scanner.USBScanner
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import javafx.util.StringConverter
import jssc.SerialPortList

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

	@FXML private lateinit var gridContent: GridPane

	@FXML private lateinit var connection: ToggleGroup

	@FXML private lateinit var radioUSB: RadioButton
	@FXML private lateinit var choiceUSBPort: ChoiceBox<Pair<String, Boolean>>
	@FXML private lateinit var labelUSBStatus: Label
	@FXML private lateinit var progressUSB: ProgressBar
	@FXML private lateinit var buttonUSBConnect: Button

	@FXML private lateinit var radioBluetooth: RadioButton
	@FXML private lateinit var comboBluetoothAddress: ComboBox<Pair<String, String>>
	@FXML private lateinit var textBluetoothChannel: TextField
	@FXML private lateinit var labelBluetoothStatus: Label
	@FXML private lateinit var progressBluetooth: ProgressBar
	@FXML private lateinit var buttonBluetoothScan: Button
	@FXML private lateinit var buttonBluetoothConnect: Button

	@FXML private lateinit var checkboxOnDemand: CheckBox

	private lateinit var primaryStage: Stage
	private lateinit var configController: ConfigController
	private lateinit var deviceConfiguration: DeviceConfiguration

	private val selectedChannel: Channel?
		get() = when {
			radioUSB.isSelected -> Channel.USB
			radioBluetooth.isSelected -> Channel.BLUETOOTH
			else -> null
		}

	private val usbPortList: ObservableList<Pair<String, Boolean>> = FXCollections.observableArrayList()
	private val bluetoothDeviceList: ObservableList<Pair<String, String>> = FXCollections.observableArrayList()

	private val usbCommunicator = USBCommunicator()
	private val btCommunicator = BluetoothCommunicator()

	/* Communicator Extension */

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
			comboBluetoothAddress.isDisable = !value
			textBluetoothChannel.isDisable = !value
			buttonBluetoothConnect.isDisable = !value
		}

	private var enabled: Boolean = false
		set(value) {
			field = value
			usbEnabled = deviceConfiguration.hasUSB
			bluetoothEnabled = deviceConfiguration.hasBluetooth
			bluetoothController.enabled = value && deviceConfiguration.hasBluetooth
			dataController.enabled = usbCommunicator.isConnected
					&& value && deviceConfiguration.hasUSB
					&& deviceConfiguration.hasBluetooth
			temperatureController.enabled = value && deviceConfiguration.hasTemp
			lcdController.enabled = value && deviceConfiguration.hasLcd
			registryController.enabled = value && deviceConfiguration.hasRegistry
			ioController.enabled = value && deviceConfiguration.hasGpio
			rgbController.enabled = value && deviceConfiguration.hasRgb
			indicatorsController.enabled = value && deviceConfiguration.hasIndicators
			lightController.enabled = value && deviceConfiguration.hasLight
			rawController.enabled = value
			checkboxOnDemand.isDisable = !value
			//buttonRxClear.isDisable = !value
		}

	private val bluetoothScannerListener = object : ScannerListener<BluetoothCommunicator.Descriptor> {
		override fun onAvailableDevicesChanged(channel: Channel, devices: Collection<BluetoothCommunicator.Descriptor>) {
			updateBluetoothDevices(devices)
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
		choiceUSBPort.items = SortedList(usbPortList, compareBy { it.first })
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
			deviceConfiguration.usbDevice = value?.first
			configController.save()
		}

		/* BLUETOOTH */
		comboBluetoothAddress.items = SortedList(bluetoothDeviceList, compareBy({ it.second }, { it.first }))
		comboBluetoothAddress.converter = object : StringConverter<Pair<String, String>>() {
			override fun toString(pair: Pair<String, String>?): String? = pair
					?.let { (address, name) -> "${name} (${address})" }

			override fun fromString(string: String?): Pair<String, String>? {
				return "(.*) \\(([0-9A-Fa-f]+:[0-9A-Fa-f]+:[0-9A-Fa-f]+:[0-9A-Fa-f]+:[0-9A-Fa-f]+:[0-9A-Fa-f]+)\\)"
						.toRegex()
						.let { regex -> string?.let { regex.matchEntire(it) } }
						?.groupValues
						?.takeIf { it.size == 3 }
						?.let { it[2] to it[1] }
			}
		}
		comboBluetoothAddress.selectionModel.selectedItemProperty().addListener { _, _, value ->
			deviceConfiguration.bluetoothAddress = value?.first
			configController.save()
		}
		comboBluetoothAddress.focusedProperty().addListener { _, _, focus ->
			if (!focus) {
				deviceConfiguration.bluetoothAddress = comboBluetoothAddress.value?.first
				configController.save()
			}
		}

		textBluetoothChannel.focusedProperty().addListener { _, _, focus ->
			if (!focus) {
				deviceConfiguration.bluetoothChannel = textBluetoothChannel.text.toIntOrNull()
				configController.save()
			}
		}

		BluetoothScanner.register(bluetoothScannerListener)
		USBScanner.register(usbScannerListener)

		btCommunicator.register(this)
		usbCommunicator.register(this)
	}

	private lateinit var bluetoothController: BluetoothController
	private lateinit var dataController: DataController
	private lateinit var temperatureController: TemperatureController
	private lateinit var lcdController: LcdController
	private lateinit var registryController: RegistryController
	private lateinit var ioController: IoController
	private lateinit var rgbController: RgbStripController
	private lateinit var indicatorsController: IndicatorsController
	private lateinit var lightController: LightController
	private lateinit var rawController: RawController
	private val modules: List<ModuleControllerInterface>
		get() = listOf(
				bluetoothController,
				dataController,
				temperatureController,
				lcdController,
				registryController,
				ioController,
				rgbController,
				indicatorsController,
				lightController,
				rawController)

	private fun startup() {
		bluetoothController = BluetoothController
				.getRoot(primaryStage, configController, deviceConfiguration, usbCommunicator, btCommunicator)
				{ selectedChannel }
		dataController = DataController
				.getRoot(primaryStage, configController, deviceConfiguration, usbCommunicator, btCommunicator)
				{ selectedChannel }
		temperatureController = TemperatureController
				.getRoot(primaryStage, configController, deviceConfiguration, usbCommunicator, btCommunicator)
				{ selectedChannel }
		lcdController = LcdController
				.getRoot(primaryStage, configController, deviceConfiguration, usbCommunicator, btCommunicator)
				{ selectedChannel }
		registryController = RegistryController
				.getRoot(primaryStage, configController, deviceConfiguration, usbCommunicator, btCommunicator)
				{ selectedChannel }
		ioController = IoController
				.getRoot(primaryStage, configController, deviceConfiguration, usbCommunicator, btCommunicator)
				{ selectedChannel }
		rgbController = RgbStripController
				.getRoot(primaryStage, configController, deviceConfiguration, usbCommunicator, btCommunicator)
				{ selectedChannel }
		indicatorsController = IndicatorsController
				.getRoot(primaryStage, configController, deviceConfiguration, usbCommunicator, btCommunicator)
				{ selectedChannel }
		lightController = LightController
				.getRoot(primaryStage, configController, deviceConfiguration, usbCommunicator, btCommunicator)
				{ selectedChannel }
		rawController = RawController
				.getRoot(primaryStage, configController, deviceConfiguration, usbCommunicator, btCommunicator)
				{ selectedChannel }

		var index = 6
		for (module in modules) {
			for ((label, content, buttons) in module.rows) {
				if (label != null) gridContent.add(label, 0, index)
				if (content != null) gridContent.add(content, 1, index)
				if (buttons != null) gridContent.add(buttons, 2, index)
				index++
			}
		}

		when (deviceConfiguration.selectedChannel) {
			Channel.USB -> radioUSB.isSelected = true
			Channel.BLUETOOTH -> radioBluetooth.isSelected = true
		}
		radioBluetooth.selectedProperty().addListener { _, _, value ->
			if (value) deviceConfiguration.selectedChannel = Channel.BLUETOOTH
			configController.save()
		}
		radioUSB.selectedProperty().addListener { _, _, value ->
			dataController.enabled = usbCommunicator.isConnected && value
			if (value) deviceConfiguration.selectedChannel = Channel.USB
			configController.save()
		}

		updateSerialPorts(SerialPortList.getPortNames().map { USBCommunicator.Descriptor(it) })

		comboBluetoothAddress.value = deviceConfiguration.bluetoothAddress
				?.let { it to (deviceConfiguration.bluetoothName ?: "") }
		textBluetoothChannel.text = deviceConfiguration.bluetoothChannel?.toString() ?: ""

		enabled = usbCommunicator.isConnected || btCommunicator.isConnected
	}

	fun shutdown() {
		usbCommunicator.shutdown()
		btCommunicator.shutdown()
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
		if (btCommunicator.isDisconnected) {
			val descriptor = BluetoothCommunicator.Descriptor(
					comboBluetoothAddress.value?.first ?: "",
					textBluetoothChannel.text.toIntOrNull() ?: 6)

			if (btCommunicator.connect(descriptor)) {
				buttonBluetoothConnect.text = "Cancel"
			}
		} else {
			btCommunicator.disconnect()
			buttonBluetoothConnect.text = "Connect"
		}
	}

	@FXML
	fun onBluetoothScan() {
		//buttonBluetoothScan.isDisable = true
		BluetoothScanner.scan()
	}


	@FXML
	//fun onTxSend() {
	//	"0x([0-9A-Fa-f]{1,4})".toRegex()
	//			.matchEntire(textTxMessageType.text)
	//			?.groups
	//			?.get(0)
	//			?.value
	//			?.toIntOrNull()
	//			?.let { code -> MessageKind.values().find { it.code == code } }
	//			?.also { messageType ->
	//				bluetoothCommunicator.send(messageType, areaTxMessage.text
	//						.replace("[ \t\n\r]".toRegex(), "")
	//						.replace("0x([0-9A-Fa-f]{1,4})".toRegex()) {
	//							"${it.groups[1]?.value?.toIntOrNull(16)?.toChar() ?: ""}"
	//						}
	//						.toByteArray())
	//			}
	//}

	override fun onConnecting(channel: Channel) = Platform.runLater {
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

	override fun onConnect(channel: Channel) = Platform.runLater {
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
		enabled = usbCommunicator.isConnected || btCommunicator.isConnected || checkboxOnDemand.isSelected
	}

	override fun onConnectionReady(channel: Channel) = Platform.runLater {
		// nothing
	}

	override fun onDisconnect(channel: Channel) = Platform.runLater {
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
		enabled = usbCommunicator.isConnected || btCommunicator.isConnected || checkboxOnDemand.isSelected
	}

	override fun onMessageReceived(channel: Channel, message: IntArray) = Platform.runLater {
		enabled = usbCommunicator.isConnected || btCommunicator.isConnected || checkboxOnDemand.isSelected
	}

	override fun onMessagePrepare(channel: Channel) {
		enshureUsbDescriptor()
		enshureBluetoothDescriptor()
	}

	override fun onMessageSent(channel: Channel, message: IntArray, remaining: Int) = Platform.runLater {
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
			rawController.enabled = true
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

	override fun onDeviceCapabilitiesChanged(channel: Channel, capabilities: DeviceCapabilities) = Platform.runLater {
		deviceConfiguration.hasBluetooth = capabilities.hasBluetooth
		deviceConfiguration.hasUSB = capabilities.hasUSB
		deviceConfiguration.hasTemp = capabilities.hasTemp
		deviceConfiguration.hasLcd = capabilities.hasLCD
		deviceConfiguration.hasRegistry = capabilities.hasRegistry
		deviceConfiguration.hasGpio = true //capabilities.hasMotionSensor
		deviceConfiguration.hasRgb = capabilities.hasRgbStrip
		deviceConfiguration.hasIndicators = capabilities.hasRgbIndicators
		deviceConfiguration.hasLight = capabilities.hasRgbLight
		configController.save()
		enabled = enabled // refresh capabilities
	}

	override fun onDeviceNameChanged(channel: Channel, name: String) = Platform.runLater {
		deviceConfiguration.bluetoothName = name
		configController.save()
	}

	private fun enshureUsbDescriptor() {
		if (usbCommunicator.isDisconnected) {
			usbCommunicator.connectionDescriptor = choiceUSBPort.value
					?.takeIf { it.second }
					?.let { USBCommunicator.Descriptor(it.first) }
		}
	}

	private fun enshureBluetoothDescriptor() {
		if (btCommunicator.isDisconnected) {
			btCommunicator.connectionDescriptor = BluetoothCommunicator.Descriptor(
					comboBluetoothAddress.value?.first ?: "",
					textBluetoothChannel.text.toIntOrNull() ?: 6)
		}
	}

	private fun updateSerialPorts(devices: Collection<USBCommunicator.Descriptor>) {
		val portNames = devices
				.map { it.portName }
				.map { it to true }
				.toMutableList()

		if (usbPortList.filter { it.second }.size != portNames.size
				|| !usbPortList.filter { it.second }.all { port -> portNames.any { port.first == it.first } }) {

			deviceConfiguration.usbDevice
					?.takeIf { device -> portNames.none { it.first == device } }
					?.also { portNames.add(it to false) }

			val removedPorts = usbPortList
					.filterNot { (port, _) -> portNames.map { (p, _) -> p }.contains(port) }
			val addedPorts = portNames
					.filterNot { (port, _) -> usbPortList.map { (p, _) -> p }.contains(port) }
			usbPortList.removeAll(removedPorts)
			usbPortList.replaceAll { (port, connected) ->
				port to (portNames.find { (p, _) -> port == p }?.second ?: connected)
			}
			usbPortList.addAll(addedPorts)

			val selected = usbPortList.find { it.first == deviceConfiguration.usbDevice }
			if (selected != null) {
				choiceUSBPort.selectionModel.select(selected)
			}
		}
	}

	private fun updateBluetoothDevices(devices: Collection<BluetoothCommunicator.Descriptor>) {
		val deviceList = devices
				.map { it.address to (it.name ?: "") }
				.toMutableList()

		if (bluetoothDeviceList.size != deviceList.size
				|| !bluetoothDeviceList.all { port -> deviceList.any { port.first == it.first } }) {

			deviceConfiguration.bluetoothAddress
					?.takeIf { device -> deviceList.none { it.first == device } }
					?.also { deviceList.add(it to (deviceConfiguration.bluetoothName ?: "")) }

			val removedDevices = bluetoothDeviceList
					.filterNot { (addr, _) -> deviceList.map { (a, _) -> a }.contains(addr) }
			val addedPorts = deviceList
					.filterNot { (addr, _) -> bluetoothDeviceList.map { (a, _) -> a }.contains(addr) }
			bluetoothDeviceList.removeAll(removedDevices)
			bluetoothDeviceList.replaceAll { (addr, name) ->
				addr to (deviceList.find { (p, _) -> addr == p }?.second ?: name)
			}
			bluetoothDeviceList.addAll(addedPorts)

			val selected = bluetoothDeviceList.find { it.first == deviceConfiguration.usbDevice }
			if (selected != null) {
				comboBluetoothAddress.selectionModel.select(selected)
			}
		}
	}
}
