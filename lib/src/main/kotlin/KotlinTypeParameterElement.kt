package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.TypeMirror

open class KotlinTypeParameterElement internal constructor(
		private val element: TypeParameterElement,
		protected val protoTypeParam: ProtoBuf.TypeParameter,
		protoNameResolver: NameResolver,
		processingEnv: ProcessingEnvironment
) : KotlinElement(element, protoNameResolver, processingEnv), TypeParameterElement {

	val variance: ProtoBuf.TypeParameter.Variance = protoTypeParam.variance
	val reified: Boolean = protoTypeParam.reified

	//TODO(bounds)
	override fun getBounds(): MutableList<out TypeMirror> = element.bounds

	override fun getGenericElement(): KotlinElement
			= KotlinElement.get(element.genericElement, processingEnv)
			?: throw IllegalStateException("Generic element of KotlinTypeParameterElement is not a KotlinElement")

	companion object {
		/*TODO(KotlinTypeParam factory)
			fun get(element: TypeParameterElement): KotlinTypeParameterElement? {
			val protoNameResolver = KotlinElement.getNameResolver(element)
			val protoTypeParam = element.simpleName .*/
	}

	override fun toString() = element.toString()
}
