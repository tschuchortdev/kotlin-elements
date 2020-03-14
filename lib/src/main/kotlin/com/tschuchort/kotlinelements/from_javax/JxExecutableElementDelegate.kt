package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.KJExecutableElementInterface
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.ExecutableElement

internal class JxExecutableElementDelegate(
		private val javaxElem: ExecutableElement,
		enclosingElement: Lazy<KJElement>,
		override val origin: KJOrigin,
		private val processingEnv: ProcessingEnvironment
) : KJExecutableElementInterface, AnnotatedConstruct by javaxElem {

	override val enclosingElement: KJElement by enclosingElement

	override val simpleName: String
		get() = javaxElem.simpleName.toString()

	override fun asTypeMirror(): KJExecutableType
			= javaxElem.asType().toKJTypeMirror(processingEnv) as KJExecutableType

	override fun asJavaxElement(): ExecutableElement = javaxElem

	override val parameters: List<KJParameterElement> by lazy {
		javaxElem.parameters.map { it.toKJElement(processingEnv) as KJParameterElement }
	}

	override val returnType: KJTypeMirror? by lazy {
		javaxElem.returnType.toKJTypeMirror(processingEnv)
	}

	override val thrownTypes: List<KJTypeMirror> by lazy {
		javaxElem.thrownTypes.mapNotNull { it.toKJTypeMirror(processingEnv) }
	}

	override val enclosedElements: Set<KJElement> by lazy {
		javaxElem.enclosedElements.map { it.toKJElement(processingEnv) }.toSet()
	}
}