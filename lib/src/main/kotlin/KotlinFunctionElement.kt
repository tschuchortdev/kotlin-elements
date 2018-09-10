package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*

class KotlinFunctionElement internal constructor(
		javaElement: ExecutableElement,
		jvmOverloadElements: List<ExecutableElement>,
		private val protoFunction: ProtoBuf.Function,
		private val protoNameResolver: NameResolver,
		processingEnv: ProcessingEnvironment
) : KotlinExecutableElement(javaElement, jvmOverloadElements, processingEnv), HasKotlinModality {

	val isInline: Boolean = protoFunction.isInline
	val isInfix: Boolean = protoFunction.isInfix
	val isTailRec: Boolean = protoFunction.isTailRec
	val isSuspend: Boolean = protoFunction.isSuspend
	val isOperator: Boolean = protoFunction.isOperator
	val isFree: Boolean = TODO("is free function")
	val isExtension: Boolean = TODO("is extension function")

	/** Whether this function has the `expect` keyword
	 *
	 * An expect function is a function declaration with actual definition in a different
	 * file, akin to a declaration in a header file in C++. They are used in multiplatform
	 * projects where different implementations are needed depending on target platform
	 */
	val isExpect: Boolean = protoFunction.isExpectFunction

	/**
	 * Whether this function has the `external` keyword
	 *
	 * An external function is a function declaration with the actual definition in native
	 * code, similar to the `native` keyword in Java
	 */
	val isExternal: Boolean = protoFunction.isExternalFunction

	/**
	 * modality
	 * one of: [KotlinModality.FINAL], [KotlinModality.OPEN], [KotlinModality.ABSTRACT], [KotlinModality.NONE]
	 */
	override val modality: KotlinModality = when(protoFunction.modality) {
		ProtoBuf.Modality.FINAL -> KotlinModality.FINAL
		ProtoBuf.Modality.ABSTRACT -> KotlinModality.ABSTRACT
		ProtoBuf.Modality.OPEN -> KotlinModality.OPEN
		ProtoBuf.Modality.SEALED -> throw AssertionError("Function modality should never be SEALED")
		null -> KotlinModality.NONE
	}

	val visibility: ProtoBuf.Visibility = protoFunction.visibility!!

	override fun getTypeParameters(): List<KotlinTypeParameterElement>
			= protoFunction.typeParameterList.zipWith(javaElement.typeParameters) { protoTypeParam, javaTypeParam ->
		if(doTypeParamsMatch(javaTypeParam, protoTypeParam, protoNameResolver))
			KotlinTypeParameterElement(javaTypeParam, protoTypeParam, processingEnv)
		else
			throw AssertionError(
					"Kotlin ProtoBuf.TypeParameters should always match up with Java TypeParameterElements")
	}

	/**
	 * Returns a [KotlinTypeParameterElement] for this [TypeParameterElement] if it's a type parameter
	 * of this function or null otherwise
	 *
	 * this function is mostly necessary to be used when finding the corresponding [KotlinElement] for
	 * some arbitrary Java [Element] since only the surrounding element of the type parameter has enough
	 * information to construct it
	 */
	internal fun getKotlinTypeParameter(typeParamElem: TypeParameterElement): KotlinTypeParameterElement?
			= protoFunction.typeParameterList.filter { doTypeParamsMatch(typeParamElem, it, protoNameResolver) }
			.singleOrNull()
			?.let { protoTypeParam -> KotlinTypeParameterElement(typeParamElem, protoTypeParam, processingEnv) }
}
