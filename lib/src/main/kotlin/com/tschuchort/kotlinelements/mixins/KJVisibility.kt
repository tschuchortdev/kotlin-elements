package com.tschuchort.kotlinelements.mixins

import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import kotlin.AssertionError

/**
 * Mixin interface for Kotlin elements that have visibility.
 */
interface HasVisibility {
	/**
	 * visibility modifier including implicit modifiers (`public` in case for non-local elements)
	 *
	 * one of: [KJVisibility.INTERNAL], [KJVisibility.PRIVATE], [KJVisibility.PROTECTED],
	 * [KJVisibility.PUBLIC], [KJVisibility.PRIVATE_TO_THIS], [KJVisibility.LOCAL]
	 */
	val visibility: KJVisibility
}

enum class KJVisibility {
	/** Visible only in the enclosing module */
	INTERNAL,

	/** Visible only in the enclosing package (like a Java element with no visibility modifier */
	PACKAGE_PRIVATE,

	/** Only visible from instances of the enclosing declaration container */
	PRIVATE,

	/** Visible from instances of the enclosing declaration container or its subclasses */
	PROTECTED,

	/** Visible everywhere */
	PUBLIC,

	/**
	 * `private` element that references a non-reified contravariant type parameter
	 * (with `in`-variance) in its signature. This means that only the instance
	 * that the `private` member belongs to can access it. In contrast to regular
	 * `private` members which can also be accessed by other instances of the same type
	 * such as used in an `equals` method
	 *
	 * Example:
	 *  ```kotlin
	 *  class A<in T>(t: T) {
	 *  	private val t: T = t // visibility for t is PRIVATE_TO_THIS
	 *
	 *      fun test() {
	 *          val x: T = t // correct
	 *          val y: T = this.t // also correct
	 *      }
	 *      fun createOrNull(a: A<String>) {
	 *         val x: String = a.t // incorrect, because a.t can be Any
	 *      }
	 *  }
	 *  ```
	 *
	 *  See: https://github.com/JetBrains/kotlin/blob/4b138ae2b8a63e79e27567fddeecf6c5af60b458/core/descriptors/src/org/jetbrains/kotlin/descriptors/Visibilities.java#L91
	 */
	PRIVATE_TO_THIS,

	/**
	 * Visible only in the local scope (like inside a function body)
	 */
	LOCAL;

	fun asJavaxModifier(): Modifier? = when (this) {
		INTERNAL -> null
		PACKAGE_PRIVATE -> null
		PRIVATE -> Modifier.PRIVATE
		PROTECTED -> Modifier.PROTECTED
		PUBLIC -> Modifier.PUBLIC
		PRIVATE_TO_THIS -> Modifier.PRIVATE
		LOCAL -> null
	}

	companion object {
		fun fromJavax(javaxElem: Element)
				: KJVisibility = with(javaxElem.modifiers) {
			when {
				contains(Modifier.PUBLIC)    -> PUBLIC
				contains(Modifier.PROTECTED) -> PROTECTED
				contains(Modifier.PRIVATE)   -> PRIVATE
				else                         -> PACKAGE_PRIVATE
			}
		}

		internal fun fromKm(flags: Flags) = when {
			Flag.IS_INTERNAL(flags) -> INTERNAL
			Flag.IS_PRIVATE(flags) -> PRIVATE
			Flag.IS_PROTECTED(flags) -> PROTECTED
			Flag.IS_PUBLIC(flags) -> PUBLIC
			Flag.IS_PRIVATE_TO_THIS(flags) -> PRIVATE_TO_THIS
			Flag.IS_LOCAL(flags) -> LOCAL
			else -> throw AssertionError("No visibility flag found in metadata flags")
		}
	}
}

