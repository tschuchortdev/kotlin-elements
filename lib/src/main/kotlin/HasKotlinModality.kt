package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.modality
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.lang.model.element.Element

/**
 * An element that is overridable or derivable and has a modality modifier
 * (i.e. classes, functions and properties but not constructors)
 */
interface HasKotlinModality : Element {
	/**
	 * modality
	 * one of: [KotlinModality.FINAL], [KotlinModality.OPEN], [KotlinModality.ABSTRACT], [KotlinModality.ABSTRACT], [KotlinModality.NONE]
	 */
	val modality: KotlinModality
}

enum class KotlinModality {
	FINAL, OPEN, ABSTRACT, SEALED;

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
