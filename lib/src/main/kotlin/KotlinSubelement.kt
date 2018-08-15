package com.tschuchort.kotlinelements

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

/**
 * A [KotlinSubelement] is guaranteed to always be enclosed by a
 * [KotlinSyntacticElement]. It can not exist at top level or as child of a Java element.
 *
 * This class exists mostly to provide a conveniently typed API
 */
abstract class KotlinSubelement internal constructor(
		element: Element,
		processingEnv: ProcessingEnvironment
) : KotlinSyntacticElement(element, processingEnv) {

	override fun getEnclosingElement(): KotlinSyntacticElement {
		val enclosingElement = super.getEnclosingElement()

		return enclosingElement as? KotlinSyntacticElement
		?: throw IllegalStateException(
				"Enclosing element \"$enclosingElement\" of element \"$this\" " +
				"is not of type `KotlinSyntacticElement`")
	}
}