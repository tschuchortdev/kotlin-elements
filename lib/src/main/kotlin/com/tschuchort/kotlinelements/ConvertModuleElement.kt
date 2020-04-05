package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.from_javax.JxModuleElement
import com.tschuchort.kotlinelements.originatesFromKotlin
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ModuleElement

private fun convertModule(elem: ModuleElement, processingEnv: ProcessingEnvironment): KJModuleElement {
	assert(elem.kind == ElementKind.MODULE)

	val origin = if (elem.originatesFromKotlin())
		KJOrigin.Kotlin.Declared
	else
		KJOrigin.fromJavax(elem, processingEnv)

	return JxModuleElement(elem, origin, processingEnv)
}