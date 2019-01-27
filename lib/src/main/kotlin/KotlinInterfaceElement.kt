package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

/**
 * A Kotlin interface declaration
 */
class KotlinInterfaceElementImpl internal constructor(
		javaElement: TypeElement,
		metadata: KotlinClassMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinTypeElement(javaElement, metadata, processingEnv), EnclosesKotlinElements,
	EnclosesKotlinFunctions, EnclosesKotlinProperties, EnclosesKotlinTypes {

	val companion: KotlinTypeElement?
		get() = enclosedElementsDelegate.companion

	override val enclosedKotlinElements: Set<KotlinElement>
		get() = enclosedElementsDelegate.kotlinElements

	override val functions: Set<KotlinFunctionElement>
		get() = enclosedElementsDelegate.functions

	override val properties: Set<KotlinPropertyElement>
		get() = enclosedElementsDelegate.properties

	override val kotlinTypes: Set<KotlinTypeElement>
		get() = enclosedElementsDelegate.types

	val interfaceDefaultImpls: KotlinInterfaceDefaultImplElement?
		get() = TODO("return InterfaceDefaultImpls")
}

/**
 * A class that contains default implementations of methods
 * in a Kotlin interface
 */
class KotlinInterfaceDefaultImplElement(
		override val javaElement: TypeElement,
		metadata: KotlinSyntheticClassMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinCompatElement(javaElement) {

	val correspondingInterface: KotlinInterfaceElement by lazy {
		enclosingElement as KotlinInterfaceElement
	}

	override val enclosingElement: KotlinRelatedElement by lazy {
		javaElement.enclosingElement.asKotlin(processingEnv)!!
	}
}