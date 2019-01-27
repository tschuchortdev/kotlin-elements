package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.jvm.jvmClassModuleName
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import mixins.*
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements

/**
 * A static Kotlin- or Java-language level construct that originates from Kotlin source code.
 *
 * That means either an element representing a syntactic construct in Kotlin source code
 * ([KotlinElement]) or a Java [Element] that was generated by the Kotlin compiler but is
 * not accessible from Kotlin code and has no 1:1 correspondence to a syntactic
 * construct in Kotlin code ([KotlinCompatElement])
 */
sealed class KotlinRelatedElement

/**
 * A Java [Element] that was generated by the Kotlin compiler for
 * Java compatibility or implementation purposes and has no 1:1 correspondence
 * to a syntactic construct in Kotlin source code and is not accessible from
 * Kotlin source code except through annotation use-site targets (in contrast
 * to generated elements like default constructors that are accessible from
 * Kotlin source code)
 *
 * Since the element itself never appears explicitly in Kotlin source code, it is
 * not considered a [KotlinElement]
 *
 * Those elements are not necessarily [Elements.Origin.MANDATED] because they
 * are sometimes generated by explicit annotations like @[JvmOverloads]
 */
abstract class KotlinCompatElement(
		override val javaElement: Element
) : KotlinRelatedElement(), Has1To1JavaMapping, AnnotatedConstruct by javaElement {

	abstract val enclosingElement: KotlinRelatedElement

	final override fun toString(): String = javaElement.toString()
	final override fun equals(other: Any?): Boolean = (javaElement == other)
	final override fun hashCode(): Int = javaElement.hashCode()
}

/** Unspecified [KotlinCompatElement] */
class UnspecifiedKotlinCompatElement(
		javaElement: Element,
		override val enclosingElement: KotlinRelatedElement
) : KotlinCompatElement(javaElement)

/**
 * Represents a syntax element such as a class, method, typealias, and so on
 * in the Kotlin source code.
 *
 * This includes compiler generated elements that are directly accessible
 * from Kotlin code but not those that are generated by the Kotlin compiler
 * for Java-compatibility or implementation purposes such as java overloads of a method,
 * backing fields, file facade classes, interface default implementations and so on
 *
 * Not every [KotlinElement] is necessarily backed by at least one Java [Element]
 * (type aliases in particular) and not every [KotlinElement] that corresponds
 * to only a single Java [Element] (for example: methods with @[JvmOverloads] annotation)
 */
sealed class KotlinElement: KotlinRelatedElement(), AnnotatedConstruct {
	abstract val enclosingElement: KotlinElement?

	abstract val simpleName: Name

	abstract fun asType(): TypeMirror

	//TODO("visitor API")

	abstract override fun toString(): String
	abstract override fun equals(other: Any?): Boolean
	abstract override fun hashCode(): Int
}

/** An element that declares a new type like a class, object, interface or annotation class */
sealed class KotlinTypeElement(
	final override val javaElement: TypeElement,
	metadata: KotlinClassMetadata,
	processingEnv: ProcessingEnvironment
) : KotlinElement(), KotlinParameterizable,
	KotlinQualifiedNameable, HasKotlinVisibility, HasKotlinModality, Has1To1JavaMapping,
	HasKotlinMultiPlatformImplementations, HasKotlinExternalImplementation, AnnotatedConstruct by javaElement {

	protected val protoClass: ProtoBuf.Class = metadata.data.classProto
	protected val protoNameResolver: NameResolver = metadata.data.nameResolver
	protected val protoTypeTable: ProtoBuf.TypeTable = protoClass.typeTable

	protected val jvmModuleName: String? = protoClass.jvmClassModuleName?.let{ protoNameResolver.getString(it) }

	override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoClass.visibility!!)

	override val modality: KotlinModality = KotlinModality.fromProtoBuf(protoClass.modality!!)

	/** Whether this type is an inner type */
	val isInner: Boolean = protoClass.isInnerClass

	/** The interfaces implemented by this type */
	val interfaces: List<TypeMirror> = javaElement.interfaces

	/** The superclass extended by this type */
	val superclass: TypeMirror = javaElement.superclass

	final override val isExpect: Boolean = protoClass.isExpectClass

	final override val isExternal: Boolean = protoClass.isExternalClass

	override val enclosingElement: KotlinElement by lazy {
		javaElement.enclosingElement.asKotlin(processingEnv) as KotlinElement
	}

	final override val typeParameters: List<KotlinTypeParameterElement>
		get() = parameterizableDelegate.typeParameters

	final override fun asType(): TypeMirror = javaElement.asType()

	final override val qualifiedName: Name = javaElement.qualifiedName

	final override val simpleName: Name = javaElement.simpleName

	final override fun equals(other: Any?)
			= (other as? KotlinTypeElement)?.javaElement == javaElement

	final override fun hashCode() = javaElement.hashCode()

	final override fun toString() = javaElement.toString()

	private fun getCompanionSimpleName(): String? =
		if (protoClass.hasCompanionObjectName())
			protoNameResolver.getString(protoClass.companionObjectName)
		else
			null

	private val enclosedElementsDelegate by lazy {
		// lazy to avoid leaking this in ctor
		EnclosedElementsDelegate(
			enclosingKtElement = this,
			protoTypeAliases = protoClass.typeAliasList,
			protoProps = protoClass.propertyList,
			protoCtors = protoClass.constructorList,
			protoFunctions = protoClass.functionList,
			companionSimpleName = getCompanionSimpleName(),
			enclosedJavaElems = javaElement.enclosedElements,
			protoNameResolver = protoNameResolver,
			protoTypeTable = protoTypeTable,
			processingEnv = processingEnv
		)
	}

	protected open val enclosedKotlinElements: Set<KotlinElement> = enclosedElementsDelegate.kotlinElements
	protected open val companion: KotlinObjectElement? = enclosedElementsDelegate.companion
	protected open val constructors: Set<KotlinConstructorElement> = enclosedElementsDelegate.constructors
	protected open val functions: Set<KotlinFunctionElement> = enclosedElementsDelegate.functions
	protected open val properties: Set<KotlinPropertyElement> = enclosedElementsDelegate.properties
	protected open val kotlinTypes: Set<KotlinTypeElement> = enclosedElementsDelegate.types

	private val parameterizableDelegate by lazy {
		// lazy to avoid leaking this in ctor
		KotlinParameterizableDelegate(
			this, protoClass.typeParameterList,
			javaElement.typeParameters, protoNameResolver
		)
	}
}

