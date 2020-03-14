package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.HasModality
import com.tschuchort.kotlinelements.mixins.HasReceiver
import com.tschuchort.kotlinelements.mixins.HasVisibility
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement

internal class JxSetterElement(
		private val javaxElem: ExecutableElement,
		enclosingProperty: KJPropertyElement,
		origin: KJOrigin,
		private val processingEnv: ProcessingEnvironment
) : KJSetterElement(),
	HasReceiver by JxHasReceiverDelegate(javaxElem, processingEnv),
	HasVisibility by JxHasVisibilityDelegate(javaxElem),
	HasModality by JxHasModalityDelegate(javaxElem),
	KJExecutableElementInterface by JxExecutableElementDelegate(javaxElem, origin, processingEnv) {

	override val isDefaultImplementation: Boolean = false
	override val isInline: Boolean = false
	override val isExternal: Boolean = false

	override val enclosingElement: KJPropertyElement = enclosingProperty

	override val enclosedElements: Set<KJParameterElement>
		get() = super.enclosedElements
}


