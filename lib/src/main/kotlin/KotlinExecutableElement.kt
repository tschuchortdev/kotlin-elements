package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

abstract class KotlinExecutableElement internal constructor(
		private val element: ExecutableElement,
		private val protoNameResolver: NameResolver,
		private val typeParameterList: List<ProtoBuf.TypeParameter>,
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
				else -> IllegalStateException("ElementKind of ExecutableElement \"$element\" should never be \"${element.kind}\"")
			}
		}
	}

	//TODO("handle Kotlin receiver type")
	override fun getReceiverType(): TypeMirror = element.receiverType

	//TODO("handle Kotlin thrown types")
	override fun getThrownTypes(): List<TypeMirror> = element.thrownTypes

	//TODO("return KotlinVariableElement parameters")
	override fun getParameters(): List<VariableElement> = element.parameters

	override fun isVarArgs(): Boolean = element.isVarArgs

	override fun isDefault(): Boolean = element.isDefault

	override fun getDefaultValue(): AnnotationValue = element.defaultValue

	//TODO("handle Kotlin return types")
	override fun getReturnType(): TypeMirror = element.returnType

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
			= findMatchingProtoTypeParam(typeParamElem, typeParameterList, protoNameResolver)
			?.let { protoTypeParam -> KotlinTypeParameterElement(typeParamElem, protoTypeParam, processingEnv) }

	override fun toString() = element.toString()
	override fun equals(other: Any?) = element.equals(other)
	override fun hashCode() = element.hashCode()
}