/** A Kotlin class declaration */
class KotlinClassElement internal constructor(
	javaElement: TypeElement,
	metadata: KotlinClassMetadata,
	processingEnv: ProcessingEnvironment
) : KotlinTypeElement(javaElement, metadata, processingEnv), EnclosesKotlinConstructors,
	EnclosesKotlinFunctions, EnclosesKotlinProperties, EnclosesKotlinTypes,
	HasKotlinCompanion {

	/** Whether this class is a data class */
	val isDataClass: Boolean = protoClass.isDataClass

	override val enclosedKotlinElements: Set<KotlinElement> = super<KotlinTypeElement>.enclosedKotlinElements
	override val companion: KotlinObjectElement? = super.companion
	override val constructors: Set<KotlinConstructorElement> = super.constructors
	override val functions: Set<KotlinFunctionElement> = super.functions
	override val properties: Set<KotlinPropertyElement> = super.properties
	override val kotlinTypes: Set<KotlinTypeElement> = super.kotlinTypes
}

/** A Kotlin interface declaration */
class KotlinInterfaceElement internal constructor(
	javaElement: TypeElement,
	metadata: KotlinClassMetadata,
	processingEnv: ProcessingEnvironment
) : KotlinTypeElement(javaElement, metadata, processingEnv),
	EnclosesKotlinFunctions, EnclosesKotlinProperties, EnclosesKotlinTypes,
	HasKotlinCompanion {

	override val enclosedKotlinElements: Set<KotlinElement> = super<KotlinTypeElement>.enclosedKotlinElements
	override val companion: KotlinObjectElement? = super.companion
	override val functions: Set<KotlinFunctionElement> = super.functions
	override val properties: Set<KotlinPropertyElement> = super.properties
	override val kotlinTypes: Set<KotlinTypeElement> = super.kotlinTypes
}

/**
 * A class that is generated by the Kotlin compiler to hold default implementations
 * for functions in an interface
 */
