package com.poterion.monitor.api.communication

import jssc.SerialPortList

/**
 * USB device scanner.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
object USBScanner : Scanner<USBCommunicator.Descriptor>() {
	override fun getAvailableDevices() = SerialPortList.getPortNames().map { USBCommunicator.Descriptor(it) }
}