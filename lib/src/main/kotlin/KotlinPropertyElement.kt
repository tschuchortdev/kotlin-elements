package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

class KotlinPropertyElement internal constructor(
		/** The Java backing field element of this property, if it has one */
		val javaFieldElement: VariableElement?,
		/** The Java setter element of this property, if it has one */
		val javaSetterElement: ExecutableElement?,
		/** The Java getter element of this property, if it has one */
		val javaGetterElement: ExecutableElement?,
		/**
		 * If the Kotlin property has annotations with target [AnnotationTarget.PROPERTY]
		 * the Kotlin compiler will generate an empty parameterless void-returning
		 * synthetic method named "propertyName$annotations" to hold the annotations that
		 * are targeted at the property and not backing field, getter or setter
		 */
		@Deprecated("Deprecated in Kotlin specification but kept for binary compatibility")
		val javaSyntheticAnnotationHolderElement: ExecutableElement?,
		private val protoProperty: ProtoBuf.Property,
		private val protoNameResolver: NameResolver,
		processingEnv: ProcessingEnvironment
) : KotlinSubelement(processingEnv), HasKotlinVisibility, HasKotlinModality {

	init {
		val presentJavaElems = arrayListOf(javaFieldElement, javaSetterElement, javaGetterElement).filterNotNull()

		require(presentJavaElems.isNotEmpty())

		// all non-null java elements must have the same enclosing element
		require(presentJavaElems.allEqualBy(Element::getEnclosingElement))

		// all non-null java elements should have the same type
		require(arrayListOf(javaFieldElement?.asType(), javaSetterElement?.parameters?.single()?.asType(), javaGetterElement?.returnType)
				.filterNotNull().allEqual())

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

	override val modality: KotlinModality = KotlinModality.fromProtoBuf(protoProperty.modality!!)

	override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoProperty.visibility!!)

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

	override fun getModifiers(): Set<Modifier> = emptySet() //TODO("KotlinPropertyElement modifiers")

	override fun getSimpleName(): Name
			= processingEnv.elementUtils.getName(protoNameResolver.getString(protoProperty.name))

	override fun getKind(): ElementKind = ElementKind.OTHER

	//TODO("KotlinPropertyElement: return correctly resolved Kotlin type")
	override fun asType(): TypeMirror =
			javaFieldElement?.run { asType()!! }
			?: javaGetterElement?.run { returnType }
			?: javaSetterElement?.run { parameters.single().asType() }!!

	/**
	 * To be consistent with [Element], a [KotlinPropertyElement] is not considered to enclose anything
	 */
	override fun getEnclosedElements(): List<Nothing> = emptyList()

	override fun getEnclosingElement(): KotlinElement {
		val nonNullJavaElem = javaFieldElement ?: javaGetterElement ?: javaSetterElement
			?: throw AssertionError("at least one java element must be non-null")

		return nonNullJavaElem.enclosingElement!!.correspondingKotlinElement(processingEnv)!!
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

	override fun <R : Any?, P : Any?> accept(v: ElementVisitor<R, P>, p: P): R {
		return v.visitUnknown(this, p)
	}

	override fun toString() = simpleName.toString()

	override fun equals(other: Any?): Boolean {
		return if (other is KotlinPropertyElement)
			javaFieldElement == other.javaFieldElement
			&& javaSetterElement == other.javaSetterElement
			&& javaGetterElement == other.javaGetterElement
		else
			false
	}

	override fun hashCode() = Objects.hash(javaFieldElement, javaSetterElement,
			javaGetterElement, javaSyntheticAnnotationHolderElement)
}

/**
 * true if this element may be a synthetic annotation holder for
 * property annotations with [AnnotationTarget.PROPERTY]
 */
internal fun ExecutableElement.maybeSyntheticPropertyAnnotHolder()
		= simpleName.toString().endsWith("\$annotations")
		  && returnType.kind == TypeKind.VOID
		  && parameters.isEmpty()

/** true if this element may be a kotlin generated getter of a property */
internal fun ExecutableElement.maybeKotlinGetter()
		= simpleName.toString().run { startsWith("get") || startsWith("is") }
		  && returnType.kind != TypeKind.VOID
		  && parameters.isEmpty()

/** true if this element may be a kotlin generated setter of a property */
internal fun ExecutableElement.maybeKotlinSetter()
		= simpleName.toString().startsWith("set")
		  && returnType.kind == TypeKind.VOID
		  && parameters.isNotEmpty()

/** returns the setter name that would be generated for a field with this simple name */
internal fun kotlinSetterName(fieldSimpleName: String): String
		= "set" + fieldSimpleName.removePrefix("is").capitalize()

/** returns the getter name that would be generated for a field with this simple name */
internal fun kotlinGetterName(fieldSimpleName: String): String {
	return if (fieldSimpleName.startsWith("is"))
		fieldSimpleName
	else
		"get" + fieldSimpleName.capitalize()
}
