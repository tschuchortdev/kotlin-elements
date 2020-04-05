package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.KJExecutableElementInterface
import com.tschuchort.kotlinelements.KJGetterElement
import com.tschuchort.kotlinelements.mixins.KJOrigin
import com.tschuchort.kotlinelements.KJPropertyElement
import com.tschuchort.kotlinelements.mixins.HasModality
import com.tschuchort.kotlinelements.mixins.HasReceiver
import com.tschuchort.kotlinelements.mixins.HasVisibility
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement

internal class JxGetterElement(
		private val javaxElem: ExecutableElement,
		enclosingProperty: KJPropertyElement,
		origin: KJOrigin,
		private val processingEnv: ProcessingEnvironment
) : KJGetterElement(),
	HasReceiver by JxHasReceiverDelegate(javaxElem, processingEnv),
	HasVisibility by JxHasVisibilityDelegate(javaxElem),
	HasModality by JxHasModalityDelegate(javaxElem),
	KJExecutableElementInterface by JxExecutableElement(javaxElem, origin, processingEnv) {

	override val isDefaultImplementation: Boolean = false
	override val isInline: Boolean = false
	override val isExternal: Boolean = false

	override val enclosingElement: KJPropertyElement = enclosingProperty

	override val parameters: List<Nothing>
		get() = super.parameters

	override val enclosedElements: Set<Nothing>
		get() = super.enclosedElements
}