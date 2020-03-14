/******************************************************************************
 * Copyright (C) 2020 Jan Kubovy (jan@kubovy.eu)                              *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/
package com.poterion.bluetooth.tester.controllers

import com.poterion.bluetooth.tester.*
import com.poterion.bluetooth.tester.data.DeviceConfiguration
import com.poterion.communication.serial.MessageKind
import com.poterion.communication.serial.bools2Byte
import com.poterion.communication.serial.byte2Bools
import com.poterion.communication.serial.communicator.BluetoothCommunicator
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.USBCommunicator
import com.poterion.communication.serial.extensions.*
import com.poterion.communication.serial.listeners.*
import com.poterion.communication.serial.payload.*
import com.poterion.communication.serial.scanner.BluetoothScanner
import com.poterion.communication.serial.scanner.ScannerListener
import com.poterion.communication.serial.scanner.USBScanner
import com.poterion.utils.kotlin.noop
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.StringConverter
import jssc.SerialPortList
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

/**
 * Bluetooth tester's controller.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class DeviceController : CommunicatorListener, BluetoothCommunicatorListener, DataCommunicatorListener,
        IOCommunicatorListener, LcdCommunicatorListener, RegistryCommunicatorListener, RgbIndicatorCommunicatorListener,
        RgbLightCommunicatorListener, RgbStripCommunicatorListener, TempCommunicatorListener {

    companion object {
        internal fun getRoot(primaryStage: Stage,
                             configController: ConfigController,
                             deviceConfiguration: DeviceConfiguration): Pair<Parent, DeviceController> =
                FXMLLoader(DeviceController::class.java.getResource("device.fxml"))
                        .let { it.load<Parent>() to it.getController<DeviceController>() }
                        .also { (_, controller) ->
                            controller.primaryStage = primaryStage
                            controller.configController = configController
                            controller.deviceConfiguration = deviceConfiguration
                        }
                        .also { (_, controller) -> controller.startup() }
    }

    @FXML private lateinit var connection: ToggleGroup

    @FXML private lateinit var radioUSB: RadioButton
    @FXML private lateinit var choiceUSBPort: ChoiceBox<Pair<String, Boolean>>
    @FXML private lateinit var labelUSBStatus: Label
    @FXML private lateinit var progressUSB: ProgressBar
    @FXML private lateinit var buttonUSBConnect: Button

    @FXML private lateinit var radioBluetooth: RadioButton
    @FXML private lateinit var comboBluetoothAddress: ComboBox<Pair<String, String>>
    @FXML private lateinit var textBluetoothChannel: TextField
    @FXML private lateinit var labelBluetoothStatus: Label
    @FXML private lateinit var progressBluetooth: ProgressBar
    @FXML private lateinit var buttonBluetoothScan: Button
    @FXML private lateinit var buttonBluetoothConnect: Button

    @FXML private lateinit var checkboxOnDemand: CheckBox

    @FXML private lateinit var textBluetoothName: TextField
    @FXML private lateinit var textBluetoothPin: TextField
    @FXML private lateinit var comboBluetoothPairingMethod: ComboBox<BluetoothPairingMode>
    @FXML private lateinit var buttonSettingsGet: Button
    @FXML private lateinit var buttonSettingsSet: Button

    @FXML private lateinit var textDataPart: TextField
    @FXML private lateinit var textDataBlockSize: TextField
    @FXML private lateinit var buttonDataDownload: Button
    @FXML private lateinit var buttonDataUpload: Button

    @FXML private lateinit var comboTempIndex: ComboBox<String>
    @FXML private lateinit var textTemperature: TextField
    @FXML private lateinit var textHumidity: TextField
    @FXML private lateinit var buttonTemperatureGet: Button

    @FXML private lateinit var comboLcdIndex: ComboBox<String>
    @FXML private lateinit var checkboxLcdBacklight: CheckBox
    @FXML private lateinit var textLcdLine: TextField
    @FXML private lateinit var areaLcd: TextArea
    @FXML private lateinit var buttonLcdGet: Button
    @FXML private lateinit var buttonLcdSet: Button
    @FXML private lateinit var buttonLcdClear: Button
    @FXML private lateinit var buttonLcdReset: Button

    @FXML private lateinit var textRegistryAddress: TextField
    @FXML private lateinit var textRegistryRegistry: TextField
    @FXML private lateinit var labelRegistry00: Label
    @FXML private lateinit var labelRegistry01: Label
    @FXML private lateinit var labelRegistry02: Label
    @FXML private lateinit var labelRegistry03: Label
    @FXML private lateinit var labelRegistry04: Label
    @FXML private lateinit var labelRegistry05: Label
    @FXML private lateinit var labelRegistry06: Label
    @FXML private lateinit var labelRegistry07: Label
    @FXML private lateinit var checkboxRegistry00: CheckBox
    @FXML private lateinit var checkboxRegistry01: CheckBox
    @FXML private lateinit var checkboxRegistry02: CheckBox
    @FXML private lateinit var checkboxRegistry03: CheckBox
    @FXML private lateinit var checkboxRegistry04: CheckBox
    @FXML private lateinit var checkboxRegistry05: CheckBox
    @FXML private lateinit var checkboxRegistry06: CheckBox
    @FXML private lateinit var checkboxRegistry07: CheckBox
    @FXML private lateinit var buttonRegistryGet: Button
    @FXML private lateinit var buttonRegistrySet: Button

    @FXML private lateinit var checkboxGpio00: CheckBox
    @FXML private lateinit var checkboxGpio01: CheckBox
    @FXML private lateinit var checkboxGpio02: CheckBox
    @FXML private lateinit var checkboxGpio03: CheckBox
    @FXML private lateinit var checkboxGpio04: CheckBox
    @FXML private lateinit var checkboxGpio05: CheckBox
    @FXML private lateinit var checkboxGpio06: CheckBox
    @FXML private lateinit var checkboxGpio07: CheckBox
    @FXML private lateinit var checkboxGpio08: CheckBox
    @FXML private lateinit var checkboxGpio09: CheckBox
    @FXML private lateinit var checkboxGpio10: CheckBox
    @FXML private lateinit var checkboxGpio11: CheckBox
    @FXML private lateinit var checkboxGpio12: CheckBox
    @FXML private lateinit var checkboxGpio13: CheckBox
    @FXML private lateinit var checkboxGpio14: CheckBox
    @FXML private lateinit var checkboxGpio15: CheckBox
    @FXML private lateinit var checkboxGpio16: CheckBox
    @FXML private lateinit var checkboxGpio17: CheckBox
    @FXML private lateinit var checkboxGpio18: CheckBox
    @FXML private lateinit var checkboxGpio19: CheckBox

    @FXML private lateinit var buttonGpioGet00: Button
    @FXML private lateinit var buttonGpioGet01: Button
    @FXML private lateinit var buttonGpioGet02: Button
    @FXML private lateinit var buttonGpioGet03: Button
    @FXML private lateinit var buttonGpioGet04: Button
    @FXML private lateinit var buttonGpioGet05: Button
    @FXML private lateinit var buttonGpioGet06: Button
    @FXML private lateinit var buttonGpioGet07: Button
    @FXML private lateinit var buttonGpioGet08: Button
    @FXML private lateinit var buttonGpioGet09: Button
    @FXML private lateinit var buttonGpioGet10: Button
    @FXML private lateinit var buttonGpioGet11: Button
    @FXML private lateinit var buttonGpioGet12: Button
    @FXML private lateinit var buttonGpioGet13: Button
    @FXML private lateinit var buttonGpioGet14: Button
    @FXML private lateinit var buttonGpioGet15: Button
    @FXML private lateinit var buttonGpioGet16: Button
    @FXML private lateinit var buttonGpioGet17: Button
    @FXML private lateinit var buttonGpioGet18: Button
    @FXML private lateinit var buttonGpioGet19: Button

    @FXML private lateinit var comboRgbIndex: ComboBox<String>
    @FXML private lateinit var comboRgbItem: ComboBox<String>
    @FXML private lateinit var textRgbListSize: TextField
    @FXML private lateinit var comboRgbPattern: ComboBox<RgbPattern>
    @FXML private lateinit var colorRgb: ColorPicker
    @FXML private lateinit var textRgbDelay: TextField
    @FXML private lateinit var textRgbMin: TextField
    @FXML private lateinit var textRgbMax: TextField
    @FXML private lateinit var textRgbTimeout: TextField
    @FXML private lateinit var radioRgbColorOrderRGB: RadioButton
    @FXML private lateinit var radioRgbColorOrderGRB: RadioButton
    @FXML private lateinit var buttonRgbGet: Button
    @FXML private lateinit var buttonRgbAdd: Button
    @FXML private lateinit var buttonRgbSet: Button

    @FXML private lateinit var comboIndicatorsIndex: ComboBox<String>
    @FXML private lateinit var textIndicatorsLed: TextField
    @FXML private lateinit var comboIndicatorsPattern: ComboBox<RgbPattern>
    @FXML private lateinit var colorIndicators: ColorPicker
    @FXML private lateinit var textIndicatorsDelay: TextField
    @FXML private lateinit var textIndicatorsMin: TextField
    @FXML private lateinit var textIndicatorsMax: TextField
    @FXML private lateinit var radioIndicatorsColorOrderRGB: RadioButton
    @FXML private lateinit var radioIndicatorsColorOrderGRB: RadioButton
    @FXML private lateinit var circleIndicators00: Circle
    @FXML private lateinit var circleIndicators01: Circle
    @FXML private lateinit var circleIndicators02: Circle
    @FXML private lateinit var circleIndicators03: Circle
    @FXML private lateinit var circleIndicators04: Circle
    @FXML private lateinit var circleIndicators05: Circle
    @FXML private lateinit var circleIndicators06: Circle
    @FXML private lateinit var circleIndicators07: Circle
    @FXML private lateinit var circleIndicators08: Circle
    @FXML private lateinit var circleIndicators09: Circle
    @FXML private lateinit var circleIndicators10: Circle
    @FXML private lateinit var circleIndicators11: Circle
    @FXML private lateinit var circleIndicators12: Circle
    @FXML private lateinit var circleIndicators13: Circle
    @FXML private lateinit var circleIndicators14: Circle
    @FXML private lateinit var circleIndicators15: Circle
    @FXML private lateinit var circleIndicators16: Circle
    @FXML private lateinit var circleIndicators17: Circle
    @FXML private lateinit var circleIndicators18: Circle
    @FXML private lateinit var circleIndicators19: Circle
    @FXML private lateinit var circleIndicators20: Circle
    @FXML private lateinit var circleIndicators21: Circle
    @FXML private lateinit var circleIndicators22: Circle
    @FXML private lateinit var circleIndicators23: Circle
    @FXML private lateinit var circleIndicators24: Circle
    @FXML private lateinit var circleIndicators25: Circle
    @FXML private lateinit var circleIndicators26: Circle
    @FXML private lateinit var circleIndicators27: Circle
    @FXML private lateinit var circleIndicators28: Circle
    @FXML private lateinit var circleIndicators29: Circle
    @FXML private lateinit var circleIndicators30: Circle
    @FXML private lateinit var circleIndicators31: Circle
    @FXML private lateinit var circleIndicators32: Circle
    @FXML private lateinit var circleIndicators33: Circle
    @FXML private lateinit var circleIndicators34: Circle
    @FXML private lateinit var circleIndicators35: Circle
    @FXML private lateinit var circleIndicators36: Circle
    @FXML private lateinit var circleIndicators37: Circle
    @FXML private lateinit var circleIndicators38: Circle
    @FXML private lateinit var circleIndicators39: Circle
    @FXML private lateinit var circleIndicators40: Circle
    @FXML private lateinit var circleIndicators41: Circle
    @FXML private lateinit var circleIndicators42: Circle
    @FXML private lateinit var circleIndicators43: Circle
    @FXML private lateinit var circleIndicators44: Circle
    @FXML private lateinit var circleIndicators45: Circle
    @FXML private lateinit var circleIndicators46: Circle
    @FXML private lateinit var circleIndicators47: Circle
    @FXML private lateinit var circleIndicators48: Circle
    @FXML private lateinit var circleIndicators49: Circle
    @FXML private lateinit var buttonIndicatorsGet: Button
    @FXML private lateinit var buttonIndicatorsSet: Button

    @FXML private lateinit var comboLightIndex: ComboBox<String>
    @FXML private lateinit var comboLightItem: ComboBox<String>
    @FXML private lateinit var textLightListSize: TextField
    @FXML private lateinit var choiceLightRainbow: ChoiceBox<String>
    @FXML private lateinit var comboLightPattern: ComboBox<RgbLightPattern>
    @FXML private lateinit var colorLight1: ColorPicker
    @FXML private lateinit var colorLight2: ColorPicker
    @FXML private lateinit var colorLight3: ColorPicker
    @FXML private lateinit var colorLight4: ColorPicker
    @FXML private lateinit var colorLight5: ColorPicker
    @FXML private lateinit var colorLight6: ColorPicker
    @FXML private lateinit var colorLight7: ColorPicker
    @FXML private lateinit var textLightDelay: TextField
    @FXML private lateinit var textLightWidth: TextField
    @FXML private lateinit var textLightFading: TextField
    @FXML private lateinit var textLightMin: TextField
    @FXML private lateinit var textLightMax: TextField
    @FXML private lateinit var textLightTimeout: TextField
    @FXML private lateinit var radioLightColorOrderRGB: RadioButton
    @FXML private lateinit var radioLightColorOrderGRB: RadioButton
    @FXML private lateinit var buttonLightGet: Button
    @FXML private lateinit var buttonLightAdd: Button
    @FXML private lateinit var buttonLightSet: Button

    @FXML private lateinit var textTxMessageType: TextField
    @FXML private lateinit var textTxCRC: TextField
    @FXML private lateinit var areaTxMessage: TextArea
    @FXML private lateinit var areaRxMessage: TextArea

    private lateinit var primaryStage: Stage
    private lateinit var configController: ConfigController
    private lateinit var deviceConfiguration: DeviceConfiguration

    private val selectedChannel: Channel?
        get() = when {
            radioUSB.isSelected -> Channel.USB
            radioBluetooth.isSelected -> Channel.BLUETOOTH
            else -> null
        }

    private val usbPortList: ObservableList<Pair<String, Boolean>> = FXCollections.observableArrayList()
    private val bluetoothDeviceList: ObservableList<Pair<String, String>> = FXCollections.observableArrayList()

    /* USB Communicator */
    private val usbCommunicator = USBCommunicator()
    private val usbBluetoothCommunicator = BluetoothCommunicatorExtension(usbCommunicator)
    private val usbDataCommunicator = DataCommunicatorExtension(usbCommunicator)
    private val usbIoCommunicator = IOCommunicatorExtension(usbCommunicator)
    private val usbLcdCommunicator = LcdCommunicatorExtension(usbCommunicator)
    private val usbRegistryCommunicator = RegistryCommunicatorExtension(usbCommunicator)
    private val usbRgbIndicatorCommunicator = RgbIndicatorCommunicatorExtension(usbCommunicator) {
        if (radioIndicatorsColorOrderGRB.isSelected) ColorOrder.GRB else ColorOrder.RGB
    }
    private val usbRgbLightCommunicator = RgbLightCommunicatorExtension(usbCommunicator) {
        if (radioLightColorOrderGRB.isSelected) ColorOrder.GRB else ColorOrder.RGB
    }
    private val usbRgbStripCommunicator = RgbStripCommunicatorExtension(usbCommunicator) {
        if (radioRgbColorOrderGRB.isSelected) ColorOrder.GRB else ColorOrder.RGB
    }
    private val usbStateMachineCommunicator = StateMachineCommunicatorExtension(usbCommunicator)
    private val usbTemperatureCommunicator = TempCommunicatorExtension(usbCommunicator)

    /* Bluetooth Communicator */
    private val btCommunicator = BluetoothCommunicator()
    private val btBluetoothCommunicator = BluetoothCommunicatorExtension(btCommunicator)
    private val btIoCommunicator = IOCommunicatorExtension(btCommunicator)
    private val btDataCommunicator = DataCommunicatorExtension(btCommunicator)
    private val btLcdCommunicator = LcdCommunicatorExtension(btCommunicator)
    private val btRegistryCommunicator = RegistryCommunicatorExtension(btCommunicator)
    private val btRgbIndicatorCommunicator = RgbIndicatorCommunicatorExtension(btCommunicator) {
        if (radioIndicatorsColorOrderGRB.isSelected) ColorOrder.GRB else ColorOrder.RGB
    }
    private val btRgbLightCommunicator = RgbLightCommunicatorExtension(btCommunicator) {
        if (radioLightColorOrderGRB.isSelected) ColorOrder.GRB else ColorOrder.RGB
    }
    private val btRgbStripCommunicator = RgbStripCommunicatorExtension(btCommunicator) {
        if (radioRgbColorOrderGRB.isSelected) ColorOrder.GRB else ColorOrder.RGB
    }
    private val btStateMachineCommunicator = StateMachineCommunicatorExtension(btCommunicator)
    private val btTemperatureCommunicator = TempCommunicatorExtension(btCommunicator)

    /* Communicator Extension */

    private val bluetoothCommunicator: BluetoothCommunicatorExtension<*>?
        get() = when (selectedChannel) {
            Channel.USB -> usbBluetoothCommunicator
            Channel.BLUETOOTH -> btBluetoothCommunicator
            else -> null
        }

    private val dataCommunicator: DataCommunicatorExtension<*>?
        get() = when (selectedChannel) {
            Channel.USB -> usbDataCommunicator
            Channel.BLUETOOTH -> btDataCommunicator
            else -> null
        }

    private val lcdCommunicator: LcdCommunicatorExtension<*>?
        get() = when (selectedChannel) {
            Channel.USB -> usbLcdCommunicator
            Channel.BLUETOOTH -> btLcdCommunicator
            else -> null
        }

    private val ioCommunicator: IOCommunicatorExtension<*>?
        get() = when (selectedChannel) {
            Channel.USB -> usbIoCommunicator
            Channel.BLUETOOTH -> btIoCommunicator
            else -> null
        }

    private val registryCommunicator: RegistryCommunicatorExtension<*>?
        get() = when (selectedChannel) {
            Channel.USB -> usbRegistryCommunicator
            Channel.BLUETOOTH -> btRegistryCommunicator
            else -> null
        }

    private val rgbIndicatorCommunicator: RgbIndicatorCommunicatorExtension<*>?
        get() = when (selectedChannel) {
            Channel.USB -> usbRgbIndicatorCommunicator
            Channel.BLUETOOTH -> btRgbIndicatorCommunicator
            else -> null
        }

    private val rgbLightCommunicator: RgbLightCommunicatorExtension<*>?
        get() = when (selectedChannel) {
            Channel.USB -> usbRgbLightCommunicator
            Channel.BLUETOOTH -> btRgbLightCommunicator
            else -> null
        }

    private val rgbStripCommunicator: RgbStripCommunicatorExtension<*>?
        get() = when (selectedChannel) {
            Channel.USB -> usbRgbStripCommunicator
            Channel.BLUETOOTH -> btRgbStripCommunicator
            else -> null
        }

    private val stateMachineCommunicator: StateMachineCommunicatorExtension<*>?
        get() = when (selectedChannel) {
            Channel.USB -> usbStateMachineCommunicator
            Channel.BLUETOOTH -> btStateMachineCommunicator
            else -> null
        }

    private val temperatureCommunicator: TempCommunicatorExtension<*>?
        get() = when (selectedChannel) {
            Channel.USB -> usbTemperatureCommunicator
            Channel.BLUETOOTH -> btTemperatureCommunicator
            else -> null
        }

    private val rgbConfigs: MutableMap<Int, MutableMap<Int, RgbStripConfiguration>> = mutableMapOf()
    private val ws281xPatterns: MutableMap<Int, MutableMap<Int, RgbPattern>> = mutableMapOf()
    private val ws281xColors: MutableMap<Int, MutableMap<Int, Color>> = mutableMapOf()
    private val ws281xDelays: MutableMap<Int, MutableMap<Int, Int?>> = mutableMapOf()
    private val ws281xMinMax: MutableMap<Int, MutableMap<Int, Pair<Int?, Int?>>> = mutableMapOf()
    private val ws281xLights: MutableMap<Int, MutableMap<Int, RgbLightConfiguration>> = mutableMapOf()

    private var dataFile: File? = null
    private val dataBuffer = ByteArray(8192)
    private var dataPart = 0
    private var dataChunkSize = 0

    private var usbEnabled: Boolean = true
        set(value) {
            field = value
            radioUSB.isDisable = !value
            choiceUSBPort.isDisable = !value
            buttonUSBConnect.isDisable = !value
        }

    private var bluetoothEnabled: Boolean = true
        set(value) {
            field = value
            radioBluetooth.isDisable = !value
            comboBluetoothAddress.isDisable = !value
            textBluetoothChannel.isDisable = !value
            buttonBluetoothConnect.isDisable = !value
        }

    private var bluetoothSettingsEnabled: Boolean = false
        set(value) {
            field = value
            textBluetoothName.isDisable = !value
            textBluetoothPin.isDisable = !value
            comboBluetoothPairingMethod.isDisable = !value
            buttonSettingsGet.isDisable = !value
            buttonSettingsSet.isDisable = !value
        }

    private var dataEnabled: Boolean = false
        set(value) {
            field = value && radioUSB.isSelected
            textDataPart.isDisable = !value || !radioUSB.isSelected
            textDataBlockSize.isDisable = !value || !radioUSB.isSelected
            buttonDataDownload.isDisable = !value || !radioUSB.isSelected
            buttonDataUpload.isDisable = !value || !radioUSB.isSelected
        }

    private var tempEnabled: Boolean = false
        set(value) {
            field = value
            comboTempIndex.isDisable = !value
            textTemperature.isDisable = !value
            textHumidity.isDisable = !value
            buttonTemperatureGet.isDisable = !value
        }

    private var lcdEnabled: Boolean = false
        set(value) {
            field = value
            comboLcdIndex.isDisable = !value
            checkboxLcdBacklight.isDisable = !value
            textLcdLine.isDisable = !value
            areaLcd.isDisable = !value
            buttonLcdGet.isDisable = !value
            buttonLcdSet.isDisable = !value
            buttonLcdClear.isDisable = !value
            buttonLcdReset.isDisable = !value
        }

    private var registryEnabled: Boolean = false
        set(value) {
            field = value
            textRegistryAddress.isDisable = !value
            textRegistryRegistry.isDisable = !value
            labelRegistry00.isDisable = !value
            labelRegistry01.isDisable = !value
            labelRegistry02.isDisable = !value
            labelRegistry03.isDisable = !value
            labelRegistry04.isDisable = !value
            labelRegistry05.isDisable = !value
            labelRegistry06.isDisable = !value
            labelRegistry07.isDisable = !value
            checkboxRegistry00.isDisable = !value
            checkboxRegistry01.isDisable = !value
            checkboxRegistry02.isDisable = !value
            checkboxRegistry03.isDisable = !value
            checkboxRegistry04.isDisable = !value
            checkboxRegistry05.isDisable = !value
            checkboxRegistry06.isDisable = !value
            checkboxRegistry07.isDisable = !value
            buttonRegistryGet.isDisable = !value
            buttonRegistrySet.isDisable = !value
        }

    private var gpioEnabled: Boolean = false
        set(value) {
            field = value
            checkboxGpio00.isDisable = !value
            checkboxGpio01.isDisable = !value
            checkboxGpio02.isDisable = !value
            checkboxGpio03.isDisable = !value
            checkboxGpio04.isDisable = !value
            checkboxGpio05.isDisable = !value
            checkboxGpio06.isDisable = !value
            checkboxGpio07.isDisable = !value
            checkboxGpio08.isDisable = !value
            checkboxGpio09.isDisable = !value
            checkboxGpio10.isDisable = !value
            checkboxGpio11.isDisable = !value
            checkboxGpio12.isDisable = !value
            checkboxGpio13.isDisable = !value
            checkboxGpio14.isDisable = !value
            checkboxGpio15.isDisable = !value
            checkboxGpio16.isDisable = !value
            checkboxGpio17.isDisable = !value
            checkboxGpio18.isDisable = !value
            checkboxGpio19.isDisable = !value
            buttonGpioGet00.isDisable = !value
            buttonGpioGet01.isDisable = !value
            buttonGpioGet02.isDisable = !value
            buttonGpioGet03.isDisable = !value
            buttonGpioGet04.isDisable = !value
            buttonGpioGet05.isDisable = !value
            buttonGpioGet06.isDisable = !value
            buttonGpioGet07.isDisable = !value
            buttonGpioGet08.isDisable = !value
            buttonGpioGet09.isDisable = !value
            buttonGpioGet10.isDisable = !value
            buttonGpioGet11.isDisable = !value
            buttonGpioGet12.isDisable = !value
            buttonGpioGet13.isDisable = !value
            buttonGpioGet14.isDisable = !value
            buttonGpioGet15.isDisable = !value
            buttonGpioGet16.isDisable = !value
            buttonGpioGet17.isDisable = !value
            buttonGpioGet18.isDisable = !value
            buttonGpioGet19.isDisable = !value
        }

    private var rgbStripEnabled: Boolean = false
        set(value) {
            field = value
            comboRgbIndex.isDisable = !value
            comboRgbItem.isDisable = !value
            textRgbListSize.isDisable = !value
            comboRgbPattern.isDisable = !value
            colorRgb.isDisable = !value
            textRgbDelay.isDisable = !value
            textRgbMin.isDisable = !value
            textRgbMax.isDisable = !value
            textRgbTimeout.isDisable = !value
            radioRgbColorOrderRGB.isDisable = !value
            radioRgbColorOrderGRB.isDisable = !value
            buttonRgbGet.isDisable = !value
            buttonRgbAdd.isDisable = !value
            buttonRgbSet.isDisable = !value
        }

    private var rgbIndicatorsEnabled: Boolean = false
        set(value) {
            field = value
            comboIndicatorsIndex.isDisable = !value
            textIndicatorsLed.isDisable = !value
            comboIndicatorsPattern.isDisable = !value
            colorIndicators.isDisable = !value
            textIndicatorsDelay.isDisable = !value
            textIndicatorsMin.isDisable = !value
            textIndicatorsMax.isDisable = !value
            radioIndicatorsColorOrderRGB.isDisable = !value
            radioIndicatorsColorOrderGRB.isDisable = !value
            buttonIndicatorsGet.isDisable = !value
            buttonIndicatorsSet.isDisable = !value
        }

    private var rgbLightEnabled: Boolean = false
        set(value) {
            field = value
            comboLcdIndex.isDisable = !value
            comboLightItem.isDisable = !value
            textLightListSize.isDisable = !value
            choiceLightRainbow.isDisable = !value
            comboLightPattern.isDisable = !value
            colorLight1.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
            colorLight2.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
            colorLight3.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
            colorLight4.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
            colorLight5.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
            colorLight6.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
            colorLight7.isDisable = !value || choiceLightRainbow.selectionModel.selectedIndex > 0
            textLightDelay.isDisable = !value || comboLightPattern.selectionModel.selectedItem?.delay == null
            textLightWidth.isDisable = !value || comboLightPattern.selectionModel.selectedItem?.width == null
            textLightFading.isDisable = !value || comboLightPattern.selectionModel.selectedItem?.fading == null
            textLightMin.isDisable = !value || comboLightPattern.selectionModel.selectedItem?.min == null
            textLightMax.isDisable = !value || comboLightPattern.selectionModel.selectedItem?.max == null
            textLightTimeout.isDisable = !value || comboLightPattern.selectionModel.selectedItem?.timeout == null
            radioLightColorOrderRGB.isDisable = !value
            radioLightColorOrderGRB.isDisable = !value
            buttonLightGet.isDisable = !value
            buttonLightAdd.isDisable = !value
            buttonLightSet.isDisable = !value
        }

    private var txEnabled: Boolean = false
        set(value) {
            field = value
            textTxMessageType.isDisable = !value
            textTxCRC.isDisable = !value
            areaTxMessage.isDisable = !value
        }

    private var enabled: Boolean = false
        set(value) {
            field = value
            usbEnabled = deviceConfiguration.hasUSB
            bluetoothEnabled = deviceConfiguration.hasBluetooth
            bluetoothSettingsEnabled = value && deviceConfiguration.hasBluetooth
            dataEnabled = usbCommunicator.isConnected
                    && value && deviceConfiguration.hasUSB
                    && deviceConfiguration.hasBluetooth
            tempEnabled = value && deviceConfiguration.hasTemp
            lcdEnabled = value && deviceConfiguration.hasLcd
            registryEnabled = value && deviceConfiguration.hasRegistry
            gpioEnabled = value && deviceConfiguration.hasGpio
            rgbStripEnabled = value && deviceConfiguration.hasRgb
            rgbIndicatorsEnabled = value && deviceConfiguration.hasIndicators
            rgbLightEnabled = value && deviceConfiguration.hasLight
            txEnabled = value
            checkboxOnDemand.isDisable = !value
            areaRxMessage.isDisable = !value
            //buttonRxClear.isDisable = !value
        }

    private val bluetoothScannerListener = object : ScannerListener<BluetoothCommunicator.Descriptor> {
        override fun onAvailableDevicesChanged(channel: Channel, devices: Collection<BluetoothCommunicator.Descriptor>) {
            updateBluetoothDevices(devices)
        }
    }

    private val usbScannerListener = object : ScannerListener<USBCommunicator.Descriptor> {
        override fun onAvailableDevicesChanged(channel: Channel, devices: Collection<USBCommunicator.Descriptor>) {
            updateSerialPorts(devices)
        }
    }

    @FXML
    fun initialize() {
        /* USB */
        choiceUSBPort.items = SortedList(usbPortList, compareBy { it.first })
        choiceUSBPort.converter = object : StringConverter<Pair<String, Boolean>>() {
            override fun toString(pair: Pair<String, Boolean>?): String? = pair
                    ?.let { (port, available) -> "${port}${if (!available) " (disconnected)" else ""}" }

            override fun fromString(string: String?): Pair<String, Boolean>? = string
                    ?.let { "(.*)( \\(disconnected\\))?".toRegex() }
                    ?.matchEntire(string)
                    ?.groupValues
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { it[0] to (it.size <= 1 || it[1] != " (disconnected)") }
        }
        choiceUSBPort.selectionModel.selectedItemProperty().addListener { _, _, value ->
            deviceConfiguration.usbDevice = value?.first
            configController.save()
        }

        /* BLUETOOTH */
        comboBluetoothAddress.items = SortedList(bluetoothDeviceList, compareBy({ it.second }, { it.first }))
        comboBluetoothAddress.converter = object : StringConverter<Pair<String, String>>() {
            override fun toString(pair: Pair<String, String>?): String? = pair
                    ?.let { (address, name) -> "${name} (${address})" }

            override fun fromString(string: String?): Pair<String, String>? {
                return "(.*) \\(([0-9A-Fa-f]+:[0-9A-Fa-f]+:[0-9A-Fa-f]+:[0-9A-Fa-f]+:[0-9A-Fa-f]+:[0-9A-Fa-f]+)\\)"
                        .toRegex()
                        .let { regex -> string?.let { regex.matchEntire(it) } }
                        ?.groupValues
                        ?.takeIf { it.size == 3 }
                        ?.let { it[2] to it[1] }
            }
        }
        comboBluetoothAddress.selectionModel.selectedItemProperty().addListener { _, _, value ->
            deviceConfiguration.bluetoothAddress = value?.first
            configController.save()
        }
        comboBluetoothAddress.focusedProperty().addListener { _, _, focus ->
            if (!focus) {
                deviceConfiguration.bluetoothAddress = comboBluetoothAddress.value?.first
                configController.save()
            }
        }

        textBluetoothChannel.focusedProperty().addListener { _, _, focus ->
            if (!focus) {
                deviceConfiguration.bluetoothChannel = textBluetoothChannel.text.toIntOrNull()
                configController.save()
            }
        }

        textDataPart.focusedProperty().addListener { _, _, focus ->
            if (!focus) {
                deviceConfiguration.dataPart = textDataPart.text.toIntOrNull()
                configController.save()
            }
        }
        textDataBlockSize.focusedProperty().addListener { _, _, focus ->
            if (!focus) {
                deviceConfiguration.dataBlockSize = textDataBlockSize.text.toIntOrNull()
                configController.save()
            }
        }

        comboBluetoothPairingMethod.items.addAll(BluetoothPairingMode.values())
        comboBluetoothPairingMethod.selectionModel.select(0)

        /* LCD */
        textLcdLine.focusedProperty().addListener { _, _, focus ->
            if (!focus) {
                deviceConfiguration.lcdLine = textLcdLine.text.toIntOrNull()
                configController.save()
            }
        }

        /* MCP23017 */
        textRegistryAddress.focusedProperty().addListener { _, _, focus ->
            if (!focus) {
                deviceConfiguration.registryAddress = textRegistryAddress.text.takeUnless { it.isNullOrEmpty() }
                configController.save()
            }
        }

        /* RGB */
        comboRgbPattern.items.addAll(RgbPattern.values())
        comboRgbPattern.selectionModel.selectedItemProperty().addListener { _, _, value ->
            textRgbDelay.isDisable = value.delay == null
            textRgbMin.isDisable = value.min == null
            textRgbMax.isDisable = value.max == null
            textRgbTimeout.isDisable = value.timeout == null

            textRgbDelay.text = ""
            textRgbMin.text = ""
            textRgbMax.text = ""
            textRgbTimeout.text = ""

            value.delay?.also { textRgbDelay.promptText = "${it}" }
            value.min?.also { textRgbMin.promptText = "${it}" }
            value.max?.also { textRgbMax.promptText = "${it}" }
            value.timeout?.also { textRgbTimeout.promptText = "${it}" }
        }
        comboRgbPattern.selectionModel.select(0)
        colorRgb.value = Color.BLACK
        colorRgb.customColors.clear()
        colorRgb.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
        comboRgbIndex.selectionModel.selectedItemProperty().addListener { _, _, _ -> comboRgbItem.select(0) }
        comboRgbItem.selectionModel.selectedItemProperty().addListener { _, _, value ->
            val num = comboRgbIndex.value?.toIntOrNull() ?: 0
            val index = value?.toIntOrNull() ?: 0
            val rgbConfig = rgbConfigs
                    .getOrPut(num) { mutableMapOf() }
                    .getOrPut(index) { RgbStripConfiguration(RgbPattern.OFF, java.awt.Color.BLACK, 0, 0, 255, 1) }
            setRgb(rgbConfig)
        }
        comboRgbItem.valueProperty().addListener { _, _, value ->
            val num = comboRgbIndex.value?.toIntOrNull() ?: 0
            val index = value?.toIntOrNull() ?: 0
            val rgbConfig = rgbConfigs.getOrPut(num) { mutableMapOf() }[index]
            if (rgbConfig != null) setRgb(rgbConfig)
        }

        /* WS281x INDICATORS */
        circleIndicators00.fill = Color.TRANSPARENT
        circleIndicators01.fill = Color.TRANSPARENT
        circleIndicators02.fill = Color.TRANSPARENT
        circleIndicators03.fill = Color.TRANSPARENT
        circleIndicators04.fill = Color.TRANSPARENT
        circleIndicators05.fill = Color.TRANSPARENT
        circleIndicators06.fill = Color.TRANSPARENT
        circleIndicators07.fill = Color.TRANSPARENT
        circleIndicators08.fill = Color.TRANSPARENT
        circleIndicators09.fill = Color.TRANSPARENT
        circleIndicators10.fill = Color.TRANSPARENT
        circleIndicators11.fill = Color.TRANSPARENT
        circleIndicators12.fill = Color.TRANSPARENT
        circleIndicators13.fill = Color.TRANSPARENT
        circleIndicators14.fill = Color.TRANSPARENT
        circleIndicators15.fill = Color.TRANSPARENT
        circleIndicators16.fill = Color.TRANSPARENT
        circleIndicators17.fill = Color.TRANSPARENT
        circleIndicators18.fill = Color.TRANSPARENT
        circleIndicators19.fill = Color.TRANSPARENT
        circleIndicators20.fill = Color.TRANSPARENT
        circleIndicators21.fill = Color.TRANSPARENT
        circleIndicators22.fill = Color.TRANSPARENT
        circleIndicators23.fill = Color.TRANSPARENT
        circleIndicators24.fill = Color.TRANSPARENT
        circleIndicators25.fill = Color.TRANSPARENT
        circleIndicators26.fill = Color.TRANSPARENT
        circleIndicators27.fill = Color.TRANSPARENT
        circleIndicators28.fill = Color.TRANSPARENT
        circleIndicators29.fill = Color.TRANSPARENT
        circleIndicators30.fill = Color.TRANSPARENT
        circleIndicators31.fill = Color.TRANSPARENT
        circleIndicators32.fill = Color.TRANSPARENT
        circleIndicators33.fill = Color.TRANSPARENT
        circleIndicators34.fill = Color.TRANSPARENT
        circleIndicators35.fill = Color.TRANSPARENT
        circleIndicators36.fill = Color.TRANSPARENT
        circleIndicators37.fill = Color.TRANSPARENT
        circleIndicators38.fill = Color.TRANSPARENT
        circleIndicators39.fill = Color.TRANSPARENT
        circleIndicators40.fill = Color.TRANSPARENT
        circleIndicators41.fill = Color.TRANSPARENT
        circleIndicators42.fill = Color.TRANSPARENT
        circleIndicators43.fill = Color.TRANSPARENT
        circleIndicators44.fill = Color.TRANSPARENT
        circleIndicators45.fill = Color.TRANSPARENT
        circleIndicators46.fill = Color.TRANSPARENT
        circleIndicators47.fill = Color.TRANSPARENT
        circleIndicators48.fill = Color.TRANSPARENT
        circleIndicators49.fill = Color.TRANSPARENT
        comboIndicatorsPattern.items.addAll(RgbPattern.values())
        comboIndicatorsPattern.selectionModel.select(0)
        colorIndicators.value = Color.BLACK
        colorIndicators.customColors.clear()
        colorIndicators.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)

        /* WS281x LIGHT */
        choiceLightRainbow.items.addAll("Colors", "Ranbow Row", "Rainbow Row Circle", "Rainbow", "Rainbow Circle")
        choiceLightRainbow.selectionModel.selectedIndexProperty().addListener { _, _, value ->
            colorLight1.isDisable = value.toInt() > 0
            colorLight2.isDisable = value.toInt() > 0
            colorLight3.isDisable = value.toInt() > 0
            colorLight4.isDisable = value.toInt() > 0
            colorLight5.isDisable = value.toInt() > 0
            colorLight6.isDisable = value.toInt() > 0
            colorLight7.isDisable = value.toInt() > 0
        }
        choiceLightRainbow.selectionModel.select(0)
        comboLightPattern.items.addAll(RgbLightPattern.values())
        comboLightPattern.selectionModel.selectedItemProperty().addListener { _, _, value ->
            textLightDelay.isDisable = value.delay == null
            textLightWidth.isDisable = value.width == null
            textLightFading.isDisable = value.fading == null
            textLightMin.isDisable = value.min == null
            textLightMax.isDisable = value.max == null
            textLightTimeout.isDisable = value.timeout == null

            textLightDelay.text = ""
            textLightWidth.text = ""
            textLightFading.text = ""
            textLightMin.text = ""
            textLightMax.text = ""
            textLightTimeout.text = ""

            value.delay?.also { textLightDelay.promptText = "${it}" }
            value.width?.also { textLightWidth.promptText = "${it}" }
            value.fading?.also { textLightFading.promptText = "${it}" }
            value.min?.also { textLightMin.promptText = "${it}" }
            value.max?.also { textLightMax.promptText = "${it}" }
            value.timeout?.also { textLightTimeout.promptText = "${it}" }
        }

        comboLightPattern.selectionModel.select(RgbLightPattern.OFF)
        colorLight1.value = Color.BLACK
        colorLight1.customColors.clear()
        colorLight1.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
        colorLight2.value = Color.BLACK
        colorLight2.customColors.clear()
        colorLight2.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
        colorLight3.value = Color.BLACK
        colorLight3.customColors.clear()
        colorLight3.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
        colorLight4.value = Color.BLACK
        colorLight4.customColors.clear()
        colorLight4.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
        colorLight5.value = Color.BLACK
        colorLight5.customColors.clear()
        colorLight5.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
        colorLight6.value = Color.BLACK
        colorLight6.customColors.clear()
        colorLight6.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
        colorLight7.value = Color.BLACK
        colorLight7.customColors.clear()
        colorLight7.customColors.addAll(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
        comboLightIndex.selectionModel.selectedItemProperty().addListener { _, _, _ -> comboLightItem.select(0) }
        comboLightItem.selectionModel.selectedItemProperty().addListener { _, _, value ->
            val num = comboLightIndex.value?.toIntOrNull() ?: 0
            val index = value?.toIntOrNull() ?: 0
            val lightConfig = ws281xLights
                    .getOrPut(num) { mutableMapOf() }
                    .getOrPut(index) {
                        RgbLightConfiguration(RgbLightPattern.OFF,
                                java.awt.Color.BLACK, java.awt.Color.BLACK, java.awt.Color.BLACK, java.awt.Color.BLACK,
                                java.awt.Color.BLACK, java.awt.Color.BLACK, java.awt.Color.BLACK,
                                100, 3, 0, 0, 255, 1)
                    }
            setLight(lightConfig)
        }
        comboLightItem.valueProperty().addListener { _, _, value ->
            val num = comboLightIndex.value?.toIntOrNull() ?: 0
            val index = value?.toIntOrNull() ?: 0
            val lightConfig = ws281xLights.getOrPut(num) { mutableMapOf() }[index]
            if (lightConfig != null) setLight(lightConfig)
        }
        BluetoothScanner.register(bluetoothScannerListener)
        USBScanner.register(usbScannerListener)

        btCommunicator.register(this)
        usbCommunicator.register(this)
    }

    private fun startup() {
        when (deviceConfiguration.selectedChannel) {
            Channel.USB -> radioUSB.isSelected = true
            Channel.BLUETOOTH -> radioBluetooth.isSelected = true
        }
        radioBluetooth.selectedProperty().addListener { _, _, value ->
            if (value) deviceConfiguration.selectedChannel = Channel.BLUETOOTH
            configController.save()
        }
        radioUSB.selectedProperty().addListener { _, _, value ->
            dataEnabled = usbCommunicator.isConnected && value
            if (value) deviceConfiguration.selectedChannel = Channel.USB
            configController.save()
        }

        updateSerialPorts(SerialPortList.getPortNames().map { USBCommunicator.Descriptor(it) })

        comboBluetoothAddress.value = deviceConfiguration.bluetoothAddress
                ?.let { it to (deviceConfiguration.bluetoothName ?: "") }
        textBluetoothChannel.text = deviceConfiguration.bluetoothChannel?.toString() ?: ""
        textDataPart.text = deviceConfiguration.dataPart?.toString() ?: ""
        textDataBlockSize.text = deviceConfiguration.dataBlockSize?.toString() ?: ""
        textLcdLine.text = deviceConfiguration.lcdLine?.toString() ?: ""
        textRegistryAddress.text = deviceConfiguration.registryAddress ?: ""

        enabled = usbCommunicator.isConnected || btCommunicator.isConnected
    }

    fun shutdown() {
        usbCommunicator.shutdown()
        btCommunicator.shutdown()
    }

    @FXML
    fun onUSBConnectionToggle() {
        if (usbCommunicator.isDisconnected) {
            choiceUSBPort.value
                    ?.takeIf { it.second }
                    ?.let { USBCommunicator.Descriptor(it.first) }
                    ?.let { usbCommunicator.connect(it) }
                    ?.takeIf { it }
                    ?.also { buttonUSBConnect.text = "Cancel" }
        } else {
            usbCommunicator.disconnect()
            buttonUSBConnect.text = "Connect"
        }
    }

    @FXML
    fun onBluetoothConnectionToggle() {
        if (btCommunicator.isDisconnected) {
            val descriptor = BluetoothCommunicator.Descriptor(
                    comboBluetoothAddress.value?.first ?: "",
                    textBluetoothChannel.text.toIntOrNull() ?: 6)

            if (btCommunicator.connect(descriptor)) {
                buttonBluetoothConnect.text = "Cancel"
            }
        } else {
            btCommunicator.disconnect()
            buttonBluetoothConnect.text = "Connect"
        }
    }

    @FXML
    fun onBluetoothScan() {
        //buttonBluetoothScan.isDisable = true
        BluetoothScanner.scan()
    }

    @FXML
    fun onBluetoothSettingsGet() {
        //bluetoothSettingsEnabled = false
        bluetoothCommunicator?.sendBluetoothSettingsRequest()
    }

    @FXML
    fun onBluetoothSettingsSet() {
        deviceConfiguration.bluetoothName = textBluetoothName.text
        configController.save()
        bluetoothCommunicator?.sendBluetoothSettings(comboBluetoothPairingMethod.value, textBluetoothPin.text,
                textBluetoothName.text)
    }

    @FXML
    fun onDataDownload() {
        dataPart = textDataPart.text.toIntOrNull() ?: 0
        dataChunkSize = textDataBlockSize.text.toIntOrNull() ?: 255
        if (dataPart >= 0 && dataChunkSize > 0) {
            dataFile = FileChooser()
                    .apply { title = "Choose file to download the data to ..." }
                    .showSaveDialog(primaryStage)
            if (dataFile != null) {
                dataEnabled = false
                progressUSB.progress = -1.0
                (0 until dataBuffer.size).forEach { dataBuffer[it] = 0xFF.toByte() }
                dataCommunicator?.sendDataRequest(dataPart)
            }
        }
    }

    @FXML
    fun onDataUpload() {
        val fileChooser = FileChooser()
        fileChooser.title = "Choose file to upload to the device ..."
        dataFile = fileChooser.showOpenDialog(primaryStage)
        if (dataFile != null) {
            bluetoothSettingsEnabled = false
            // TODO
        }
    }

    @FXML
    fun onTemperatureGet() {
        //dht11Enabled = false
        val num = comboTempIndex.value?.toIntOrNull() ?: 0
        temperatureCommunicator?.sendTempRequest(num)
    }

    @FXML
    fun onLcdGet() {
        //lcdEnabled = false
        val num = comboLcdIndex.value?.toIntOrNull() ?: 0
        val line = textLcdLine.text.toIntOrNull()
        lcdCommunicator?.sendLcdContentRequest(num, line)
    }

    @FXML
    fun onLcdSet() {
        val num = comboLcdIndex.value?.toIntOrNull() ?: 0
        val backlight = checkboxLcdBacklight.isSelected
        areaLcd.text.split("\n")
                .asSequence()
                .filterIndexed { line, _ -> line < 4 }
                .map { it.substring(0, min(it.length, 20)) }
                .mapIndexed { line, data -> line to data }
                .filter { (line, _) -> line == (textLcdLine.text.toIntOrNull() ?: line) }
                .toList()
                .forEach { (line, data) -> lcdCommunicator?.sendLcdLine(num, line, data, backlight) }
    }

    @FXML
    fun onLcdBacklight() {
        val num = comboLcdIndex.value?.toIntOrNull() ?: 0
        val command = if (checkboxLcdBacklight.isSelected) LcdCommand.BACKLIGHT else LcdCommand.NO_BACKLIGHT
        lcdCommunicator?.sendLcdCommand(num, command)
    }

    @FXML
    fun onLcdClear() {
        val num = comboLcdIndex.value?.toIntOrNull() ?: 0
        lcdCommunicator?.sendLcdCommand(num, LcdCommand.CLEAR)
    }

    @FXML
    fun onLcdReset() {
        val num = comboLcdIndex.value?.toIntOrNull() ?: 0
        lcdCommunicator?.sendLcdCommand(num, LcdCommand.RESET)
    }

    @FXML
    fun onRegistryGet() {
        //mcp23017Enabled = false
        val address = textRegistryAddress.getHexInt()
        val registry = textRegistryAddress.getHexInt()
        if (address != null && registry != null) registryCommunicator?.sendRegistryRequest(address, registry)
    }

    @FXML
    fun onRegistrySet() {
        val address = textRegistryAddress.getHexInt()
        val registry = textRegistryAddress.getHexInt()
        if (address != null && registry != null) registryCommunicator?.sendRegistryWrite(address, registry, bools2Byte(
                checkboxRegistry07.isSelected,
                checkboxRegistry06.isSelected,
                checkboxRegistry05.isSelected,
                checkboxRegistry04.isSelected,
                checkboxRegistry03.isSelected,
                checkboxRegistry02.isSelected,
                checkboxRegistry01.isSelected,
                checkboxRegistry00.isSelected))
    }

    @FXML
    fun onGpioGet(event: ActionEvent) {
        val port = (event.source as? Button)?.id?.let { "buttonGpioGet(\\d+)".toRegex().matchEntire(it) }
                ?.groupValues
                ?.takeIf { it.size == 2 }
                ?.get(1)
                ?.toIntOrNull()
        if (port != null) ioCommunicator?.sendIoRequest(port)
    }

    @FXML
    fun onGpioSet(event: ActionEvent) {
        val checkbox = (event.source as? CheckBox)
        val port = checkbox
                ?.id
                ?.let { "buttonGpioGet(\\d+)".toRegex().matchEntire(it) }
                ?.groupValues
                ?.takeIf { it.size == 2 }
                ?.get(1)
                ?.toIntOrNull()
        if (port != null) {
            checkbox.isIndeterminate = false
            ioCommunicator?.sendIoWrite(port, checkbox.isSelected)
        }
    }

    @FXML
    fun onRgbGet() {
        this::class.java.declaredFields
                .find { it.name == "buttonGpioGet%02d".format(it) }
                ?.get(this)
                ?.let { it as? Circle }
                ?.fill = colorIndicators.value
        //rgbEnabled = false
        val num = comboRgbIndex.value?.toIntOrNull() ?: 0
        val index = comboRgbItem.value?.toIntOrNull()
        rgbStripCommunicator?.sendRgbStripConfigurationRequest(num, index)
    }

    @FXML
    fun onRgbAdd() {
        val num = comboRgbIndex.value?.toIntOrNull() ?: 0
        val pattern = comboRgbPattern.selectionModel.selectedItem
        val color = colorRgb.value.toAwtColor()
        rgbStripCommunicator?.sendRgbStripConfiguration(
                num,
                pattern,
                color,
                textRgbDelay.text.toIntOrNull() ?: pattern.delay ?: 100,
                textRgbMin.text.toIntOrNull() ?: pattern.min ?: 0,
                textRgbMax.text.toIntOrNull() ?: pattern.max ?: 255,
                textRgbTimeout.text.toIntOrNull() ?: pattern.timeout ?: INDEFINED,
                false)
    }

    @FXML
    fun onRgbSet() {
        val num = comboRgbIndex.value?.toIntOrNull() ?: 0
        val pattern = comboRgbPattern.selectionModel.selectedItem
        val color = colorRgb.value.toAwtColor()
        rgbStripCommunicator?.sendRgbStripConfiguration(
                num,
                pattern,
                color,
                textRgbDelay.text.toIntOrNull() ?: pattern.delay ?: 100,
                textRgbMin.text.toIntOrNull() ?: pattern.min ?: 0,
                textRgbMax.text.toIntOrNull() ?: 255,
                textRgbTimeout.text.toIntOrNull() ?: pattern.timeout ?: INDEFINED,
                true)
    }

    @FXML
    fun onIndicatorsGet() {
//		//ws281xEnabled = false
        val num = comboIndicatorsIndex.value?.toIntOrNull() ?: 0
        val leds = textIndicatorsLed.text.toIntOrNull()?.let { listOf(it) } ?: (0 until 50)
        for (led in leds) {
            rgbIndicatorCommunicator?.sendRgbIndicatorLedRequest(num, led)
        }
    }

    @FXML
    fun onIndicatorsSet() {
        val num = comboIndicatorsIndex.value?.toIntOrNull() ?: 0
        val led = textIndicatorsLed.text.toIntOrNull()
        val color = colorIndicators.value.toAwtColor()
        if (led == null) {
            rgbIndicatorCommunicator?.sendRgbIndicatorSetAll(num, color)
            (0 until 50).forEach { i ->
                this::class.java.declaredFields
                        .find { it.name == "circleIndicators%02d".format(i) }
                        ?.get(this)
                        ?.let { it as? Circle }
                        ?.fill = colorIndicators.value
            }
        } else {
            rgbIndicatorCommunicator?.sendRgbIndicatorSet(
                    num,
                    led,
                    comboIndicatorsPattern.value,
                    color,
                    textIndicatorsDelay.text.toIntOrNull() ?: 50,
                    textIndicatorsMin.text.toIntOrNull() ?: 0,
                    textIndicatorsMax.text.toIntOrNull() ?: 255,
                    true)
            this::class.java.declaredFields
                    .find { it.name == "circleIndicators%02d".format(led) }
                    ?.get(this)
                    ?.let { it as? Circle }
                    ?.fill = colorIndicators.value
        }
    }

    @FXML
    fun onIndicatorsClick(event: MouseEvent) = event.source.let { it as? Circle }?.id
            ?.replace("circleIndicators", "")
            ?.toIntOrNull()
            ?.also { led ->
                val num = comboIndicatorsIndex.value?.toIntOrNull() ?: 0
                textIndicatorsLed.text = "${led}"
                val pattern = ws281xPatterns
                        .getOrPut(num) { mutableMapOf() }
                        .getOrPut(led) { RgbPattern.OFF }
                comboIndicatorsPattern.selectionModel.select(pattern)
                colorIndicators.value = ws281xColors
                        .getOrPut(num) { mutableMapOf() }
                        .getOrPut(led) { Color.BLACK }
                textIndicatorsDelay.text = ws281xDelays
                        .getOrPut(num) { mutableMapOf() }
                        .getOrPut(led) { pattern.delay }
                        ?.toString()
                        ?: ""
                textIndicatorsMin.text = ws281xMinMax
                        .getOrPut(num) { mutableMapOf() }
                        .getOrPut(led) { pattern.min to pattern.max }
                        .first
                        ?.toString()
                        ?: ""
                textIndicatorsMax.text = ws281xMinMax
                        .getOrPut(num) { mutableMapOf() }
                        .getOrPut(led) { pattern.min to pattern.max }
                        .second
                        ?.toString()
                        ?: ""
            }

    @FXML
    fun onLightGet() {
        //ws281xLightEnabled = false
        val num = comboLightIndex.value?.toIntOrNull() ?: 0
        val item = comboLightItem.value?.toIntOrNull()
        rgbLightCommunicator?.sendRgbLightItemRequest(num, item)
    }

    @FXML
    fun onLightAdd() {
        val num = comboLightIndex.value?.toIntOrNull() ?: 0
        val pattern = comboLightPattern.selectionModel.selectedItem
        val rainbow = choiceLightRainbow.selectionModel.selectedIndex.takeIf { it > 0 }?.toAwtRainbowColor()
        val color1 = rainbow ?: colorLight1.value.toAwtColor()
        val color2 = rainbow ?: colorLight2.value.toAwtColor()
        val color3 = rainbow ?: colorLight3.value.toAwtColor()
        val color4 = rainbow ?: colorLight4.value.toAwtColor()
        val color5 = rainbow ?: colorLight5.value.toAwtColor()
        val color6 = rainbow ?: colorLight6.value.toAwtColor()
        val color7 = rainbow ?: colorLight7.value.toAwtColor()
        rgbLightCommunicator?.sendRgbLightSet(
                num,
                comboLightPattern.value,
                color1, color2, color3, color4, color5, color6, color7,
                textLightDelay.text.toIntOrNull() ?: pattern.delay ?: 10,
                textLightWidth.text.toIntOrNull() ?: pattern.width ?: 3,
                textLightFading.text.toIntOrNull() ?: pattern.fading ?: 0,
                textLightMin.text.toIntOrNull() ?: pattern.min ?: 0,
                textLightMax.text.toIntOrNull() ?: pattern.max ?: 255,
                textLightTimeout.text.toIntOrNull() ?: pattern.timeout ?: INDEFINED,
                false)
    }

    @FXML
    fun onLightSet() {
        val num = comboLightIndex.value?.toIntOrNull() ?: 0
        val pattern = comboLightPattern.selectionModel.selectedItem
        val rainbow = choiceLightRainbow.selectionModel.selectedIndex.takeIf { it > 0 }?.toAwtRainbowColor()
        val color1 = rainbow ?: colorLight1.value.toAwtColor()
        val color2 = rainbow ?: colorLight2.value.toAwtColor()
        val color3 = rainbow ?: colorLight3.value.toAwtColor()
        val color4 = rainbow ?: colorLight4.value.toAwtColor()
        val color5 = rainbow ?: colorLight5.value.toAwtColor()
        val color6 = rainbow ?: colorLight6.value.toAwtColor()
        val color7 = rainbow ?: colorLight7.value.toAwtColor()
        rgbLightCommunicator?.sendRgbLightSet(
                num,
                pattern,
                color1, color2, color3, color4, color5, color6, color7,
                textLightDelay.text.toIntOrNull() ?: pattern.delay ?: 10,
                textLightWidth.text.toIntOrNull() ?: pattern.width ?: 3,
                textLightFading.text.toIntOrNull() ?: pattern.fading ?: 0,
                textLightMin.text.toIntOrNull() ?: pattern.min ?: 0,
                textLightMax.text.toIntOrNull() ?: pattern.max ?: 255,
                textLightTimeout.text.toIntOrNull() ?: pattern.timeout ?: INDEFINED,
                true)
    }

    @FXML
    fun onTxClear() {
        textTxMessageType.text = ""
        textTxCRC.text = ""
        areaTxMessage.text = ""
    }

    @FXML
    fun onRxClear() {
        areaRxMessage.text = ""
    }

