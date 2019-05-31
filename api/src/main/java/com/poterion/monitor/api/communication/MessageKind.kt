package com.poterion.monitor.api.communication

/**
 * Message kind interface.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
interface MessageKind {
	/** Code of the message type. */
	val code: Int

	/** Confirmation wait delay per chunk in ms */
	val delay: Long?
}