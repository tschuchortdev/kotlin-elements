package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.KJTypeMirror
import com.tschuchort.kotlinelements.mixins.HasReceiver
import com.tschuchort.kotlinelements.toKJTypeMirror
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement

class JxHasReceiverDelegate(
		private val javaxElem: ExecutableElement,
		private val processingEnv: ProcessingEnvironment
) : HasReceiver {
	override val receiverType: KJTypeMirror? by lazy {
		javaxElem.receiverType?.toKJTypeMirror(processingEnv)
	}
}