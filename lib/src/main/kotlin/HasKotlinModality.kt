package com.tschuchort.kotlinelements

import javax.lang.model.element.Element

/**
 * An element that is overridable or derivable (i.e. classes,
 * functions and properties but not constructors)
 */
interface HasKotlinModality : Element {
	/**
	 * modality
	 * one of: [Modality.FINAL], [Modality.OPEN], [Modality.ABSTRACT], [Modality.ABSTRACT], [Modality.NONE]
	 */
	val modality: Modality
}

enum class Modality {
	FINAL, OPEN, ABSTRACT, SEALED, NONE;
}