//	@FXML
//	fun onTxSend() {
//		"0x([0-9A-Fa-f]{1,4})".toRegex()
//				.matchEntire(textTxMessageType.text)
//				?.groups
//				?.get(0)
//				?.value
//				?.toIntOrNull()
//				?.let { code -> MessageKind.values().find { it.code == code } }
//				?.also { messageType ->
//					bluetoothCommunicator.send(messageType, areaTxMessage.text
//							.replace("[ \t\n\r]".toRegex(), "")
//							.replace("0x([0-9A-Fa-f]{1,4})".toRegex()) {
//								"${it.groups[1]?.value?.toIntOrNull(16)?.toChar() ?: ""}"
//							}
//							.toByteArray())
//				}
//	}

    override fun onConnecting(channel: Channel) = Platform.runLater {
        when (channel) {
            Channel.USB -> {
                labelUSBStatus.text = "Connecting ..."
                progressUSB.progress = -1.0
            }
            Channel.BLUETOOTH -> {
                labelBluetoothStatus.text = "Connecting ..."
                progressBluetooth.progress = -1.0
            }
        }
    }

    override fun onConnect(channel: Channel) = Platform.runLater {
        when (channel) {
            Channel.USB -> {
                labelUSBStatus.text = "Connected"
                buttonUSBConnect.text = "Disconnect"
                progressUSB.progress = 0.0
            }
            Channel.BLUETOOTH -> {
                labelBluetoothStatus.text = "Connected"
                buttonBluetoothConnect.text = "Disconnect"
                progressBluetooth.progress = 0.0
            }
        }
        enabled = usbCommunicator.isConnected || btCommunicator.isConnected || checkboxOnDemand.isSelected
    }

    override fun onConnectionReady(channel: Channel) = Platform.runLater {
        // nothing
    }

    override fun onDisconnect(channel: Channel) = Platform.runLater {
        when (channel) {
            Channel.USB -> {
                labelUSBStatus.text = "Not Connected"
                buttonUSBConnect.text = "Connect"
                progressUSB.progress = 0.0
            }
            Channel.BLUETOOTH -> {
                labelBluetoothStatus.text = "Not Connected"
                buttonBluetoothConnect.text = "Connect"
                progressBluetooth.progress = 0.0
            }
        }
        //labelPIR.text = "No update yet."
        enabled = usbCommunicator.isConnected || btCommunicator.isConnected || checkboxOnDemand.isSelected
    }

    override fun onMessageReceived(channel: Channel, message: IntArray) = Platform.runLater {
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

        areaRxMessage.text += "\n%s> %s [CRC: 0x%02X]: %s (0x%02X) with %d bytes: \n%s"
                .format(channel,
                        timestamp,
                        message[0],
                        MessageKind.values().find { it.code == message[1] } ?: "",
                        message[1],
                        message.size,
                        sb.toString())
        //areaRxMessage.scrollTop = Double.MAX_VALUE
        areaRxMessage.selectPositionCaret(areaRxMessage.length)
        areaRxMessage.deselect()

        enabled = usbCommunicator.isConnected || btCommunicator.isConnected || checkboxOnDemand.isSelected
    }

    override fun onMessagePrepare(channel: Channel) {
        enshureUsbDescriptor()
        enshureBluetoothDescriptor()
    }

    override fun onMessageSent(channel: Channel, message: IntArray, remaining: Int) = Platform.runLater {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

        var chars = ""
        val sb = StringBuilder()

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

        textTxCRC.text = "0x%02X".format(message[0])
        textTxMessageType.text = if (message.isNotEmpty()) "%s [0x%02X]"
                .format(MessageKind.values().find { it.code == message[1] } ?: "", message[1]) else ""

        areaTxMessage.text += "\n%s> %s [CRC: 0x%02X]: %s (0x%02X) with %d bytes: \n%s"
                .format(channel,
                        timestamp,
                        message[0],
                        MessageKind.values().find { it.code == message[1] } ?: "",
                        message[1],
                        message.size,
                        sb.toString())
        //areaTxMessage.scrollTop = Double.MAX_VALUE
        areaTxMessage.selectPositionCaret(areaTxMessage.length)
        areaTxMessage.deselect()

        if (remaining == 0) {
            when (channel) {
                Channel.USB -> {
                    labelUSBStatus.text = "Connected"
                    progressUSB.progress = 0.0
                }
                Channel.BLUETOOTH -> {
                    labelBluetoothStatus.text = "Connected"
                    progressBluetooth.progress = 0.0
                }
            }
            txEnabled = true
        } else {
            when (channel) {
                Channel.USB -> {
                    labelUSBStatus.text = "Sending ..."
                    progressUSB.progress = -1.0
                }
                Channel.BLUETOOTH -> {
                    labelBluetoothStatus.text = "Sending ..."
                    progressBluetooth.progress = -1.0
                }
            }
        }
    }

    override fun onDeviceCapabilitiesChanged(channel: Channel, capabilities: DeviceCapabilities) = Platform.runLater {
        deviceConfiguration.hasBluetooth = capabilities.hasBluetooth
        deviceConfiguration.hasUSB = capabilities.hasUSB
        deviceConfiguration.hasTemp = capabilities.hasTemp
        deviceConfiguration.hasLcd = capabilities.hasLCD
        deviceConfiguration.hasRegistry = capabilities.hasRegistry
        deviceConfiguration.hasGpio = true //capabilities.hasMotionSensor
        deviceConfiguration.hasRgb = capabilities.hasRgbStrip
        deviceConfiguration.hasIndicators = capabilities.hasRgbIndicators
        deviceConfiguration.hasLight = capabilities.hasRgbLight
        configController.save()
        enabled = enabled // refresh capabilities
    }

    override fun onDeviceNameChanged(channel: Channel, name: String) = Platform.runLater {
        textBluetoothName.text = name
        deviceConfiguration.bluetoothName = name
        configController.save()
    }

    override fun onBluetoothSettingsUpdated(channel: Channel,
                                            pairingMode: BluetoothPairingMode,
                                            pin: String,
                                            name: String) = Platform.runLater {
        //bluetoothSettingsEnabled = true
        comboBluetoothPairingMethod.selectionModel.select(pairingMode)
        textBluetoothPin.text = pin
        textBluetoothName.text = name
        deviceConfiguration.bluetoothName = name
        configController.save()
    }

    override fun onConsistencyCheckReceived(channel: Channel, part: Int, checksum: Int) = Platform.runLater {
    }

    override fun onDataReceived(channel: Channel,
                                part: Int,
                                address: Int,
                                length: Int,
                                data: IntArray) = Platform.runLater {
        if (address + data.size == length) { // Download finished
            dataFile?.outputStream()?.bufferedWriter()?.use { writer ->
                dataBuffer
                        .toList()
                        .chunked(dataChunkSize)
                        .mapIndexed { index, chunk -> index * dataChunkSize to chunk }
                        .toMap()
                        .forEach { (address, data) ->
                            val dataAligned = data.map { it }.toMutableList()
                            while (dataAligned.size < dataChunkSize) dataAligned.add(0xFF.toByte())
                            val line = "    0x%04X: %s | %s"
                                    .format(address,
                                            dataAligned.joinToString("") { "%02X".format(it) },
                                            dataAligned.joinToString("") {
                                                when (it) {
                                                    in 32..126 -> "${it.toChar()}"
                                                    0xFF.toByte() -> "*"
                                                    else -> "."
                                                }
                                            })
                            println("EEPROM> ${line}")
                            writer.write("${line}\n")
                        }
            }

            progressUSB.progress = 0.0
            dataEnabled = true
            dataChunkSize = 0
        } else {
            dataEnabled = false
            if (progressUSB.progress == 0.0) progressUSB.progress = -1.0
            data.indices.forEach { i ->
                dataBuffer[address + i] = data[i].toByte()
            }
        }
    }

    override fun onIoChanged(channel: Channel, port: Int, state: Boolean) = Platform.runLater {
        this::class.java.declaredFields
                .find { it.name == "checkboxGpio%02d".format(port) }
                ?.get(this)
                ?.let { it as? CheckBox }
                ?.isSelected = state
    }

    override fun onLcdCountReceived(channel: Channel, count: Int) = Platform.runLater {
        comboLcdIndex.updateCount(count)
    }

    override fun onLcdCommandReceived(channel: Channel, num: Int, command: LcdCommand) = Platform.runLater {
        comboLcdIndex.select(num)
        when (command) {
            LcdCommand.CLEAR -> areaLcd.text = "\n\n\n"
            LcdCommand.RESET -> noop()
            LcdCommand.BACKLIGHT -> {
                checkboxLcdBacklight.isIndeterminate = false
                checkboxLcdBacklight.isSelected = true
            }
            LcdCommand.NO_BACKLIGHT -> {
                checkboxLcdBacklight.isIndeterminate = false
                checkboxLcdBacklight.isSelected = false
            }
        }
    }

    override fun onLcdContentChanged(channel: Channel,
                                     num: Int,
                                     backlight: Boolean,
                                     line: Int,
                                     content: String) = Platform.runLater {
        // lcdEnabled = true
        comboLcdIndex.select(num)
        val cache = areaLcd.text
                .split("\n".toRegex())
                .mapIndexed { l, c -> l to c }
                .toMap()
                .toMutableMap()
        cache[line] = content
        checkboxLcdBacklight.isIndeterminate = false
        checkboxLcdBacklight.isSelected = backlight
        areaLcd.text = (0 until 4).joinToString("\n") { cache[it] ?: "" }
    }

    override fun onRegistryValue(channel: Channel,
                                 address: Int,
                                 registry: Int,
                                 vararg values: Int) = Platform.runLater {
        //registryEnabled = true
        textRegistryAddress.text = "0x%02X".format(address)
        textRegistryRegistry.text = "0x02X".format(registry)
        checkboxRegistry00.isIndeterminate = false
        checkboxRegistry01.isIndeterminate = false
        checkboxRegistry02.isIndeterminate = false
        checkboxRegistry03.isIndeterminate = false
        checkboxRegistry04.isIndeterminate = false
        checkboxRegistry05.isIndeterminate = false
        checkboxRegistry06.isIndeterminate = false
        checkboxRegistry07.isIndeterminate = false

        byte2Bools(values[0]).also {
            checkboxRegistry00.isSelected = it[0]
            checkboxRegistry01.isSelected = it[1]
            checkboxRegistry02.isSelected = it[2]
            checkboxRegistry03.isSelected = it[3]
            checkboxRegistry04.isSelected = it[4]
            checkboxRegistry05.isSelected = it[5]
            checkboxRegistry06.isSelected = it[6]
            checkboxRegistry07.isSelected = it[7]
            labelRegistry00.text = if (it[0]) "1" else "0"
            labelRegistry01.text = if (it[1]) "1" else "0"
            labelRegistry02.text = if (it[2]) "1" else "0"
            labelRegistry03.text = if (it[3]) "1" else "0"
            labelRegistry04.text = if (it[4]) "1" else "0"
            labelRegistry05.text = if (it[5]) "1" else "0"
            labelRegistry06.text = if (it[6]) "1" else "0"
            labelRegistry07.text = if (it[7]) "1" else "0"
        }
    }

    override fun onRgbIndicatorCountChanged(channel: Channel, count: Int) = Platform.runLater {
        comboIndicatorsIndex.updateCount(count)
    }

    override fun onRgbIndicatorConfiguration(channel: Channel,
                                             num: Int,
                                             count: Int,
                                             index: Int,
                                             configuration: RgbIndicatorConfiguration) = Platform.runLater {
        //rgbIndicatorsEnabled = true
        val color = configuration.color.toColor()
        ws281xPatterns.getOrPut(num) { mutableMapOf() }[index] = configuration.pattern
        ws281xColors.getOrPut(num) { mutableMapOf() }[index] = color
        ws281xDelays.getOrPut(num) { mutableMapOf() }[index] = configuration.delay
        ws281xMinMax.getOrPut(num) { mutableMapOf() }[index] = configuration.minimum to configuration.maximum

        textIndicatorsLed.text = "${index}"
        comboIndicatorsPattern.selectionModel.select(configuration.pattern)
        colorIndicators.value = color
        textIndicatorsDelay.text = "${configuration.delay}"
        textIndicatorsMin.text = "${configuration.minimum}"
        textIndicatorsMax.text = "${configuration.maximum}"

        (0 until 50).forEach { led ->
            this::class.java.declaredFields
                    .find { it.name == "circleWS281x%02d".format(led) }
                    ?.get(this)
                    ?.let { it as? Circle }
                    ?.fill = ws281xColors.getOrPut(num) { mutableMapOf() }.getOrDefault(led, Color.TRANSPARENT)
        }
    }

    override fun onRgbLightCountChanged(channel: Channel, count: Int) = Platform.runLater {
        comboLightIndex.updateCount(count)
    }

    override fun onRgbLightConfiguration(channel: Channel,
                                         num: Int,
                                         count: Int,
                                         index: Int,
                                         configuration: RgbLightConfiguration) = Platform.runLater {
        //rgbLightEnabled = true
        textLightListSize.text = "${count}"
        ws281xLights.getOrPut(num) { mutableMapOf() }[index] = configuration
        comboLightItem.updateCount(count)
        comboLightItem.selectionModel.select("${index}")
    }

    override fun onRgbStripCountChanged(channel: Channel, count: Int) = Platform.runLater {
        comboRgbIndex.updateCount(count)
    }

    override fun onRgbStripConfiguration(channel: Channel,
                                         num: Int,
                                         count: Int,
                                         index: Int,
                                         configuration: RgbStripConfiguration) = Platform.runLater {
        //rgbStripEnabled = true
        textRgbListSize.text = "${count}"
        rgbConfigs.getOrPut(num) { mutableMapOf() }[index] = configuration
        comboRgbItem.updateCount(count)
        comboRgbItem.selectionModel.select("${index}")
    }

    override fun onTempCountReceived(channel: Channel, count: Int) = Platform.runLater {
        comboTempIndex.updateCount(count)
    }

    override fun onTempReceived(channel: Channel, num: Int, temp: Double, humidity: Double) = Platform.runLater {
        // dht11Enabled = true
        comboTempIndex.select(num)
        textTemperature.text = "%.2f".format(temp)
        textHumidity.text = "%.0f".format(humidity)
    }

    private fun setRgb(configuration: RgbStripConfiguration) {
        comboRgbPattern.selectionModel.select(configuration.pattern)
        colorRgb.value = configuration.color.toColor()
        textRgbDelay.text = "${configuration.delay}"
        textRgbMin.text = "${configuration.minimum}"
        textRgbMax.text = "${configuration.maximum}"
        textRgbTimeout.text = "${configuration.timeout}"
    }

    private fun setLight(configuration: RgbLightConfiguration) {
        comboLightPattern.selectionModel.select(configuration.pattern)
        colorLight1.value = configuration.color1.toColor()
        colorLight2.value = configuration.color2.toColor()
        colorLight3.value = configuration.color3.toColor()
        colorLight4.value = configuration.color4.toColor()
        colorLight5.value = configuration.color5.toColor()
        colorLight6.value = configuration.color6.toColor()
        colorLight7.value = configuration.color7.toColor()
        textLightDelay.text = "${configuration.delay}"
        textLightWidth.text = "${configuration.width}"
        textLightFading.text = "${configuration.fading}"
        textLightMin.text = "${configuration.minimum}"
        textLightMax.text = "${configuration.maximum}"
        textLightTimeout.text = "${configuration.timeout}"
    }

    private fun enshureUsbDescriptor() {
        if (usbCommunicator.isDisconnected) {
            usbCommunicator.connectionDescriptor = choiceUSBPort.value
                    ?.takeIf { it.second }
                    ?.let { USBCommunicator.Descriptor(it.first) }
        }
    }

    private fun enshureBluetoothDescriptor() {
        if (btCommunicator.isDisconnected) {
            btCommunicator.connectionDescriptor = BluetoothCommunicator.Descriptor(
                    comboBluetoothAddress.value?.first ?: "",
                    textBluetoothChannel.text.toIntOrNull() ?: 6)
        }
    }

    private fun updateSerialPorts(devices: Collection<USBCommunicator.Descriptor>) {
        val portNames = devices
                .map { it.portName }
                .map { it to true }
                .toMutableList()

        if (usbPortList.filter { it.second }.size != portNames.size
                || !usbPortList.filter { it.second }.all { port -> portNames.any { port.first == it.first } }) {

            deviceConfiguration.usbDevice
                    ?.takeIf { device -> portNames.none { it.first == device } }
                    ?.also { portNames.add(it to false) }

            val removedPorts = usbPortList
                    .filterNot { (port, _) -> portNames.map { (p, _) -> p }.contains(port) }
            val addedPorts = portNames
                    .filterNot { (port, _) -> usbPortList.map { (p, _) -> p }.contains(port) }
            usbPortList.removeAll(removedPorts)
            usbPortList.replaceAll { (port, connected) ->
                port to (portNames.find { (p, _) -> port == p }?.second ?: connected)
            }
            usbPortList.addAll(addedPorts)

            val selected = usbPortList.find { it.first == deviceConfiguration.usbDevice }
            if (selected != null) {
                choiceUSBPort.selectionModel.select(selected)
            }
        }
    }

    private fun updateBluetoothDevices(devices: Collection<BluetoothCommunicator.Descriptor>) {
        val devices = devices
                .map { it.address to (it.name ?: "") }
                .toMutableList()

        if (bluetoothDeviceList.size != devices.size
                || !bluetoothDeviceList.all { port -> devices.any { port.first == it.first } }) {

            deviceConfiguration.bluetoothAddress
                    ?.takeIf { device -> devices.none { it.first == device } }
                    ?.also { devices.add(it to (deviceConfiguration.bluetoothName ?: "")) }

            val removedDevices = bluetoothDeviceList
                    .filterNot { (addr, _) -> devices.map { (a, _) -> a }.contains(addr) }
            val addedPorts = devices
                    .filterNot { (addr, _) -> bluetoothDeviceList.map { (a, _) -> a }.contains(addr) }
            bluetoothDeviceList.removeAll(removedDevices)
            bluetoothDeviceList.replaceAll { (addr, name) ->
                addr to (devices.find { (p, _) -> addr == p }?.second ?: name)
            }
            bluetoothDeviceList.addAll(addedPorts)

            val selected = bluetoothDeviceList.find { it.first == deviceConfiguration.usbDevice }
            if (selected != null) {
                comboBluetoothAddress.selectionModel.select(selected)
            }
        }
    }
}