class KotlinInterfaceDefaultImplElement internal constructor(
		override val javaElement: TypeElement,
		metadata: KotlinSyntheticClassMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinCompatElement(javaElement) {
	override val enclosingElement: KotlinRelatedElement by lazy {
		javaElement.enclosingElement.asKotlin(processingEnv) as KotlinElement
	}
}

/** A Kotlin object singleton declaration */
class KotlinObjectElement internal constructor(
	javaElement: TypeElement,
	metadata: KotlinClassMetadata,
	processingEnv: ProcessingEnvironment
) : KotlinTypeElement(javaElement, metadata, processingEnv), EnclosesKotlinTypes,
	EnclosesKotlinProperties, EnclosesKotlinFunctions {

	/** Whether this object is a companion object */
	val isCompanion: Boolean = (protoClass.classKind == ProtoBuf.Class.Kind.COMPANION_OBJECT)

	override val enclosedKotlinElements: Set<KotlinElement> = super<KotlinTypeElement>.enclosedKotlinElements
	override val companion: KotlinObjectElement? = super.companion
	override val functions: Set<KotlinFunctionElement> = super.functions
	override val properties: Set<KotlinPropertyElement> = super.properties
	override val kotlinTypes: Set<KotlinTypeElement> = super.kotlinTypes
}


/** A declaration of an annotation class */
class KotlinAnnotationElement internal constructor(
	javaElement: TypeElement,
	metadata: KotlinClassMetadata,
	processingEnv: ProcessingEnvironment
) : KotlinTypeElement(javaElement, metadata, processingEnv) {
	/** The parameters of the annotation */
	val parameters: List<KotlinAnnotationParameterElement> by lazy {
		javaElement.enclosedElements.map {
			check(it.kind == ElementKind.METHOD)
			KotlinAnnotationParameterElement(it as ExecutableElement, this)
		}
	}
}

/** A parameter of an annotation class */
class KotlinAnnotationParameterElement internal constructor(
	override val javaElement: ExecutableElement,
	/**
	 * An annotation parameter is enclosed by its annotation class
	 */
	override val enclosingElement: KotlinAnnotationElement
) : KotlinElement(), Has1To1JavaMapping, AnnotatedConstruct by javaElement {

	/**
	 * The default value of this annotation parameter or
	 * [null] if it doesn't have one
	 */
	val defaultValue: Any? = javaElement.defaultValue

	override val simpleName: Name = javaElement.simpleName

	override fun asType(): TypeMirror = javaElement.asType()

	override fun equals(other: Any?): Boolean = (javaElement == other)
	override fun hashCode(): Int = javaElement.hashCode()
	override fun toString(): String = javaElement.toString() //TODO("annotation parameter toString")
}

/** A declaration of an enum class */
class KotlinEnumElement internal constructor(
	javaElement: TypeElement,
	metadata: KotlinClassMetadata,
	processingEnv: ProcessingEnvironment
) : KotlinTypeElement(javaElement, metadata, processingEnv), EnclosesKotlinConstructors,
	EnclosesKotlinFunctions, EnclosesKotlinProperties, EnclosesKotlinTypes,
	HasKotlinCompanion {

	/** The enum constants defined by this enum declaration */
	val enumConstants: List<KotlinEnumConstantElement> get() = TODO("Kotlin enum constants list")

	override val enclosedKotlinElements: Set<KotlinElement> = super<KotlinTypeElement>.enclosedKotlinElements
	override val companion: KotlinObjectElement? = super.companion
	override val constructors: Set<KotlinConstructorElement> = super.constructors
	override val functions: Set<KotlinFunctionElement> = super.functions
	override val properties: Set<KotlinPropertyElement> = super.properties
	override val kotlinTypes: Set<KotlinTypeElement> = super.kotlinTypes
}

/** A declaration of an enum constant */
class KotlinEnumConstantElement internal constructor(
	override val javaElement: Element
) : KotlinElement(), Has1To1JavaMapping, AnnotatedConstruct by javaElement {

	override val enclosingElement: KotlinEnumElement
		get() = TODO("implement enum constant enclosing element")

	//TODO("check if enum constants are kotlinTypes in all cases")

	override val simpleName: Name = javaElement.simpleName

	override fun asType(): TypeMirror = javaElement.asType()

	override fun equals(other: Any?): Boolean
			= (other as KotlinEnumConstantElement).javaElement == javaElement

	override fun hashCode(): Int = javaElement.hashCode()
	override fun toString(): String = javaElement.toString()
}

/**
 *  A [KotlinExecutableElement] corresponds to a single method, free function or constructor
 *  in Kotlin source code, but may correspond to several Java [ExecutableElement]s that are
 *  generated when @[JvmOverloads] is used
 */
abstract class KotlinExecutableElement internal constructor(
	/**
	 * the Java method associated with this Kotlin method or the one that
	 * has the same signature (all parameters) if there are multiple associated
	 * Java elements generated by @[JvmOverloads]
	 */
	final override val javaElement: ExecutableElement,
	javaOverloadElements: List<ExecutableElement>,
	override val enclosingElement: KotlinElement
) : KotlinElement(), Has1To1JavaMapping, AnnotatedConstruct by javaElement {

	init {
		if(javaOverloadElements.isNotEmpty())
		// check that all the javaElements associated with this Kotlin ExecutableElement...
			with(javaOverloadElements + javaElement) {
				// ...have the same return type
				require(allEqualBy(ExecutableElement::getReturnType))

				// ...have the same receiver type
				require(allEqualBy(ExecutableElement::getReceiverType))

				// ...have the same name
				require(allEqualBy(ExecutableElement::getSimpleName))

				// ...have the same enclosing javaElement
				require(allEqualBy(ExecutableElement::getEnclosingElement))

				// ...none enclose elements
				assert(all { it.enclosedElements.isEmpty() })

				// Unfortunately @JvmOverloads has RetentionPolicy.BINARY so we can not check that
				// the elements actually have a @JvmOverloads annotation if there are multiple
			}
	}

	/**
	 * Java method elements that were generated by @[JvmOverloads] and have
	 * less parameters than the Kotlin method
	 */
	open val javaOverloads: List<JavaOverload> = javaOverloadElements.map(::JavaOverload)

	/**
	 * Returns {@code true} if this method or constructor accepts a variable
	 * number of arguments and returns {@code false} otherwise.
	 *
	 * @return {@code true} if this method or constructor accepts a variable
	 * number of arguments and {@code false} otherwise
	 */
	val isVarArgs: Boolean = javaElement.isVarArgs

	/**
	 * The formal parameters of this executable.
	 * They are returned in declaration order.
	 *
	 * @return the formal parameters,
	 * or an empty list if there are none
	 */
	abstract val parameters: List<KotlinParameterElement>

	val receiverType: TypeMirror? = javaElement.receiverType //TODO("handle Kotlin receiver type")

	val thrownTypes: List<TypeMirror> = javaElement.thrownTypes //TODO("handle Kotlin thrown kotlinTypes")

	val returnType: TypeMirror = javaElement.returnType //TODO("handle Kotlin return kotlinTypes")

	final override fun asType(): TypeMirror = javaElement.asType() //TODO("handle kotlin executable element asType")

	final override fun equals(other: Any?)
			= (other as? KotlinExecutableElement)?.javaElement == javaElement

	final override fun hashCode() = Objects.hash(javaElement)
	abstract override fun toString(): String

	/** Java overload of a Kotlin function with default parameters and [JvmOverloads] annotation */
	inner class JavaOverload(override val javaElement: ExecutableElement)
		: KotlinCompatElement(javaElement) {

		/** A [JavaOverload] is enclosed by its corresponding non-overloaded Kotlin function */
		override val enclosingElement: KotlinExecutableElement = this@KotlinExecutableElement
	}
}

/** A declaration of a Kotlin method or free function */
class KotlinFunctionElement internal constructor(
	javaElement: ExecutableElement,
	javaOverloadElements: List<ExecutableElement>,
	enclosingElement: KotlinElement,
	private val protoFunction: ProtoBuf.Function,
	private val protoNameResolver: NameResolver,
	elemUtils: Elements
) : KotlinExecutableElement(javaElement, javaOverloadElements, enclosingElement),
	KotlinParameterizable, HasKotlinModality, HasKotlinVisibility,
	HasKotlinMultiPlatformImplementations, HasKotlinExternalImplementation {

	val isInline: Boolean = protoFunction.isInline
	val isInfix: Boolean = protoFunction.isInfix
	val isTailRec: Boolean = protoFunction.isTailRec
	val isSuspend: Boolean = protoFunction.isSuspend
	val isOperator: Boolean = protoFunction.isOperator

	//TODO("is free function")
	//TODO("is extension function")

	override val isExpect: Boolean = protoFunction.isExpectFunction
	override val isExternal: Boolean = protoFunction.isExternalFunction

	override val modality: KotlinModality = KotlinModality.fromProtoBuf(protoFunction.modality!!)
		.also { assert(it != KotlinModality.SEALED) }

	override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoFunction.visibility!!)

	override val parameters: List<KotlinFunctionParameterElement> by lazy {
		KotlinFunctionParameterDelegate(this, protoFunction.valueParameterList,
			javaElement.parameters, protoNameResolver).parameters
	}

	override val typeParameters: List<KotlinTypeParameterElement>
		get() = parameterizableDelegate.typeParameters

	override val simpleName: Name
	// if JvmName is used, the name of the Kotlin function may be different than the jvm name
			= elemUtils.getName(protoNameResolver.getString(protoFunction.name))

	override fun toString(): String {
		// if JvmName is used, the name of the Kotlin function may be different than the jvm name
		val javaElemString = javaElement.toString()
		assert(Regex("[^\\(\\)]+?\\([^\\(\\)]*?\\)[^\\(\\)]*").matches(javaElemString))
		return simpleName.toString() + "(" + javaElemString.substringAfter("(")
	}

	private val parameterizableDelegate by lazy {
		// lazy to avoid leaking this in ctor
		KotlinParameterizableDelegate(
			this, protoFunction.typeParameterList,
			javaElement.typeParameters, protoNameResolver
		)
	}
}

