package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement

/*open class KotlinPropertyElement internal constructor(
		private val fieldElement: VariableElement,
		private val setterElement: KotlinElement?,
		private val getterElement: ExecutableElement?,
		private val protoProperty: ProtoBuf.Property,
		processingEnv: ProcessingEnvironment
) : KotlinElement(fieldElement, processingEnv) {

	companion object {
		fun get(fieldElement: VariableElement, processingEnv: ProcessingEnvironment): KotlinPropertyElement? {

		}

		fun get(setterElement: ExecutableElement, processingEnv: ProcessingEnvironment): KotlinPropertyElement? {

		}

		fun get(getterElement: ExecutableElement, processingEnv: ProcessingEnvironment): KotlinPropertyElement? {

		}
	}

	override fun toString() = fieldElement.toString()

	override fun equals(other: Any?)
			= fieldElement.equals(other)
			  || setterElement?.equals(other) ?: false
			  || getterElement?.equals(other) ?: false

	override fun hashCode() = fieldElement.hashCode()
}*/