package com.poterion.monitor.api.data

/**
 * RGB patterns.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
enum class RGBPattern(val code: Int, val delay: Int?, val min: Int?, val max: Int?, val timeout: Int?) {
	OFF(0x00, null, null, null, null),
	LIGHT(0x01, 1000, null, 255, 1),
	BLINK(0x02, 500, 0, 255, 3),
	FADE_IN(0x03, 200, 0, 255, 3),
	FADE_OUT(0x04, 200, 0, 255, 3),
	FADE_TOGGLE(0x05, 200, 0, 255, 3);
}