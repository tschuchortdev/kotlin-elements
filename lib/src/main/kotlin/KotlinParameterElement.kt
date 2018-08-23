package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.declaresDefaultValue
import me.eugeniomarletti.kotlin.metadata.isCrossInline
import me.eugeniomarletti.kotlin.metadata.isNoInline
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.VariableElement

class KotlinParameterElement internal constructor(
		val javaElement: VariableElement,
		protected val protoParam: ProtoBuf.ValueParameter,
		processingEnv: ProcessingEnvironment
) : KotlinSubelement(processingEnv), VariableElement by javaElement {

	/**
	 * Whether this parameter has a default value
	 *
	 * not to be confused with [javax.lang.model.element.ExecutableElement.isDefault]
	 * and [javax.lang.model.element.ExecutableElement.getDefaultValue] which
	 * merely returns the default value of an annotation class parameter
	 */
	val hasDefaultValue: Boolean = protoParam.declaresDefaultValue

	val isCrossInline: Boolean = protoParam.isCrossInline

	val isNoInline: Boolean = protoParam.isNoInline
}