@file:Suppress("unused", "RedundantModalityModifier", "ClassName")

package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.mixins.*
import java.util.*
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*

internal interface KJElement_ : AnnotatedConstruct, HasOrigin, ConvertibleToTypeMirror {
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

	abstract override fun asTypeMirror(): KJTypeMirror?

	abstract fun asJavaxElement(): Element
	companion object
}

sealed class KJElement : KJElement_  {
	abstract override fun toString(): String

	final override fun equals(other: Any?): Boolean
			= asJavaxElement() == (other as? KJElement)?.asJavaxElement()

	final override fun hashCode(): Int = Objects.hash(asJavaxElement())
	companion object
}

internal interface KJTypeParameterElement_ : KJElement_, HasVariance {
	abstract val bounds: Set<KJTypeMirror>
	abstract val reified: Boolean

	override val enclosedElements: Set<Nothing> get() = emptySet()
	abstract override fun asJavaxElement(): TypeParameterElement
	abstract override val enclosingElement: KJElement
	companion object
}

abstract class KJTypeParameterElement : KJElement(), KJTypeParameterElement_ {
	final override fun toString(): String = simpleName
	companion object
}

internal interface KJTypeElement_ : KJElement_, HasVisibility, HasQualifiedName {
	abstract val interfaces: Set<KJTypeMirror>
	abstract val superclass: KJTypeMirror?

	abstract override val enclosingElement: KJElement
	abstract override fun asJavaxElement(): TypeElement
	companion object
}

sealed class KJTypeElement : KJElement(), KJTypeElement_ {
	final override fun toString(): String = qualifiedName
	companion object
}

internal interface KJClassElement_ : KJTypeElement_, HasTypeParameters, HasModality {
	/** Whether this type is an inner type */
	abstract val isInner: Boolean

	abstract override fun asJavaxElement(): TypeElement
	companion object
}

abstract class KJClassElement : KJTypeElement(), KJClassElement_ {
	companion object
}

internal interface KJObjectElement_ : KJTypeElement_ {
	abstract val isCompanion: Boolean
	override val origin: KJOrigin.Kotlin.Declared get() = KJOrigin.Kotlin.Declared
}

abstract class KJObjectElement : KJTypeElement(), KJObjectElement_ {
	companion object
}

internal interface KJDataClassElement_ : KJClassElement_ {
	override val origin: KJOrigin.Kotlin.Declared get() = KJOrigin.Kotlin.Declared
	companion object
}

abstract class KJDataClassElement : KJClassElement(), KJDataClassElement_ {
	companion object
}

internal interface KJSealedClassElement_ : KJClassElement_ {
	abstract val sealedSubclasses: Set<KJClassElement>
	override val origin: KJOrigin.Kotlin.Declared get() = KJOrigin.Kotlin.Declared
}

abstract class KJSealedClassElement : KJClassElement() {
	companion object
}

internal interface KJInterfaceElement_ : KJTypeElement_, HasTypeParameters, HasModality

abstract class KJInterfaceElement : KJTypeElement(), KJInterfaceElement_ {
	companion object
}

internal interface KJEnumElement_ : KJTypeElement_, HasModality {
	/** The enum constants defined by this enum declaration in declaration order */
	abstract val constants: Set<KJEnumConstantElement>
}

abstract class KJEnumElement : KJTypeElement(), KJEnumElement_ {
	companion object
}

internal interface KJEnumConstantElement_ : KJElement_ {
	abstract override fun asJavaxElement(): VariableElement
	abstract override val enclosingElement: KJEnumElement
	override val enclosedElements: Set<Nothing> get () = emptySet()
}

abstract class KJEnumConstantElement : KJElement(), KJEnumConstantElement_ {
	final override fun toString(): String = simpleName
	companion object
}

internal interface KJFileFacadeElement_ : KJTypeElement_, HasModality {
	abstract val isMultiFileClassFacade: Boolean

	override val origin: KJOrigin.Kotlin.Interop get () = KJOrigin.Kotlin.Interop
	override val interfaces: Set<Nothing> get () = emptySet()
	override val superclass: Nothing? get () = null
	abstract override fun asJavaxElement(): TypeElement
}

abstract class KJFileFacadeElement : KJTypeElement(), KJFileFacadeElement_ {
	companion object
}

internal interface KJAnnotationElement_ : KJTypeElement_ {
	abstract val attributes: List<KJAnnotationAttributeElement>

	abstract override fun asJavaxElement(): TypeElement
	override val interfaces: Set<Nothing> get () = emptySet()
	override val superclass: Nothing? get () = null
}

