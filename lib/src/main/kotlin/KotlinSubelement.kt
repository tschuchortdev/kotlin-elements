package com.tschuchort.kotlinelements

import javax.annotation.processing.ProcessingEnvironment

/**
 * A [KotlinSubelement] is guaranteed to always be enclosed by a
 * [KotlinElement]. It can not exist at top level or as child of a Java element.
 *
 * This class exists mostly to provide a conveniently typed API
 */
abstract class KotlinSubelement internal constructor(
		processingEnv: ProcessingEnvironment
) : KotlinElement(processingEnv) {

	abstract override fun getEnclosingElement(): KotlinElement
}