/**
 * A Kotlin constructor declaration
 *
 * Note that annotation classes do not actually have constructors
 */
class KotlinConstructorElement internal constructor(
	javaElement: ExecutableElement,
	javaOverloadElements: List<ExecutableElement>,
	override val enclosingElement: KotlinTypeElement,
	protoConstructor: ProtoBuf.Constructor,
	protoNameResolver: NameResolver
) : KotlinExecutableElement(javaElement, javaOverloadElements, enclosingElement), HasKotlinVisibility {

	/** Whether this constructor is the primary constructor of its class */
	val isPrimary: Boolean = protoConstructor.isPrimary

	override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoConstructor.visibility!!)

	override val parameters: List<KotlinFunctionParameterElement> by lazy {
		KotlinFunctionParameterDelegate(this, protoConstructor.valueParameterList,
			javaElement.parameters, protoNameResolver).parameters
	}

	override val simpleName: Name = javaElement.simpleName

	override fun toString(): String = javaElement.toString()
}

/** A parameter of a [KotlinExecutableElement], i.e. of a constructor, function, method or setter */
sealed class KotlinParameterElement(
	final override val javaElement: VariableElement,
	override val enclosingElement: KotlinExecutableElement
) : KotlinElement(), Has1To1JavaMapping, AnnotatedConstruct by javaElement {
	override val simpleName: Name = javaElement.simpleName
	override fun asType(): TypeMirror = javaElement.asType()

	override fun equals(other: Any?): Boolean
			= (other as? KotlinFunctionParameterElement)?.javaElement == javaElement

	override fun hashCode() = javaElement.hashCode()
	override fun toString() = javaElement.toString()
}

