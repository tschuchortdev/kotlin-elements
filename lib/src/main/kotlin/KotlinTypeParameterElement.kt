package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.TypeMirror

/*open class KotlinTypeParameterElement internal constructor(
		val javaElement: TypeParameterElement,
		protected val protoTypeParam: ProtoBuf.TypeParameter,
		processingEnv: ProcessingEnvironment
) : KotlinSubelement(processingEnv), TypeParameterElement by javaElement {

	enum class Variance  { IN, OUT, INVARIANT }

	val variance: Variance = when(protoTypeParam.variance!!) {
		ProtoBuf.TypeParameter.Variance.IN -> Variance.IN
		ProtoBuf.TypeParameter.Variance.OUT -> Variance.OUT
		ProtoBuf.TypeParameter.Variance.INV -> Variance.INVARIANT
	}

	val reified: Boolean = protoTypeParam.reified

	//TODO(bounds)
	override fun getBounds(): List<TypeMirror> = javaElement.bounds

	override fun getGenericElement(): KotlinElement
			= javaElement.genericElement.toKotlinElement(processingEnv)
			  ?: throw AssertionError("Generic element of KotlinTypeParameterElement should always be a KotlinElement")

	override fun getEnclosedElements(): List<Nothing> {
		// According to documentation (as of JDK 9), an ExecutableElement
		// is not considered to enclose any elements
		assert(javaElement.enclosedElements.isNotEmpty())
		return emptyList()
	}
}*/

internal fun doTypeParamsMatch(
		typeParamElem: TypeParameterElement, protoTypeParam: ProtoBuf.TypeParameter,
		nameResolver: NameResolver): Boolean
		= (typeParamElem.simpleName.toString() == nameResolver.getString(protoTypeParam.name))

