package com.poterion.bluetooth.tester

import com.poterion.bluetooth.bm78.PairingMethod
import com.poterion.bluetooth.bm78.RGBPattern
import com.poterion.bluetooth.bm78.WS281xPattern
import com.poterion.monitor.api.communication.BluetoothCommunicator
import com.poterion.monitor.api.communication.BluetoothListener
import com.poterion.monitor.api.communication.BluetoothMessageKind
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.paint.Color
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*


class Controller : BluetoothListener {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(Controller::class.java.simpleName)
        internal fun getRoot(): Parent = FXMLLoader(Controller::class.java.getResource("main.fxml"))
                .let { it.load<Parent>() to it.getController<Controller>() }
                .let { (root, _) -> root }
    }

    @FXML private lateinit var textBluetoothAddress: TextField
    @FXML private lateinit var textBluetoothChannel: TextField
    @FXML private lateinit var labelStatus: Label
    @FXML private lateinit var progressBluetooth: ProgressBar
    @FXML private lateinit var buttonConnect: Button

    @FXML private lateinit var textBluetoothName: TextField
    @FXML private lateinit var textBluetoothPin: TextField
    @FXML private lateinit var comboBluetoothPairingMethod: ComboBox<PairingMethod>
    @FXML private lateinit var buttonSettingsGet: Button
    @FXML private lateinit var buttonSettingsSet: Button

    @FXML private lateinit var textDHT11Temperature: TextField
    @FXML private lateinit var textDHT11Humidity: TextField
    @FXML private lateinit var buttonDHT11Get: Button

    @FXML private lateinit var buttonIOsGet: Button

    @FXML private lateinit var buttonPIRGet: Button

    @FXML private lateinit var comboRGBPattern: ComboBox<RGBPattern>
    @FXML private lateinit var colorRGB: ColorPicker
    @FXML private lateinit var textRGBDelay: TextField
    @FXML private lateinit var textRGBMin: TextField
    @FXML private lateinit var textRGBMax: TextField
    @FXML private lateinit var textRGBCount: TextField
    @FXML private lateinit var buttonRGBGet: Button
    @FXML private lateinit var buttonRGBSet: Button

    @FXML private lateinit var textWS281xLED: TextField
    @FXML private lateinit var comboWS281xPattern: ComboBox<WS281xPattern>
    @FXML private lateinit var colorWS281x: ColorPicker
    @FXML private lateinit var textWS281xDelay: TextField
    @FXML private lateinit var textWS281xMin: TextField
    @FXML private lateinit var textWS281xMax: TextField
    @FXML private lateinit var buttonWS281xGet: Button
    @FXML private lateinit var buttonWS281xSet: Button

    @FXML private lateinit var comboTxMessageType: ComboBox<String>
    @FXML private lateinit var textTxMessageType: TextField
    @FXML private lateinit var textTxCRC: TextField
    @FXML private lateinit var areaTxMessage: TextArea
    @FXML private lateinit var buttonTxSend: Button

    @FXML private lateinit var areaRxMessage: TextArea
    @FXML private lateinit var buttonRxClear: Button

    private var settingsEnabled: Boolean = false
        set(value) {
            field = value
            textBluetoothName.isDisable = !value
            textBluetoothPin.isDisable = !value
            comboBluetoothPairingMethod.isDisable = !value
            buttonSettingsGet.isDisable = !value
            buttonSettingsSet.isDisable = !value
        }

    private var dht11Enabled: Boolean = false
        set(value) {
            field = value
            textDHT11Temperature.isDisable = !value
            textDHT11Humidity.isDisable = !value
            buttonDHT11Get.isDisable = !value
        }

    private var iosEnabled: Boolean = false
        set(value) {
            field = value
            buttonIOsGet.isDisable = true
        }

    private var pirEnabled: Boolean = false
        set(value) {
            field = value
            buttonPIRGet.isDisable = true
        }

    private var rgbEnabled: Boolean = false
        set(value) {
            field = value
            comboRGBPattern.isDisable = !value
            colorRGB.isDisable = !value
            textRGBDelay.isDisable = !value
            textRGBMin.isDisable = !value
            textRGBMax.isDisable = !value
            textRGBCount.isDisable = !value
            buttonRGBGet.isDisable = !value
            buttonRGBSet.isDisable = !value
        }

    private var ws281xEnabled: Boolean = false
        set(value) {
            field = value
            textWS281xLED.isDisable = !value
            comboWS281xPattern.isDisable = !value
            colorWS281x.isDisable = !value
            textWS281xDelay.isDisable = !value
            textWS281xMin.isDisable = !value
            textWS281xMax.isDisable = !value
            buttonWS281xGet.isDisable = !value
            buttonWS281xSet.isDisable = !value
        }

    private var txEnabled: Boolean = false
        set(value) {
            field = value
            comboTxMessageType.isDisable = !value
            textTxMessageType.isDisable = !value
            textTxCRC.isDisable = !value
            areaTxMessage.isDisable = !value
            buttonTxSend.isDisable = !value
        }

    private var enabled: Boolean = false
        set(value) {
            field = value
            settingsEnabled = value
            dht11Enabled = value
            iosEnabled = value
            pirEnabled = value
            rgbEnabled = value
            ws281xEnabled = value
            txEnabled = value
            areaRxMessage.isDisable = !value
            //buttonRxClear.isDisable = !value
        }

    @FXML
    fun initialize() {
        enabled = false
        comboBluetoothPairingMethod.items.addAll(PairingMethod.values())
        comboBluetoothPairingMethod.selectionModel.select(0)
        comboRGBPattern.items.addAll(RGBPattern.values())
        comboRGBPattern.selectionModel.select(0)
        comboWS281xPattern.items.addAll(WS281xPattern.values())
        comboWS281xPattern.selectionModel.select(0)

        textBluetoothAddress.text = "34:81:F4:1A:4B:29"
        BluetoothCommunicator.register(this)
    }

    @FXML
    fun onConnectionToggle() {
        if (BluetoothCommunicator.isConnected || BluetoothCommunicator.isConnecting) {
            BluetoothCommunicator.disconnect()
            buttonConnect.text = "Connect [Ctrl+C]"
        } else {
            (textBluetoothChannel.text.toIntOrNull() ?: 6)
                    .let { channel -> BluetoothCommunicator.connect(textBluetoothAddress.text, channel) }
                    .takeIf { it }
                    ?.also { buttonConnect.text = "Cancel [Ctrl+C]" }
        }
    }

    @FXML
    fun onBluetoothSettingsGet() {
        settingsEnabled = false
        send(BluetoothMessageKind.SETTINGS)
    }

    @FXML
    fun onBluetoothSettingsSet() {
        send(BluetoothMessageKind.SETTINGS,
                byteArrayOf(comboBluetoothPairingMethod.selectionModel.selectedIndex.toByte())
                        + textBluetoothPin.text.padEnd(6, 0.toChar()).substring(0, 6).toByteArray()
                        + textBluetoothName.text.padEnd(16, 0.toChar()).substring(0, 16).toByteArray())
    }

    @FXML
    fun onIOsGet() {
        iosEnabled = false
        TODO("Not implemented")
    }

    @FXML
    fun onDHT11Get() {
        dht11Enabled = false
        send(BluetoothMessageKind.DHT11)
    }

    @FXML
    fun onPIRGet() {
        pirEnabled = false
        send(BluetoothMessageKind.PIR)
    }

    @FXML
    fun onRGBGet() {
        rgbEnabled = false
        send(BluetoothMessageKind.RGB)
    }

    @FXML
    fun onRGBSet() {
        send(BluetoothMessageKind.RGB, byteArrayOf(
                comboRGBPattern.selectionModel.selectedItem.byte,
                (colorRGB.value.red * 255.0).toByte(),
                (colorRGB.value.green * 255.0).toByte(),
                (colorRGB.value.blue * 255.0).toByte(),
                ((textRGBDelay.text.toIntOrNull() ?: 50) / 256.0).toByte(),
                ((textRGBDelay.text.toIntOrNull() ?: 50) % 256.0).toByte(),
                (textRGBMin.text.toIntOrNull() ?: 0).toByte(),
                (textRGBMax.text.toIntOrNull() ?: 255).toByte(),
                (textRGBCount.text.toIntOrNull() ?: 255).toByte()))
    }

    @FXML
    fun onWS281xGet() {
        val led = textWS281xLED.text.toIntOrNull()
        if (led == null) {
            send(BluetoothMessageKind.WS281x)
        } else {
            ws281xEnabled = false
            send(BluetoothMessageKind.WS281x, byteArrayOf(led.toByte()))
        }
    }

    @FXML
    fun onWS281xSet() {
        val led = textWS281xLED.text.toIntOrNull()
        if (led == null) {
            send(BluetoothMessageKind.WS281x, byteArrayOf(
                    (colorWS281x.value.red * 255.0).toByte(),
                    (colorWS281x.value.green * 255.0).toByte(),
                    (colorWS281x.value.blue * 255.0).toByte()))
        } else {
            send(BluetoothMessageKind.WS281x, byteArrayOf(
                    led.toByte(),
                    comboWS281xPattern.selectionModel.selectedItem.byte,
                    (colorWS281x.value.red * 255.0).toByte(),
                    (colorWS281x.value.green * 255.0).toByte(),
                    (colorWS281x.value.blue * 255.0).toByte(),
                    ((textWS281xDelay.text.toIntOrNull() ?: 50) / 256.0).toByte(),
                    ((textWS281xDelay.text.toIntOrNull() ?: 50) % 256.0).toByte(),
                    (textWS281xMin.text.toIntOrNull() ?: 0).toByte(),
                    (textWS281xMax.text.toIntOrNull() ?: 255).toByte()))
        }
    }

    @FXML
    fun onRxClear() {
        areaRxMessage.text = ""
    }

    @FXML
    fun onTxSend() {
        "0x([0-9A-Fa-f]{1,4})".toRegex()
                .matchEntire(textTxMessageType.text)
                ?.groups
                ?.get(0)
                ?.value
                ?.toIntOrNull()
                ?.let { code -> BluetoothMessageKind.values().find { it.code == code } }
                ?.also { messageType ->
                    BluetoothCommunicator.send(messageType, areaTxMessage.text
                            .replace("[ \t\n\r]".toRegex(), "")
                            .replace("0x([0-9A-Fa-f]{1,4})".toRegex()) {
                                "${it.groups[1]?.value?.toIntOrNull(16)?.toChar() ?: ""}"
                            }
                            .toByteArray())
                }
    }

    override fun onConnecting() {
        super.onConnecting()
        labelStatus.text = "Connecting ..."
        progressBluetooth.progress = -1.0
    }

    override fun onConnect() {
        super.onConnect()
        labelStatus.text = "Connected"
        enabled = true
        progressBluetooth.progress = 0.0
        buttonConnect.text = "Disconnect [Ctrl+D]"
    }

    override fun onDisconnect() {
        super.onDisconnect()
        labelStatus.text = " Not Connected"
        enabled = false
        progressBluetooth.progress = 0.0
        buttonConnect.text = "Connect [Ctrl+C]"
    }

    override fun onMessage(message: ByteArray) {
        super.onMessage(message)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

        var chars = ""
        val sb = StringBuilder()
        //sb.append("ADDR: 0x00 0x01 0x02 0x03 0x04 0x05 0x06 0x07  ASCII\n\n")

        message.forEachIndexed { i, b ->
            if (i % 8 == 0) sb.append("%04x: ".format(i))
            chars += if (b in 32..126) b.toChar() else '.'
            sb.append("0x%02X%s".format(b, if (b > 255) "!" else " "))
            if (i % 8 == 7) {
                sb.append(" ${chars} |\n")
                chars = ""
            }
        }

        ((message.size % 8) until 8).forEach { i ->
            if (i % 8 == 0) sb.append("%04x: ".format(i))
            sb.append("     ")
            if (i % 8 == 7) sb.append("          |\n")
        }

        areaRxMessage.text += "\n\n${message.size} bytes received at: ${timestamp}\n${sb}"
        //areaRxMessage.scrollTop = Double.MAX_VALUE
        areaRxMessage.selectPositionCaret(areaRxMessage.length)
        areaRxMessage.deselect()

        when (message[1]) {
            BluetoothMessageKind.SETTINGS.byteCode -> {
                if (message.size == 25) {
                    PairingMethod.values().find { it.byte == message[2] }?.also { comboBluetoothPairingMethod.selectionModel.select(it) }
                    textBluetoothPin.text = message.toList().subList(3, 9).toByteArray().toString(Charset.defaultCharset())
                    textBluetoothName.text = message.toList().subList(9, 25).toByteArray().toString(Charset.defaultCharset())
                    settingsEnabled = true
                }
            }
            BluetoothMessageKind.RGB.byteCode -> {
                if (message.size == 11) {
                    RGBPattern.values().find { it.byte == message[2] }?.also { comboRGBPattern.selectionModel.select(it) }
                    colorRGB.value = Color.rgb(message[3].toUInt(), message[4].toUInt(), message[5].toUInt())
                    textRGBDelay.text = "${(message[6].toUInt() * 256) + message[7].toUInt()}"
                    textRGBMin.text = "${message[8].toUInt()}"
                    textRGBMax.text = "${message[9].toUInt()}"
                    textRGBCount.text = "${message[10].toUInt()}"
                    rgbEnabled = true
                }
            }
            BluetoothMessageKind.WS281x.byteCode -> {
                if (message.size == 11) {
                    textWS281xLED.text = "${message[2].toUInt()}"
                    WS281xPattern.values().find { it.byte == message[3] }?.also { comboWS281xPattern.selectionModel.select(it) }
                    colorWS281x.value = Color.rgb(message[4].toUInt(), message[5].toUInt(), message[6].toUInt())
                    textWS281xDelay.text = "${(message[7].toUInt() * 256) + message[8].toUInt()}"
                    textWS281xMin.text = "${message[9].toUInt()}"
                    textWS281xMax.text = "${message[10].toUInt()}"
                    ws281xEnabled = true
                }
            }
        }
    }

    override fun onMessageSent(remaining: Int) {
        super.onMessageSent(remaining)
        if (remaining == 0) {
            progressBluetooth.progress = 0.0
            txEnabled = true
        }
    }

    private fun send(messageKind: BluetoothMessageKind, data: ByteArray = byteArrayOf()) {
        textTxMessageType.text = "0x%02X".format(messageKind.code)
        areaTxMessage.text = data.joinToString(" ") { "0x%02X".format(it) }
        BluetoothCommunicator.send(messageKind, data)
        progressBluetooth.progress = -1.0
        txEnabled = false
    }

    private fun Byte.toUInt() = toUByte().toInt()
}
