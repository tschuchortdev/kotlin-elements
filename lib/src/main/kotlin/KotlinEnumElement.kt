package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.Name
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * A Kotlin enum class declaration
 */
class KotlinEnumElementImpl internal constructor(
		javaElement: TypeElement,
		metadata: KotlinClassMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinTypeElement(javaElement, metadata, processingEnv), EnclosesKotlinConstructors,
	EnclosesKotlinFunctions, EnclosesKotlinProperties, EnclosesKotlinTypes, HasKotlinCompanion {

	val enumConstants: List<KotlinEnumConstantElement> get() = TODO("Kotlin enum constants list")

	override val companion: KotlinObjectElement? by lazy { enclosedElementsDelegate.companion }

	override val constructors: Set<KotlinConstructorElement> by lazy { enclosedElementsDelegate.constructors }

	override val functions: Set<KotlinFunctionElement> by lazy { enclosedElementsDelegate.functions }

	override val properties: Set<KotlinPropertyElement> by lazy { enclosedElementsDelegate.properties }

	override val kotlinTypes: Set<KotlinTypeElement> by lazy { enclosedElementsDelegate.types }

	override val enclosedKotlinElements: Set<KotlinElement> by lazy { enclosedElementsDelegate.kotlinElements }
}

/**
 * A Kotlin enum constant within an enum class declaration
 */
class KotlinEnumConstantElementImpl internal constructor(
		val javaElement: TypeElement
) : KotlinElement(), AnnotatedConstruct by javaElement {
	override val enclosingElement: KotlinEnumElement
		get() = TODO("implement enum constant enclosing element")

	//TODO("check if enum constants are kotlinTypes in all cases")

	override val simpleName: Name = javaElement.simpleName

	override fun asType(): TypeMirror = javaElement.asType()

	override fun equals(other: Any?): Boolean = javaElement.equals(other)
	override fun hashCode(): Int = javaElement.hashCode()
	override fun toString(): String = javaElement.toString()
}