/** A parameter of a Kotlin constructor, function or method but not a setter */
class KotlinFunctionParameterElement internal constructor(
	javaElement: VariableElement,
	protoParam: ProtoBuf.ValueParameter,
	enclosingElement: KotlinExecutableElement
) : KotlinParameterElement(javaElement, enclosingElement)  {

	/**
	 * Whether this parameter has a default value
	 *
	 * Not to be confused with [javax.lang.model.element.ExecutableElement.isDefault]
	 * and [javax.lang.model.element.ExecutableElement.getDefaultValue] which
	 * merely returns the default value of an annotation class parameter
	 */
	val hasDefaultValue: Boolean = protoParam.declaresDefaultValue

	/** Whether this parameter has the `crossinline` modifier */
	val isCrossInline: Boolean = protoParam.isCrossInline

	/** Whether this parameter has the `noinline` modifier */
	val isNoInline: Boolean = protoParam.isNoInline
}

/** A Kotlin property declaration */
class KotlinPropertyElement internal constructor(
	field: VariableElement?,
	setter: ExecutableElement?,
	getter: ExecutableElement?,
	/**
	 * If the Kotlin property has annotations with target [AnnotationTarget.PROPERTY]
	 * the Kotlin compiler will generate an empty parameterless void-returning
	 * synthetic method named "propertyName$annotations" to hold the annotations that
	 * are targeted at the property and not backing field, getter or setter
	 */
	val javaAnnotationHolderElement: ExecutableElement?,
	delegateField: VariableElement?,
	protoProperty: ProtoBuf.Property,
	protoNameResolver: NameResolver,
	processingEnv: ProcessingEnvironment
) : KotlinElement(), HasKotlinVisibility, HasKotlinModality,
	HasKotlinExternalImplementation, HasKotlinMultiPlatformImplementations {

	init {
		val presentJavaElems = arrayListOf(field, getter, setter).filterNotNull()

		require(presentJavaElems.isNotEmpty())

		// all non-null java elements must have the same enclosing javaElement
		require(presentJavaElems.allEqualBy(Element::getEnclosingElement))

		// all non-null java elements should have the same type
		require(arrayListOf(field?.asType(), setter?.parameters?.single()?.asType(), getter?.returnType)
			.filterNotNull().allEqual())

		assert(protoProperty.isVal || protoProperty.isVar)
	}

	/** The backing field of this property, if it has one */
	val backingField: BackingField? = field?.let { BackingField(it) }

	/** The setter of this property, if it has one */
	val setter: KotlinSetterElement? = setter?.let { KotlinSetterElement(this, it, protoProperty) }

	/** The getter of this property, if it has one */
	val getter: KotlinGetterElement? = getter?.let { KotlinGetterElement(this, it, protoProperty) }

	/**
	 * If the Kotlin property is delegated a field is generated to hold
	 * the instance of the delegate class
	 */
	val delegateField: DelegateField? = delegateField?.let { DelegateField(it) }

	/**
	 * Returns the value of this property if this is a `const`
	 * property initialized to a compile-time constant.  Returns
	 * `null` otherwise.  The value will be of a primitive type or a
	 * [String].
	 *
	 * Enum constants are not considered to be compile-time constants.
	 */
	val constantValue: Any? = if(protoProperty.isConst)
		backingField!!.javaElement.constantValue!!
	else
		null

	/** Whether this property is delegated */
	val isDelegated: Boolean = protoProperty.isDelegated

	/** Whether this property is late-initialized */
	val isLateInit: Boolean = protoProperty.isExpectProperty

	/** whether this property is read-only */
	val isReadOnly: Boolean = protoProperty.isVal

	/** The receiver type of this property if it is not a free property */
	val receiverType: TypeMirror? get() = TODO("property receiver type")

	override val isExpect: Boolean = protoProperty.isExpectProperty

	override val isExternal: Boolean = protoProperty.isExternalProperty

	override val modality: KotlinModality = KotlinModality.fromProtoBuf(protoProperty.modality!!)

	override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoProperty.visibility!!)

	override val simpleName: Name
			= processingEnv.elementUtils.getName(protoNameResolver.getString(protoProperty.name))


	//TODO("KotlinPropertyElement: return correctly resolved Kotlin type")
	override fun asType(): TypeMirror =
		backingField?.run { asType() }
			?: getter?.run { javaElement.returnType!! }
			?: setter?.run { parameters.first().javaElement.asType() }!!

	override val enclosingElement: KotlinElement by lazy {
		val nonNullJavaElem = backingField?.javaElement
			?: this.getter?.javaElement
			?: this.setter?.javaElement
			?: throw AssertionError("at least one java javaElement must be non-null")

		nonNullJavaElem.enclosingElement!!.asKotlin(processingEnv) as KotlinElement
	}

	override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>): Array<A> {
		@Suppress("unchecked_cast")
		return javaAnnotationHolderElement?.getAnnotationsByType(annotationType)
			?: java.lang.reflect.Array.newInstance(annotationType, 0) as Array<A>
	}

	override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A? {
		return javaAnnotationHolderElement?.getAnnotation(annotationType)
	}

	override fun getAnnotationMirrors(): List<AnnotationMirror> {
		return javaAnnotationHolderElement?.annotationMirrors ?: emptyList()
	}

	/**
	 * Returns the Java modifiers that would best represent the visibility, modality,
	 * and so on of this property
	 */
	fun getAppropriateJavaModifiers(): Set<Modifier> {
		val modalityModifier: Modifier? = when(modality) {
			KotlinModality.FINAL -> Modifier.FINAL
			KotlinModality.SEALED,
			KotlinModality.ABSTRACT -> Modifier.ABSTRACT
			KotlinModality.OPEN -> null
		}

		val visibilityModifier: Modifier? = when(visibility) {
			KotlinVisibility.PRIVATE -> if(enclosingElement is KotlinPackageElement)
				null // private top level members become package private (i.e. no modifier) in Java
			else
				Modifier.PRIVATE
			KotlinVisibility.PRIVATE_TO_THIS -> Modifier.PRIVATE
			KotlinVisibility.PUBLIC -> Modifier.PUBLIC
			KotlinVisibility.PROTECTED -> Modifier.PROTECTED
			KotlinVisibility.LOCAL -> throw AssertionError(
				"This library was written with the assumption that" +
						"it is impossible to get a local javaElement. In the future" +
						"this might change.")
			KotlinVisibility.INTERNAL -> Modifier.PUBLIC
		}

		val annotatedModifiers = arrayOf<Modifier>().apply {
			if(backingField?.javaElement?.modifiers?.contains(Modifier.VOLATILE) == true)
				plus(Modifier.VOLATILE)

			if(backingField?.javaElement?.modifiers?.contains(Modifier.TRANSIENT) == true)
				plus(Modifier.TRANSIENT)

			if(backingField?.javaElement?.modifiers?.contains(Modifier.NATIVE) == true)
				plus(Modifier.NATIVE)
		}

		return setOfNotNull(modalityModifier, visibilityModifier, *annotatedModifiers)
	}

	/** A [BackingField] is hidden field generated for a Kotlin property */
	inner class BackingField(override val javaElement: VariableElement)
		: KotlinCompatElement(javaElement) {

		/** A backing field is enclosed by its property */
		override val enclosingElement: KotlinPropertyElement = this@KotlinPropertyElement
	}

	/**
	 * A [DelegateField] is a hidden field generated for a Kotlin property to hold the reference
	 * to the delegate instance
	 */
	inner class DelegateField(override val javaElement: VariableElement)
		: KotlinCompatElement(javaElement) {

		/** A delegate field is enclosed by its property */
		override val enclosingElement: KotlinPropertyElement = this@KotlinPropertyElement
	}

	override fun equals(other: Any?): Boolean {
		return if (other is KotlinPropertyElement)
			backingField == other.backingField
					&& setter == other.setter
					&& getter == other.getter
					&& delegateField == other.delegateField
		else
			false
	}

	override fun hashCode() = Objects.hash(backingField, setter,
		getter, javaAnnotationHolderElement, delegateField)

	override fun toString() = simpleName.toString()
}

