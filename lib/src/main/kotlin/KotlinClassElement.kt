package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.isDataClass
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

/**
 * A regular Kotlin class declaration
 */
class KotlinClassElementImpl internal constructor(
		javaElement: TypeElement,
		metadata: KotlinClassMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinClassElement(javaElement, metadata, processingEnv), EnclosesKotlinConstructors,
	EnclosesKotlinFunctions, EnclosesKotlinProperties, EnclosesKotlinTypes, HasKotlinCompanion {

	override val isDataClass: Boolean = protoClass.isDataClass

	override val companion: KotlinObjectElement? by lazy { enclosedElementsDelegate.companion }

	override val constructors: Set<KotlinConstructorElement> by lazy { enclosedElementsDelegate.constructors }

	override val functions: Set<KotlinFunctionElement> by lazy { enclosedElementsDelegate.functions }

	override val properties: Set<KotlinPropertyElement> by lazy { enclosedElementsDelegate.properties }

	override val kotlinTypes: Set<KotlinTypeElement> by lazy { enclosedElementsDelegate.types }

	override val enclosedKotlinElements: Set<KotlinElement> by lazy { enclosedElementsDelegate.kotlinElements }
}