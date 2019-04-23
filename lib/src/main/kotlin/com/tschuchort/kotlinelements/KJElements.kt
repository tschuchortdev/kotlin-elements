@file:Suppress("unused")

package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.mixins.*
import java.util.*
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*

sealed class KJElement : AnnotatedConstruct {
	/**
	 * The element in whose context this element was declared.
	 * Behaves similar to [Element.getEnclosingElement].
	 */
	abstract val enclosingElement: KJElement?

	/**
	 * All the elements that are enclosed by this element in no particular order.
	 * Behaves similar to [Element.getEnclosedElements].
	 *
	 * Note that type parameters are not included.
	 */
	abstract val enclosedElements: Set<KJElement>

	abstract val simpleName: String

	abstract fun asTypeMirror(): KJTypeMirror?

	abstract fun asJavaxElement(): Element

	abstract val origin: KJOrigin

	abstract override fun toString(): String

	final override fun equals(other: Any?): Boolean
			= asJavaxElement() == (other as? KJElement)?.asJavaxElement()

	final override fun hashCode(): Int = Objects.hash(asJavaxElement())
	companion object
}

abstract class KJTypeParameterElement : KJElement(), HasVariance {
	abstract val reified: Boolean
	abstract val bounds: Set<KJTypeMirror>

	abstract override fun asJavaxElement(): TypeParameterElement
	abstract override val enclosingElement: KJElement
	final override val enclosedElements: Set<Nothing> = emptySet()

	final override fun toString(): String = simpleName
	companion object
}

sealed class KJTypeElement : KJElement(), HasVisibility, HasQualifiedName {
	abstract val interfaces: Set<KJTypeMirror>
	abstract val superclass: KJTypeMirror?

	abstract override val enclosingElement: KJElement

	final override fun toString(): String = qualifiedName
	companion object
}

abstract class KJClassElement : KJTypeElement(), HasTypeParameters, HasModality {
	/** Whether this class is a data class */
	abstract val isDataClass: Boolean

	/** Whether this type is an inner type */
	abstract val isInner: Boolean

	abstract override fun asJavaxElement(): TypeElement

	companion object
}

abstract class KJSealedClass : KJClassElement() {
	abstract val sealedSubclasses: Set<KJClassElement>
	companion object
}

abstract class KJInterfaceElement : KJTypeElement(), HasTypeParameters, HasModality {
	abstract override fun asJavaxElement(): TypeElement
	companion object
}

abstract class KJObjectElement : KJTypeElement() {
	abstract val isCompanion: Boolean

	final override val origin: KJOrigin.Kotlin = KJOrigin.Kotlin
	abstract override fun asJavaxElement(): TypeElement
	companion object
}

abstract class KJEnumElement : KJTypeElement(), HasModality {
	/** The enum constants defined by this enum declaration in declaration order */
	abstract val constants: List<KJEnumConstantElement>

	abstract override fun asJavaxElement(): TypeElement
	companion object
}

abstract class KJEnumConstantElement : KJElement() {
	abstract override fun asJavaxElement(): VariableElement
	abstract override val enclosingElement: KJEnumElement
	final override val enclosedElements: Set<Nothing> = emptySet()

	final override fun toString(): String = simpleName
	companion object
}

abstract class KJFileFacadeElement : KJTypeElement(), HasModality {
	abstract val isMultiFileClassFacade: Boolean

	final override val origin: KJOrigin.KotlinJavaInterop = KJOrigin.KotlinJavaInterop
	abstract override fun asJavaxElement(): TypeElement
	companion object
}

abstract class KJAnnotationElement : KJTypeElement(), HasModality {
	abstract val attributes: List<KJAnnotationAttributeElement>

	abstract override fun asJavaxElement(): TypeElement

	final override val enclosedElements: Set<KJAnnotationAttributeElement>
			get() = attributes.toSet()

	companion object
}

abstract class KJAnnotationAttributeElement : KJElement() {
	/** The default value of this annotation parameter or null if it doesn't have one */
	abstract val defaultValue: AnnotationValue?

	abstract override fun asJavaxElement(): ExecutableElement
	abstract override val enclosingElement: KJAnnotationElement
	final override val enclosedElements: Set<Nothing> = emptySet()

	override fun toString(): String = simpleName
	companion object
}

abstract class KJTypeAliasElement : KJElement(), HasTypeParameters,
									HasSyntheticMethodForAnnotations, HasQualifiedName{
	abstract override fun asTypeMirror(): KJTypeAlias

	final override val origin: KJOrigin.Kotlin = KJOrigin.Kotlin
	abstract override val enclosingElement: KJFileFacadeElement
	final override val enclosedElements: Set<Nothing> = emptySet()

	final override fun toString(): String = qualifiedName
	companion object
}

sealed class KJExecutableElement : KJElement(), HasReceiver {
	/** The formal parameters of this executable element in declaration order */
	abstract val parameters: List<KJParameterElement>

	abstract override val receiverType: KJTypeMirror?
	abstract val returnType: KJTypeMirror?
	abstract val thrownTypes: List<KJTypeMirror>

	abstract override fun asTypeMirror(): KJExecutableType
	abstract override val enclosingElement: KJElement

	abstract override fun asJavaxElement(): ExecutableElement

	final override fun toString(): String {
		val retSignature = returnType?.let { ": $it" } ?: ""
		val paramsSignature = parameters.map { it.asTypeMirror() }.toString()
		return "$simpleName($paramsSignature)$retSignature"
	}

	companion object
}

sealed class KJParameterElement : KJElement() {
	/**
	 * Whether this parameter has a default value
	 *
	 * Not to be confused with [javax.lang.model.element.ExecutableElement.isDefault]
	 * and [javax.lang.model.element.ExecutableElement.getDefaultValue] which
	 * merely returns the default value of an annotation class parameter
	 */
	abstract val hasDefaultValue: Boolean

