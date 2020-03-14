package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.HasReceiver
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement

internal class JxInitializerElement(
		private val javaxElem: ExecutableElement,
		enclosingElement: Lazy<KJElement>,
		override val origin: KJOrigin,
		private val processingEnv: ProcessingEnvironment
) : KJInitializerElement(),
	HasReceiver by JxHasReceiverDelegate(javaxElem, processingEnv),
	KJExecutableElementInterface by JxExecutableElementDelegate(javaxElem, enclosingElement, origin, processingEnv) {

	override val kind: Kind = when (javaxElem.kind!!) {
			ElementKind.INSTANCE_INIT -> Kind.Instance
			ElementKind.STATIC_INIT -> Kind.Static
			else -> throw AssertionError(
					"Javax element given to construct this initializer doesn't have initializer kind."
			)
		}

	override val enclosedElements: Set<Nothing> = super.enclosedElements
	override val parameters: List<Nothing> = super.parameters
	override val returnType: Nothing? = super.returnType
}