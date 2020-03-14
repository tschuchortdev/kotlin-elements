package com.tschuchort.kotlinelements.from_metadata

import com.tschuchort.kotlinelements.KJElement
import com.tschuchort.kotlinelements.KJTypeParameterElement
import com.tschuchort.kotlinelements.zipWith
import kotlinx.metadata.KmTypeParameter
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeParameterElement

/**
 * Delegate for implementing [HasKotlinTypeParameters] in multiple classes
 */
internal class KotlinTypeParametersDelegate(
		private val javaxTypeParams: List<TypeParameterElement>,
		private val kmTypeParams: List<KmTypeParameter>,
		private val enclosingKtElement: KJElement,
		private val processingEnv: ProcessingEnvironment
) : HasKotlinTypeParameters {
	private val convertedElements: LinkedHashMap<TypeParameterElement, KmParameterElement> by lazy {
		check(kmTypeParams.size == javaxTypeParams.size) {
			"List of metadata type parameters should be the same length as list of Javax type parameters"
		}

		val matchedTypeParams = kmTypeParams.zipWith(javaxTypeParams) { kmTypeParam, javaxTypeParam ->
			if (doTypeParamsMatch(javaxTypeParam, kmTypeParam)) {
				val kjTypeParam = KmParameterElement(
						javaxTypeParam, kmTypeParam,
						enclosingKtElement, processingEnv
				)

				return@zipWith Pair(javaxTypeParam, kjTypeParam)
			}
			else {
				throw AssertionError("Metadata type parameter should always match up with Javax type parameter")
			}
		}

		linkedMapOf(*matchedTypeParams.toTypedArray())
	}

	override val typeParameters: List<KJTypeParameterElement> get() = convertedElements.values.toList()

	private fun doTypeParamsMatch(typeParamElem: TypeParameterElement, kmTypeParam: KmTypeParameter): Boolean
			= (typeParamElem.simpleName.toString() == kmTypeParam.name)

	override fun lookupKJTypeParameterFor(javaxTypeParam: TypeParameterElement): KmParameterElement {
			return convertedElements[javaxTypeParam] ?: throw IllegalArgumentException(
					"Could not lookup KJTypeParameterElement for TypeParameterElement $javaxTypeParam"
			)
	}
}
