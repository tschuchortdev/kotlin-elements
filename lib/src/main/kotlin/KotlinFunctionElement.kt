package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.jvm.getJvmMethodSignature
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

open class KotlinFunctionElement internal constructor(
		private val element: ExecutableElement,
		protoFunction: ProtoBuf.Function,
		protoNameResolver: NameResolver,
		processingEnv: ProcessingEnvironment
) : KotlinExecutableElement(element, protoNameResolver, protoFunction.typeParameterList, processingEnv) {

	val isInline: Boolean = protoFunction.isInline
	val isInfix: Boolean = protoFunction.isInfix
	val isTailRec: Boolean = protoFunction.isTailRec
	val isSuspend: Boolean = protoFunction.isSuspend
	val isOperator: Boolean = protoFunction.isOperator
	val isExpectFunction: Boolean = protoFunction.isExpectFunction
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
					throw IllegalStateException(
							"can not construct KotlinTypeElement because this library was written" +
							"with the assumption that it is impossible to get an `Element` of" +
							"a local function but the enclosing element \"$enclosingElement\" " +
							"is of kind \"${enclosingElement.kind}\"${enclosingElement.asTypeElement()?.run {
							"with nesting kind \"$nestingKind\""
							}}")
				}

				else ->
					element.enclosingElement.toKotlinElement(processingEnv)?.let { parentElem ->
						(parentElem as? KotlinTypeElement)?.getKotlinFunction(element)
						?: throw IllegalStateException(
								"Could not convert $element to KotlinTypeParameterElement even " +
								"though it is apparently a Kotlin element")
					}
			}
	}

	override fun toString() = element.toString()
	override fun equals(other: Any?) = element.equals(other)
	override fun hashCode() = element.hashCode()
}

internal fun ProcessingEnvironment.findMatchingProtoFunction(functionElement: ExecutableElement,
															 protoFunctions: List<ProtoBuf.Function>,
															 nameResolver: NameResolver): ProtoBuf.Function?
		= with(this.kotlinMetadataUtils) {
	getFunctionOrNull(functionElement, nameResolver, protoFunctions)
}

internal fun ProcessingEnvironment.findMatchingFunctionElement(protoFunction: ProtoBuf.Function,
															   functionElements: List<ExecutableElement>,
															   nameResolver: NameResolver): ExecutableElement?
		= with(this.kotlinMetadataUtils) {
	val functionSignature = protoFunction.getJvmMethodSignature(nameResolver)

	val matchingFunctionElems = functionElements.filter { it.jvmMethodSignature == functionSignature }

	return when(matchingFunctionElems.size) {
		0 -> null
		1 -> matchingFunctionElems.single()
		else -> throw IllegalStateException(
				"More than one element in the list of functionElements matches the protoFunction's signature\n" +
				"protoFunction signature: $functionSignature\n" +
				"matching elements: $matchingFunctionElems")
	}
}

internal fun ProcessingEnvironment.doFunctionsMatch(functionElement: ExecutableElement,
													protoFunction: ProtoBuf.Function,
													nameResolver: NameResolver): Boolean
		= with(this.kotlinMetadataUtils) {
	functionElement.jvmMethodSignature == protoFunction.getJvmMethodSignature(nameResolver)
}

