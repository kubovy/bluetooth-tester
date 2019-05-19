package com.poterion.monitor.api.communication

import javafx.application.Platform
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.RuntimeException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import javax.bluetooth.DiscoveryAgent
import javax.bluetooth.LocalDevice
import javax.microedition.io.Connector
import javax.microedition.io.StreamConnection

/**
 * Bluetooth communicator, embedded version.
 *
 * @param address Address of the target bluetooth device to connect to.
 * @param shouldConnect Whether connection should be established or not.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
object BluetoothCommunicator {

	private val LOGGER: Logger = LoggerFactory.getLogger(BluetoothCommunicator::class.java)
	const val MAX_PACKET_SIZE = 32

	private var address: String = "00:00:00:00:00:00"
	private var channel: Int = 6
	var shouldReconnect: Boolean = true

	private val url: String
		get() = "btspp://${address.replace(":", "")}:${channel};authenticate=false;encrypt=false;master=false"

	private val messageQueue: ConcurrentLinkedQueue<Pair<ByteArray, Long?>> = ConcurrentLinkedQueue()
	private val chksumQueue: ConcurrentLinkedQueue<Byte> = ConcurrentLinkedQueue()
	private var lastChecksum: Int? = null
	private val listeners = mutableListOf<BluetoothListener>()


	private var streamConnection: StreamConnection? = null
	private var outputStream: OutputStream? = null
	private var inputStream: InputStream? = null

	private val connectorExecutor = Executors.newSingleThreadExecutor()
	private val watchdogExcutor = Executors.newSingleThreadExecutor()
	private val inboundExecutor = Executors.newSingleThreadExecutor()
	private val outboundExecutor = Executors.newSingleThreadExecutor()

	private var connectorThread: Thread? = null
	private var watchdogThread: Thread? = null
	private var inboundThread: Thread? = null
	private var outboundThread: Thread? = null

	/** Whether the target bluetooth device is connected or not. */
	var isConnected = false
		private set
	var isConnecting = false
		private set
	var pingInterval: Long = -1

	private val connectorRunnable: () -> Unit = {
		while (shouldReconnect && !isConnected && isConnecting) {
			if (streamConnection != null || inputStream != null || outputStream != null) {
				disconnect()
			}

			LOGGER.debug("Connecting to ${address}...")

			inboundThread?.takeIf { it.isAlive }?.interrupt()
			outboundThread?.takeIf { it.isAlive }?.interrupt()

			while (streamConnection == null && isConnecting) try {
				//val uuid = UUID("1101", true) // Create a UUID for SPP (1101)
				streamConnection = Connector.open(url) as StreamConnection

				//Wait for client connection
				println("\nServer Started. Waiting for clients to connect...")

				//val connection = streamConnNotifier!!.acceptAndOpen()
				//println("Remote device address: " + RemoteDevice.getRemoteDevice(connection).bluetoothAddress)
				//println("Remote device name: " + RemoteDevice.getRemoteDevice(connection).getFriendlyName(true))

				//the stream is opened both in and out
				outputStream = streamConnection?.openOutputStream()
				inputStream = streamConnection?.openInputStream()
				isConnected = true
				isConnecting = false

				inboundThread?.takeIf { !it.isInterrupted }?.interrupt()
				outboundThread?.takeIf { !it.isInterrupted }?.interrupt()

				inboundThread = Thread(inboundRunnable)
				outboundThread = Thread(outboundRunnable)

				inboundExecutor.execute(inboundThread)
				outboundExecutor.execute(outboundThread)
				listeners.forEach { Platform.runLater(it::onConnect) }
			} catch (e: IOException) {
				LOGGER.error(e.message, e)
				Thread.sleep(10_000L)
				disconnect()
			}
		}
	}

	private val watchdogRunnable: () -> Unit = {
		while (true) {
			if (pingInterval > 0L && isConnected && messageQueue.isEmpty()) send(BluetoothMessageKind.IDD)
			Thread.sleep(if (pingInterval > 0L) pingInterval else 10_000L)
		}
	}

	private val outboundRunnable: () -> Unit = {
		try {
			while (!Thread.interrupted() && isConnected) {
				if (chksumQueue.isNotEmpty()) {
					val chksum = chksumQueue.poll()
					var data = listOf(BluetoothMessageKind.CRC.byteCode, chksum).toByteArray()
					data = listOf(data.calculateChecksum().toByte(), BluetoothMessageKind.CRC.byteCode, chksum).toByteArray()
					outputStream?.write(data)
					outputStream?.flush()
					LOGGER.debug("Outbound CRC: ${"0x%02X".format(chksum)} (${chksumQueue.size})")
				} else if (messageQueue.isNotEmpty()) {
					val (message, delay) = messageQueue.peek()
					val kind = BluetoothMessageKind.values().find { it.byteCode == message[0] }
					val checksum = message.calculateChecksum()
					val data = listOf(checksum.toByte(), *message.toTypedArray()).toByteArray()
					lastChecksum = null
					outputStream?.write(data)
					outputStream?.flush()

					if (kind != BluetoothMessageKind.IDD) {
						var timeout = delay ?: 500 // default delay in ms
						while (lastChecksum != checksum && timeout > 0) {
							Thread.sleep(1)
							timeout--
						}

						val correctlyReceived = checksum == lastChecksum
						if (correctlyReceived) messageQueue.poll()
						LOGGER.debug("Outbound [${"0x%02X".format(lastChecksum)}/${"0x%02X".format(checksum)}]:" +
								" ${data.joinToString(" ") { "0x%02X".format(it) }}" +
								" (remaining: ${messageQueue.size})")
						if (correctlyReceived) {
							listeners.forEach { Platform.runLater { it.onMessageSent(messageQueue.size) } }
							lastChecksum = null
						}
					} else {
						messageQueue.poll() // IDD: Fire-and-Forget
					}
				} else {
					Thread.sleep(100L)
				}
			}
		} catch (e: IOException) {
			LOGGER.warn(e.message)
			isConnected = false
		} finally {
			reconnect()
		}
	}

	private val inboundRunnable: () -> Unit = {
		try {
			while (!Thread.interrupted()) {
				val buffer = ByteArray(256)
				val read = inputStream?.read(buffer) ?: 0

				if (read == -1) throw RuntimeException("Disconnected")
				if (read > 0) {
					val chksumReceived = buffer[0].toInt() and 0xFF
					val chksum = buffer.toList().subList(1, read).toByteArray().calculateChecksum()
					LOGGER.debug("Inbound RAW [${"0x%02X".format(chksumReceived)}/${"0x%02X".format(chksum)}]:" +
							" ${buffer.copyOfRange(0, read).joinToString(" ") { "0x%02X".format(it) }}")

					if (chksum == chksumReceived) {
						val messageKind = buffer[1]
								.let { byte -> BluetoothMessageKind.values().find { it.byteCode == byte } }
								?: BluetoothMessageKind.UNKNOWN

						if (messageKind != BluetoothMessageKind.CRC) chksumQueue.add(chksum.toByte())

						when (messageKind) {
							BluetoothMessageKind.CRC -> {
								lastChecksum = (buffer[2].toInt() and 0xFF)
								LOGGER.debug("Inbound: CRC: ${"0x%02X".format(lastChecksum)}")
							}
							else -> {
								listeners.forEach { Platform.runLater { it.onMessage(buffer.copyOfRange(0, read)) } }
							}
						}
					}
				}
			}
		} catch (e: IOException) {
			LOGGER.info(e.message)
		} finally {
			reconnect()
		}
	}

	init {
		watchdogThread = Thread(watchdogRunnable)
		watchdogExcutor.execute(watchdogThread)
	}

	fun devices() = LocalDevice
			.getLocalDevice()
			.discoveryAgent
			.retrieveDevices(DiscoveryAgent.PREKNOWN)
			//.retrieveDevices(DiscoveryAgent.CACHED)
			?.toList()
			?.map { it.getFriendlyName(false) to it.bluetoothAddress }
			?: emptyList()

	/**
	 * Connect to a bluetooth device.
	 *
	 * @param address Address of the target device. Optional, overrides the cached value.
	 */
	fun connect(address: String, channel: Int): Boolean {
		if (address.matches("[0-9A-Fa-f]{2}(:[0-9A-Fa-f]{2}){5}".toRegex()) &&
				(!isConnected && !isConnecting || address != this.address || channel != this.channel)) {

			messageQueue.clear()
			chksumQueue.clear()
			if (isConnected) disconnect()

			isConnecting = true
			listeners.forEach { Platform.runLater(it::onConnecting) }
			this.address = address
			this.channel = channel

			if (connectorThread?.isAlive != true) {
				connectorThread = Thread(connectorRunnable)
				connectorExecutor.execute(connectorThread)
			}
			return true
		}
		return false
	}

	private fun reconnect() {
		disconnect()
		connect(this.address, this.channel)
	}

	/** Disconnects from a bluetooth device. */
	fun disconnect() {
		LOGGER.debug("Disconnecting from ${address}...")
		messageQueue.clear()
		chksumQueue.clear()
		listeners.forEach { Platform.runLater(it::onDisconnect) }
		try {
			if (inboundThread?.isAlive == true) inboundThread?.interrupt()
			if (outboundThread?.isAlive == true) outboundThread?.interrupt()
			if (connectorThread?.isAlive == true) connectorThread?.interrupt()
			inputStream?.close()
			inputStream = null
			outputStream?.close()
			outputStream = null
			streamConnection?.close()
			streamConnection = null
		} catch (e: IOException) {
			LOGGER.error(e.message, e)
		} finally {
			isConnected = false
		}
	}

	fun cancel() {
		isConnecting = false
		listeners.forEach { Platform.runLater(it::onDisconnect) }
	}

	/**
	 * Queues a new message to be sent to target bluetooth device.
	 *
	 * @param kind Message kind.
	 * @param message Message.
	 */
	fun send(kind: MessageKind, message: ByteArray = byteArrayOf()) = message
			.let { data ->
				ByteArray(data.size + 1) { i ->
					when (i) {
						0 -> kind.byteCode
						else -> data[i - 1]
					}
				}.also { messageQueue.offer(it to kind.delay) }
			}

	/**
	 * Register a new bluetooth listener.
	 *
	 * @param listener Listener to register.
	 */
	fun register(listener: BluetoothListener) {
		if (!listeners.contains(listener)) listeners.add(listener)
	}

	/**
	 * Unregister an existing bluetooth listener.
	 *
	 * @param listener Listener to unregister.
	 */
	fun unregister(listener: BluetoothListener) = listeners.remove(listener)
}