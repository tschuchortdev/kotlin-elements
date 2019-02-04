package com.tschuchort.kotlinelements.mixins

import com.tschuchort.kotlinelements.KotlinElement
import javax.lang.model.element.ExecutableElement

/**
 * Mixin interface for KotlinElements that have a synthetic
 * `elementName$annotations$ method to hold annotations for the
 * KotlinElement which may not have a 1-to-1 correspondance to a
 * regular Java element.
 */
interface HasSyntheticMethodForAnnotations {
	/**
	 * A synthetic method element that may be generated by the Kotlin compiler
	 * to hold annotations of the [KotlinElement] if it has any
	 */
	val javaAnnotationHolderElement: ExecutableElement?
}