/** The accessor (getter or setter) of a property */
sealed class KotlinAccessorElement(
	/** An accessor is enclosed by its property */
	final override val enclosingElement: KotlinPropertyElement,
	javaElement: ExecutableElement
) : KotlinExecutableElement(javaElement, emptyList<Nothing>(), enclosingElement), HasKotlinVisibility,
	HasKotlinModality, HasKotlinExternalImplementation {

	/** Whether this accessor is the default implementation and not a custom getter/setter written by programmer */
	abstract val isDefaultImplementation: Boolean

	/** Whether this accessor is inline */
	abstract val isInline: Boolean

	/** A property accessor never has java overloads  */
	final override val javaOverloads: List<Nothing> = emptyList()

	final override val simpleName: Name get() = javaElement.simpleName
	override fun toString() = javaElement.toString()
}

/** Getter element of a Kotlin property */
class KotlinGetterElement internal constructor(
	enclosingElement: KotlinPropertyElement,
	javaElement: ExecutableElement,
	protoProperty: ProtoBuf.Property
) : KotlinAccessorElement(enclosingElement, javaElement) {

	override val parameters: List<Nothing> = emptyList()
	override val isDefaultImplementation: Boolean = protoProperty.isGetterDefault
	override val isExternal: Boolean = protoProperty.isGetterExternal
	override val isInline: Boolean = protoProperty.isGetterInline
	override val modality: KotlinModality = KotlinModality.fromProtoBuf(protoProperty.getterModality!!)
	override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoProperty.getterVisibility!!)
}

