package com.poterion.monitor.api.communication

/**
 * Bluetooth listener interface.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
interface BluetoothListener {
	/** On connecting callback */
	fun onConnecting() {
	}

	/** On connection established callback */
	fun onConnect() {
	}

	/** On connection lost callback */
	fun onDisconnect() {
	}

	/**
	 * On message callback
	 *
	 * @param message Received message
	 */
	fun onMessageReceived(message: ByteArray) {
	}

	/**
	 * On message sent callback.
	 *
	 * @param message Sent raw message
	 * @param remaining Remaining message count in the queue.
	 */
	fun onMessageSent(message: ByteArray, remaining: Int) {
	}
}