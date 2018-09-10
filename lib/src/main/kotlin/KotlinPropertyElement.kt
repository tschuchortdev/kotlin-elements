package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement

/*open class KotlinPropertyElement internal constructor(
		val javaFieldElement: VariableElement?,
		val javaSetterElement: ExecutableElement?,
		val javaGetterElement: ExecutableElement?,
		protoProperty: ProtoBuf.Property,
		processingEnv: ProcessingEnvironment
) : KotlinElement(processingEnv), HasKotlinVisibility, HasKotlinModality {

	init {
		require(arrayListOf(javaFieldElement, javaSetterElement, javaGetterElement).any { it != null })

		assert(protoProperty.isVal || protoProperty.isVar)
	}

	val isConst: Boolean = protoProperty.isConst

	val isDelegated: Boolean = protoProperty.isDelegated

	/** Whether this property has the `expect` keyword
	 *
	 * An expect property is a property declaration with actual definition in a different
	 * file, akin to a declaration in a header file in C++. They are used in multiplatform
	 * projects where different implementations are needed depending on target platform
	 */
	val isExpect: Boolean = protoProperty.isExpectProperty

	/**
	 * Whether this property has the `external` keyword
	 *
	 * An external property is a property declaration with the actual definition in native
	 * code, similar to the `native` keyword in Java
	 */
	val isExternal: Boolean = protoProperty.isExternalProperty

	val isLateInit: Boolean = protoProperty.isLateInit

	val isReadOnly: Boolean = protoProperty.isVal


	override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoProperty.visibility)

	override fun toString() = TODO("property toString")

	override fun equals(other: Any?)
			= javaFieldElement?.equals(other)
			  || javaSetterElement?.equals(other) ?: false
			  || javaGetterElement?.equals(other) ?: false

	override fun hashCode() = Objects.hash(javaFieldElement, javaSetterElement, javaGetterElement)
}*/