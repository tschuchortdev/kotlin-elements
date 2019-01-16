package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.declaresDefaultValue
import me.eugeniomarletti.kotlin.metadata.isCrossInline
import me.eugeniomarletti.kotlin.metadata.isNoInline
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.Name
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

/**
 * A value parameter of a function, method or constructor
 */
class KotlinParameterElement internal constructor(
		val javaElement: VariableElement,
		private val protoParam: ProtoBuf.ValueParameter,
		override val enclosingElement: KotlinExecutableElement
) : KotlinElement(), AnnotatedConstruct by javaElement {

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

	override val simpleName: Name = javaElement.simpleName

	override fun asType(): TypeMirror = javaElement.asType()

	override fun equals(other: Any?): Boolean
			= (other as? KotlinParameterElement)?.javaElement == javaElement

	override fun hashCode() = Objects.hash(javaElement, protoParam)

	override fun toString() = javaElement.toString()
}

internal fun doParametersMatch(javaParamElem: VariableElement, protoParam: ProtoBuf.ValueParameter, protoNameResolver: NameResolver)
		= (javaParamElem.simpleName.toString() == protoNameResolver.getString(protoParam.name))
	//TODO("also check that parameter types match")

