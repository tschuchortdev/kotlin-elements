package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

/**
 * A declaration of an annotation class
 */
class KotlinAnnotationElement internal constructor(
		javaElement: TypeElement,
		metadata: KotlinClassMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinTypeElement(javaElement, metadata, processingEnv) {

	val parameters: List<KotlinAnnotationParameterElement> by lazy {
		javaElement.enclosedElements.map {
			check(it.kind == ElementKind.METHOD)
			KotlinAnnotationParameterElement(it as ExecutableElement, this, processingEnv)
		}
	}
}

/**
 * Parameter of an annotation class
 *
 * Annotation parameters are special because annotation classes
 * don't really have a constructor in Java. Instead they are
 * declared as methods of an interface and can be accessed
 * like a property in Kotlin
 */
class KotlinAnnotationParameterElement internal constructor(
		val javaElement: ExecutableElement,
		/**
		 * An annotation parameter is enclosed by its annotation class
		 */
		override val enclosingElement: KotlinAnnotationElement,
		processingEnv: ProcessingEnvironment
) : KotlinElement(processingEnv), AnnotatedConstruct by javaElement {

	/**
	 * The default value of this annotation parameter or
	 * `null` if it doesn't have one
	 */
	val defaultValue: Any? = javaElement.defaultValue

	override val simpleName: Name = javaElement.simpleName

	override fun asType(): TypeMirror = javaElement.asType()

	override fun equals(other: Any?): Boolean = javaElement.equals(other)
	override fun hashCode(): Int = javaElement.hashCode()
	override fun toString(): String = javaElement.toString() //TODO("annotation parameter toString")
}