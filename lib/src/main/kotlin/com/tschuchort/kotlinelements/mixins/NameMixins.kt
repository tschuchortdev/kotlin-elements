package com.tschuchort.kotlinelements.mixins

/**
 * Mixin interface for elements that may be unnamed,
 * specifically modules and packages.
 */
interface CanBeUnnamed {
	/** Whether this element is unnamed */
	val isUnnamed: Boolean
}

/**
 * Mixin interface for elements that have a simple (non-qualified) name.
 */
interface HasSimpleName {
	/** The simple (non-qualified) name of this element. */
	val simpleName: String
}

/**
 * Mixin interface for elements that have a qualified name.
 */
interface HasQualifiedName : HasSimpleName {
	/** Returns the fully qualified name of an element. */
	val qualifiedName: String
}

