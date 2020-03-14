package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.VariableElement

internal class JxFieldElement(
		private val javaxElem: VariableElement,
		override val origin: KJOrigin.Java,
		override val enclosingElement: KJElement,
		private val processingEnv: ProcessingEnvironment
) : KJFieldElement(), AnnotatedConstruct by javaxElem {

	override val enclosedElements: Set<Nothing> = emptySet()

	override val simpleName: String = javaxElem.simpleName.toString()

	override fun asTypeMirror(): KJTypeMirror = javaxElem.asType().toKJTypeMirror(processingEnv)!!

	override fun asJavaxElement(): VariableElement = javaxElem

	override val receiverType: KJTypeMirror?
		get() = javaxElem.enclosingElement.asType()?.toKJTypeMirror(processingEnv)
}