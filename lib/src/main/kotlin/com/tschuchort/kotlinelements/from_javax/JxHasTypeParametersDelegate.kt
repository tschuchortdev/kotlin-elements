package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.KJTypeParameterElement
import com.tschuchort.kotlinelements.mixins.HasTypeParameters
import com.tschuchort.kotlinelements.toKJElement
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Parameterizable

internal class JxHasTypeParametersDelegate(
		private val javaxElem: Parameterizable,
		private val processingEnv: ProcessingEnvironment
) : HasTypeParameters {

	override val typeParameters: List<KJTypeParameterElement> by lazy {
		javaxElem.typeParameters.map { it.toKJElement(processingEnv) }
	}
}