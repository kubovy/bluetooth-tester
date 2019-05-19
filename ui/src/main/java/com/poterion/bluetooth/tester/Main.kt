package com.poterion.bluetooth.tester

import com.poterion.monitor.api.communication.BluetoothCommunicator
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage

class Main : Application() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java, *args)
        }
    }

    override fun start(primaryStage: Stage) {
        val root = Controller.getRoot()
        primaryStage.title = "Bluetooth tester | 2019 (c) Jan Kubovy"
        primaryStage.scene = Scene(root, 800.0, 1040.0)
        primaryStage.isResizable = false
        primaryStage.show()
        primaryStage.setOnCloseRequest {
            BluetoothCommunicator.disconnect()
            Platform.exit()
        }
    }
}
