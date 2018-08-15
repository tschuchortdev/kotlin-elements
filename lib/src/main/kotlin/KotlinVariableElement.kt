package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.element.VariableElement

/*class KotlinVariableElement internal constructor(
		private val element: VariableElement,
		processingEnv: ProcessingEnvironment
): KotlinSyntacticElement(element, processingEnv), VariableElement {

	companion object {
		fun get(element: VariableElement, processingEnv: ProcessingEnvironment): KotlinVariableElement? {

		}
	}
}

internal fun findMatchingProtoValueParam(typeParamElem: TypeParameterElement, protoTypeParams: List<ProtoBuf.TypeParameter>,
										nameResolver: NameResolver): ProtoBuf.TypeParameter? {

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

internal fun findMatchingVariableElement(protoTypeParam: ProtoBuf.TypeParameter, typeParamElems: List<TypeParameterElement>,
										  nameResolver: NameResolver): TypeParameterElement? {

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

internal fun doTypeParamsMatch(typeParamElem: TypeParameterElement, protoTypeParam: ProtoBuf.TypeParameter, nameResolver: NameResolver)
		= typeParamElem.simpleName.toString() == nameResolver.getString(protoTypeParam.name)*/