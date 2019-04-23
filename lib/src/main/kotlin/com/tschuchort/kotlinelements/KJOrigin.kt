package com.tschuchort.kotlinelements

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.util.Elements

sealed class KJOrigin {
	/** Element originates from Java source code */
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
		 * A synthetic construct is one that is neither implicitly nor explicitly declared
		 * in the source code. Such a construct is typically a translation artifact created
		 * by a compiler.
		 */
		object Synthetic : Java()
		/** Element with unknown origin. */
		object Unknown : Java()
	}

	/** Element originates from Kotlin source code. */
	sealed class Kotlin : KJOrigin() {
		/** Describes a construct explicitly declared in source code. */
		object Explicit : Kotlin()
		/** Describes a construct that was not explicitly declared but generated for Java-interop */
		object Generated : Kotlin()
		/** Elements that belong to a synthetic class such as `$DefaultImpls` or `$WhenMappings`. */
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