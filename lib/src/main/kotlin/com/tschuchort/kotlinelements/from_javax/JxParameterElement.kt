package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement

internal class JxParameterElement(
		private val javaxElem: VariableElement,
		override val origin: KJOrigin,
		private val processingEnv: ProcessingEnvironment
) : KJParameterElement(), AnnotatedConstruct by javaxElem {

	override val enclosingElement: KJElement?
		get() = javaxElem.enclosingElement.toKJElement(processingEnv)

	override val simpleName: String = javaxElem.simpleName.toString()

	override fun asTypeMirror(): KJTypeMirror? = javaxElem.asType().toKJTypeMirror(processingEnv)

	override fun toString(): String = javaxElem.toString()

	override val hasDefaultValue: Boolean = false

	override val isCrossInline: Boolean = false

	override val isNoInline: Boolean = false

	override val isVararg: Boolean
		get() {
			val methodElem = (javaxElem.enclosingElement as ExecutableElement)
			return methodElem.isVarArgs && methodElem.parameters.last() == javaxElem
		}

	override fun asJavaxElement(): VariableElement = javaxElem
}