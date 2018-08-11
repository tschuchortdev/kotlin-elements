package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.lang.model.element.Element

/**
 * An javaElement that is overridable or derivable and has a modality modifier
 * (i.e. classes, functions and properties but not constructors)
 */
interface HasKotlinModality {
	/**
	 * modality modifier including implicit modifiers (`final` if no modifier
	 * is explicitly specified or `open` in case it was inherited by `override`)
	 * one of: [KotlinModality.FINAL], [KotlinModality.OPEN], [KotlinModality.ABSTRACT], [KotlinModality.ABSTRACT]
	 */
	val modality: KotlinModality
}

enum class KotlinModality {
	/** has `final` modifier or no modifier (final by default) */
	FINAL,
	/** has `open` modifier or inherited it */
	OPEN,
	/** has `abstract` modifier` */
	ABSTRACT,
	/** has `sealed` modifier.
	 * Maps to `abstract` modifier in the underlying Java javaElement
	 * */
	SEALED;

	companion object {
		internal fun fromProtoBuf(protoModality: ProtoBuf.Modality): KotlinModality
				= when(protoModality) {
			ProtoBuf.Modality.FINAL -> FINAL
			ProtoBuf.Modality.ABSTRACT -> ABSTRACT
			ProtoBuf.Modality.OPEN -> OPEN
			ProtoBuf.Modality.SEALED -> SEALED
		}
	}
}
