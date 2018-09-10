package com.tschuchort.kotlinelements
import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*

internal inline fun <reified R : Any> List<*>.castList() = map { it as R }

/**
 * checks if all elements in the list are equal with respect to the comparator
 */
internal inline fun <T> List<T>.allEqualBy(crossinline areEqual: (T, T) -> Boolean): Boolean {
	if(size < 2)
		return true

	val first = first()

	for(other in this) {
		if(!areEqual(first, other))
			return false
	}

	return true
}

/**
 * checks if all elements in the list are equal with respect to the metric (a property for example)
 */
internal inline fun <T,R> List<T>.allEqualBy(crossinline metric: T.() -> R): Boolean {
	if(size < 2)
		return true

	val firstMetric = first().metric()

	for(other in this) {
		if(firstMetric != other.metric())
			return false
	}

	return true
}

internal inline fun <T,S,R> List<T>.zipWith(other: List<S>, crossinline zipper: (T,S) -> R): List<R>
		= zip(other).map { (fst, snd) -> zipper(fst, snd) }

/**
 * checks whether [this] is equal to a subset of [superset]
 * i.e. every element of [this] is equal to some (not necessarily
 * distinct) element in [superset]
 */
internal fun <T> List<T>.equalsSubset(superset: List<T>)
		= superset.toHashSet().containsAll(this)

//TODO("make internal for release")
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