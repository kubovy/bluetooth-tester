package com.poterion.monitor.api.communication

/**
 * Bluetooth device scanner.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
object BluetoothScanner : Scanner<BluetoothCommunicator.Descriptor>() {

	override fun getAvailableDevices() = null

//	override fun getAvailableDevices() = LocalDevice
//			.getLocalDevice()
//			.discoveryAgent
//			.retrieveDevices(DiscoveryAgent.PREKNOWN)
//			//.retrieveDevices(DiscoveryAgent.CACHED)
//			?.map { BluetoothCommunicator.Descriptor(address = it.bluetoothAddress, channel = 6) }
//			?.toList()
}