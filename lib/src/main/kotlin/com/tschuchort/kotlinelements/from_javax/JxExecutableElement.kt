package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.KJExecutableElementInterface
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

internal class JxExecutableElement(
		private val elemDelegate: KJElement,
		parameters: Lazy<List<KJParameterElement>>,
		returnType: Lazy<KJTypeMirror?>,
		thrownTypes: Lazy<List<KJTypeMirror>>
) : KJElement by elemDelegate, KJExecutableElementInterface {

	override val parameters: List<KJParameterElement> by parameters
	override val returnType: KJTypeMirror? by returnType
	override val thrownTypes: List<KJTypeMirror> by thrownTypes

	override fun asTypeMirror(): KJExecutableType = elemDelegate.asTypeMirror() as KJExecutableType
	override val enclosingElement: KJElement = elemDelegate.enclosingElement as KJElement
	override fun asJavaxElement(): ExecutableElement = elemDelegate.asJavaxElement() as ExecutableElement

	/*override val parameters: List<KJParameterElement> by lazy {
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
	}*/

	companion object {
		fun fromJavax(elemDelegate: KJElement, javaxElem: ExecutableElement): JxExecutableElement {

		}
	}
}