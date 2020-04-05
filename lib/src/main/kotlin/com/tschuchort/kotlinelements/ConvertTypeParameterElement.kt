package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.from_javax.JxTypeParameterElement
import com.tschuchort.kotlinelements.originatesFromKotlin
import com.tschuchort.kotlinelements.mixins.HasTypeParameters
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeParameterElement

private fun convertTypeParameter(elem: TypeParameterElement, processingEnv: ProcessingEnvironment)
		: KJTypeParameterElement {
	assert(elem.kind == ElementKind.TYPE_PARAMETER)

	return if (elem.originatesFromKotlin()) {
		val methodElem = elem.enclosingElement.toKJElement(processingEnv) as HasTypeParameters

		/* This is an O(n^2) algorithm if the user iterates over TypeParameterElements
		and converts them all to KJTypeParameterElements, but we will accept this because it
		shouldn't happen often and the number of parameters per method is usually small. */
		methodElem.typeParameters.single {
			it.simpleName == elem.simpleName.toString()
		}
	}
	else {
		JxTypeParameterElement(elem, KJOrigin.fromJavax(elem, processingEnv), processingEnv)
	}
}