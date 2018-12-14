package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeParameterElement


/**
 * An element that can have type parameters
 */
interface KotlinParameterizable {
	 val typeParameters: List<KotlinTypeParameterElement>
}

/**
 * Mixin for implementing [KotlinParameterizable] in multiple classes
 */
internal class KotlinParameterizableMixin(
		private val protoTypeParams: List<ProtoBuf.TypeParameter>,
		private val javaTypeParams: List<TypeParameterElement>,
		private val protoNameResolver: NameResolver,
		processingEnv: ProcessingEnvironment
) : KotlinParameterizable {

	override val typeParameters: List<KotlinTypeParameterElement> by lazy {
		protoTypeParams.zipWith(javaTypeParams) { protoTypeParam, javaTypeParam ->
			if (doTypeParamsMatch(javaTypeParam, protoTypeParam))
				KotlinTypeParameterElement(javaTypeParam, protoTypeParam, processingEnv)
			else
				throw AssertionError("Kotlin ProtoBuf.TypeParameters should always " +
									 "match up with Java TypeParameterElements")
		}
	}

	private fun doTypeParamsMatch(typeParamElem: TypeParameterElement, protoTypeParam: ProtoBuf.TypeParameter): Boolean
			= (typeParamElem.simpleName.toString() == protoNameResolver.getString(protoTypeParam.name))

}