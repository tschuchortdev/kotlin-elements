package com.tschuchort.kotlinelements.from_metadata

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.HasTypeParameters
import com.tschuchort.kotlinelements.mixins.KJOrigin
import com.tschuchort.kotlinelements.mixins.KJVariance
import kotlinx.metadata.Flag
import kotlinx.metadata.KmTypeParameter
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.TypeParameterElement

/** Type parameter of a [HasTypeParameters] element */
class KmParameterElement internal constructor(
		private val javaxElem: TypeParameterElement,
		private val kmTypeParameter: KmTypeParameter,
		override val enclosingElement: KJElement,
		private val processingEnv: ProcessingEnvironment
) : KJTypeParameterElement(), AnnotatedConstruct by javaxElem {

	private val typeMirror by lazy { javaxElem.asType().toKJTypeMirror(processingEnv)!! }

	override fun asTypeMirror(): KJTypeMirror = typeMirror

	override val origin: KJOrigin = KJOrigin.Kotlin

	override fun asJavaxElement(): TypeParameterElement = javaxElem

	//TODO("test type parameter annotations")

	/** Variance of this type parameter */
	override val variance: KJVariance = KJVariance.fromKm(kmTypeParameter.variance)

	/** Whether this type parameter is reified */
	override val reified: Boolean = Flag.TypeParameter.IS_REIFIED(kmTypeParameter.flags)

	/** The bounds of this type parameter */
	override val bounds: Set<KJTypeMirror> by lazy {
		kmTypeParameter.upperBounds.map { bound ->
			TODO("Bounds to KJTypeMirror")
		}.toSet()
	}

	override val simpleName: String = javaxElem.simpleName.toString()
}