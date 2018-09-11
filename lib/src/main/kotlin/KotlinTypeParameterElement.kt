package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.metadata.shadow.serialization.deserialization.getName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.TypeMirror

class KotlinTypeParameterElement internal constructor(
		val javaElement: TypeParameterElement,
		protoTypeParam: ProtoBuf.TypeParameter,
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

	override fun getEnclosingElement(): KotlinElement
			= javaElement.enclosingElement.correspondingKotlinElement(processingEnv)!!

	override fun getGenericElement(): KotlinElement = enclosingElement

	override fun getEnclosedElements(): List<Nothing> {
		// According to documentation (as of JDK 9), an ExecutableElement
		// is not considered to enclose any elements
		assert(javaElement.enclosedElements.isNotEmpty())
		return emptyList()
	}

	override fun equals(other: Any?) =
			if(other is KotlinTypeParameterElement)
				other.javaElement == javaElement
			else
				false

	override fun toString() = javaElement.toString()

	override fun hashCode() = javaElement.hashCode()
}

internal fun doTypeParamsMatch(typeParamElem: TypeParameterElement, protoTypeParam: ProtoBuf.TypeParameter,
							   nameResolver: NameResolver): Boolean
		= (typeParamElem.simpleName.toString() == nameResolver.getString(protoTypeParam.name))
		//TODO("also check if bounds of type parameters match")
