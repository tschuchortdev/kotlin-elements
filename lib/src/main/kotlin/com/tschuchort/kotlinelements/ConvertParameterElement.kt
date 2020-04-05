package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.from_javax.JxParameterElement
import com.tschuchort.kotlinelements.originatesFromKotlin
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.VariableElement

private fun convertParameter(elem: VariableElement, processingEnv: ProcessingEnvironment)
		: KJParameterElement {
	assert(elem.kind == ElementKind.PARAMETER)

	return if (elem.originatesFromKotlin()) {
		val methodElem = elem.enclosingElement.toKJElement(processingEnv) as KJExecutableElement

		/* This is an O(n^2) algorithm if the user iterates over ParameterElements
		and converts them all to KJParameterElements, but we will accept this because it
		shouldn't happen often and the number of parameters per method is usually small. */
		methodElem.parameters.single {
			it.simpleName == elem.simpleName.toString()
		}
	}
	else {
		JxParameterElement(elem, KJOrigin.fromJavax(elem, processingEnv), processingEnv)
	}
}