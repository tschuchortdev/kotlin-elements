package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

/**
 * A declaration of an annotation class
 */
class KotlinAnnotationElementImpl internal constructor(
		override val javaElement: TypeElement,
		metadata: KotlinClassMetadata
) : KotlinAnnotationElement(), AnnotatedConstruct by javaElement {

	override val enclosingElement: KotlinElement?
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

	override val simpleName: Name
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

	override fun asType(): TypeMirror {
	}

	override fun toString(): String {
	}

	override fun equals(other: Any?): Boolean {
	}

	override fun hashCode(): Int {
	}

	override val superclass: TypeMirror
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val typeParameters: List<KotlinTypeParameterElement>
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val qualifiedName: Name
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val visibility: KotlinVisibility
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val modality: KotlinModality
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val isExpect: Boolean
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val isExternal: Boolean
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

	override val isInner: Boolean = false

	override val interfaces: List<TypeMirror> = javaElement.interfaces

	override val parameters: List<KotlinAnnotationParameterElement> by lazy {
		javaElement.enclosedElements.map {
			check(it.kind == ElementKind.METHOD)
			KotlinAnnotationParameterElementImpl(it as ExecutableElement, this)
		}
	}
}

class KotlinAnnotationParameterElementImpl internal constructor(
		override val javaElement: ExecutableElement,
		override val enclosingElement: KotlinAnnotationElement
) : KotlinAnnotationParameterElement(), AnnotatedConstruct by javaElement {

	override val defaultValue: Any? = javaElement.defaultValue

	override val simpleName: Name = javaElement.simpleName

	override fun asType(): TypeMirror = javaElement.asType()

	override fun equals(other: Any?): Boolean = (javaElement == other)
	override fun hashCode(): Int = javaElement.hashCode()
	override fun toString(): String = javaElement.toString() //TODO("annotation parameter toString")
}