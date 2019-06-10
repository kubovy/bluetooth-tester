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
		const val WIDTH = 1200.0
		const val HEIGHT = 1360.0

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
		}
	}
}
