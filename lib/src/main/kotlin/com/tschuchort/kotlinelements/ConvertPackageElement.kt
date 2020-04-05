package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.from_javax.JxPackageElement
import com.tschuchort.kotlinelements.originatesFromKotlin
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement

private fun convertPackage(elem: PackageElement, processingEnv: ProcessingEnvironment): KJPackageElement {
	assert(elem.kind == ElementKind.PACKAGE)

	val origin = if (elem.originatesFromKotlin())
		KJOrigin.Kotlin.Declared
	else
		KJOrigin.fromJavax(elem, processingEnv)

	return JxPackageElement(elem, origin, processingEnv)
}