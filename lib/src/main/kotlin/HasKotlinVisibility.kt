package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.lang.model.element.Element

/**
 * Mixin interface for Kotlin elements that have visibility.
 */
interface HasKotlinVisibility {
	/**
	 * visibility modifier including implicit modifiers (`public` in case for non-local elements)
	 *
	 * one of: [KotlinVisibility.INTERNAL], [KotlinVisibility.PRIVATE], [KotlinVisibility.PROTECTED],
	 * [KotlinVisibility.PUBLIC], [KotlinVisibility.PRIVATE_TO_THIS], [KotlinVisibility.LOCAL]
	 */
	val visibility: KotlinVisibility
}

enum class KotlinVisibility {
	/** has `internal` modifier */
	INTERNAL,

	/** has `private` modifier */
	PRIVATE,

	/** has `protected` modifier */
	PROTECTED,

	/** has `public` or no visibility modifier */
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
	 *      fun foo(a: A<String>) {
	 *         val x: String = a.t // incorrect, because a.t can be Any
	 *      }
	 *  }
	 *  ```
	 *
	 *  See: https://github.com/JetBrains/kotlin/blob/4b138ae2b8a63e79e27567fddeecf6c5af60b458/core/descriptors/src/org/jetbrains/kotlin/descriptors/Visibilities.java#L91
	 */
	PRIVATE_TO_THIS,

	/**
	 * is a local element (inside an executable element)
	 * and thus has no explicit visibility modifier
	 */
	LOCAL;

	companion object {
		internal fun fromProtoBuf(protoVisibility: ProtoBuf.Visibility): KotlinVisibility
				= when(protoVisibility) {
			ProtoBuf.Visibility.INTERNAL -> INTERNAL
			ProtoBuf.Visibility.PRIVATE -> PRIVATE
			ProtoBuf.Visibility.PROTECTED -> PROTECTED
			ProtoBuf.Visibility.PUBLIC -> PUBLIC
			ProtoBuf.Visibility.PRIVATE_TO_THIS -> PRIVATE_TO_THIS
			ProtoBuf.Visibility.LOCAL -> LOCAL
		}
	}
}