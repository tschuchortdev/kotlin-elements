package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.isDataClass
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

/**
 * A regular Kotlin class declaration
 */
class KotlinClassElement internal constructor(
		javaElement: TypeElement,
		metadata: KotlinClassMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinTypeElement(javaElement, metadata, processingEnv), EnclosesKotlinConstructors,
	EnclosesKotlinFunctions, EnclosesKotlinProperties, EnclosesKotlinTypes, HasKotlinCompanion {

	val isDataClass: Boolean = protoClass.isDataClass

	override val companion: KotlinObjectElement? by lazy { enclosedElementsDelegate.companion }

	override val constructors: Set<KotlinConstructorElement> by lazy { enclosedElementsDelegate.constructors }

	override val functions: Set<KotlinFunctionElement> by lazy { enclosedElementsDelegate.functions }

	override val properties: Set<KotlinPropertyElement> by lazy { enclosedElementsDelegate.properties }

	override val types: Set<KotlinTypeElement> by lazy { enclosedElementsDelegate.types }

	override val enclosedKotlinElements: Set<KotlinElement> by lazy { enclosedElementsDelegate.kotlinElements }
}