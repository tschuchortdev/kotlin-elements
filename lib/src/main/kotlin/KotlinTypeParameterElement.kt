package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.Name
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.TypeMirror

/**
 * Type parameter of a [KotlinParameterizable] element
 */
class KotlinTypeParameterElement internal constructor(
		val javaElement: TypeParameterElement,
		protoTypeParam: ProtoBuf.TypeParameter,
		override val enclosingElement: KotlinElement
) : KotlinElement(), AnnotatedConstruct by javaElement {

	//TODO("test type parameter annotations")

	/** Variance of a type parameter */
	enum class Variance  { IN, OUT, INVARIANT }

	/** Variance of this type parameter */
	val variance: Variance = when(protoTypeParam.variance!!) {
		ProtoBuf.TypeParameter.Variance.IN -> Variance.IN
		ProtoBuf.TypeParameter.Variance.OUT -> Variance.OUT
		ProtoBuf.TypeParameter.Variance.INV -> Variance.INVARIANT
	}

	/** Whether this type parameter is reified */
	val reified: Boolean = protoTypeParam.reified

	/** The bounds of this type parameter */
	val bounds: List<TypeMirror> = javaElement.bounds //TODO("bounds KotlinTypeMirrors")

	/**
	 * The [KotlinElement] that is parameterized by this [KotlinTypeParameterElement].
	 * Same as the [enclosingElement]
	 */
	val genericElement: KotlinElement by lazy { enclosingElement }

	override val simpleName: Name = javaElement.simpleName

	//TODO("translate type parameter TypeMirror")
	override fun asType(): TypeMirror = javaElement.asType()

	override fun equals(other: Any?) = (other as? KotlinTypeParameterElement)?.javaElement == javaElement
	override fun hashCode() = javaElement.hashCode()
	override fun toString() = javaElement.toString()
}
