package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.tools.Diagnostic

open class KotlinFunctionElement internal constructor(
		private val element: ExecutableElement,
		private val protoFunction: ProtoBuf.Function,
		private val protoNameResolver: NameResolver,
		processingEnv: ProcessingEnvironment
) : KotlinExecutableElement(element, processingEnv) {

	val isInline: Boolean = protoFunction.isInline
	val isInfix: Boolean = protoFunction.isInfix
	val isTailRec: Boolean = protoFunction.isTailRec
	val isSuspend: Boolean = protoFunction.isSuspend
	val isOperator: Boolean = protoFunction.isOperator

	/** Whether this function has the `expect` keyword
	 *
	 * An expect function is a function declaration with actual definition in a different
	 * file, akin to a declaration in a header file in C++. They are used in multiplatform
	 * projects where different implementations are needed depending on target platform
	 */
	val isExpectFunction: Boolean = protoFunction.isExpectFunction

	/**
	 * Whether this function has the `external` keyword
	 *
	 * An external function is a class declaration with the actual definition in native
	 * code, similar to the `native` keyword in Java
	 */
	val isExternalFunction: Boolean = protoFunction.isExternalFunction

	/**
	 * modality
	 * one of: `FINAL`, `OPEN`, `ABSTRACT`, `SEALED` TODO("how can a function be sealed?")
	 */
	val modality: ProtoBuf.Modality = protoFunction.modality!!

	val visibility: ProtoBuf.Visibility = protoFunction.visibility!!

	companion object {
		fun get(element: ExecutableElement, processingEnv: ProcessingEnvironment): KotlinFunctionElement?
				= when {
				element is KotlinFunctionElement -> element

				/*
				Afaik it is not possible (as of 08/2018) to get an `Element` of a local variable, function or class
				so we can assume that a valid `ExecutableElement` must always belong to a class or interface and the
				enclosing element is a `TypeElement`
				 */
				element.isLocal() -> {
					val enclosingElement = element.enclosingElement
					processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "${(element as? TypeElement)?.nestingKind}")
					throw IllegalStateException(
							"Can not construct KotlinFunctionElement of element \"$element\" because this library was written " +
							"with the assumption that it is impossible to get an `Element` of " +
							"a local function but the enclosing element \"$enclosingElement\" " +
							"is of kind \"${enclosingElement.kind}\"${enclosingElement.asTypeElement()?.run {
							" with nesting kind \"$nestingKind\""
							}}")
				}

				else ->
					// to construct the KotlinFunctionElement, metadata of the parent element is needed
					// so the construction is delegated to the parent element
					(element.enclosingElement.toKotlinElement(processingEnv)
							as? KotlinTypeElement)
							?.getKotlinMethod(element)
						?: throw IllegalStateException(
								"Could not convert $element to KotlinFunctionElement even " +
								"though it is apparently a Kotlin element")

			}
	}

	override fun getTypeParameters(): List<KotlinTypeParameterElement>
			= element.typeParameters
			.map { typeParamElem ->
				getKotlinTypeParameter(typeParamElem)
				?: throw IllegalStateException(
						"Could not find matching ProtoBuf.TypeParameter for TypeParameterElement \"$typeParamElem\"" +
						"which is a sub-element of \"$this\"")
			}

	/**
	 * returns a [KotlinTypeParameterElement] for this [TypeParameterElement] if it's a type parameter
	 * of this function or null otherwise
	 *
	 * this function is mostly necessary to be used by [KotlinTypeParameterElement.get] because only the
	 * enclosing function has enough information to create the [KotlinTypeParameterElement] and the factory
	 * function alone can not do it
	 */
	internal fun getKotlinTypeParameter(typeParamElem: TypeParameterElement): KotlinTypeParameterElement?
			= findMatchingProtoTypeParam(typeParamElem, protoFunction.typeParameterList, protoNameResolver)
			?.let { protoTypeParam -> KotlinTypeParameterElement(typeParamElem, protoTypeParam, processingEnv) }
}
