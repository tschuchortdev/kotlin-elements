package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.TypeMirror

open class KotlinTypeParameterElement internal constructor(
		private val element: TypeParameterElement,
		protected val protoTypeParam: ProtoBuf.TypeParameter,
		protoNameResolver: NameResolver
) : KotlinElement(element, protoNameResolver), TypeParameterElement {

	val variance: ProtoBuf.TypeParameter.Variance = protoTypeParam.variance
	val reified: Boolean = protoTypeParam.reified

	//TODO(bounds)
	override fun getBounds(): MutableList<out TypeMirror> = element.bounds

	override fun getGenericElement(): KotlinElement
			= KotlinElement.get(element.genericElement)
			?: throw IllegalStateException("Generic element of KotlinTypeParameterElement is not a KotlinElement")

	companion object {
		/*TODO(KotlinTypeParam factory)
			fun get(element: TypeParameterElement): KotlinTypeParameterElement? {
			val protoNameResolver = KotlinElement.getNameResolver(element)
			val protoTypeParam = element.simpleName .*/
	}
}

