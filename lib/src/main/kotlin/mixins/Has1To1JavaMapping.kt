package mixins

import javax.lang.model.element.Element

/**
 * Mixin interface for Kotlin elements that have a 1-to-1 mapping
 * to a Java element
 */
interface Has1To1JavaMapping {
	val javaElement: Element
}