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

import com.poterion.bluetooth.tester.api.ConfigListener
import com.poterion.bluetooth.tester.data.Configuration
import com.poterion.bluetooth.tester.data.DeviceConfiguration
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.util.Callback
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MainController : ConfigListener {
	companion object {
		val LOGGER: Logger = LoggerFactory.getLogger(MainController::class.java)
		internal fun getRoot(primaryStage: Stage): Pair<Parent, MainController> = FXMLLoader(MainController::class.java.getResource("main.fxml"))
				.let { it.load<Parent>() to it.getController<MainController>() }
				.also { (_, controller) -> controller.primaryStage = primaryStage }
				.also { (_, controller) -> controller.startup() }
	}

	@FXML private lateinit var borderLayout: BorderPane
	@FXML private lateinit var listConfigurations: ListView<DeviceConfiguration>
	@FXML private lateinit var btnRemoveDevice: Button

	private lateinit var primaryStage: Stage

	private val configController = ConfigController()
	private var deviceUIs = mutableMapOf<DeviceConfiguration, Pair<Parent, DeviceController>>()

	init {
		configController.register(this)
	}

	@FXML
	fun initialize() {
		btnRemoveDevice.isDisable = true

		listConfigurations.cellFactory = Callback<ListView<DeviceConfiguration>, ListCell<DeviceConfiguration>> {
			object : ListCell<DeviceConfiguration>() {
				override fun updateItem(item: DeviceConfiguration?, empty: Boolean) {
					super.updateItem(item, empty)
					if (item != null) {
						text = item.bluetoothName ?: item.bluetoothAddress ?: item.usbDevice
					} else {
						text = null
					}
				}
			}
		}

		listConfigurations.selectionModel.selectedItemProperty().addListener { _, _, selected ->
			btnRemoveDevice.isDisable = selected == null
			if (selected != null) {
				if (!deviceUIs.containsKey(selected)) {
					deviceUIs[selected] = DeviceController.getRoot(primaryStage, configController, selected)
				}
				borderLayout.center = deviceUIs[selected]?.first
			} else {
				borderLayout.center = null
			}
		}
	}

	private fun startup() {
		listConfigurations.items.addAll(configController.configuration.deviceConfigurations)
		if (listConfigurations.items.isNotEmpty()) listConfigurations.selectionModel.select(0)
	}

	fun shutdown() {
		deviceUIs.map { it.value.second }.forEach { it.shutdown() }
	}

	override fun onUpdate(configuration: Configuration) {
		listConfigurations.refresh()
	}

	@FXML
	fun onAddDevice() {
		val newDeviceConfiguration = DeviceConfiguration()
		configController.configuration.deviceConfigurations.add(newDeviceConfiguration)
		configController.save()
		listConfigurations.items.add(newDeviceConfiguration)
		listConfigurations.selectionModel.select(newDeviceConfiguration)
	}

	@FXML
	fun onRemoveDevice() {
		listConfigurations.selectionModel.selectedItem?.also {
			configController.configuration.deviceConfigurations.remove(it)
			configController.save()
			listConfigurations.items.remove(it)
			if (listConfigurations.items.isNotEmpty()) {
				listConfigurations.selectionModel.select(0)
			} else {
				listConfigurations.selectionModel.clearSelection()
			}
		}
	}

}
