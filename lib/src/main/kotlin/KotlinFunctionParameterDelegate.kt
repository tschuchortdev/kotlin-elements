package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.lang.model.element.VariableElement

/**
 * Returns the Kotlin parameters for a [KotlinExecutableElement].
 */
internal class KotlinFunctionParameterDelegate(
	enclosingElement: KotlinExecutableElement,
	protoParams: List<ProtoBuf.ValueParameter>,
	javaParams: List<VariableElement>,
	private val protoNameResolver: NameResolver
) {
	val parameters: List<KotlinFunctionParameterElement> by lazy{
		protoParams.zipWith(javaParams) { protoParam, javaParam ->
			if (doParametersMatch(javaParam, protoParam))
				KotlinFunctionParameterElement(javaParam, protoParam, enclosingElement)
			else
				throw AssertionError("Kotlin ProtoBuf.Parameters should always " +
						"match up with Java VariableElements")
		}
	}

	private fun doParametersMatch(javaParamElem: VariableElement, protoParam: ProtoBuf.ValueParameter)
			= (javaParamElem.simpleName.toString() == protoNameResolver.getString(protoParam.name))
			//TODO("also check that parameter kotlinTypes match")
}