abstract class KJAnnotationElement : KJTypeElement(), KJAnnotationElement_ {
	companion object
}

internal interface KJAnnotationAttributeElement_ : KJElement_ {
	/** The default value of this annotation parameter or null if it doesn't have one */
	abstract val defaultValue: AnnotationValue?

	abstract override fun asJavaxElement(): ExecutableElement
	abstract override val enclosingElement: KJAnnotationElement
	override val enclosedElements: Set<Nothing> get () = emptySet()
}

abstract class KJAnnotationAttributeElement : KJElement(), KJAnnotationAttributeElement_ {
	final override fun toString(): String = simpleName
	companion object
}

internal interface KJTypeAliasElement_ : KJElement_, HasTypeParameters, HasQualifiedName, HasVisibility {
	abstract override fun asTypeMirror(): KJTypeAlias
	abstract override val enclosingElement: KJFileFacadeElement
	override val origin: KJOrigin.Kotlin.Declared get () = KJOrigin.Kotlin.Declared
	override val enclosedElements: Set<Nothing> get() = emptySet()
}

abstract class KJTypeAliasElement : KJElement(), KJTypeAliasElement_ {
	final override fun toString(): String = qualifiedName
	companion object
}

internal interface KJExecutableElement_ : KJElement_,  HasReceiver {
	/** The formal parameters of this executable element in declaration order */
	abstract val parameters: List<KJParameterElement>

	abstract val returnType: KJTypeMirror?
	abstract val thrownTypes: List<KJTypeMirror>

	override val enclosedElements: Set<KJParameterElement>
		get() = parameters.toSet()

	abstract override fun asTypeMirror(): KJExecutableType
	abstract override val enclosingElement: KJElement

	abstract override fun asJavaxElement(): ExecutableElement
}

sealed class KJExecutableElement : KJElement(), KJExecutableElement_ {
	final override fun toString(): String {
		val retSignature = returnType?.let { ": $it" } ?: ""
		val paramsSignature = parameters.map { it.asTypeMirror() }.toString()
		return "$simpleName($paramsSignature)$retSignature"
	}

	companion object
}

internal interface KJFunctionElement_ : KJExecutableElement_,
												HasTypeParameters,
												HasModality,
												HasVisibility,
												HasKotlinExternalImplementation,
												HasKotlinMultiPlatformImplementations,
												HasJvmOverloads {
	abstract val isInline: Boolean
	abstract val isInfix: Boolean
	abstract val isTailRec: Boolean
	abstract val isSuspend: Boolean
	abstract val isOperator: Boolean
	abstract val isStatic: Boolean
}

abstract class KJFunctionElement : KJExecutableElement(), KJFunctionElement_ {
	companion object
}

internal interface KJConstructorElement_ : KJExecutableElement_, HasVisibility, HasJvmOverloads {
	/** Whether this constructor is the primary constructor of its class */
	abstract val isPrimary: Boolean

	abstract override val enclosingElement: KJTypeElement
}

abstract class KJConstructorElement : KJExecutableElement(), KJConstructorElement_ {
	companion object
}

internal interface KJJvmOverloadElement_ : KJExecutableElement_, HasTypeParameters {
	/**
	 * The original function or constructor that this element is a generated JvmOverload of.
	 * Will be either a [KJFunctionElement] or a [KJConstructorElement].
	 */
	abstract val originalMethod: ExecutableElement

	override val origin: KJOrigin.Kotlin.Interop get() = KJOrigin.Kotlin.Interop
}

abstract class KJJvmOverloadElement : KJExecutableElement(), KJJvmOverloadElement_ {
	companion object
}

abstract class KJInitializerElement : KJExecutableElement() {
	enum class Kind { Instance, Static }
	abstract val kind: Kind

	abstract override val origin: KJOrigin
	override val enclosedElements: Set<Nothing> = emptySet()
	override val parameters: List<Nothing> = emptyList()
	override val returnType: Nothing? = null
	companion object
}

internal interface KJAccessorElement_ : KJExecutableElement_, HasVisibility,
										HasModality, HasKotlinExternalImplementation {
	/**
	 * Whether this accessor is the default implementation and not a
	 * custom getter/setter written by programmer
	 * */
	abstract val isDefaultImplementation: Boolean

	/** Whether this accessor is inline */
	abstract val isInline: Boolean

	abstract override val enclosingElement: KJPropertyElement
}

sealed class KJAccessorElement : KJExecutableElement(), KJAccessorElement_ {
	companion object
}

internal interface KJGetterElement_ : KJAccessorElement_ {
	override val parameters: List<Nothing> get() = emptyList()
	override val enclosedElements: Set<Nothing> get() = emptySet()
}

