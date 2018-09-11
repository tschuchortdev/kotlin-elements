package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.declaresDefaultValue
import me.eugeniomarletti.kotlin.metadata.isCrossInline
import me.eugeniomarletti.kotlin.metadata.isNoInline
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.VariableElement

class KotlinParameterElement internal constructor(
		val javaElement: VariableElement,
		private val protoParam: ProtoBuf.ValueParameter,
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

	override fun getEnclosingElement(): KotlinElement {
		return javaElement.enclosingElement.correspondingKotlinElement(processingEnv)!!
	}

	override fun getEnclosedElements(): List<Nothing> {
		// a parameter element shouldn't enclose anything
		assert(javaElement.enclosedElements.isEmpty())
		return emptyList()
	}

	override fun equals(other: Any?): Boolean
			= (other as? KotlinParameterElement)?.javaElement == javaElement

	override fun hashCode() = Objects.hash(javaElement, protoParam)

	override fun toString() = javaElement.toString()
}

fun doParametersMatch(javaParamElem: VariableElement, protoParam: ProtoBuf.ValueParameter, protoNameResolver: NameResolver)
		= (javaParamElem.simpleName.toString() == protoNameResolver.getString(protoParam.name))
	//TODO("also check that parameter types match")

