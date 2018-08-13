package com.tschuchort.kotlinelements

import javax.lang.model.element.ExecutableElement
import me.eugeniomarletti.kotlin.metadata.jvm.getJvmMethodSignature
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.TypeElement

open class KotlinExecutableElement internal constructor(
		private val element: ExecutableElement,
		protected val protoFunction: ProtoBuf.Function,
		protoNameResolver: NameResolver,
		processingEnv: ProcessingEnvironment
) : KotlinElement(element, protoNameResolver, processingEnv), ExecutableElement {

	companion object {
		fun get(element: ExecutableElement, processingEnv: ProcessingEnvironment)
				: KotlinExecutableElement? = with(processingEnv.kotlinMetadataUtils) {

			when {
				element is KotlinExecutableElement -> element

				/*
				Afaik it is not possible (as of 08/2018) to get an `Element` of a local variable, function or class
				so we can assume that a valid `ExecutableElement` must always belong to a class or interface and the
				enclosing element is a `TypeElement`
				 */
				element.isLocal() -> {
					val enclosingElement = element.enclosingElement
					throw IllegalStateException("can not construct KotlinTypeElement because this library was written" +
												"with the assumption that it is impossible to get an `Element` of" +
												"a local function but the enclosing element \"$enclosingElement\" " +
												"is of kind \"${enclosingElement.kind}\"${enclosingElement.asTypeElement()?.run {
													"with nesting kind \"$nestingKind\""
												}}")
				}

				else -> {
					val parentTypeElem = element.enclosingElement.asTypeElement()
												 ?.let { typeElem -> KotlinTypeElement.get(typeElem, processingEnv) }

					if(parentTypeElem == null) {
						throw IllegalStateException(
								"can not convert \"$element\" to KotlinExecutableElement because parent element " +
								"\"$parentTypeElem\" of kind \"${element.enclosingElement.kind}\" is not a KotlinTypeElement")
					}

					val protoFunction = processingEnv.findMatchingProtoFunction(element, )

					protoFunction?.let { KotlinExecutableElement(element, protoFunction, protoNameResolver) }
				}
			}
		}
	}

	override fun toString() = element.toString()
}

internal fun ProcessingEnvironment.findMatchingProtoFunction(functionElement: ExecutableElement,
															 protoFunctions: List<ProtoBuf.Function>,
															 nameResolver: NameResolver): ProtoBuf.Function?
		= with(this.kotlinMetadataUtils) {
	getFunctionOrNull(functionElement, nameResolver, protoFunctions)
}

internal fun ProcessingEnvironment.doFunctionsMatch(functionElement: ExecutableElement,
													protoFunction: ProtoBuf.Function,
													nameResolver: NameResolver): Boolean
		= with(this.kotlinMetadataUtils) {
	functionElement.jvmMethodSignature == protoFunction.getJvmMethodSignature(nameResolver)
}

internal fun ProcessingEnvironment.findMatchingFunctionElement(protoFunction: ProtoBuf.Function,
															   functionElements: List<ExecutableElement>,
															   nameResolver: NameResolver): ExecutableElement?
		= with(this.kotlinMetadataUtils) {
	val functionSignature = protoFunction.getJvmMethodSignature(nameResolver)

	functionElements.firstOrNull { it.jvmMethodSignature == functionSignature }
}

