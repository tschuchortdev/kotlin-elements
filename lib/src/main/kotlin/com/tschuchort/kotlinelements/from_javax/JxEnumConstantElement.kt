package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.VariableElement

class JxEnumConstantElement(
		private val javaxElem: VariableElement,
		enclosingElement: Lazy<KJEnumElement>,
		override val origin: KJOrigin,
		private val processingEnv: ProcessingEnvironment
) : KJEnumConstantElement(), AnnotatedConstruct by javaxElem {
	override val simpleName: String = javaxElem.simpleName.toString()

	override fun asTypeMirror(): KJTypeMirror?
			= javaxElem.asType().toKJTypeMirror(processingEnv)

	override fun asJavaxElement(): VariableElement = javaxElem

	override val enclosingElement: KJEnumElement by enclosingElement
}
