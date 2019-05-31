package com.poterion.monitor.api.data

/**
 * WS281x light configuration.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
class WS281xLightConfig(var pattern: WS281xLightPattern,
						var color1Red: Int,
						var color1Green: Int,
						var color1Blue: Int,
						var color2Red: Int,
						var color2Green: Int,
						var color2Blue: Int,
						var color3Red: Int,
						var color3Green: Int,
						var color3Blue: Int,
						var color4Red: Int,
						var color4Green: Int,
						var color4Blue: Int,
						var color5Red: Int,
						var color5Green: Int,
						var color5Blue: Int,
						var color6Red: Int,
						var color6Green: Int,
						var color6Blue: Int,
						var color7Red: Int,
						var color7Green: Int,
						var color7Blue: Int,
						var delay: Int,
						var width: Int,
						var fading: Int,
						var min: Int,
						var max: Int,
						var timeout: Int)