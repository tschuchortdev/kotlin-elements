package com.tschuchort.kotlinelements.mixins

import com.tschuchort.kotlinelements.*
import javax.lang.model.element.TypeParameterElement


/**
 * A construct that can have type parameters
 */
interface HasTypeParameters {
	val typeParameters: List<KJTypeParameterElement>
}

/**
 * This interface exists for all Kotlin elements that have type parameters.
 * They must also support looking up the corresponding KJElement for a Javax TypeParameter
 * since only the enclosing element has enough information to construct them and we don't want
 * to iterate through all enclosed type parameters to find the corresponding one.
 */
internal interface HasTypeParametersWithLookup : HasTypeParameters {
	fun lookupKJTypeParameterFor(javaxTypeParam: TypeParameterElement): KJTypeParameterElement
}