/* Setter element of a Kotlin property */
class KotlinSetterElement internal constructor(
	enclosingElement: KotlinPropertyElement,
	javaElement: ExecutableElement,
	protoProperty: ProtoBuf.Property
) : KotlinAccessorElement(enclosingElement, javaElement) {
	override val isDefaultImplementation: Boolean = protoProperty.isSetterDefault
	override val isExternal: Boolean = protoProperty.isSetterExternal
	override val isInline: Boolean = protoProperty.isSetterInline
	override val modality: KotlinModality = KotlinModality.fromProtoBuf(protoProperty.setterModality!!)
	override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoProperty.setterVisibility!!)

	override val parameters: List<KotlinParameterElement>
			= listOf(KotlinSetterParameterElement(this, javaElement.parameters.first()))
}

/** Parameter of a Kotlin setter */
class KotlinSetterParameterElement internal constructor(
	/** A setter parameter is enclosed by its setter */
	override val enclosingElement: KotlinSetterElement,
	javaElement: VariableElement
) : KotlinParameterElement(javaElement, enclosingElement)

/** Declaration of a package that contains Kotlin code */
class KotlinPackageElement internal constructor(
	override val javaElement: PackageElement,
	metadata: KotlinPackageMetadata,
	processingEnv: ProcessingEnvironment
) : KotlinElement(), KotlinQualifiedNameable,
	EnclosesKotlinTypes, EnclosesKotlinPackages, EnclosesKotlinFunctions,
	EnclosesKotlinProperties, EnclosesKotlinTypeAliases, EnclosesJavaPackages,
	EnclosesJavaTypes, Has1To1JavaMapping, AnnotatedConstruct by javaElement {

	/** Whether this package is unnamed */
	val isUnnamed: Boolean = javaElement.isUnnamed

	override val qualifiedName: Name = javaElement.qualifiedName

	override val simpleName: Name = javaElement.simpleName

	override fun asType(): TypeMirror = javaElement.asType()

	override val enclosingElement: KotlinElement? by lazy {
		javaElement.enclosingElement?.run { asKotlin(processingEnv) as KotlinElement }
	}

	override val enclosedJavaElements: Set<Element> by lazy {
		javaElement.enclosedElements.asSequence().filter { !it.originatesFromKotlinCode() }.toSet()
	}

	override val javaPackages: Set<PackageElement> by lazy {
		enclosedJavaElements.filter { it.kind == ElementKind.PACKAGE }
			.castList<PackageElement>().toSet()
	}

	override val javaTypes: Set<TypeElement> by lazy {
		enclosedJavaElements.mapNotNull { it.asTypeElement() }.toSet()
	}

	override val enclosedKotlinElements: Set<KotlinElement>
		get() = enclosedElementsDelegate.kotlinElements

	override val functions: Set<KotlinFunctionElement>
		get() = enclosedElementsDelegate.functions

	override val properties: Set<KotlinPropertyElement>
		get() = enclosedElementsDelegate.properties

	override val kotlinTypes: Set<KotlinTypeElement>
		get() = enclosedElementsDelegate.types

	override val kotlinPackages: Set<KotlinPackageElement>
		get() = enclosedElementsDelegate.packages

	override val typeAliases: Set<KotlinTypeAliasElement>
		get() = enclosedElementsDelegate.typeAliases

	override fun toString() = javaElement.toString()
	override fun equals(other: Any?)
			= (other as? KotlinPackageElement)?.javaElement == javaElement
	override fun hashCode() = javaElement.hashCode()

	private val protoPackage: ProtoBuf.Package = metadata.data.packageProto
	private val protoTypeTable: ProtoBuf.TypeTable = protoPackage.typeTable
	private val protoNameResolver: NameResolver = metadata.data.nameResolver

	private val enclosedElementsDelegate = EnclosedElementsDelegate(
		enclosingKtElement = this,
		protoTypeAliases = protoPackage.typeAliasList,
		protoProps = protoPackage.propertyList,
		protoCtors = emptyList(),
		protoFunctions = protoPackage.functionList,
		companionSimpleName = null,
		enclosedJavaElems = javaElement.enclosedElements,
		protoNameResolver = protoNameResolver,
		protoTypeTable = protoTypeTable,
		processingEnv = processingEnv
	)
}

