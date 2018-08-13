package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*

open class KotlinElement internal constructor(
		private val element: Element,
		protected val protoNameResolver: NameResolver,
		protected val processingEnv: ProcessingEnvironment
) : Element by element {

	protected val typeUtils = processingEnv.typeUtils
	protected val elementUtils = processingEnv.elementUtils

	companion object {
		fun get(element: Element, processingEnv: ProcessingEnvironment): KotlinElement? =
				if(element is KotlinElement)
					element
				else
					getNameResolver(element)?.let { nameResolver ->
						KotlinElement(element, nameResolver, processingEnv)
					}

		/**
		 * returns the `NameResolver` of the closest parent element (or this element) that has one
		 */
		internal fun getNameResolver(elem: Element): NameResolver? {
			val metadata = elem.kotlinMetadata
			return when(metadata) {
				is KotlinPackageMetadata -> metadata.data.nameResolver
				is KotlinClassMetadata -> metadata.data.nameResolver
				else -> elem.enclosingElement?.let(::getNameResolver)
			}
		}

		/**
		 * returns the `KotlinMetadata` of the closest parent element (or this element) that has one
		 */
		internal fun getMetadata(elem: Element): KotlinMetadata?
				= elem.kotlinMetadata ?: elem.enclosingElement?.let(::getMetadata)
	}

	/**
	 * Whether this element is a top level element in Kotlin source code
	 *
	 * Only the location in Kotlin code is considered. Some elements (like free functions or multiple
	 * classes in a single file) are not strictly top level from a Java point of view because the compiler
	 * generates class facades to hold them even though they would appear to be top level in Kotlin
	 *
	 * PackageElements are also considered top level
	 */
	//TODO(make sure isTopLevel handles multifile class facades and all that stuff correctly)
	val isTopLevel =
			enclosingElement?.run { kind == ElementKind.PACKAGE || kotlinMetadata is KotlinPackageMetadata }
			?: true

	override fun getEnclosingElement(): KotlinElement? = element.enclosingElement?.toKotlinElement(processingEnv)

	override fun getEnclosedElements(): List<KotlinElement> {
	}

	override fun toString() = element.toString()
}

fun Element.isKotlinElement() = KotlinElement.getNameResolver(this) != null

fun Element.toKotlinElement(processingEnv: ProcessingEnvironment): KotlinElement = when(kind) {
	ElementKind.PACKAGE -> KotlinPackageElement.get(this as PackageElement, processingEnv)

	ElementKind.CLASS, ElementKind.ENUM,
	ElementKind.INTERFACE, ElementKind.ANNOTATION_TYPE -> KotlinTypeElement.get(this as TypeElement, processingEnv)

	ElementKind.METHOD, ElementKind.CONSTRUCTOR,
	ElementKind.INSTANCE_INIT, ElementKind.STATIC_INIT -> KotlinExecutableElement.get()

	ElementKind.TYPE_PARAMETER -> KotlinTypeParameterElement.get()

	ElementKind.FIELD, ElementKind.ENUM_CONSTANT, ElementKind.PARAMETER,
	ElementKind.LOCAL_VARIABLE, ElementKind.RESOURCE_VARIABLE,
	ElementKind.EXCEPTION_PARAMETER -> KotlinVariableElement.get(this as VariableElement, processingEnv)

	//TODO(handle module elements gracefully)
	ElementKind.MODULE, ElementKind.OTHER -> throw UnsupportedOperationException(
			"Can not convert element \"$this\" of unsupported kind \"$kind\" to KotlinElement")

	null -> NullPointerException("Can not convert to KotlinElement: kind of element \"$this\" was null")

	else -> throw UnsupportedOperationException(
			"Can not convert element \"$this\" of unsupported kind \"$kind\" to KotlinElement.\n" +
			"This ElementKind was probably added to the Java language at a later date")
}!!
