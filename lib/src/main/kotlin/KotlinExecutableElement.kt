package com.tschuchort.kotlinelements

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

abstract class KotlinExecutableElement internal constructor(
		private val element: ExecutableElement,
		processingEnv: ProcessingEnvironment
) : KotlinElement(element, processingEnv), ExecutableElement {

	companion object {
		fun get(element: ExecutableElement, processingEnv: ProcessingEnvironment): KotlinExecutableElement? {
			return if(element is KotlinExecutableElement)
				element
			else when(element.kind) {
				ElementKind.METHOD -> KotlinFunctionElement.get(element, processingEnv)
				ElementKind.CONSTRUCTOR -> KotlinConstructorElement.get(element, processingEnv)
				ElementKind.INSTANCE_INIT, ElementKind.STATIC_INIT -> throw UnsupportedOperationException(
						"Can not convert element $element of kind ${element.kind} to KotlinElement")
				else -> throw IllegalStateException(
						"ElementKind of ExecutableElement \"$element\" should never be \"${element.kind}\"")
			}
		}
	}

	abstract override fun getTypeParameters(): List<KotlinTypeParameterElement>

	//TODO("handle Kotlin receiver type")
	override fun getReceiverType(): TypeMirror? = element.receiverType

	//TODO("handle Kotlin thrown types")
	override fun getThrownTypes(): List<TypeMirror> = element.thrownTypes

	//TODO("return KotlinVariableElement parameters")
	override fun getParameters(): List<VariableElement> = element.parameters

	override fun isVarArgs(): Boolean = element.isVarArgs

	override fun isDefault(): Boolean = element.isDefault

	override fun getDefaultValue(): AnnotationValue = element.defaultValue

	//TODO("handle Kotlin return types")
	override fun getReturnType(): TypeMirror = element.returnType
}