/** A module declaration that contains Kotlin packages */
class KotlinModuleElement internal constructor(
	override val javaElement: ModuleElement,
	processingEnv: ProcessingEnvironment
) : KotlinElement(), EnclosesKotlinPackages, KotlinQualifiedNameable,
	EnclosesJavaPackages, Has1To1JavaMapping, AnnotatedConstruct by javaElement {

	/** Whether this module is open */
	val isOpen: Boolean = javaElement.isOpen

	/** Whether this module is unnamed */
	val isUnnamed: Boolean = javaElement.isUnnamed

	/** The directives declared by this module */
	val directives: List<ModuleElement.Directive> = javaElement.directives

	override val javaPackages: Set<PackageElement> by lazy {
		javaElement.enclosedElements
			.filter { !it.originatesFromKotlinCode() }
			.map {
				assert(it.kind == ElementKind.PACKAGE)
				it as PackageElement
			}.toSet()
	}

	override val enclosedKotlinElements: Set<KotlinPackageElement> get() = kotlinPackages

	/** The kotlin packages enclosed by this module */
	override val kotlinPackages: Set<KotlinPackageElement> by lazy {
		javaElement.enclosedElements.mapNotNull {
			it.asKotlin(processingEnv) as? KotlinPackageElement
		}.toSet()
	}

	/** Always `null` because a module is never enclosed by another element */
	override val enclosingElement: Nothing? = null
	override val qualifiedName: Name = javaElement.qualifiedName
	override val simpleName: Name = javaElement.simpleName
	override fun asType(): TypeMirror = javaElement.asType()

	override fun equals(other: Any?): Boolean
			= (other as? KotlinModuleElement)?.javaElement == javaElement

	override fun hashCode(): Int = Objects.hash(javaElement)
	override fun toString(): String = javaElement.toString()
}

/** A kotlin type alias declaration */
class KotlinTypeAliasElement internal constructor(
	/**
	 * If the Kotlin type alias has annotations the Kotlin compiler will generate
	 * an empty parameterless void-returning synthetic method named
	 * "aliasName$annotations" to hold the annotations
	 */
	val javaAnnotationHolderElement: ExecutableElement?,
	protoTypeAlias: ProtoBuf.TypeAlias,
	protoTypeTable: ProtoBuf.TypeTable,
	protoNameResolver: NameResolver,
	override val enclosingElement: KotlinElement,
	elementUtils: Elements
) : KotlinElement(), HasKotlinVisibility, KotlinParameterizable {

	init {
		assert(protoTypeAlias.hasAnnotations == (javaAnnotationHolderElement != null))
	}

	val underlyingType: TypeMirror = TODO("alias underlying type")

	val expandedType: TypeMirror = TODO("alias expanded type")

	override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoTypeAlias.visibility!!)

	override val simpleName: Name = elementUtils.getName(protoNameResolver.getString(protoTypeAlias.name))

	override val typeParameters: List<KotlinTypeParameterElement>
		get() = TODO("type alias type parameters")

	override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>): Array<A>
			= javaAnnotationHolderElement?.getAnnotationsByType(annotationType)
			?: java.lang.reflect.Array.newInstance(annotationType, 0) as Array<A>

	override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A?
			= javaAnnotationHolderElement?.getAnnotation(annotationType)


	override fun getAnnotationMirrors(): List<AnnotationMirror>
			= javaAnnotationHolderElement?.annotationMirrors ?: emptyList()

	override fun asType(): TypeMirror = underlyingType

	override fun equals(other: Any?): Boolean
			= if(other is KotlinTypeAliasElement)
		other.enclosingElement == enclosingElement && other.simpleName == simpleName
	else
		false

	override fun hashCode(): Int = Objects.hash(enclosingElement, simpleName)

	override fun toString(): String
			= simpleName.toString() + typeParameters.joinToString(", ", "<", ">")

}

/** Type parameter of a [KotlinParameterizable] element */
class KotlinTypeParameterElement internal constructor(
	val javaElement: TypeParameterElement,
	protoTypeParam: ProtoBuf.TypeParameter,
	override val enclosingElement: KotlinElement
) : KotlinElement(), AnnotatedConstruct by javaElement {

	//TODO("test type parameter annotations")

	/** Variance of a type parameter */
	enum class Variance  { IN, OUT, INVARIANT }

	/** Variance of this type parameter */
	val variance: Variance = when(protoTypeParam.variance!!) {
		ProtoBuf.TypeParameter.Variance.IN -> Variance.IN
		ProtoBuf.TypeParameter.Variance.OUT -> Variance.OUT
		ProtoBuf.TypeParameter.Variance.INV -> Variance.INVARIANT
	}

	/** Whether this type parameter is reified */
	val reified: Boolean = protoTypeParam.reified

	/** The bounds of this type parameter */
	val bounds: List<TypeMirror> = javaElement.bounds //TODO("bounds KotlinTypeMirrors")

	/**
	 * The [KotlinElement] that is parameterized by this [KotlinTypeParameterElement].
	 * Same as the [enclosingElement]
	 */
	val genericElement: KotlinElement by lazy { enclosingElement }

	override val simpleName: Name = javaElement.simpleName

	//TODO("translate type parameter TypeMirror")
	override fun asType(): TypeMirror = javaElement.asType()

	override fun equals(other: Any?) = (other as? KotlinTypeParameterElement)?.javaElement == javaElement
	override fun hashCode() = javaElement.hashCode()
	override fun toString() = javaElement.toString()
}