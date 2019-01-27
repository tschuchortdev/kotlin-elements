package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.classKind
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

/**
 * A Kotlin object declaration
 */
class KotlinObjectElementImpl internal constructor(
		javaElement: TypeElement,
		metadata: KotlinClassMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinTypeElement(javaElement, metadata, processingEnv),
	EnclosesKotlinTypes, EnclosesKotlinProperties, EnclosesKotlinFunctions {

	val isCompanion: Boolean = (protoClass.classKind == ProtoBuf.Class.Kind.COMPANION_OBJECT)

	override val functions: Set<KotlinFunctionElement> by lazy { enclosedElementsDelegate.functions }

	override val properties: Set<KotlinPropertyElement> by lazy { enclosedElementsDelegate.properties }

	override val kotlinTypes: Set<KotlinTypeElement> by lazy { enclosedElementsDelegate.types }

	override val enclosedKotlinElements: Set<KotlinElement> by lazy { enclosedElementsDelegate.kotlinElements }
}