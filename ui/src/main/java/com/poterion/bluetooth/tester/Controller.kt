package com.poterion.bluetooth.tester

import com.poterion.monitor.api.data.PairingMethod
import com.poterion.monitor.api.data.RGBPattern
import com.poterion.monitor.api.data.WS281xPattern
import com.poterion.monitor.api.communication.*
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
import javafx.scene.shape.Circle
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.ColorPicker
import javafx.scene.control.ProgressBar
import javafx.scene.input.MouseEvent
import kotlin.experimental.and
import kotlin.math.min


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

    @FXML private lateinit var checkboxLCDBacklight: CheckBox
    @FXML private lateinit var textLCDLine: TextField
    @FXML private lateinit var areaLCD: TextArea
    @FXML private lateinit var buttonLCDGet: Button
    @FXML private lateinit var buttonLCDSet: Button
    @FXML private lateinit var buttonLCDClear: Button
    @FXML private lateinit var buttonLCDReset: Button


    @FXML private lateinit var textMCP23017Address: TextField
    @FXML private lateinit var labelMCP23017A00: Label
    @FXML private lateinit var labelMCP23017A01: Label
    @FXML private lateinit var labelMCP23017A02: Label
    @FXML private lateinit var labelMCP23017A03: Label
    @FXML private lateinit var labelMCP23017A04: Label
    @FXML private lateinit var labelMCP23017A05: Label
    @FXML private lateinit var labelMCP23017A06: Label
    @FXML private lateinit var labelMCP23017A07: Label
    @FXML private lateinit var labelMCP23017B00: Label
    @FXML private lateinit var labelMCP23017B01: Label
    @FXML private lateinit var labelMCP23017B02: Label
    @FXML private lateinit var labelMCP23017B03: Label
    @FXML private lateinit var labelMCP23017B04: Label
    @FXML private lateinit var labelMCP23017B05: Label
    @FXML private lateinit var labelMCP23017B06: Label
    @FXML private lateinit var labelMCP23017B07: Label
    @FXML private lateinit var checkboxMCP23017A00: CheckBox
    @FXML private lateinit var checkboxMCP23017A01: CheckBox
    @FXML private lateinit var checkboxMCP23017A02: CheckBox
    @FXML private lateinit var checkboxMCP23017A03: CheckBox
    @FXML private lateinit var checkboxMCP23017A04: CheckBox
    @FXML private lateinit var checkboxMCP23017A05: CheckBox
    @FXML private lateinit var checkboxMCP23017A06: CheckBox
    @FXML private lateinit var checkboxMCP23017A07: CheckBox
    @FXML private lateinit var checkboxMCP23017B00: CheckBox
    @FXML private lateinit var checkboxMCP23017B01: CheckBox
    @FXML private lateinit var checkboxMCP23017B02: CheckBox
    @FXML private lateinit var checkboxMCP23017B03: CheckBox
    @FXML private lateinit var checkboxMCP23017B04: CheckBox
    @FXML private lateinit var checkboxMCP23017B05: CheckBox
    @FXML private lateinit var checkboxMCP23017B06: CheckBox
    @FXML private lateinit var checkboxMCP23017B07: CheckBox
    @FXML private lateinit var buttonMCP23017Get: Button
    @FXML private lateinit var buttonMCP23017Set: Button

    @FXML private lateinit var labelPIR: Label
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
    @FXML private lateinit var circleWS281x00: Circle
    @FXML private lateinit var circleWS281x01: Circle
    @FXML private lateinit var circleWS281x02: Circle
    @FXML private lateinit var circleWS281x03: Circle
    @FXML private lateinit var circleWS281x04: Circle
    @FXML private lateinit var circleWS281x05: Circle
    @FXML private lateinit var circleWS281x06: Circle
    @FXML private lateinit var circleWS281x07: Circle
    @FXML private lateinit var circleWS281x08: Circle
    @FXML private lateinit var circleWS281x09: Circle
    @FXML private lateinit var circleWS281x10: Circle
    @FXML private lateinit var circleWS281x11: Circle
    @FXML private lateinit var circleWS281x12: Circle
    @FXML private lateinit var circleWS281x13: Circle
    @FXML private lateinit var circleWS281x14: Circle
    @FXML private lateinit var circleWS281x15: Circle
    @FXML private lateinit var circleWS281x16: Circle
    @FXML private lateinit var circleWS281x17: Circle
    @FXML private lateinit var circleWS281x18: Circle
    @FXML private lateinit var circleWS281x19: Circle
    @FXML private lateinit var circleWS281x20: Circle
    @FXML private lateinit var circleWS281x21: Circle
    @FXML private lateinit var circleWS281x22: Circle
    @FXML private lateinit var circleWS281x23: Circle
    @FXML private lateinit var circleWS281x24: Circle
    @FXML private lateinit var circleWS281x25: Circle
    @FXML private lateinit var circleWS281x26: Circle
    @FXML private lateinit var circleWS281x27: Circle
    @FXML private lateinit var circleWS281x28: Circle
    @FXML private lateinit var circleWS281x29: Circle
    @FXML private lateinit var circleWS281x30: Circle
    @FXML private lateinit var circleWS281x31: Circle
    @FXML private lateinit var circleWS281x32: Circle
    @FXML private lateinit var circleWS281x33: Circle
    @FXML private lateinit var circleWS281x34: Circle
    @FXML private lateinit var circleWS281x35: Circle
    @FXML private lateinit var circleWS281x36: Circle
    @FXML private lateinit var circleWS281x37: Circle
    @FXML private lateinit var circleWS281x38: Circle
    @FXML private lateinit var circleWS281x39: Circle
    @FXML private lateinit var circleWS281x40: Circle
    @FXML private lateinit var circleWS281x41: Circle
    @FXML private lateinit var circleWS281x42: Circle
    @FXML private lateinit var circleWS281x43: Circle
    @FXML private lateinit var circleWS281x44: Circle
    @FXML private lateinit var circleWS281x45: Circle
    @FXML private lateinit var circleWS281x46: Circle
    @FXML private lateinit var circleWS281x47: Circle
    @FXML private lateinit var circleWS281x48: Circle
    @FXML private lateinit var circleWS281x49: Circle
    @FXML private lateinit var buttonWS281xGet: Button
    @FXML private lateinit var buttonWS281xSet: Button

    @FXML private lateinit var comboTxMessageType: ComboBox<String>
    @FXML private lateinit var textTxMessageType: TextField
    @FXML private lateinit var textTxCRC: TextField
    @FXML private lateinit var areaTxMessage: TextArea
    @FXML private lateinit var buttonTxSend: Button

    @FXML private lateinit var areaRxMessage: TextArea
    @FXML private lateinit var buttonRxClear: Button

    val ws281xPatterns = mutableMapOf<Int, WS281xPattern?>()
    val ws281xColors = mutableMapOf<Int, Color>()
    val ws281xDelays = mutableMapOf<Int, Int>()
    val ws281xMinMax = mutableMapOf<Int, Pair<Int, Int>>()

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

    private var lcdEnabled: Boolean = false
        set(value) {
            field = value
            checkboxLCDBacklight.isDisable = !value
            textLCDLine.isDisable = !value
            areaLCD.isDisable = !value
            buttonLCDGet.isDisable = !value
            buttonLCDSet.isDisable = !value
            buttonLCDClear.isDisable = !value
            buttonLCDReset.isDisable = !value
        }

    private var mcp23017Enabled: Boolean = false
        set(value) {
            field = value
            textMCP23017Address.isDisable = !value
            labelMCP23017A00.isDisable = !value
            labelMCP23017A01.isDisable = !value
            labelMCP23017A02.isDisable = !value
            labelMCP23017A03.isDisable = !value
            labelMCP23017A04.isDisable = !value
            labelMCP23017A05.isDisable = !value
            labelMCP23017A06.isDisable = !value
            labelMCP23017A07.isDisable = !value
            labelMCP23017B00.isDisable = !value
            labelMCP23017B01.isDisable = !value
            labelMCP23017B02.isDisable = !value
            labelMCP23017B03.isDisable = !value
            labelMCP23017B04.isDisable = !value
            labelMCP23017B05.isDisable = !value
            labelMCP23017B06.isDisable = !value
            labelMCP23017B07.isDisable = !value
            checkboxMCP23017A00.isDisable = !value
            checkboxMCP23017A01.isDisable = !value
            checkboxMCP23017A02.isDisable = !value
            checkboxMCP23017A03.isDisable = !value
            checkboxMCP23017A04.isDisable = !value
            checkboxMCP23017A05.isDisable = !value
            checkboxMCP23017A06.isDisable = !value
            checkboxMCP23017A07.isDisable = !value
            checkboxMCP23017B00.isDisable = !value
            checkboxMCP23017B01.isDisable = !value
            checkboxMCP23017B02.isDisable = !value
            checkboxMCP23017B03.isDisable = !value
            checkboxMCP23017B04.isDisable = !value
            checkboxMCP23017B05.isDisable = !value
            checkboxMCP23017B06.isDisable = !value
            checkboxMCP23017B07.isDisable = !value
            buttonMCP23017Get.isDisable = !value
            buttonMCP23017Set.isDisable = !value
        }

    private var pirEnabled: Boolean = false
        set(value) {
            field = value
            buttonPIRGet.isDisable = !value
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
            lcdEnabled = value
            mcp23017Enabled = value
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
        colorRGB.value = Color.BLACK
        colorRGB.customColors.clear()
        colorRGB.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
        comboWS281xPattern.items.addAll(WS281xPattern.values())
        comboWS281xPattern.selectionModel.select(0)
        colorWS281x.value = Color.BLACK
        colorWS281x.customColors.clear()
        colorWS281x.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)

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
        //settingsEnabled = false
        BluetoothMessageKind.SETTINGS.sendGetRequest()
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
        //iosEnabled = false
        TODO("Not implemented")
    }

    @FXML
    fun onDHT11Get() {
        //dht11Enabled = false
        BluetoothMessageKind.DHT11.sendGetRequest()
    }

    @FXML
    fun onLCDGet() {
        //lcdEnabled = false
        BluetoothMessageKind.LCD.sendGetRequest(textLCDLine.text.toIntOrNull())
    }

    @FXML
    fun onLCDSet() {
        areaLCD.text.split("\n")
                .asSequence()
                .filterIndexed { line, _ -> line < 4 }
                .map { it.substring(0, min(it.length, 20))  }
                .map { string -> string.toCharArray().map { it.toInt() }.toTypedArray() }
                .mapIndexed { line, data -> line to data }
                .filter { (line, _) -> line == (textLCDLine.text.toIntOrNull() ?: line) }
                .toList()
                .forEach { (line, data) -> BluetoothMessageKind.LCD.sendConfiguration(line, data.size, *data) }
    }

    @FXML
    fun onLCDBacklight() {
        BluetoothMessageKind.LCD.sendConfiguration(if (checkboxLCDBacklight.isSelected) 0x7E else 0x7D)
    }

    @FXML
    fun onLCDClear() {
        BluetoothMessageKind.LCD.sendConfiguration(0x7B)
    }

    @FXML
    fun onLCDReset() {
        BluetoothMessageKind.LCD.sendConfiguration(0x7C)
    }

    @FXML
    fun onMCP23017Get() {
        //mcp23017Enabled = false
        "0x(\\d+)".toRegex()
                .matchEntire(textMCP23017Address.text)
                ?.takeIf { it.groupValues.size == 2 }
                ?.groupValues
                ?.get(1)
                ?.toInt(16)
                .also { BluetoothMessageKind.MCP23017.sendGetRequest(it ?: 0x27) }
    }

    @FXML
    fun onMCP23017Set() {
        BluetoothMessageKind.MCP23017.sendConfiguration(
                bools2Byte(checkboxMCP23017A07.isSelected,
                        checkboxMCP23017A06.isSelected,
                        checkboxMCP23017A05.isSelected,
                        checkboxMCP23017A04.isSelected,
                        checkboxMCP23017A03.isSelected,
                        checkboxMCP23017A02.isSelected,
                        checkboxMCP23017A01.isSelected,
                        checkboxMCP23017A00.isSelected),
                bools2Byte(checkboxMCP23017B07.isSelected,
                        checkboxMCP23017B06.isSelected,
                        checkboxMCP23017B05.isSelected,
                        checkboxMCP23017B04.isSelected,
                        checkboxMCP23017B03.isSelected,
                        checkboxMCP23017B02.isSelected,
                        checkboxMCP23017B01.isSelected,
                        checkboxMCP23017B00.isSelected))
    }

    @FXML
    fun onPIRGet() {
        //pirEnabled = false
        BluetoothMessageKind.PIR.sendGetRequest()
    }

    @FXML
    fun onRGBGet() {
        //rgbEnabled = false
        BluetoothMessageKind.RGB.sendGetRequest()
    }

    @FXML
    fun onRGBSet() {
        BluetoothMessageKind.RGB.sendConfiguration(
                comboRGBPattern.selectionModel.selectedItem.code,
                colorRGB.value.red * 255.0,
                colorRGB.value.green * 255.0,
                colorRGB.value.blue * 255.0,
                (textRGBDelay.text.toIntOrNull() ?: 50) / 256.0,
                (textRGBDelay.text.toIntOrNull() ?: 50) % 256.0,
                textRGBMin.text.toIntOrNull() ?: 0,
                textRGBMax.text.toIntOrNull() ?: 255,
                textRGBCount.text.toIntOrNull() ?: 255)
    }

    @FXML
    fun onWS281xGet() {
        //ws281xEnabled = false
        val led = textWS281xLED.text.toIntOrNull()
        BluetoothMessageKind.WS281x.sendGetRequest(led)
    }

    @FXML
    fun onWS281xSet() {
        val led = textWS281xLED.text.toIntOrNull()
        if (led == null) {
            BluetoothMessageKind.WS281x.sendConfiguration(
                    colorWS281x.value.red * 255.0,
                    colorWS281x.value.green * 255.0,
                    colorWS281x.value.blue * 255.0)
        } else {
            BluetoothMessageKind.WS281x.sendConfiguration(
                    led,
                    comboWS281xPattern.selectionModel.selectedItem.code,
                    colorWS281x.value.red * 255.0,
                    colorWS281x.value.green * 255.0,
                    colorWS281x.value.blue * 255.0,
                    (textWS281xDelay.text.toIntOrNull() ?: 50) / 256.0,
                    (textWS281xDelay.text.toIntOrNull() ?: 50) % 256.0,
                    textWS281xMin.text.toIntOrNull() ?: 0,
                    textWS281xMax.text.toIntOrNull() ?: 255)
        }
    }

    @FXML
    fun onWS281xLED(event: MouseEvent) = event.source.let { it as? Circle }?.id
            ?.replace("circleWS281x", "")
            ?.toIntOrNull()
            ?.also { led ->
                println(led)
                textWS281xLED.text = "${led}"
                ws281xPatterns[led]?.also { comboWS281xPattern.selectionModel.select(it) }
                colorWS281x.value = ws281xColors[led] ?: Color.BLACK
                textWS281xDelay.text = "${ws281xDelays[led] ?: ""}"
                textWS281xMin.text = "${ws281xMinMax[led]?.first ?: ""}"
                textWS281xMax.text = "${ws281xMinMax[led]?.second ?: ""}"
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
        labelPIR.text = "No update yet."
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

        areaRxMessage.text += "\n${message.size} bytes received at: ${timestamp}\n${sb}"
        //areaRxMessage.scrollTop = Double.MAX_VALUE
        areaRxMessage.selectPositionCaret(areaRxMessage.length)
        areaRxMessage.deselect()

        when (message[1]) {
            BluetoothMessageKind.SETTINGS.byteCode -> {
                if (message.size == 25) {
                    PairingMethod.values()
                            .find { it.byteCode == message[2] }
                            ?.also { comboBluetoothPairingMethod.selectionModel.select(it) }
                    textBluetoothPin.text = message
                            .toList().subList(3, 9)
                            .toByteArray()
                            .toString(Charset.defaultCharset())
                    textBluetoothName.text = message
                            .toList().subList(9, 25)
                            .toByteArray()
                            .toString(Charset.defaultCharset())
                    settingsEnabled = true
                }
            }
            BluetoothMessageKind.DHT11.byteCode -> {
                if (message.size == 4) {
                    textDHT11Temperature.text = "${message[2]}"
                    textDHT11Humidity.text = "${message[3]}"
                    dht11Enabled = true
                }
            }
            BluetoothMessageKind.LCD.byteCode -> {
                if (message.size > 4) {
                    val backlight = message[2].toUInt() > 0
                    val line = message[3].toUInt()
                    val columns = message[4].toUInt()
                    val string = (0 until columns)
                            .map { message[it + 5] }
                            .toByteArray()
                            .toString(Charset.defaultCharset())
                    val content = areaLCD.text
                            .split("\n".toRegex())
                            .mapIndexed { l, c -> l to c }
                            .toMap()
                            .toMutableMap()
                    content[line] = string
                    checkboxLCDBacklight.isIndeterminate = false
                    checkboxLCDBacklight.isSelected = backlight
                    areaLCD.text = (0 until 4).joinToString("\n") { content[it] ?: "" }
                    lcdEnabled = true
                }
            }
            BluetoothMessageKind.MCP23017.byteCode -> {
                if (message.size == 5) {
                    checkboxMCP23017A00.isIndeterminate = false
                    checkboxMCP23017A01.isIndeterminate = false
                    checkboxMCP23017A02.isIndeterminate = false
                    checkboxMCP23017A03.isIndeterminate = false
                    checkboxMCP23017A04.isIndeterminate = false
                    checkboxMCP23017A05.isIndeterminate = false
                    checkboxMCP23017A06.isIndeterminate = false
                    checkboxMCP23017A07.isIndeterminate = false
                    checkboxMCP23017B00.isIndeterminate = false
                    checkboxMCP23017B01.isIndeterminate = false
                    checkboxMCP23017B02.isIndeterminate = false
                    checkboxMCP23017B03.isIndeterminate = false
                    checkboxMCP23017B04.isIndeterminate = false
                    checkboxMCP23017B05.isIndeterminate = false
                    checkboxMCP23017B06.isIndeterminate = false
                    checkboxMCP23017B07.isIndeterminate = false

                    checkboxMCP23017A00.isSelected = (message[3] and 0b00000001.toByte()) > 0
                    checkboxMCP23017A01.isSelected = (message[3] and 0b00000010.toByte()) > 0
                    checkboxMCP23017A02.isSelected = (message[3] and 0b00000100.toByte()) > 0
                    checkboxMCP23017A03.isSelected = (message[3] and 0b00001000.toByte()) > 0
                    checkboxMCP23017A04.isSelected = (message[3] and 0b00010000.toByte()) > 0
                    checkboxMCP23017A05.isSelected = (message[3] and 0b00100000.toByte()) > 0
                    checkboxMCP23017A06.isSelected = (message[3] and 0b01000000.toByte()) > 0
                    checkboxMCP23017A07.isSelected = (message[3] and 0b10000000.toByte()) > 0
                    checkboxMCP23017B00.isSelected = (message[4] and 0b00000001.toByte()) > 0
                    checkboxMCP23017B01.isSelected = (message[4] and 0b00000010.toByte()) > 0
                    checkboxMCP23017B02.isSelected = (message[4] and 0b00000100.toByte()) > 0
                    checkboxMCP23017B03.isSelected = (message[4] and 0b00001000.toByte()) > 0
                    checkboxMCP23017B04.isSelected = (message[4] and 0b00010000.toByte()) > 0
                    checkboxMCP23017B05.isSelected = (message[4] and 0b00100000.toByte()) > 0
                    checkboxMCP23017B06.isSelected = (message[4] and 0b01000000.toByte()) > 0
                    checkboxMCP23017B07.isSelected = (message[4] and 0b10000000.toByte()) > 0

                    labelMCP23017A00.text = if ((message[3] and 0b00000001.toByte()) > 0) "1" else "0"
                    labelMCP23017A01.text = if ((message[3] and 0b00000010.toByte()) > 0) "1" else "0"
                    labelMCP23017A02.text = if ((message[3] and 0b00000100.toByte()) > 0) "1" else "0"
                    labelMCP23017A03.text = if ((message[3] and 0b00001000.toByte()) > 0) "1" else "0"
                    labelMCP23017A04.text = if ((message[3] and 0b00010000.toByte()) > 0) "1" else "0"
                    labelMCP23017A05.text = if ((message[3] and 0b00100000.toByte()) > 0) "1" else "0"
                    labelMCP23017A06.text = if ((message[3] and 0b01000000.toByte()) > 0) "1" else "0"
                    labelMCP23017A07.text = if ((message[3] and 0b10000000.toByte()) > 0) "1" else "0"
                    labelMCP23017B00.text = if ((message[4] and 0b00000001.toByte()) > 0) "1" else "0"
                    labelMCP23017B01.text = if ((message[4] and 0b00000010.toByte()) > 0) "1" else "0"
                    labelMCP23017B02.text = if ((message[4] and 0b00000100.toByte()) > 0) "1" else "0"
                    labelMCP23017B03.text = if ((message[4] and 0b00001000.toByte()) > 0) "1" else "0"
                    labelMCP23017B04.text = if ((message[4] and 0b00010000.toByte()) > 0) "1" else "0"
                    labelMCP23017B05.text = if ((message[4] and 0b00100000.toByte()) > 0) "1" else "0"
                    labelMCP23017B06.text = if ((message[4] and 0b01000000.toByte()) > 0) "1" else "0"
                    labelMCP23017B07.text = if ((message[4] and 0b10000000.toByte()) > 0) "1" else "0"
                    mcp23017Enabled = true
                }
            }
            BluetoothMessageKind.PIR.byteCode -> {
                if (message.size == 3) {
                    labelPIR.text = if (message[2] > 0) "Motion detected!" else "Nothing is moving."
                }
            }
            BluetoothMessageKind.RGB.byteCode -> {
                if (message.size == 11) {
                    RGBPattern.values().find { it.byteCode == message[2] }?.also { comboRGBPattern.selectionModel.select(it) }
                    colorRGB.value = Color.rgb(message[3].toUInt(), message[4].toUInt(), message[5].toUInt())
                    textRGBDelay.text = "${(message[6].toUInt() * 256) + message[7].toUInt()}"
                    textRGBMin.text = "${message[8].toUInt()}"
                    textRGBMax.text = "${message[9].toUInt()}"
                    textRGBCount.text = "${message[10].toUInt()}"
                    rgbEnabled = true
                }
            }
            BluetoothMessageKind.WS281x.byteCode -> {
                if (message.size == 12) {
                    val count = message[2].toUInt()
                    val led = message[3].toUInt()
                    ws281xPatterns[led] = WS281xPattern.values().find { it.byteCode == message[4] }
                    ws281xColors[led] = Color.rgb(message[5].toUInt(), message[6].toUInt(), message[7].toUInt())
                    ws281xDelays[led] = (message[8].toUInt() * 256) + message[9].toUInt()
                    ws281xMinMax[led] = message[10].toUInt() to message[11].toUInt()

                    textWS281xLED.text = "${led}"
                    ws281xPatterns[led]?.also { comboWS281xPattern.selectionModel.select(it) }
                    colorWS281x.value = ws281xColors[led] ?: Color.BLACK
                    textWS281xDelay.text = "${ws281xDelays[led] ?: ""}"
                    textWS281xMin.text = "${ws281xMinMax[led]?.first ?: ""}"
                    textWS281xMax.text = "${ws281xMinMax[led]?.second ?: ""}"

                    this::class.java.declaredFields
                            .find { it.name == "circleWS281x%02d".format(led) }
                            ?.get(this)
                            ?.let { it as? Circle }
                            ?.fill = ws281xColors[led]

                    ws281xEnabled = true
                }
            }
        }
        enabled = true
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
        textTxCRC.text = "0x%02X".format((byteArrayOf(messageKind.byteCode) + data).calculateChecksum())
        areaTxMessage.text = data.joinToString(" ") { "0x%02X".format(it) }
        BluetoothCommunicator.send(messageKind, data)
        progressBluetooth.progress = -1.0
        txEnabled = false
    }

    private fun Byte.toUInt() = toUByte().toInt()
}
