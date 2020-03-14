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

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.poterion.bluetooth.tester.api.ConfigListener
import com.poterion.bluetooth.tester.data.Configuration
import javafx.application.Platform
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class ConfigController(configFileName: String = "config.yaml") {
	companion object {
		val LOGGER: Logger = LoggerFactory.getLogger(ConfigController::class.java)
	}

	private val configFile = File(configFileName)
	var configuration: Configuration
	private val listeners = mutableListOf<ConfigListener>()

	private val mapper
		get() = ObjectMapper(YAMLFactory()).apply {
			configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		}

	init {
		MainController.LOGGER.info("Starting with ${configFile.absolutePath} config file")
		configuration = if (configFile.exists())
			mapper.readValue(configFile, Configuration::class.java) else Configuration()
	}

	/**
	 * Saves the configuration.
	 */
	fun save() = try {
		mapper.writeValue(configFile, configuration)
		listeners.forEach { Platform.runLater { it.onUpdate(configuration) } }
	} catch (e: Exception) {
		LOGGER.error(e.message, e)
	}

	/**
	 * Registers a new configuration listener.
	 *
	 * @param listener Configuration listener to register.
	 */
	fun register(listener: ConfigListener) {
		listeners.add(listener)
	}
}