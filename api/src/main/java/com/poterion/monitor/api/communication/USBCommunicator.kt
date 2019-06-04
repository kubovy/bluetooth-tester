package com.poterion.monitor.api.communication

import javafx.application.Platform
import jssc.SerialPort
import jssc.SerialPortEvent
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

	enum class State {
		IDLE,
		LENGTH_HIGH,
		LENGTH_LOW,
		ADDITIONAL
	}

	private val LOGGER: Logger = LoggerFactory.getLogger(USBCommunicator::class.java)
	const val MAX_PACKET_SIZE = 35

	private var serialPort: SerialPort? = null

	private val messageQueue: ConcurrentLinkedQueue<Pair<ByteArray, Long?>> = ConcurrentLinkedQueue()
	private val chksumQueue: ConcurrentLinkedQueue<Byte> = ConcurrentLinkedQueue()
	private var lastChecksum: Int? = null
	private val listeners = mutableListOf<CommunicatorListener>()

	private val outboundExecutor = Executors.newSingleThreadExecutor()
	private var outboundThread: Thread? = null

	val isConnected
		get() = serialPort?.isOpened == true

	/**
	 * This implements the application communication layer (see components/serial_communication.h)
	 */
	private fun processIncomingMessage(data: ByteArray) {
		try {
			val chksumReceived = data[0].toInt() and 0xFF
			val chksum = data.toList().subList(1, data.size).toByteArray().calculateChecksum()
			LOGGER.debug("Inbound RAW [${"0x%02X".format(chksumReceived)}/${"0x%02X".format(chksum)}]:" +
					" ${data.joinToString(" ") { "0x%02X".format(it) }}")

			if (chksum == chksumReceived) {
				val messageKind = data[1]
						.let { byte -> MessageKind.values().find { it.code.toByte() == byte } }
						?: MessageKind.UNKNOWN

				if (messageKind != MessageKind.CRC) chksumQueue.add(chksum.toByte())

				when (messageKind) {
					MessageKind.CRC -> {
						lastChecksum = (data[2].toInt() and 0xFF)
						LOGGER.debug("Inbound: CRC: ${"0x%02X".format(lastChecksum)}")
					}
					else -> {
					}
				}
				listeners.forEach { Platform.runLater { it.onMessageReceived(Channel.USB, data) } }
			}
		} catch (e: ArrayIndexOutOfBoundsException) {
			LOGGER.info(e.message, e)
		} catch (e: SerialPortException) {
			LOGGER.info(e.message, e)
		}
	}

	/**
	 * This implements a lower communication layer similar to the IS1678S.
	 *
	 * One application packet can be divided into multiple interface packets.
	 *
	 * ----------------------------------
	 * |             PACKET             |
	 * ----------------------------------
	 * |SYNC|LENH|LENL|DATA        |CRC |
	 * ----------------------------------
	 * |0xAA|0xXX|0xXX|0xXX .. 0xXX|0xCC|
	 * ----------------------------------
	 * |    |         |---- LEN ---|    |
	 * |    |-------- CRC -------- |    |
	 * ----------------------------------
	 */
	private val serialPortEventListerner = object : SerialPortEventListener {
		private val buffer: ByteArray = ByteArray(MAX_PACKET_SIZE)
		private var chksum = 0
		private var index = 0
		private var length = 0
		private var state = State.IDLE

		override fun serialEvent(event: SerialPortEvent) {
			if (event.isRXCHAR) { // If data is available
				val (length, data) = event.eventValue
						.takeIf { it > 0 }
						?.let { it to serialPort?.readBytes(it) }
						?.takeIf { (length, data) -> data?.size == length }
						?: 0 to null

				if (data != null && data.size == length) {
					data.forEach { byte ->
						when (state) {
							State.IDLE -> {
								if (byte.toUInt() == 0xAA) {
									this.state = State.LENGTH_HIGH
									this.chksum = 0
									LOGGER.debug("USB> 0xAA: IDLE -> LENGTH_HIGH")
								}
							}
							State.LENGTH_HIGH -> {
								this.length = byte.toUInt() * 256
								this.chksum += byte.toUInt()
								this.state = State.LENGTH_LOW
								LOGGER.debug("USB> 0x%02X: LENGTH_HIGH -> LENGTH_LOW".format(byte))
							}
							State.LENGTH_LOW -> {
								this.length += byte.toUInt()
								if (this.length > buffer.size) {
									this.state = State.IDLE
									LOGGER.debug("USB> 0x%02X: LENGTH_LOW -> IDLE".format(byte))
								} else {
									this.chksum += byte.toUInt()
									this.index = 0
									this.state = State.ADDITIONAL
									LOGGER.debug("USB> 0x%02X: LENGTH_LOW -> ADDITIONAL".format(byte))
								}
							}
							State.ADDITIONAL -> {
								if (this.index < this.length) {
									this.buffer[this.index++] = byte
									this.chksum += byte.toUInt()
								} else {
									this.chksum = (0xFF - this.chksum + 1) and 0xFF
									if (this.chksum == byte.toUInt()) {
										LOGGER.debug("USB> 0x%02X: checksum OK (0x%02X)".format(byte, this.chksum))
										processIncomingMessage(this.buffer.sliceArray(0 until this.length))
									} else {
										LOGGER.warn("USB> 0x%02X: Wrong checksum (0x%02X)".format(byte, this.chksum))
									}
									this.state = State.IDLE
								}
							}
						}
					}
				}
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
			try {
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
			} catch (e: SerialPortException) {
				LOGGER.error(e.message, e)
				disconnect()
			}
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
			LOGGER.error(e.message)
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

	@Suppress("EXPERIMENTAL_API_USAGE")
	private fun Byte.toUInt() = toUByte().toInt()
}