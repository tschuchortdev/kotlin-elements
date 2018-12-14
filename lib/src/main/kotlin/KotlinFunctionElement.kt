package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*

class KotlinFunctionElement internal constructor(
		javaElement: ExecutableElement,
		javaOverloadElements: List<ExecutableElement>,
		enclosingElement: KotlinElement,
		private val protoFunction: ProtoBuf.Function,
		private val protoNameResolver: NameResolver,
		processingEnv: ProcessingEnvironment
) : KotlinExecutableElement(javaElement, javaOverloadElements, enclosingElement, processingEnv),
	KotlinParameterizable by KotlinParameterizableMixin(
			protoFunction.typeParameterList, javaElement.typeParameters, protoNameResolver, processingEnv),
	HasKotlinModality, HasKotlinVisibility {

	val isInline: Boolean = protoFunction.isInline
	val isInfix: Boolean = protoFunction.isInfix
	val isTailRec: Boolean = protoFunction.isTailRec
	val isSuspend: Boolean = protoFunction.isSuspend
	val isOperator: Boolean = protoFunction.isOperator

	//TODO("remove kName debug property")
	val kName = protoNameResolver.getString(protoFunction.name)
	//TODO("is free function")
	//TODO("is extension function")

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
	 * one of: [KotlinModality.FINAL], [KotlinModality.OPEN], [KotlinModality.ABSTRACT]
	 */
	override val modality: KotlinModality = KotlinModality.fromProtoBuf(protoFunction.modality!!)
			.also { assert(it != KotlinModality.SEALED) }

	override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoFunction.visibility!!)

	override val parameters: List<KotlinParameterElement> by lazy {
		protoFunction.valueParameterList.zipWith(javaElement.parameters) { protoParam, javaParam ->
			if (doParametersMatch(javaParam, protoParam, protoNameResolver))
				KotlinParameterElement(javaParam, protoParam, this, processingEnv)
			else
				throw AssertionError("Kotlin ProtoBuf.Parameters should always " +
									 "match up with Java VariableElements")
		}
	}

	override val simpleName: Name
			// if JvmName is used, the name of the Kotlin function may be different than the jvm name
			get() = processingEnv.elementUtils.getName(protoNameResolver.getString(protoFunction.name))

	override fun toString(): String {
		// if JvmName is used, the name of the Kotlin function may be different than the jvm name
		val javaElemString = javaElement.toString()
		assert(Regex("[^\\(\\)]+?\\([^\\(\\)]*?\\)[^\\(\\)]*").matches(javaElemString))
		return simpleName.toString() + "(" + javaElemString.substringAfter("(")
	}
}
