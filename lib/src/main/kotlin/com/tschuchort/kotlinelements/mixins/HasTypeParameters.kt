package com.tschuchort.kotlinelements.mixins

import com.tschuchort.kotlinelements.*
import javax.lang.model.element.TypeParameterElement


/**
 * A construct that can have type parameters
 */
interface HasTypeParameters {
	val typeParameters: List<KJTypeParameterElement>
	companion object
}
