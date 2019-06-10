package com.poterion.monitor.api.communication

/**
 * Scanner listener.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
interface ScannerListener<ConnectionDescriptor> {
	/**
	 * On available devices changed callback.
	 *
	 * @param devices Collection of currently available devices.
	 */
	fun onAvailableDevicesChanged(channel: Channel, devices: Collection<ConnectionDescriptor>)
}