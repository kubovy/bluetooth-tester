package com.poterion.bluetooth.tester.api

import com.poterion.bluetooth.tester.data.Configuration

/**
 * Configuration listener.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
interface ConfigListener {
	/**
	 * Configuration update callback.
	 *
	 * @param configuration New configuration object.
	 */
	fun onUpdate(configuration: Configuration)
}