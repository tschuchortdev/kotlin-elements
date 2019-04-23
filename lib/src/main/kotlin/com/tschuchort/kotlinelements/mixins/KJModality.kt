package com.tschuchort.kotlinelements.mixins

import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

/**
 * An javaElement that is overridable or derivable and has a modality modifier
 * (i.e. classes, functions and properties but not constructors)
 */
interface HasModality {
	/**
	 * modality modifier including implicit modifiers (`final` if no modifier
	 * is explicitly specified or `open` in case it was inherited by `override`)
	 * one of: [KJModality.FINAL], [KJModality.OPEN], [KJModality.ABSTRACT], [KJModality.ABSTRACT]
	 */
	val modality: KJModality
}

enum class KJModality {
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
		internal fun fromProtoBuf(protoModality: ProtoBuf.Modality): KJModality
				= when(protoModality) {
			ProtoBuf.Modality.FINAL -> FINAL
			ProtoBuf.Modality.ABSTRACT -> ABSTRACT
			ProtoBuf.Modality.OPEN -> OPEN
			ProtoBuf.Modality.SEALED -> SEALED
		}

		internal fun fromKm(flags: Flags) = when {
			Flag.IS_FINAL(flags) -> FINAL
			Flag.IS_ABSTRACT(flags) -> ABSTRACT
			Flag.IS_OPEN(flags) -> OPEN
			Flag.IS_SEALED(flags) -> SEALED
			else -> throw AssertionError("No modality flag found in metadata flags")
		}

		internal fun fromJavax(javaxElem: Element): KJModality = with(javaxElem.modifiers) {
			when {
				contains(Modifier.FINAL) -> FINAL
				contains(Modifier.ABSTRACT) -> ABSTRACT
				else -> OPEN
			}
		}
	}
}
