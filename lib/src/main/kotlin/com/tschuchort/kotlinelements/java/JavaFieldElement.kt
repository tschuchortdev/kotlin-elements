package com.tschuchort.kotlinelements.java

import com.tschuchort.kotlinelements.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.VariableElement

internal class JavaFieldElement(
		private val javaxElem: VariableElement,
		private val processingEnv: ProcessingEnvironment
) : KJFieldElement(), AnnotatedConstruct by javaxElem {

	override val enclosingElement: KJElement
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

	override val enclosedElements: Set<Nothing> = emptySet()

	override val simpleName: String = javaxElem.simpleName.toString()

	override fun asTypeMirror(): KJTypeMirror = javaxElem.asType().toKJTypeMirror(processingEnv)!!

	override val origin: KJOrigin.Java = KJOrigin.fromJavax(javaxElem, processingEnv)

	override fun asJavaxElement(): VariableElement = javaxElem

	override val receiverType: KJTypeMirror
		get() = enclosingElement.asTypeMirror()
}