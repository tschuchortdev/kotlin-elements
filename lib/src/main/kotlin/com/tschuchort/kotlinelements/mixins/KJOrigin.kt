package com.tschuchort.kotlinelements.mixins

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.util.Elements

/** The origin of a construct */
sealed class KJOrigin {
	/** A construct originating from Java source code */
	sealed class Java : KJOrigin() {
		/** Describes a construct explicitly declared in source code. */
		object Explicit : Java()
		/**
		 * A mandated construct is one that is not explicitly declared in the source code,
		 * but whose presence is mandated by the specification. Such a construct is said
		 * to be implicitly declared. One example of a mandated element is a default
		 * constructor in a class that contains no explicit constructor declarations.
		 * Another example of a mandated construct is an implicitly declared container
		 * annotation used to hold multiple annotations of a repeatable annotation type.
		 */
		object Mandated : Java()
		/**
		 * An inferred construct is one that does not appear in Java code but was inferred from
		 * existing constructs, for example a property element inferred from existing getter
		 * and setter elements.
		 */
		object Inferred : Java()
		/**
		 * A synthetic construct is one that is neither implicitly nor explicitly declared
		 * in the source code. Such a construct is typically a translation artifact created
		 * by a compiler.
		 */
		object Synthetic : Java()
		/** Element with unknown origin. */
		object Unknown : Java()

		/**
		 * Returns the corresponding [Elements.Origin] or null when a direct conversion
		 * is not possible.
		 */
		fun toJavaxOrigin(): Elements.Origin? = when (this) {
			Explicit                                                                                -> Elements.Origin.EXPLICIT
			Mandated                                                                                -> Elements.Origin.MANDATED
			Synthetic                                                                               -> Elements.Origin.SYNTHETIC
			Inferred, Unknown -> null
		}
	}

	/** A construct originating from Kotlin source code */
	sealed class Kotlin : KJOrigin() {
		/** A regular Kotlin-syntactic construct that was declared either explicitly or implicitly
		 * in source code. The Kotlin-equivalent of both [KJOrigin.Java.Explicit] and
		 * [KJOrigin.Java.Mandated] because there is no way to differentiate between explicit and
		 * implicit constructs for Kotlin code.
		 */
		object Declared : Kotlin()
		/** A construct that was not explicitly declared but generated for Java-interop. */
		object Interop : Kotlin()
		/** A construct that belongs to a synthetic class such as `$DefaultImpls` or `$WhenMappings`. */
		object Synthetic : Kotlin()
	}

	companion object {
		internal fun fromJavax(javaxElem: Element, processingEnv: ProcessingEnvironment)
				= when(processingEnv.elementUtils.getOrigin(javaxElem)) {
			Elements.Origin.EXPLICIT  -> Java.Explicit
			Elements.Origin.MANDATED  -> Java.Mandated
			Elements.Origin.SYNTHETIC -> Java.Synthetic
			null                      -> Java.Unknown
		}
	}
}

interface HasOrigin {
	val origin: KJOrigin
}