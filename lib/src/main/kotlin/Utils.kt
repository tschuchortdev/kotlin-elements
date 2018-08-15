package com.tschuchort.kotlinelements
import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*

internal inline fun <reified R : Any> List<*>.castList() = map { it as R }

val ProcessingEnvironment.kotlinMetadataUtils: KotlinMetadataUtils
	get() {
		return object : KotlinMetadataUtils {
			override val processingEnv = this@kotlinMetadataUtils
		}
	}

/**
 * A local element is an element declared within an executable element.
 *
 * Note that elements declared in a local type are not local but members.
 */
internal fun Element.isLocal(): Boolean = when {
	enclosingElement == null -> false
	enclosingElement!!.asTypeElement() == null -> true
	else -> false
}


internal fun Element.asTypeElement(): TypeElement? = when(kind) {
	ElementKind.CLASS, ElementKind.ENUM,
	ElementKind.INTERFACE, ElementKind.ANNOTATION_TYPE -> this as? TypeElement
	else -> null
}

internal fun Element.asExecutableElement(): ExecutableElement? = when(kind) {
	ElementKind.METHOD, ElementKind.CONSTRUCTOR,
	ElementKind.STATIC_INIT, ElementKind.INSTANCE_INIT -> this as? ExecutableElement
	else -> null
}

internal fun String.removeFirstOccurance(literal: String) = replaceFirst(Regex(Regex.escape(literal)), "")