package com.poterion.monitor.api.data

/**
 * RGB configuration.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
data class RGBConfig(var pattern: RGBPattern,
					 var red: Int,
					 var green: Int,
					 var blue: Int,
					 var delay: Int,
					 var min: Int,
					 var max: Int,
					 var timeout: Int)