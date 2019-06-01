package com.poterion.monitor.api.communication

import javafx.application.Platform
import jssc.SerialPort
import jssc.SerialPortEventListener
import jssc.SerialPortException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors

/**
 * USB communicator, embedded version.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
object USBCommunicator {
	private val LOGGER: Logger = LoggerFactory.getLogger(USBCommunicator::class.java)
	const val MAX_PACKET_SIZE = 32

	private var serialPort: SerialPort? = null

	private val messageQueue: ConcurrentLinkedQueue<Pair<ByteArray, Long?>> = ConcurrentLinkedQueue()
	private val chksumQueue: ConcurrentLinkedQueue<Byte> = ConcurrentLinkedQueue()
	private var lastChecksum: Int? = null
	private val listeners = mutableListOf<CommunicatorListener>()

	private val outboundExecutor = Executors.newSingleThreadExecutor()
	private var outboundThread: Thread? = null

	val isConnected
		get() = serialPort?.isOpened == true

	private val serialPortEventListerner = SerialPortEventListener { event ->
		if (event.isRXCHAR) { // If data is available
			val (length, buffer) = event.eventValue
					.takeIf { it > 0 }
					?.let { it to serialPort?.readBytes(it) }
					?.takeIf { (length, data) -> data?.size == length }
					?: 0 to null

			if (buffer != null && buffer.size == length) try {
				val chksumReceived = buffer[0].toInt() and 0xFF
				val chksum = buffer.toList().subList(1, length).toByteArray().calculateChecksum()
				LOGGER.debug("Inbound RAW [${"0x%02X".format(chksumReceived)}/${"0x%02X".format(chksum)}]:" +
						" ${buffer.copyOfRange(0, length).joinToString(" ") { "0x%02X".format(it) }}")

				if (chksum == chksumReceived) {
					val messageKind = buffer[1]
							.let { byte -> MessageKind.values().find { it.code.toByte() == byte } }
							?: MessageKind.UNKNOWN

					if (messageKind != MessageKind.CRC) chksumQueue.add(chksum.toByte())

					when (messageKind) {
						MessageKind.CRC -> {
							lastChecksum = (buffer[2].toInt() and 0xFF)
							LOGGER.debug("Inbound: CRC: ${"0x%02X".format(lastChecksum)}")
						}
						else -> {
							listeners.forEach { Platform.runLater { it.onMessageReceived(Channel.USB, buffer.copyOfRange(0, length)) } }
						}
					}
				}
			} catch (e: ArrayIndexOutOfBoundsException) {
				LOGGER.info(e.message, e)
			} catch (e: SerialPortException) {
				LOGGER.info(e.message, e)
			}
		}
	}

	private val outboundRunnable: () -> Unit = {
		try {
			while (!Thread.interrupted() && isConnected) {
				if (chksumQueue.isNotEmpty()) {
					val chksum = chksumQueue.poll()
					var data = listOf(MessageKind.CRC.code.toByte(), chksum).toByteArray()
					data = listOf(data.calculateChecksum().toByte(), MessageKind.CRC.code.toByte(), chksum).toByteArray()

					serialPort?.writeBytes(data.wrap())
					LOGGER.debug("Outbound CRC: ${"0x%02X".format(chksum)} (${chksumQueue.size})")
					listeners.forEach { Platform.runLater { it.onMessageSent(Channel.USB, data, messageQueue.size) } }
				} else if (messageQueue.isNotEmpty()) {
					val (message, delay) = messageQueue.peek()
					val kind = MessageKind.values().find { it.code.toByte() == message[0] }
					val checksum = message.calculateChecksum()
					val data = listOf(checksum.toByte(), *message.toTypedArray()).toByteArray()
					lastChecksum = null
					serialPort?.writeBytes(data.wrap())

					if (kind != MessageKind.IDD) {
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
							listeners.forEach { Platform.runLater { it.onMessageSent(Channel.USB, data, messageQueue.size) } }
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
		}
	}

	/**
	 * Connect to an USB device.
	 *
	 * @param portName Port name (OS dependent)
	 */
	fun connect(portName: String): Boolean {
		if (!isConnected || portName != serialPort?.portName) {
			if (isConnected) disconnect()

			listeners.forEach { Platform.runLater { it.onConnecting(Channel.USB) } }
			messageQueue.clear()
			chksumQueue.clear()

			serialPort = SerialPort(portName)
			serialPort?.openPort()
			serialPort?.setParams(SerialPort.BAUDRATE_115200,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE)
			serialPort?.addEventListener(serialPortEventListerner)
			outboundThread?.takeIf { !it.isInterrupted }?.interrupt()
			outboundThread = Thread(outboundRunnable)
			outboundExecutor.execute(outboundThread)

			listeners.forEach { Platform.runLater { it.onConnect(Channel.USB) } }
			return true
		}
		return false
	}

	/** Disconnects from an USB device. */
	fun disconnect() {
		LOGGER.debug("Disconnecting from ${serialPort?.portName ?: ""}...")
		messageQueue.clear()
		chksumQueue.clear()
		listeners.forEach { Platform.runLater { it.onDisconnect(Channel.USB) } }
		try {
			if (outboundThread?.isAlive == true) outboundThread?.interrupt()
			serialPort?.closePort()
			serialPort = null
		} catch (e: IOException) {
			LOGGER.error(e.message, e)
		}
	}

	/**
	 * Queues a new message to be sent to target USB device.
	 *
	 * @param kind Message kind.
	 * @param message Message.
	 */
	fun send(kind: MessageKind, message: ByteArray = byteArrayOf()) = message
			.let { data ->
				ByteArray(data.size + 1) { i ->
					when (i) {
						0 -> kind.code.toByte()
						else -> data[i - 1]
					}
				}.also { messageQueue.offer(it to kind.delay) }
			}

	/**
	 * Register a new USB listener.
	 *
	 * @param listener Listener to register.
	 */
	fun register(listener: CommunicatorListener) {
		if (!listeners.contains(listener)) listeners.add(listener)
	}

	/**
	 * Unregister an existing USB listener.
	 *
	 * @param listener Listener to unregister.
	 */
	fun unregister(listener: CommunicatorListener) = listeners.remove(listener)

	private fun ByteArray.wrap() = ByteArray(size + 4) {
		when (it) {
			0 -> 0xAA.toByte()
			1 -> (size / 256.0).toByte()
			2 -> (size % 256.0).toByte()
			size + 3 -> (0xFF - ((size / 256.0).toInt() + (size % 256.0).toInt() + reduce { acc, byte -> (acc + byte).toByte() }) + 1).toByte()
			else -> get(it - 3)
		}
	}
}