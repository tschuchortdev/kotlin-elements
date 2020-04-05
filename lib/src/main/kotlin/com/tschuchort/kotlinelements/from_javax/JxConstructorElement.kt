package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.KJExecutableElementInterface
import com.tschuchort.kotlinelements.mixins.HasReceiver
import com.tschuchort.kotlinelements.mixins.HasVisibility
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement

internal class JxConstructorElement(
		private val javaxElem: ExecutableElement,
		enclosingElem: Lazy<KJTypeElement>,
		override val origin: KJOrigin,
		private val processingEnv: ProcessingEnvironment
) : KJConstructorElement(),
	HasVisibility by JxHasVisibilityDelegate(javaxElem),
	HasReceiver by JxHasReceiverDelegate(javaxElem, processingEnv),
	KJExecutableElementInterface by JxExecutableElement(javaxElem, enclosingElem, origin, processingEnv) {

	override val isPrimary: Boolean = false

	override val enclosedElements: Set<KJParameterElement>
		get() = super.enclosedElements

	override val enclosingElement: KJTypeElement by enclosingElem
}