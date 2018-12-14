package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

class KotlinInterfaceElement internal constructor(
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

	override val types: Set<KotlinTypeElement>
		get() = enclosedElementsDelegate.types
}