package com.tschuchort.kotlinelements
import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.NestingKind
import javax.lang.model.element.TypeElement

internal inline fun <reified R : Any> List<*>.castList() = map { it as R }

val ProcessingEnvironment.kotlinMetadataUtils: KotlinMetadataUtils
	get() {
		return object : KotlinMetadataUtils {
			override val processingEnv = this@kotlinMetadataUtils
		}
	}

internal fun Element.isLocal(): Boolean =
		asTypeElement().let { it == null || it.nestingKind == NestingKind.LOCAL }
		|| enclosingElement?.isLocal() ?: false

internal fun Element.asTypeElement(): TypeElement? = when(kind) {
	ElementKind.CLASS, ElementKind.ENUM,
	ElementKind.INTERFACE, ElementKind.ANNOTATION_TYPE -> this as? TypeElement
	else -> null
}

internal fun String.removeFirstOccurance(literal: String) = replaceFirst(Regex(Regex.escape(literal)), "")