package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.KJTypeElementInterface
import com.tschuchort.kotlinelements.mixins.HasVisibility
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

internal class JxTypeElementDelegate(
		private val javaxElem: TypeElement,
		override val origin: KJOrigin,
		private val processingEnv: ProcessingEnvironment
) : KJTypeElementInterface,
	HasVisibility by JxHasVisibilityDelegate(javaxElem),
	AnnotatedConstruct by javaxElem {

	override val enclosedElements: Set<KJElement>
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

	override val simpleName: String = javaxElem.simpleName.toString()

	override fun asTypeMirror(): KJTypeMirror? = javaxElem.asType().toKJTypeMirror(processingEnv)

	override fun asJavaxElement(): Element = javaxElem

	override val interfaces: Set<KJTypeMirror> by lazy {
		javaxElem.interfaces.mapNotNull { it.toKJTypeMirror(processingEnv) }.toSet()
	}

	override val superclass: KJTypeMirror? by lazy {
		javaxElem.superclass.toKJTypeMirror(processingEnv)
	}

	override val enclosingElement: KJElement by lazy {
		javaxElem.enclosingElement.toKJElement(processingEnv)
	}

	override val qualifiedName: String = javaxElem.qualifiedName.toString()
}