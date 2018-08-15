package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.TypeMirror

open class KotlinTypeParameterElement internal constructor(
		private val element: TypeParameterElement,
		protected val protoTypeParam: ProtoBuf.TypeParameter,
		processingEnv: ProcessingEnvironment
) : KotlinSubelement(element, processingEnv), TypeParameterElement {

	enum class Variance  { IN, OUT, INVARIANT }

	val variance: Variance = when(protoTypeParam.variance!!) {
		ProtoBuf.TypeParameter.Variance.IN -> Variance.IN
		ProtoBuf.TypeParameter.Variance.OUT -> Variance.OUT
		ProtoBuf.TypeParameter.Variance.INV -> Variance.INVARIANT
	}

	val reified: Boolean = protoTypeParam.reified

	//TODO(bounds)
	override fun getBounds(): List<TypeMirror> = element.bounds

	override fun getGenericElement(): KotlinSyntacticElement
			= element.genericElement.toKotlinElement(processingEnv)
			?: throw IllegalStateException("Generic element of KotlinTypeParameterElement is not a KotlinSyntacticElement")

	companion object {
		fun get(element: TypeParameterElement, processingEnv: ProcessingEnvironment): KotlinTypeParameterElement? {
			return if (element is KotlinTypeParameterElement)
				element
			else
				// to construct the KotlinTypeParameterElement, metadata of the parent element is needed
				// so the construction is delegated to the parent element
				element.enclosingElement.toKotlinElement(processingEnv)?.let { parentElem ->
					when (parentElem) {
						is KotlinTypeElement -> parentElem.getKotlinTypeParameter(element)
						is KotlinFunctionElement -> parentElem.getKotlinTypeParameter(element)
						//TODO("handle other kinds of Kotlin executable elements)
						else -> null
					} ?: throw IllegalStateException(
							"Could not convert $element to KotlinTypeParameterElement even " +
							"though it is apparently a Kotlin element")
				}
		}
	}
}

internal fun findMatchingProtoTypeParam(
		typeParamElem: TypeParameterElement,
		protoTypeParams: List<ProtoBuf.TypeParameter>,
		nameResolver: NameResolver
): ProtoBuf.TypeParameter? {
	val matchingProtoTypeParams = protoTypeParams.filter { protoTypeParam ->
		doTypeParamsMatch(typeParamElem, protoTypeParam, nameResolver)
	}

	return when(matchingProtoTypeParams.size) {
		0 -> null
		1 -> matchingProtoTypeParams.single()
		else -> throw IllegalStateException("More than one element in the list of protoTypeParams " +
											"matches the name of the TypeParameterElement")
	}
}

internal fun findMatchingTypeParamElement(
		protoTypeParam: ProtoBuf.TypeParameter,
		typeParamElems: List<TypeParameterElement>,
		nameResolver: NameResolver
): TypeParameterElement? {
	val matchingTypeParamElems = typeParamElems.filter { typeParamElem ->
		doTypeParamsMatch(typeParamElem, protoTypeParam, nameResolver)
	}

	return when(matchingTypeParamElems.size) {
		0 -> null
		1 -> matchingTypeParamElems.single()
		else -> throw IllegalStateException("More than one element in the list of TypeParameterElements " +
											"matches the name of the protoTypeParam")
	}
}

internal fun doTypeParamsMatch(
		typeParamElem: TypeParameterElement, protoTypeParam: ProtoBuf.TypeParameter, nameResolver: NameResolver
): Boolean
		= typeParamElem.simpleName.toString() == nameResolver.getString(protoTypeParam.name)

