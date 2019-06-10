package com.poterion.monitor.api.communication

import javafx.application.Platform
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Device scanner.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
abstract class Scanner<ConnectionDescriptor> {
	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(Scanner::class.java)
	}

	private val scannerExecutor = Executors.newSingleThreadExecutor()
	private val scannerThread: Thread
	private val devices = mutableListOf<ConnectionDescriptor>()
	private val listeners = mutableListOf<ScannerListener<ConnectionDescriptor>>()

	private val scannerRunnable: () -> Unit = {
		while (!Thread.interrupted()) {
			try {
				getAvailableDevices()?.also { currentDevices ->
					if (devices.size != currentDevices.size || devices.any { !currentDevices.contains(it) }) {
						devices.clear()
						devices.addAll(currentDevices)
						listeners.forEach { Platform.runLater { it.onAvailableDevicesChanged(Channel.USB, devices) } }
					}
				}
				Thread.sleep(1000L)
			} catch (e: IOException) {
				LOGGER.error(e.message)
			}
		}
	}

	init {
		scannerThread = Thread(scannerRunnable)
		scannerExecutor.execute(scannerThread)
	}

	/**
	 * Available devices getter.
	 *
	 * @return Available devices.
	 */
	abstract fun getAvailableDevices(): Collection<ConnectionDescriptor>?

	fun shutdown() {
		scannerThread.interrupt()
		scannerExecutor.shutdown()
		scannerExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)
	}

	/**
	 * Register a new listener.
	 *
	 * @param listener Listener to register.
	 */
	fun register(listener: ScannerListener<ConnectionDescriptor>) {
		if (!listeners.contains(listener)) listeners.add(listener)
	}

	/**
	 * Unregister an existing listener.
	 *
	 * @param listener Listener to unregister.
	 */
	fun unregister(listener: ScannerListener<ConnectionDescriptor>) = listeners.remove(listener)
}