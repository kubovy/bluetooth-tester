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
package com.poterion.bluetooth.tester

import com.poterion.bluetooth.tester.controllers.MainController
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * Main application class.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class Main : Application() {
	companion object {
		const val WIDTH = 1250.0
		const val HEIGHT = 1460.0

		@JvmStatic
		fun main(args: Array<String>) {
			launch(Main::class.java, *args)
		}
	}

	override fun start(primaryStage: Stage) {
		val (root, controller) = MainController.getRoot(primaryStage)
		primaryStage.title = "Bluetooth tester | 2019 (c) Jan Kubovy"
		primaryStage.scene = Scene(root, WIDTH, HEIGHT)
		primaryStage.minWidth = WIDTH
		//primaryStage.maxWidth = WIDTH
		primaryStage.minHeight = HEIGHT
		//primaryStage.isResizable = false
		primaryStage.show()
		primaryStage.setOnCloseRequest {
			controller.shutdown()
			Platform.exit()
			System.exit(0)
		}
	}
}
