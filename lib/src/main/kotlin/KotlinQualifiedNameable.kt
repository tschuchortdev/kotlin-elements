package com.tschuchort.kotlinelements

import javax.lang.model.element.Name

/**
 * A Kotlin javaElement that has a qualified name
 */
interface KotlinQualifiedNameable {
	/**
	 * Returns the fully qualified name of an javaElement.
	 *
	 * @return the fully qualified name of an javaElement
	 */
	val qualifiedName: Name
}