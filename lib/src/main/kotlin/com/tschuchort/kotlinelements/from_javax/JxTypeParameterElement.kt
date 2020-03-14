package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.KJOrigin
import com.tschuchort.kotlinelements.mixins.KJVariance
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.TypeParameterElement

class JxTypeParameterElement(
		val javaxElem: TypeParameterElement,
		override val origin: KJOrigin,
		private val processingEnv: ProcessingEnvironment
): KJTypeParameterElement(), AnnotatedConstruct by javaxElem {

	override val simpleName: String get()
			= javaxElem.simpleName.toString()

	override fun asTypeMirror(): KJTypeMirror?
			= javaxElem.asType().toKJTypeMirror(processingEnv)

	override val reified: Boolean = false

	override val bounds: Set<KJTypeMirror> by lazy {
		javaxElem.bounds.mapNotNull { it.toKJTypeMirror(processingEnv) }.toSet()
	}

	override fun asJavaxElement(): TypeParameterElement = javaxElem

	override val enclosingElement: KJElement by lazy {
		javaxElem.enclosingElement!!.toKJElement(processingEnv)
	}

	override val variance: KJVariance = KJVariance.INVARIANT
}