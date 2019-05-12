package com.poterion.bluetooth.tester

import javafx.application.Application
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
        primaryStage.title = "Bluetooth tester"
        primaryStage.scene = Scene(root, 800.0, 800.0)
        primaryStage.isResizable = false
        primaryStage.show()
    }
}
