package com.tschuchort.kotlinelements.from_metadata

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.KJVisibility
import com.tschuchort.kotlinelements.mixins.KJModality
import com.tschuchort.kotlinelements.mixins.KJOrigin
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.TypeParameterElement

internal class KmClassElement(
		private val javaxElem: TypeElement,
		private val kmClass: KmClass,
		private val processingEnv: ProcessingEnvironment
) : KJClassElement(), EnclosesKotlinElements, HasKotlinTypeParameters, AnnotatedConstruct by javaxElem {

	private val typeParametersDelegate = KotlinTypeParametersDelegate(
			javaxElem.typeParameters, kmClass.typeParameters,
			this, processingEnv
	)

	private val enclosedElementsDelegate = KotlinEnclosedElementsDelegate(
			enclosingKtElement = this,
			companionSimpleName = kmClass.companionObject,
			javaxElements = javaxElem.enclosedElements,
			kmConstructors = kmClass.constructors,
			kmFunctions = kmClass.functions,
			kmProperties = kmClass.properties,
			kmTypeAliases = kmClass.typeAliases,
			processingEnv = processingEnv
	)

	override val typeParameters: List<KJTypeParameterElement>
		get() = typeParametersDelegate.typeParameters

	override fun lookupEnclosedKJElementFor(enclosedJavaxElem: Element): KJElement
			= enclosedElementsDelegate.lookupEnclosedKJElementFor(enclosedJavaxElem)

	override fun lookupKJTypeParameterFor(javaxTypeParam: TypeParameterElement): KmParameterElement
			= typeParametersDelegate.lookupKJTypeParameterFor(javaxTypeParam)

	override val enclosedElements: Set<KJElement> get() = enclosedElementsDelegate.enclosedElements

	override val simpleName: String = javaxElem.simpleName.toString()

	override fun asTypeMirror(): KJTypeMirror = javaxElem.asType().toKJTypeMirror(processingEnv)!!

	override val origin: KJOrigin = KJOrigin.Kotlin

	override val interfaces: Set<KJTypeMirror> by lazy {
		TODO("interfaces to KJTypeMirror")
	}

	override val superclass: KJTypeMirror? by lazy {
		TODO("superclass to KJTypeMirror")
	}

	override val enclosingElement: KJElement by lazy {
		javaxElem.enclosingElement.toKJElement(processingEnv)
	}

	override fun asJavaxElement(): TypeElement = javaxElem

	override val visibility: KJVisibility = KJVisibility.fromKm(kmClass.flags)

	override val modality: KJModality = KJModality.fromKm(kmClass.flags)

	override val qualifiedName: String = javaxElem.qualifiedName.toString()

	override val isDataClass: Boolean = Flag.Class.IS_DATA(kmClass.flags)

	override val isInner: Boolean = Flag.Class.IS_INNER(kmClass.flags)
}