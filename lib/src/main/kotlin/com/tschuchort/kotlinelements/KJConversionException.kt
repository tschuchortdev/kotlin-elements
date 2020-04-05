package com.tschuchort.kotlinelements

import javax.lang.model.element.Element

class KJConversionException(val additionalMessage: String? = null, cause: Throwable) : RuntimeException(cause) {
	override val message: String
		get() = "Failed to convert between Kotlin metadata and Javax." +
				if (additionalMessage != null)
					"Additional information:\n$additionalMessage"
				else
					""
}
