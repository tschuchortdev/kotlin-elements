package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

internal class JxClassElement(
		private val javaxElem: TypeElement,
		origin: KJOrigin,
		private val  processingEnv: ProcessingEnvironment
) : KJClassElement(),
	KJTypeElementInterface by JxTypeElementDelegate(javaxElem, origin, processingEnv) {

}


