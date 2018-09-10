package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.lang.model.element.Element

/**
 * An element that has a visibility modifier
 */
interface HasKotlinVisibility : Element {
	/**
	 * visibility
	 * one of: [KotlinVisibility.INTERNAL], [KotlinVisibility.PRIVATE], [KotlinVisibility.PROTECTED],
	 * [KotlinVisibility.PUBLIC], [KotlinVisibility.PRIVATE_TO_THIS], [KotlinVisibility.LOCAL]
	 */
	val visibility: KotlinVisibility
}

enum class KotlinVisibility {
	INTERNAL, PRIVATE, PROTECTED, PUBLIC, PRIVATE_TO_THIS, LOCAL;

	companion object {
		internal fun fromProtoBuf(protoVisibility: ProtoBuf.Visibility?): KotlinVisibility
				= when(protoVisibility) {
			ProtoBuf.Visibility.INTERNAL -> INTERNAL
			ProtoBuf.Visibility.PRIVATE -> PRIVATE
			ProtoBuf.Visibility.PROTECTED -> PROTECTED
			ProtoBuf.Visibility.PUBLIC -> PUBLIC
			ProtoBuf.Visibility.PRIVATE_TO_THIS -> PRIVATE_TO_THIS
			ProtoBuf.Visibility.LOCAL -> LOCAL
			null -> PUBLIC
		}
	}
}