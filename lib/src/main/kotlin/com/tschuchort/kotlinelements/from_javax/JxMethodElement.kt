package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.KJExecutableElementInterface
import com.tschuchort.kotlinelements.mixins.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier

internal class JxMethodElement(
		private val javaxElem: ExecutableElement,
		enclosingElement: Lazy<KJElement>,
		origin: KJOrigin,
		private val processingEnv: ProcessingEnvironment
) : KJFunctionElement(),
	HasVisibility by JxHasVisibilityDelegate(javaxElem),
	HasReceiver by JxHasReceiverDelegate(javaxElem, processingEnv),
	HasModality by JxHasModalityDelegate(javaxElem),
	HasTypeParameters by JxHasTypeParametersDelegate(javaxElem, processingEnv),
	KJExecutableElementInterface by JxExecutableElementDelegate(javaxElem, enclosingElement, origin, processingEnv) {

	override val isInline: Boolean = false
	override val isInfix: Boolean = false
	override val isTailRec: Boolean = false
	override val isSuspend: Boolean = false
	override val isOperator: Boolean = false

	override val isStatic: Boolean
		get() = Modifier.STATIC in javaxElem.modifiers

	override val enclosedElements: Set<KJParameterElement>
		get() = super.enclosedElements

	override val isExternal: Boolean = false
	override val isExpect: Boolean = false

}