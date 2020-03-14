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
package com.poterion.bluetooth.tester

import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.paint.Color

fun TextField.getHexInt() = "0x(\\d+)".toRegex()
		.matchEntire(text)
		?.takeIf { it.groupValues.size == 2 }
		?.groupValues
		?.get(1)
		?.toInt(16)

fun java.awt.Color.toColor() = Color.rgb(red, green, blue)

fun Color.toAwtColor() = java.awt.Color(red.toFloat(), green.toFloat(), blue.toFloat())

fun Int.toAwtRainbowColor() = java.awt.Color(0x01, 0x02, this)

fun ComboBox<String>.updateCount(count: Int) {
	val selected = value?.toIntOrNull() ?: 0
	items.setAll((0 until count).map { "${it}" })
	if (selected < count) selectionModel.select("${selected}")
}

fun ComboBox<String>.select(num: Int) {
	while (items.size <= num) items.add("${items.size}")
	selectionModel.select("${num}")
}