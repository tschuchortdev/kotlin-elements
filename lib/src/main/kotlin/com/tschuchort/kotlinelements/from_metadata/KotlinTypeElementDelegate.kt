package com.tschuchort.kotlinelements.from_metadata

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.KJModality
import com.tschuchort.kotlinelements.mixins.KJOrigin
import com.tschuchort.kotlinelements.mixins.KJVisibility
import kotlinx.metadata.internal.metadata.ProtoBuf
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.TypeElement

internal class KotlinTypeElementDelegate(
		private val javaxElem: TypeElement,
		private val classProto: ProtoBuf.Class,
		override val origin: KJOrigin.Kotlin,
		private val processingEnv: ProcessingEnvironment
) : KJClassElement(), AnnotatedConstruct by javaxElem {

	override val enclosedElements: Set<KJElement> by lazy {
		(javaxElem.enclosedElements + javaxElem.typeParameters)
				.map { it.toKJElement(processingEnv) }.toSet()
	}

	override val simpleName: String = javaxElem.simpleName.toString()

	override fun asTypeMirror(): KJTypeMirror = javaxElem.asType().toKJTypeMirror(processingEnv)!!

	override val interfaces: Set<KJTypeMirror> by lazy {
		javaxElem.interfaces.mapNotNull { it.toKJTypeMirror(processingEnv) }.toSet()
	}

	override val superclass: KJTypeMirror? by lazy {
		javaxElem.superclass.toKJTypeMirror(processingEnv)
	}

	override val enclosingElement: KJElement by lazy {
		javaxElem.enclosingElement.toKJElement(processingEnv)
	}

	override fun javaxElement(): TypeElement = javaxElem

	override val visibility: KJVisibility = KJVisibility.fromProtoBuf(classProto.visibility!!)

	override val modality: KJModality = KJModality.fromProtoBuf(classProto.modality!!)

	override val qualifiedName: String = javaxElem.qualifiedName.toString()

	override val typeParameters: List<KJTypeParameterElement> by lazy {
		javaxElem.typeParameters.map { it.toKJElement(processingEnv) as KJTypeParameterElement }
	}

	override val isDataClass: Boolean = classProto.isDataClass

	override val isInner: Boolean = classProto.isInnerClass
}