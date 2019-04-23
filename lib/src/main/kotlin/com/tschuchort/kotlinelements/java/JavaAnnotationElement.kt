package com.tschuchort.kotlinelements.java

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.KJModality
import com.tschuchort.kotlinelements.mixins.KJVisibility
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.Name
import javax.lang.model.element.TypeElement

internal class JavaAnnotationElement(
		private val javaxElem: TypeElement,
		private val processingEnv: ProcessingEnvironment
) : KJAnnotationElement(), AnnotatedConstruct by javaxElem {

	override val simpleName: String = javaxElem.simpleName.toString()

	override fun asTypeMirror(): KJTypeMirror = javaxElem.asType().toKJTypeMirror()

	override val origin: KJOrigin =
		KJOrigin.fromJavax(javaxElem, processingEnv)

	override fun toString(): String = javaxElem.toString()

	override fun equals(other: Any?): Boolean = (other as? JavaAnnotationElement)?.javaxElem == javaxElem

	override fun hashCode(): Int = Objects.hash(javaxElem)

	override val interfaces: Set<KJTypeMirror> by lazy {
		javaxElem.interfaces.map { it.toKJTypeMirror() }.toSet()
	}

	override val superclass: KJTypeMirror by lazy {
		javaxElem.superclass.toKJTypeMirror()
	}

	override val enclosingElement: KJElement by lazy {
		javaxElem.enclosingElement.toKJElement(processingEnv)
	}

	override fun javaxElement(): TypeElement = javaxElem

	override val visibility: KJVisibility = KJVisibility.fromJavax(javaxElem)

	override val qualifiedName: String = javaxElem.qualifiedName.toString()

	override val attributes: List<KJAnnotationAttributeElement> by lazy {
		javaxElem.enclosedElements.map {
			it.toKJElement(processingEnv) as KJAnnotationAttributeElement
		}
	}

	override val modality: KJModality = KJModality.fromJavax(javaxElem)
}
