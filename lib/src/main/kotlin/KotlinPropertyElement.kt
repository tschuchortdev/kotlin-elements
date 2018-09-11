package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

open class KotlinPropertyElement internal constructor(
		val javaFieldElement: VariableElement?,
		val javaSetterElement: ExecutableElement?,
		val javaGetterElement: ExecutableElement?,
		private val protoProperty: ProtoBuf.Property,
		private val protoNameResolver: NameResolver,
		processingEnv: ProcessingEnvironment
) : KotlinSubelement(processingEnv) {

	init {
		require(arrayListOf(javaFieldElement, javaSetterElement, javaGetterElement).any { it != null })

		// all non-null java elements must have the same enclosing element
		assert(arrayListOf(javaFieldElement?.enclosingElement, javaSetterElement?.enclosingElement, javaGetterElement?.enclosingElement)
				.filterNotNull()
				.allEqual()
		)

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

	val modality = protoProperty.modality

	val visibility = protoProperty.visibility

	val isGetterDefault: Boolean = protoProperty.isGetterDefault
	val isGetterNotDefault: Boolean = protoProperty.isGetterNotDefault
	val isGetterExternal: Boolean = protoProperty.isGetterExternal
	val isGetterInline: Boolean = protoProperty.isGetterInline
	val hasGetter: Boolean = protoProperty.hasGetter
	val getterModality = protoProperty.getterModality
	val getterVisibility = protoProperty.getterVisibility
	val getterHasAnnotations = protoProperty.getterHasAnnotations

	val isSetterDefault: Boolean = protoProperty.isSetterDefault
	val isSetterNotDefault: Boolean = protoProperty.isSetterNotDefault
	val isSetterExternal: Boolean = protoProperty.isSetterExternal
	val isSetterInline: Boolean = protoProperty.isSetterInline
	val hasSetter: Boolean = protoProperty.hasSetter
	val setterModality = protoProperty.setterModality
	val setterVisibility = protoProperty.setterVisibility
	val setterHasAnnotations = protoProperty.setterHasAnnotations

	val hasConstant = protoProperty.hasConstant


	override fun getModifiers(): MutableSet<Modifier> = TODO("property modifiers")

	override fun getSimpleName(): Name
			= processingEnv.elementUtils.getName(protoNameResolver.getString(protoProperty.name))

	override fun getKind(): ElementKind = ElementKind.OTHER

	override fun asType(): TypeMirror = TODO("KotlinPropertyElement asType")

	/**
	 * To be consistent with [Element], a [KotlinPropertyElement] is not considered to enclose anything
	 */
	override fun getEnclosedElements(): List<Nothing> = emptyList()

	override fun getEnclosingElement(): KotlinElement {
		val nonNullJavaElem = javaFieldElement ?: javaGetterElement ?: javaSetterElement
			?: throw AssertionError("at least one java element must be non-null")

		return nonNullJavaElem.enclosingElement!!.correspondingKotlinElement(processingEnv)!!
	}

	override fun <R : Any?, P : Any?> accept(v: ElementVisitor<R, P>, p: P): R {
		return v.visitUnknown(this, p)
	}

	override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>?): Array<A> {
		TODO("property annotations")
	}

	override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A {
		TODO("property annotations")
	}

	override fun getAnnotationMirrors(): MutableList<out AnnotationMirror> {
		TODO("property annotations")
	}

	override fun toString() = TODO("property toString")

	override fun equals(other: Any?) = (other as? KotlinPropertyElement)?.let { other ->
		javaFieldElement?.equals(other.javaFieldElement) ?: true
		&& javaSetterElement?.equals(other.javaSetterElement) ?: true
		&& javaGetterElement?.equals(other.javaGetterElement) ?: true
	} ?: false


	override fun hashCode() = Objects.hash(javaFieldElement, javaSetterElement, javaGetterElement)
}