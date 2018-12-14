package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.load.java.JvmAbi
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

/** A Kotlin property */
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
		private val protoProperty: ProtoBuf.Property,
		protoNameResolver: NameResolver,
		processingEnv: ProcessingEnvironment
) : KotlinElement(processingEnv), HasKotlinVisibility, HasKotlinModality {

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

	/** The backing Java field element of this property, if it has one */
	val backingField: BackingField? = field?.let { BackingField(it) }

	/** The setter Java element of this property, if it has one */
	val setter: Setter? = setter?.let { Setter(it) }

	/** The getter Java element of this property, if it has one */
	val getter: Getter? = getter?.let { Getter(it) }

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

	//TODO("handle delegated properties")
	val isDelegated: Boolean = protoProperty.isDelegated

	/**
	 * Whether this property has the `expect` keyword
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

	override val simpleName: Name
			= processingEnv.elementUtils.getName(protoNameResolver.getString(protoProperty.name))


	//TODO("KotlinPropertyElement: return correctly resolved Kotlin type")
	/*override fun asType(): TypeMirror =
			javaFieldElement?.run { asType()!! }
			?: javaGetterElement?.run { returnType }
			?: javaSetterElement?.run { parameters.single().asType() }!!*/

	override val enclosingElement: KotlinElement by lazy {
		val nonNullJavaElem = backingField?.javaElement
							  ?: this.getter?.javaElement
							  ?: this.setter?.javaElement
							  ?: throw AssertionError("at least one java javaElement must be non-null")

		nonNullJavaElem.enclosingElement!!.asKotlin(processingEnv) as KotlinElement
	}

	override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>): Array<A> {
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

	/** A [BackingField] is hidden field generated for a Kotlin property */
	inner class BackingField(override val javaElement: VariableElement)
		: KotlinCompatElement(javaElement, processingEnv) {

		/** A backing field is enclosed by its property */
		override val enclosingElement: KotlinPropertyElement = this@KotlinPropertyElement
	}

	/**
	 * A [DelegateField] is a hidden field generated for a Kotlin property to hold the reference
	 * to the delegate instance
	 */
	inner class DelegateField(override val javaElement: VariableElement)
		: KotlinCompatElement(javaElement, processingEnv) {

		/** A [DelegateField] is enclosed by its property */
		override val enclosingElement: KotlinPropertyElement = this@KotlinPropertyElement
	}

	/** The accessor (getter or setter) of a property */
	abstract inner class Accessor(open val javaElement: ExecutableElement)
		: KotlinElement(processingEnv), HasKotlinVisibility, HasKotlinModality, AnnotatedConstruct by javaElement {

		/** Whether this accessor is the default implementation and not a custom getter/setter written by programmer */
		abstract val isDefaultImplementation: Boolean
		abstract val isExternal: Boolean
		abstract val isInline: Boolean

		/** An accessor is enclosed by its property */
		override val enclosingElement: KotlinPropertyElement = this@KotlinPropertyElement

		override val simpleName: Name get() = javaElement.simpleName

		override fun asType(): TypeMirror = javaElement.asType()

		override fun equals(other: Any?): Boolean = (other as? Accessor)?.javaElement == javaElement
		override fun hashCode(): Int = Objects.hashCode(javaElement)
		override fun toString() = javaElement.toString()
	}

	/** Getter element of a Kotlin property */
	inner class Getter(javaElement: ExecutableElement)
		: Accessor(javaElement) {

		override val isDefaultImplementation: Boolean = protoProperty.isGetterDefault
		override val isExternal: Boolean = protoProperty.isGetterExternal
		override val isInline: Boolean = protoProperty.isGetterInline
		override val modality: KotlinModality = KotlinModality.fromProtoBuf(protoProperty.getterModality!!)
		override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoProperty.getterVisibility!!)
	}

	/* Setter element of a Kotlin property */
	inner class Setter(javaElement: ExecutableElement)
		: Accessor(javaElement) {

		override val isDefaultImplementation: Boolean = protoProperty.isSetterDefault
		override val isExternal: Boolean = protoProperty.isSetterExternal
		override val isInline: Boolean = protoProperty.isSetterInline
		override val modality: KotlinModality = KotlinModality.fromProtoBuf(protoProperty.setterModality!!)
		override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoProperty.setterVisibility!!)

		/** The parameter of the setter element */
		val parameter: Parameter = Parameter(javaElement.parameters.first())

		/** The parameter element of a Kotlin setter */
		inner class Parameter(javaElement: VariableElement) : KotlinCompatElement(javaElement, processingEnv) {
			/** A setter parameter element is enclosed by the setter element */
			override val enclosingElement = this@Setter
		}
	}
}

/**
 * true if this java element may be a synthetic annotation holder for
 * property annotations with [AnnotationTarget.PROPERTY]
 */
internal fun ExecutableElement.maybeSyntheticPropertyAnnotHolder()
		//TODO("replace string with constant from JvmAbi but it is private")
		= simpleName.toString().endsWith(ANNOTATIONS_SUFFIX)
		  && returnType.kind == TypeKind.VOID
		  && parameters.isEmpty()

/** true if this javaElement may be a kotlin generated getter of a property */
internal fun ExecutableElement.maybeKotlinGetter()
		= JvmAbi.isGetterName(simpleName.toString())
		  && returnType.kind != TypeKind.VOID
		  && parameters.isEmpty()

/** true if this javaElement may be a kotlin generated setter of a property */
internal fun ExecutableElement.maybeKotlinSetter()
		= JvmAbi.isSetterName(simpleName.toString())
		  && returnType.kind == TypeKind.VOID
		  && parameters.isNotEmpty()

