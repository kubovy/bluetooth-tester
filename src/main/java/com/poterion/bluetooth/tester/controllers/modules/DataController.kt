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
import com.poterion.communication.serial.extensions.DataCommunicatorExtension
import com.poterion.communication.serial.listeners.DataCommunicatorListener
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File

/**
 * Data controller.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class DataController : ModuleControllerInterface, DataCommunicatorListener {
	companion object {
		internal fun getRoot(primaryStage: Stage,
							 configController: ConfigController,
							 deviceConfiguration: DeviceConfiguration,
							 usbCommunicator: USBCommunicator,
							 btCommunicator: BluetoothCommunicator,
							 selectedChannel: () -> Channel?): DataController =
				FXMLLoader(DataController::class.java.getResource("data.fxml"))
						.also { it.load<GridPane>() }
						.getController<DataController>()
						.also { controller ->
							controller.primaryStage = primaryStage
							controller.configController = configController
							controller.deviceConfiguration = deviceConfiguration
							controller.usbCommunicator = DataCommunicatorExtension(usbCommunicator)
							controller.btCommunicator = DataCommunicatorExtension(btCommunicator)
							controller.selectedChannel = selectedChannel
							controller.startup()
						}
	}

	@FXML private lateinit var labelTitle: Label

	@FXML private lateinit var gridContent: GridPane
	@FXML private lateinit var textDataPart: TextField
	@FXML private lateinit var textDataBlockSize: TextField

	@FXML private lateinit var hboxButtons: HBox
	@FXML private lateinit var buttonDataDownload: Button
	@FXML private lateinit var buttonDataUpload: Button

	private lateinit var primaryStage: Stage
	private lateinit var configController: ConfigController
	private lateinit var deviceConfiguration: DeviceConfiguration

	private lateinit var usbCommunicator: DataCommunicatorExtension<USBCommunicator.Descriptor>
	private lateinit var btCommunicator: DataCommunicatorExtension<BluetoothCommunicator.Descriptor>
	private lateinit var selectedChannel: () -> Channel?

	private val communicator: DataCommunicatorExtension<*>?
		get() = when (selectedChannel()) {
			Channel.USB -> usbCommunicator
			Channel.BLUETOOTH -> btCommunicator
			else -> null
		}

	override var enabled: Boolean = false
		set(value) {
			field = value
			// TODO Bluetooth EEPROM must not be transmitted over bluetooth since bluetooth is off
			textDataPart.isDisable = !value // || !radioUSB.isSelected
			textDataBlockSize.isDisable = !value // || !radioUSB.isSelected
			buttonDataDownload.isDisable = !value // || !radioUSB.isSelected
			buttonDataUpload.isDisable = !value // || !radioUSB.isSelected
		}

	override val rows: List<Triple<Node?, Node?, Node?>>
		get() = listOf(Triple(labelTitle, gridContent, hboxButtons))

	private var dataFile: File? = null
	private val dataBuffer = ByteArray(8192)
	private var dataPart = 0
	private var dataChunkSize = 0

	@FXML
	fun initialize() {
		textDataPart.focusedProperty().addListener { _, _, focus ->
			if (!focus) {
				deviceConfiguration.dataPart = textDataPart.text.toIntOrNull()
				configController.save()
			}
		}
		textDataBlockSize.focusedProperty().addListener { _, _, focus ->
			if (!focus) {
				deviceConfiguration.dataBlockSize = textDataBlockSize.text.toIntOrNull()
				configController.save()
			}
		}
	}

	private fun startup() {
		usbCommunicator.register(this)
		btCommunicator.register(this)
		textDataPart.text = deviceConfiguration.dataPart?.toString() ?: ""
		textDataBlockSize.text = deviceConfiguration.dataBlockSize?.toString() ?: ""
	}

	@FXML
	fun onDataDownload() {
		dataPart = textDataPart.text.toIntOrNull() ?: 0
		dataChunkSize = textDataBlockSize.text.toIntOrNull() ?: 255
		if (dataPart >= 0 && dataChunkSize > 0) {
			dataFile = FileChooser()
					.apply { title = "Choose file to download the data to ..." }
					.showSaveDialog(primaryStage)
			if (dataFile != null) {
				enabled = false
				// TODO progressUSB.progress = -1.0
				(0 until dataBuffer.size).forEach { dataBuffer[it] = 0xFF.toByte() }
				communicator?.sendDataRequest(dataPart)
			}
		}
	}

	@FXML
	fun onDataUpload() {
		val fileChooser = FileChooser()
		fileChooser.title = "Choose file to upload to the device ..."
		dataFile = fileChooser.showOpenDialog(primaryStage)
		if (dataFile != null) {
			// TODO bluetoothSettingsEnabled = false
		}
	}

	override fun onConsistencyCheckReceived(channel: Channel, part: Int, checksum: Int) = Platform.runLater {
	}

	override fun onDataReceived(channel: Channel,
								part: Int,
								address: Int,
								length: Int,
								data: IntArray) = Platform.runLater {
		if (address + data.size == length) { // Download finished
			dataFile?.outputStream()?.bufferedWriter()?.use { writer ->
				dataBuffer
						.toList()
						.chunked(dataChunkSize)
						.mapIndexed { index, chunk -> index * dataChunkSize to chunk }
						.toMap()
						.forEach { (address, data) ->
							val dataAligned = data.map { it }.toMutableList()
							while (dataAligned.size < dataChunkSize) dataAligned.add(0xFF.toByte())
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

			// TODO progressUSB.progress = 0.0
			enabled = true
			dataChunkSize = 0
		} else {
			enabled = false
			// TODO if (progressUSB.progress == 0.0) progressUSB.progress = -1.0
			data.indices.forEach { i ->
				dataBuffer[address + i] = data[i].toByte()
			}
		}
	}
}
