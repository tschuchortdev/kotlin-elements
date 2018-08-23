package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*

/**
 * An [Element] that has a 1:1 correspondence to an actual syntactic element
 * in the Kotlin source code.
 * That excludes elements that are generated by the Kotlin compiler implicitly
 * for Java-compatibility reasons such fields, getters, setters, interface
 * default implementations and so on
 */
abstract class KotlinElement internal constructor(
		protected val processingEnv: ProcessingEnvironment
) : Element {
	companion object {
		/**
		 * Returns the [NameResolver] of the closest parent element (or this element) that has one
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
		 * Returns the [KotlinMetadata] of the closest parent element (or this element) that has one
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
	 * [PackageElement]s are also considered top level
	 */
	//TODO(make sure isTopLevel handles multifile class facades and all that stuff correctly)
	val isTopLevel =
			enclosingElement?.run { kind == ElementKind.PACKAGE || kotlinMetadata is KotlinPackageMetadata }
			?: true

	abstract override fun getEnclosedElements(): List<KotlinElement>

	abstract override fun getEnclosingElement(): Element?

	protected fun ExecutableElement.jvmSignature() = getJvmMethodSignature(processingEnv)

	abstract override fun toString(): String
	abstract override fun equals(other: Any?): Boolean
	abstract override fun hashCode(): Int
}

internal fun ExecutableElement.getJvmMethodSignature(processingEnv: ProcessingEnvironment): String
		= with(processingEnv.kotlinMetadataUtils) {
	this@getJvmMethodSignature.jvmMethodSignature
}

fun Element.isKotlinElement() = KotlinElement.getNameResolver(this) != null

/**
 * a Java [Element] that was generated by the Kotlin compiler implicitly
 * and doesn't have a 1:1 correspondence with actual syntactic
 * elements in the Kotlin source code
 */
interface KotlinSyntheticElement : Element

/*
fun Element.toKotlinElement(processingEnv: ProcessingEnvironment): KotlinElement? {
	if(this is KotlinElement)
		return this

	if(!isKotlinElement())
		return null

	when (kind) {
		ElementKind.CLASS, ElementKind.ENUM,
		ElementKind.INTERFACE, ElementKind.ANNOTATION_TYPE -> with(this as TypeElement) {
			(kotlinMetadata as? KotlinClassMetadata)?.let { metadata ->
				KotlinTypeElement(this, metadata, processingEnv)
			}
			?: (enclosingElement?.toKotlinElement(processingEnv) as? KotlinTypeElement)
					?.let { enclosingTypeElem ->
				if (qualifiedName.toString() == enclosingTypeElem.qualifiedName.toString() + ".DefaultImpl"
					&& enclosingTypeElem.kind == ElementKind.INTERFACE) {
					KotlinInterfaceDefaultImpls(this, enclosingTypeElem, processingEnv)
				}
			}
		}

		ElementKind.METHOD, ElementKind.CONSTRUCTOR,
		ElementKind.INSTANCE_INIT, ElementKind.STATIC_INIT ->
			KotlinExecutableElement.get(element as ExecutableElement, processingEnv)

		ElementKind.TYPE_PARAMETER ->
			KotlinTypeParameterElement.get(element as TypeParameterElement, processingEnv)

		ElementKind.FIELD, ElementKind.ENUM_CONSTANT, ElementKind.PARAMETER,
		ElementKind.LOCAL_VARIABLE, ElementKind.RESOURCE_VARIABLE,
		ElementKind.EXCEPTION_PARAMETER -> TODO("implement KotlinVariableElement")

		ElementKind.MODULE -> TODO("handle module elements gracefully")

		ElementKind.PACKAGE -> KotlinPackageElement(this as PackageElement, (kotlinMetadata as KotlinPackageMetadata), processingEnv)

		ElementKind.OTHER -> throw UnsupportedOperationException(
				"Can not convert element \"$element\" of unsupported kind \"${element.kind}\" to KotlinSyntacticElement")

		null -> throw NullPointerException("Can not convert to KotlinSyntacticElement: kind of element \"$element\" was null")

		else -> throw UnsupportedOperationException(
				"Can not convert element \"$element\" of unsupported kind \"${element.kind}\" to KotlinSyntacticElement.\n" +
				"Element ElementKind was probably added to the Java language at a later date")
	}
}*/
