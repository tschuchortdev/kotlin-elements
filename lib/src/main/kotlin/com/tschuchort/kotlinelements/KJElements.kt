@file:Suppress("unused", "RedundantModalityModifier")

package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.mixins.*
import java.util.*
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*

internal interface KJElementInterface : AnnotatedConstruct, HasOrigin {
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
}

sealed class KJElement : KJElementInterface {
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

internal interface KJTypeElementInterface : KJElementInterface, HasVisibility, HasQualifiedName {
	abstract val interfaces: Set<KJTypeMirror>
	abstract val superclass: KJTypeMirror?

	abstract override val enclosingElement: KJElement
}

sealed class KJTypeElement : KJElement(), KJTypeElementInterface {
	internal abstract fun lookupMemberKJElementFor(memberElem: Element): KJElement

	final override fun toString(): String = qualifiedName
	companion object
}

internal interface KJClassElementInterface : KJTypeElementInterface, HasTypeParameters, HasModality {
	/** Whether this type is an inner type */
	abstract val isInner: Boolean

	abstract override fun asJavaxElement(): TypeElement
}

abstract class KJClassElement : KJTypeElement(), KJClassElementInterface {
	companion object
}

abstract class KJDataClassElement : KJClassElement() {
	final override val origin: KJOrigin.Kotlin.Declared = KJOrigin.Kotlin.Declared
	companion object
}

abstract class KJSealedClassElement : KJClassElement() {
	abstract val sealedSubclasses: Set<KJClassElement>
	final override val origin: KJOrigin.Kotlin.Declared = KJOrigin.Kotlin.Declared
	companion object
}

abstract class KJInterfaceElement : KJTypeElement(), HasTypeParameters, HasModality {
	abstract override fun asJavaxElement(): TypeElement
	companion object
}

abstract class KJObjectElement : KJTypeElement() {
	abstract val isCompanion: Boolean

	final override val origin: KJOrigin.Kotlin.Declared = KJOrigin.Kotlin.Declared
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

	final override val origin: KJOrigin.Kotlin.Interop = KJOrigin.Kotlin.Interop
	final override val interfaces: Set<Nothing> = emptySet()
	final override val superclass: Nothing? = null
	abstract override fun asJavaxElement(): TypeElement
	companion object
}

abstract class KJAnnotationElement : KJTypeElement(), HasModality {
	abstract val attributes: List<KJAnnotationAttributeElement>

	abstract override fun asJavaxElement(): TypeElement

	final override val interfaces: Set<Nothing> = emptySet()
	final override val superclass: Nothing? = null

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

	final override val origin: KJOrigin.Kotlin.Declared = KJOrigin.Kotlin.Declared
	abstract override val enclosingElement: KJFileFacadeElement
	final override val enclosedElements: Set<Nothing> = emptySet()

	final override fun toString(): String = qualifiedName
	companion object
}

internal interface KJExecutableElementInterface : KJElementInterface {
	/** The formal parameters of this executable element in declaration order */
	abstract val parameters: List<KJParameterElement>

	abstract val returnType: KJTypeMirror?
	abstract val thrownTypes: List<KJTypeMirror>

	abstract override fun asTypeMirror(): KJExecutableType
	abstract override val enclosingElement: KJElement

	abstract override fun asJavaxElement(): ExecutableElement
}


sealed class KJExecutableElement : KJElement(), KJExecutableElementInterface, HasReceiver {
	override val enclosedElements: Set<KJParameterElement>
		get() = parameters.toSet()

	final override fun toString(): String {
		val retSignature = returnType?.let { ": $it" } ?: ""
		val paramsSignature = parameters.map { it.asTypeMirror() }.toString()
		return "$simpleName($paramsSignature)$retSignature"
	}

	companion object
}

abstract class KJFunctionElement : KJExecutableElement(),
								   HasTypeParameters,
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

	abstract override val enclosingElement: KJTypeElement

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

internal interface KJAccessorElementInterface : HasVisibility, HasModality,
												HasKotlinExternalImplementation {
	/**
	 * Whether this accessor is the default implementation and not a
	 * custom getter/setter written by programmer
	 * */
	abstract val isDefaultImplementation: Boolean

	/** Whether this accessor is inline */
	abstract val isInline: Boolean
}

sealed class KJAccessorElement : KJExecutableElement(), KJAccessorElementInterface {
	abstract override val enclosingElement: KJPropertyElement
	companion object
}

abstract class KJGetterElement : KJAccessorElement() {
	override val parameters: List<Nothing> = emptyList()
	override val enclosedElements: Set<Nothing> = emptySet()
	companion object
}

abstract class KJSetterElement : KJAccessorElement()

abstract class KJParameterElement : KJElement() {
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
	override val enclosedElements: Set<Nothing> = emptySet()
	companion object
}

abstract class KJPropertyElement : KJElement(), HasReceiver, HasVisibility,
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
	companion object
}

sealed class KJFieldElement : KJElement(), HasReceiver {
	abstract override fun asJavaxElement(): VariableElement

	abstract override val enclosingElement: KJPropertyElement

	override val enclosedElements: Set<Nothing> = emptySet()
	final override fun toString(): String = simpleName
	companion object
}

/**
 * A [KJDelegateFieldElement] is the field that contains the delegate instance
 * for Kotlin delegated properties.
 */
abstract class KJDelegateFieldElement : KJFieldElement() {
	/** A delegate field is always enclosed by its property */
	abstract override val enclosingElement: KJPropertyElement
	companion object
}

/**
 * A [KJBackingFieldElement] is the field underlying a property. In Kotlin
 * it is automatically generated while in Java we infer it from the name
 * (i.e. `mProp` would be the backing field of the property `prop` with the
 * accessors `setProp` and `getProp`).
 */
abstract class KJBackingFieldElement : KJFieldElement() {
	/* A backing field is always enclosed by its property */
	abstract override val enclosingElement: KJPropertyElement
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