	/** Whether this parameter has the `crossinline` modifier */
	abstract val isCrossInline: Boolean

	/** Whether this parameter has the `noinline` modifier */
	abstract val isNoInline: Boolean

	/** Whether this parameter has the `vararg` modifier */
	abstract val isVararg: Boolean

	abstract override fun asJavaxElement(): VariableElement
	final override val enclosedElements: Set<Nothing> = emptySet()
	companion object
}

abstract class KJFunctionElement : KJExecutableElement(), HasTypeParameters,
								   HasModality,
								   HasVisibility,
								   HasKotlinExternalImplementation,
								   HasKotlinMultiPlatformImplementations {
	abstract val isInline: Boolean
	abstract val isInfix: Boolean
	abstract val isTailRec: Boolean
	abstract val isSuspend: Boolean
	abstract val isOperator: Boolean
	abstract val isStatic: Boolean
	companion object
}

abstract class KJConstructorElement : KJExecutableElement(), HasVisibility {
	/** Whether this constructor is the primary constructor of its class */
	abstract val isPrimary: Boolean

	abstract override val enclosedElements: Set<KJParameterElement>

	companion object
}

abstract class KJGeneratedJvmOverloadElement : KJExecutableElement(), HasVisibility {
}

abstract class KJStaticInitializerElement : KJExecutableElement() {
	abstract override val origin: KJOrigin.Java
	final override val enclosedElements: Set<Nothing> = emptySet()
	companion object
}

abstract class KJInstanceInitializerElement : KJExecutableElement() {
	abstract override val origin: KJOrigin.Java
	final override val enclosedElements: Set<Nothing> = emptySet()
	companion object
}

sealed class KJAccessorElement : KJExecutableElement(), BelongsToProperty,
								 HasVisibility,
								 HasModality,
								 HasKotlinExternalImplementation {
	/** Whether this accessor is the default implementation and not a custom getter/setter written by programmer */
	abstract val isDefaultImplementation: Boolean

	/** Whether this accessor is inline */
	abstract val isInline: Boolean

	final override val enclosingElement: KJPropertyElement get() = correspondingProperty
	abstract override val enclosedElements: Set<KJParameterElement>
	companion object
}

abstract class KJGetterElement : KJAccessorElement() {
	final override val parameters: List<Nothing> = emptyList()
	final override val enclosedElements: Set<Nothing> = emptySet()
	companion object
}

abstract class KJSetterElement : KJAccessorElement()

abstract class KJPropertyElement : KJElement(), HasReceiver, HasVisibility,
								   HasModality,
								   HasSyntheticMethodForAnnotations,
								   HasKotlinMultiPlatformImplementations,
								   HasKotlinExternalImplementation {
	/**
	 * If the Kotlin property has annotations with target [AnnotationTarget.PROPERTY]
	 * the Kotlin compiler will generate an empty parameterless void-returning
	 * synthetic method named "propertyName$annotations" to hold the annotations that
	 * are targeted at the property and not backing field, getter or setter
	 */
	abstract override val javaAnnotationHolderElement: ExecutableElement?

	/**
	 * If the Kotlin property is delegated a field is generated to hold
	 * the instance of the delegate class
	 */
	abstract val delegateField: KJDelegateFieldElement?
	abstract val backingField: KJBackingFieldElement?
	abstract val getter: KJGetterElement?
	abstract val setter: KJSetterElement?

	/**
	 * Returns the value of this property if this is a const
	 * property initialized to a compile-time constant. Returns
	 * null otherwise. The value will be of a primitive type or a [String].
	 *
	 * Enum constants are not considered to be compile-time constants.
	 */
	abstract val constantValue: Any?

	/** Whether this property is delegated */
	abstract val isDelegated: Boolean

	/** Whether this property is late-initialized */
	abstract val isLateInit: Boolean

	/** whether this property is read-only */
	abstract val isReadOnly: Boolean

	abstract override val enclosingElement: KJTypeElement
	companion object
}

abstract class KJFieldElement : KJElement(), HasReceiver {
	abstract override fun asJavaxElement(): VariableElement
	override val enclosedElements: Set<Nothing> = emptySet()

	final override fun toString(): String = simpleName
	companion object
}

abstract class KJDelegateFieldElement : KJFieldElement(), BelongsToProperty {
	final override val enclosingElement: KJPropertyElement get() = correspondingProperty
	companion object
}

abstract class KJBackingFieldElement : KJFieldElement(), BelongsToProperty {
	final override val enclosingElement: KJPropertyElement get() = correspondingProperty
	companion object
}

abstract class KJModuleElement : KJElement(), HasQualifiedName, CanBeUnnamed {
	/** Whether this module is open */
	abstract val isOpen: Boolean

	/** Whether this module is unnamed */
	abstract override val isUnnamed: Boolean

	/** The directives declared by this module */
	abstract val directives: List<ModuleElement.Directive>

	abstract override val enclosedElements: Set<KJPackageElement>
	abstract override fun asJavaxElement(): ModuleElement
	abstract override fun asTypeMirror(): KJModuleType

	final override fun toString(): String
			= if (isUnnamed) "unnamed module" else qualifiedName

	companion object
}

abstract class KJPackageElement : KJElement(), HasQualifiedName, CanBeUnnamed {
	/** Whether this package is unnamed */
	abstract override val isUnnamed: Boolean

	abstract override fun asJavaxElement(): PackageElement
	abstract override fun asTypeMirror(): KJPackageType

	final override fun toString(): String
			= if (isUnnamed) "unnamed package" else qualifiedName

	companion object
}

abstract class KJOtherElement : KJElement() {
	companion object
}