abstract class KJGetterElement : KJAccessorElement() {
	companion object
}

internal interface KJSetterElement_ : KJAccessorElement_ {
	override val returnType: KJTypeMirror
}

abstract class KJSetterElement : KJAccessorElement(), KJSetterElement_

internal interface KJParameterElement_ : KJElement_ {
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
	override val enclosedElements: Set<Nothing> get() = emptySet()
}

abstract class KJParameterElement : KJElement(), KJParameterElement_ {
	companion object
}

internal interface KJPropertyElement_ : KJElement_, HasReceiver, HasVisibility,
										HasModality,
										HasSyntheticMethodForAnnotations,
										HasKotlinMultiPlatformImplementations,
										HasKotlinExternalImplementation {
	/**
	 * If the Kotlin property has annotations with target [AnnotationTarget.PROPERTY]
	 * the Kotlin compiler will generate an empty parameterless void-returning
	 * synthetic method named "propertyName$annotations" to hold the annotations that
	 * are targeted at the property and not backing field, getter or setter.
	 */
	abstract override val javaAnnotationHolderElement: ExecutableElement?

	/**
	 * If the Kotlin property is delegated a field is generated to hold
	 * the instance of the delegate class.
	 */
	abstract val delegateField: KJDelegateFieldElement?

	/**
	 * The backing field is the underlying field of the property.
	 */
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
}

abstract class KJPropertyElement : KJElement(), KJPropertyElement_ {
	companion object
}

internal interface KJFieldElement_ : KJElement_, HasReceiver {
	abstract override fun asJavaxElement(): VariableElement
	abstract override val enclosingElement: KJElement
	override val enclosedElements: Set<Nothing> get() = emptySet()
}

abstract class KJFieldElement : KJElement(), KJFieldElement_ {
	final override fun toString(): String = simpleName
	companion object
}

internal interface KJDelegateFieldElement_ : KJFieldElement_ {
	/** A delegate field is always enclosed by its property */
	abstract override val enclosingElement: KJPropertyElement
}

/**
 * A [KJDelegateFieldElement] is the field that contains the delegate instance
 * for Kotlin delegated properties.
 */
abstract class KJDelegateFieldElement : KJFieldElement(), KJDelegateFieldElement_ {
	companion object
}

internal interface KJBackingFieldElement_ : KJFieldElement_ {
	/** A backing field is always enclosed by its property */
	abstract override val enclosingElement: KJPropertyElement
}

/**
 * A [KJBackingFieldElement] is the field underlying a property. In Kotlin
 * it is automatically generated while in Java we infer it from the name
 * (i.e. `mProp` would be the backing field of the property `prop` with the
 * accessors `setProp` and `getProp`).
 */
abstract class KJBackingFieldElement : KJFieldElement(), KJBackingFieldElement_ {
	companion object
}

internal interface KJModuleElement_ : KJElement_, HasQualifiedName, CanBeUnnamed {
	/** Whether this module is open */
	abstract val isOpen: Boolean

	/** Whether this module is unnamed */
	abstract override val isUnnamed: Boolean

	/** The directives declared by this module */
	abstract val directives: List<ModuleElement.Directive>

	abstract override val enclosedElements: Set<KJPackageElement>
	abstract override fun asJavaxElement(): ModuleElement
	abstract override fun asTypeMirror(): KJModuleType
}

abstract class KJModuleElement : KJElement(), KJModuleElement_  {
	final override fun toString(): String
			= if (isUnnamed) "unnamed module" else qualifiedName

	companion object
}

internal interface KJPackageElement_ : KJElement_, HasQualifiedName, CanBeUnnamed {
	/** Whether this package is unnamed */
	abstract override val isUnnamed: Boolean

	abstract override fun asJavaxElement(): PackageElement
	abstract override fun asTypeMirror(): KJPackageType
}

abstract class KJPackageElement : KJElement(), KJPackageElement_ {
	final override fun toString(): String
			= if (isUnnamed) "unnamed package" else qualifiedName

	companion object
}

internal interface KJOtherElement_ : KJElement_ {
	/** A [KJOtherElement] will always have originated from a Javax [Element] with kind [ElementKind.OTHER]. */
	abstract override val origin: KJOrigin.Java

	/** This [Element] will have kind [ElementKind.OTHER]. */
	abstract override fun asJavaxElement(): Element
}

/** An element that was converted from a Javax [Element] with kind [ElementKind.OTHER]. */
abstract class KJOtherElement : KJElement() {
	companion object
}