@file:Suppress("unused")

package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.KJPrimitiveType.Companion.packageName
import com.tschuchort.kotlinelements.mixins.*
import java.util.*
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.type.*

/** Mixin interface for a [KJTypeMirror] that may have a nullability modifier */
interface HasNullability {
	val nullable: Boolean?
	companion object
}

internal fun nullabilitySuffix(it: HasNullability) = when(it.nullable) {
	true  -> "?"
	false -> ""
	null  -> "!"
}

/** Mixin interface for [KJTypeMirror]s that may have type arguments */
interface HasTypeArguments {
	val typeArguments: List<KJTypeMirror>
	companion object
}

/** Represents the use of a type, similar to [TypeMirror] */
sealed class KJTypeMirror: AnnotatedConstruct {
	abstract fun toJavaxTypeMirror(): TypeMirror

	/**
	 * Returns an informative string representation of this type.
	 * If possible, the string should be of a form suitable for representing this type in source code, but
	 * it doesn't have to be (this is particularly true of [KJUnionType], [KJIntersectionType], [KJNullType],
	 * [KJErrorType] and so called platform types where the nullability is unknown (suffixed with a !)).
	 * Any names embedded in the result are qualified if possible.
	 */
	abstract override fun toString(): String
	abstract override fun equals(other: Any?): Boolean
	abstract override fun hashCode(): Int

	companion object
}

/** Represents the type of an executable element such as a method or constructor */
abstract class KJExecutableType : KJTypeMirror() {
	/** The parameter types in declaration order */
	abstract val parameterTypes: List<KJTypeMirror>
	abstract val typeVariables: List<KJTypeVariable>
	abstract val receiverType: KJTypeMirror?
	abstract val returnType: KJTypeMirror?
	abstract val thrownTypes: List<KJTypeMirror>

	abstract override fun toJavaxTypeMirror(): ExecutableType

	final override fun toString(): String
			= "${receiverType.let { "$it." }}($parameterTypes) ${returnType.let { "-> $it" }}"

	final override fun equals(other: Any?): Boolean = (other as? KJExecutableType)?.let {
		it.parameterTypes == parameterTypes
				&& it.receiverType == receiverType
				&& it.returnType == returnType
				&& it.thrownTypes == thrownTypes
				&& it.typeVariables == typeVariables
				&& it.annotationMirrors == annotationMirrors
	} ?: false

	final override fun hashCode(): Int = Objects.hash(
			parameterTypes, receiverType, returnType,
			thrownTypes, typeVariables, annotationMirrors
	)

	companion object
}

/** Represents the intersection of multiple types. This type may appear
 * as the bound of a [KJWildcardType] */
abstract class KJIntersectionType : KJTypeMirror() {
	abstract val bounds: List<KJTypeMirror>

	abstract override fun toJavaxTypeMirror(): IntersectionType

	final override fun toString(): String = bounds.joinToString("&")

	final override fun equals(other: Any?): Boolean = (other as? KJIntersectionType)?.let {
		it.bounds == bounds && it.annotationMirrors == annotationMirrors
	} ?: false

	final override fun hashCode(): Int = Objects.hash(bounds, annotationMirrors)

	companion object
}

/** Represents the null type. The type of the expression null */
object KJNullType : KJTypeMirror() {
	@Suppress("UNCHECKED_CAST")
	override fun <A : Annotation> getAnnotationsByType(annotationType: Class<A>): Array<A>
			= java.lang.reflect.Array.newInstance(annotationType, 0) as Array<A>

	override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A? = null

	override fun getAnnotationMirrors(): List<AnnotationMirror> = emptyList()

	override fun toJavaxTypeMirror(): NullType = JavaxNullType

	override fun toString(): String = "Null"

	override fun equals(other: Any?): Boolean = other is KJNullType

	override fun hashCode(): Int = System.identityHashCode(this)

	private object JavaxNullType : NullType {
		override fun getKind(): TypeKind = TypeKind.NULL

		override fun <R : Any?, P : Any?> accept(v: TypeVisitor<R, P>, p: P): R
				= v.visitNull(this, p)

		@Suppress("UNCHECKED_CAST")
		override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>?): Array<A>
				=  java.lang.reflect.Array.newInstance(annotationType, 0) as Array<A>

		override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A? = null

		override fun getAnnotationMirrors(): List<AnnotationMirror> = emptyList()
	}
}

/** Represents a type that was declared in source code and is not built-in */
abstract class KJDeclaredType : KJTypeMirror(), HasTypeArguments, HasQualifiedName, HasNullability, ConvertibleToElement {
	/** Returns the type of the innermost enclosing instance or null */
	abstract val enclosingType: KJTypeMirror?

	abstract override fun asElement(): KJTypeElement
	abstract override fun toJavaxTypeMirror(): DeclaredType

	override fun toString(): String = if(enclosingType == null)
		"$packageName.$simpleName<$typeArguments>"
	else
		"$enclosingType.$simpleName"

	override fun equals(other: Any?): Boolean = (other as? KJDeclaredType)?.let { other ->
		other.qualifiedName == qualifiedName && other.nullable == nullable
				&& other.typeArguments == typeArguments
				&& other.annotationMirrors == annotationMirrors
	} ?: false

	override fun hashCode(): Int
			= Objects.hash(qualifiedName, nullable, typeArguments, annotationMirrors)

	companion object
}

/**
 * Represents types that do not necessarily have the same name in Java code as
 * in Kotlin, including primitives, collections and arrays.
 */
sealed class KJMappedType : KJTypeMirror(), HasNullability, HasQualifiedName {
	companion object
}

abstract class KJPrimitiveType : KJMappedType() {
	abstract val kind: Kind

	abstract val boxed: Boolean

	enum class Kind {
		BOOLEAN {
			override val simpleName = "Boolean"
			override val javaxPrimitiveTypeKind = TypeKind.BOOLEAN
			override val javaBoxedQualifiedName = "java.lang.Boolean"
		},
		BYTE {
			override val simpleName = "Byte"
			override val javaxPrimitiveTypeKind = TypeKind.BYTE
			override val javaBoxedQualifiedName = "java.lang.Byte"
		},
		SHORT {
			override val simpleName = "Short"
			override val javaxPrimitiveTypeKind = TypeKind.SHORT
			override val javaBoxedQualifiedName = "java.lang.Short"
		},
		INT {
			override val simpleName = "Int"
			override val javaxPrimitiveTypeKind = TypeKind.INT
			override val javaBoxedQualifiedName = "java.lang.Integer"
		},
		LONG {
			override val simpleName = "Long"
			override val javaxPrimitiveTypeKind = TypeKind.LONG
			override val javaBoxedQualifiedName = "java.lang.Long"
		},
		CHAR {
			override val simpleName = "Char"
			override val javaxPrimitiveTypeKind = TypeKind.CHAR
			override val javaBoxedQualifiedName = "java.lang.Character"
		},
		FLOAT {
			override val simpleName = "Float"
			override val javaxPrimitiveTypeKind = TypeKind.FLOAT
			override val javaBoxedQualifiedName = "java.lang.Float"
		},
		DOUBLE {
			override val simpleName = "Double"
			override val javaxPrimitiveTypeKind = TypeKind.DOUBLE
			override val javaBoxedQualifiedName = "java.lang.Double"
		},
		UNIT {
			override val simpleName = "Unit"
			override val javaxPrimitiveTypeKind = TypeKind.VOID
			override val javaBoxedQualifiedName: Nothing? = null
		};

		abstract val simpleName: String
		abstract val javaxPrimitiveTypeKind: TypeKind
		abstract val javaBoxedQualifiedName: String?

		val kotlinQualifiedName get() = "$packageName.$simpleName"
	}

	companion object {
		internal const val packageName = "kotlin"
	}

	final override val simpleName: String get() = kind.simpleName

	final override val qualifiedName: String get() = kind.kotlinQualifiedName

	final override fun toString(): String = qualifiedName + nullabilitySuffix(this)

	final override fun equals(other: Any?): Boolean = (other as? KJPrimitiveType)?.let {
		it.kind == kind && it.nullable == nullable && it.annotationMirrors == annotationMirrors
	} ?: false

	final override fun hashCode(): Int = Objects.hash(kind, annotationMirrors)
}

abstract class KJMappedBuiltInType : KJMappedType(), HasTypeArguments,
	ConvertibleToElement, HasSimpleName, HasQualifiedName {

	abstract val kind: Kind

	enum class Kind {
		Object {
			override val javaSimpleName: kotlin.String = "Object"
			override val kotlinSimpleName: kotlin.String = "Any"
		},
		Cloneable {
			override val javaSimpleName: kotlin.String = "Cloneable"
		},
		Comparable {
			override val javaSimpleName: kotlin.String = "Comparable"
		},
		Enum {
			override val javaSimpleName: kotlin.String = "Enum"
		},
		Annotation {
			override val javaSimpleName: kotlin.String = "Annotation"
		},
		Deprecated {
			override val javaSimpleName: kotlin.String = "Deprecated"
		},
		CharSequence {
			override val javaSimpleName: kotlin.String = "CharSequence"
		},
		String {
			override val javaSimpleName: kotlin.String = "String"
		},
		Number {
			override val javaSimpleName: kotlin.String = "Number"
		},
		Throwable {
			override val javaSimpleName: kotlin.String = "Throwable"
		};

		internal abstract val javaSimpleName: kotlin.String
		internal open val kotlinSimpleName: kotlin.String get() = javaSimpleName

		internal val javaQualifiedName get() = "$javaPackageName.$javaSimpleName"
		internal val kotlinQualifiedName get() = "$kotlinPackageName.$kotlinSimpleName"
	}

	override val qualifiedName: String
		get() = kind.kotlinQualifiedName

	override val simpleName: String
		get() = kind.kotlinSimpleName

	final override fun toString(): String = qualifiedName + nullabilitySuffix(this)

	final override fun equals(other: Any?): Boolean = (other as? KJMappedBuiltInType)?.let {
		it.qualifiedName == qualifiedName && it.nullable == nullable
				&& it.typeArguments == typeArguments
				&& it.annotationMirrors == annotationMirrors
	} ?: false

	final override fun hashCode(): Int
			= Objects.hash(qualifiedName, nullable, typeArguments, annotationMirrors)

	companion object {
		protected const val javaPackageName: kotlin.String = "java.lang"
		protected const val kotlinPackageName: kotlin.String = "kotlin"
	}
}

abstract class KJMappedCollectionType : KJMappedType(), HasTypeArguments, HasSimpleName, HasQualifiedName {
	abstract val enclosingType: KJTypeMirror?
	abstract val mutable: Boolean?
	abstract val kind: Kind

	enum class Kind {
		Iterator {
			override val kotlinNamePattern = "$1Iterator$2"
			override val javaQualifiedName = "java.util.Iterator"
		},
		Iterable {
			override val kotlinNamePattern = "$1Iterable$2"
			override val javaQualifiedName = "java.lang.Iterable"
		},
		Collection {
			override val kotlinNamePattern = "$1Collection$2"
			override val javaQualifiedName = "java.util.Collection"
		},
		Set {
			override val kotlinNamePattern = "$1Set$2"
			override val javaQualifiedName = "java.util.Set"
		},
		List {
			override val kotlinNamePattern = "$1List$2"
			override val javaQualifiedName = "java.util.List"
		},
		ListIterator {
			override val kotlinNamePattern = "$1ListIterator$2"
			override val javaQualifiedName = "java.util.ListIterator"
		},
		Map {
			override val kotlinNamePattern = "$1Map$2"
			override val javaQualifiedName = "java.util.Map"
		},
		MapEntry {
			override val kotlinNamePattern = "$1Map$2.$1Entry$2"
			override val javaQualifiedName = "java.util.Map.Entry"
		};

		/** Template to create the name based on mutability and type args */
		internal abstract val kotlinNamePattern: String

		/** The fully qualified name of this type in Java */
		internal abstract val javaQualifiedName: String

		/** The fully qualified name of this type in Kotlin, based on mutability and type args */
		internal fun kotlinQualifiedNameWithArgs(
				mutable: Boolean?,
				typeArgNames: kotlin.collections.List<String>
		): String {
			val mutabilityPrefix = when(mutable) {
				true  -> "Mutable"
				false -> ""
				null  -> "(Mutable)"
			}

			val typeArgsSuffix = if(typeArgNames.isNotEmpty())
				"<$typeArgNames>"
			else
				""

			val name = kotlinNamePattern
					.replace("$1", mutabilityPrefix)
					.replace("$2", typeArgsSuffix)

			return "$kotlinPackageName.$name"
		}
	}

	final override val simpleName: String
		get() = kind.kotlinQualifiedNameWithArgs(mutable, emptyList()).substringAfterLast(".")

	final override val qualifiedName: String
		get() = kind.kotlinQualifiedNameWithArgs(mutable, emptyList())

	final override fun toString(): String
			= kind.kotlinQualifiedNameWithArgs(mutable, typeArguments.map(KJTypeMirror::toString)) +
			nullabilitySuffix(this)

	final override fun equals(other: Any?): Boolean = (other as? KJMappedCollectionType)?.let {
		it.mutable == mutable && it.nullable == nullable && it.kind == kind
				&& it.typeArguments == it.typeArguments
				&& it.annotationMirrors == annotationMirrors
	} ?: false

	final override fun hashCode(): Int
			= Objects.hash(mutable, nullable, kind, typeArguments, annotationMirrors)

	companion object {
		private const val kotlinPackageName = "kotlin.collections"
	}
}

/** Represents an array type */
sealed class KJArrayType : KJMappedType(), HasQualifiedName, HasSimpleName {
	abstract val componentType: KJTypeMirror
	abstract override fun toJavaxTypeMirror(): ArrayType
	companion object
}

/**
 * Represents an array of reference types, this includes
 * [java.lang.Integer], [java.lang.Double] and so on.
 */
abstract class KJReferenceArrayType : KJArrayType(), HasTypeArguments {
	final override val typeArguments: List<KJTypeMirror> get() = listOf(componentType)

	final override val simpleName: String = "Array"

	final override val qualifiedName: String = "kotlin.$simpleName"

	final override fun toString(): String = "$qualifiedName<$componentType>" + nullabilitySuffix(this)

	final override fun hashCode(): Int = Objects.hash(nullable, componentType, annotationMirrors)

	final override fun equals(other: Any?): Boolean = (other as? KJArrayType)?.let {
		it.nullable == nullable && it.componentType == componentType
				&& it.annotationMirrors == annotationMirrors
	} ?: false

	companion object
}

/**
 * Represents a special array of primitive types like [IntArray], [DoubleArray] and so on.
 * They get translated to an array of primitives in Java (int[], double[] etc).
 */
abstract class KJPrimitiveArrayType : KJArrayType() {
	abstract override val componentType: KJPrimitiveType

	final override val simpleName: String
		get() = componentType.simpleName + "Array"

	final override val qualifiedName: String
		get() = componentType.qualifiedName + "Array"

	final override fun toString(): String = qualifiedName + nullabilitySuffix(this)

	final override fun equals(other: Any?): Boolean = (other as? KJPrimitiveArrayType)?.let {
		it.componentType == componentType && it.nullable == nullable
				&& it.annotationMirrors == annotationMirrors
	} ?: false

	final override fun hashCode(): Int = Objects.hash(componentType, nullable, annotationMirrors)

	companion object
}

/** Represents the use of a type alias */
abstract class KJTypeAlias : KJDeclaredType() {
	/**
	 * Right-hand side of the type alias definition.
	 * May contain type aliases and type parameters.
	 */
	abstract val underlyingType: KJTypeMirror

	/**
	 * Fully expanded type corresponding to this type alias.
	 * May contain type parameters of the corresponding type alias.
	 * May not contain type aliases.
	 */
	abstract val expandedType: KJTypeMirror

	final override val enclosingType: Nothing? = null

	final override fun toString(): String = expandedType.toString()

	final override fun equals(other: Any?): Boolean = (other as? KJTypeAlias)?.let {
		it.expandedType == expandedType
				&& it.underlyingType == underlyingType
				&& it.nullable == nullable
				&& it.annotationMirrors == annotationMirrors
	} ?: false

	final override fun hashCode(): Int
			= Objects.hash(expandedType, underlyingType, nullable, annotationMirrors)

	companion object
}

abstract class KJInlineClassType : KJDeclaredType() {
	//TODO("inline class type")

	abstract val underlyingType: KJTypeMirror
	companion object
}

/** Represents a type that could not be resolved */
abstract class KJErrorType : KJDeclaredType() {
	abstract override fun toJavaxTypeMirror(): ErrorType
}

/** Represents a type variable */
abstract class KJTypeVariable : KJTypeMirror(), HasSimpleName, ConvertibleToElement {
	/** Returns the upper bound of this type variable.
	 *  If this type variable was declared with no explicit upper bounds,
	 *  the result is kotlin.Any. If it was declared with multiple upper bounds,
	 *  the result is a [KJIntersectionType]; individual bounds can be found by
	 *  examining the result's bounds. */
	abstract val upperBound: KJTypeMirror

	/** Returns the lower bound of this type variable.
	 * While a type parameter cannot include an explicit lower bound declaration,
	 * capture conversion can produce a type variable with a non-trivial lower bound.
	 * Type variables otherwise have a lower bound of [KJNullType] */
	abstract val lowerBound: KJTypeMirror

	abstract val variance: KJVariance

	abstract override fun asElement(): KJElement
	abstract override fun toJavaxTypeMirror(): TypeVariable

	final override fun toString(): String = simpleName

	final override fun equals(other: Any?): Boolean = (other as? KJTypeVariable)?.let {
		it.simpleName == simpleName && it.upperBound == upperBound && it.lowerBound == lowerBound
				&& it.annotationMirrors == annotationMirrors
	} ?: false

	final override fun hashCode(): Int
			= Objects.hash(upperBound, lowerBound, simpleName, annotationMirrors)

	companion object
}

/** Represents a union type. As of the RELEASE_7 source version,
 * union types can appear as the type of a multi-catch exception parameter **/
abstract class KJUnionType : KJTypeMirror() {
	abstract val alternatives: List<KJTypeMirror>

	abstract override fun toJavaxTypeMirror(): UnionType

	final override fun toString(): String = alternatives.joinToString("|")

	final override fun equals(other: Any?): Boolean = (other as? KJUnionType)?.let {
		it.alternatives == alternatives && it.annotationMirrors == annotationMirrors
	} ?: false

	final override fun hashCode(): Int = Objects.hash(alternatives, annotationMirrors)

	companion object
}

/**
 * Represents a wildcard type argument, also known as
 * use-site variance type projections in Kotlin.
 *
 * The wildcards are converted between Kotlin and Java
 * in the following way:
 *
 * Kotlin		  		=>				Java
 * -----------------------------------------------
 * When appearing as a parameter:
 * Foo<out T?> 							Foo<? extends T>
 * Foo<out T>          					Foo<? extends T>
 * Foo<out @JvmSuppressWildcards T>		Foo<T>
 * Foo<in T?> 							Foo<? super T>
 * Foo<in T>            				Foo<? super T>
 * Foo<in @JvmSuppressWildcards T>		Foo<T>
 * Foo<*>								Foo<?>
 *
 * When appearing as a return type:
 * Foo<out T?>							Foo<T>
 * Foo<out T>							Foo<T>
 * Foo<in T?>							Foo<T>
 * Foo<in T>							Foo<T>
 * Foo<out T?>							Foo<T>
 * Foo<out T>							Foo<T>
 *
 *
 * Java 		  		=>				Kotlin
 * -----------------------------------------------
 * Foo<? extends T>						Foo<out T!>!
 * Foo<? super T>						Foo<in T!>!
 * Foo<?>								Foo<*>!
 * T[]									Array<(out) T>!
 */
abstract class KJWildcardType : KJTypeMirror() {
	abstract val kind: Kind

	sealed class Kind {
		sealed class Bounded : Kind() {
			abstract val bound: KJTypeMirror?
			abstract val varianceInferredFromPlatformType: Boolean

			data class In(
					override val bound: KJTypeMirror,
					override val varianceInferredFromPlatformType: Boolean = false
			) : Bounded()

			data class Out(
					override val bound: KJTypeMirror,
					override val varianceInferredFromPlatformType: Boolean = false
			) : Bounded()
		}

		object Star : Kind()
	}

	/**
	 * The Javax [TypeMirror] representation of this [KJWildcardType] is not
	 * necessarily a [WildcardType], particularly in the case of Java arrays
	 * for which the wildcard is implicit and the variance inferred.
	 */
	abstract override fun toJavaxTypeMirror(): TypeMirror

	final override fun toString(): String = when(kind) {
		is Kind.Bounded -> with(kind as Kind.Bounded) {
			val varianceName = when(this) {
				is Kind.Bounded.In -> "in"
				is Kind.Bounded.Out -> "out"
			}

			val varianceModifier = if(varianceInferredFromPlatformType)
				"($varianceName)"
			else
				varianceName

			return@with "$varianceModifier $bound"
		}
		Kind.Star   -> "*"
	}

	final override fun equals(other: Any?): Boolean = (other as? KJWildcardType)?.let {
		it.kind == kind && it.annotationMirrors == annotationMirrors
	} ?: false

	final override fun hashCode(): Int = Objects.hash(kind, annotationMirrors)

	companion object
}

/** Represents a pseudo-type like that of a package or module */
sealed class KJPseudoType : KJTypeMirror() {
	companion object
}

/** Represents the pseudo-type of a package */
abstract class KJPackageType : KJPseudoType(), HasQualifiedName, CanBeUnnamed {
	abstract override fun toJavaxTypeMirror(): NoType

	override fun toString(): String = qualifiedName

	final override fun equals(other: Any?): Boolean = (other as? KJPackageType)?.let {
		it.qualifiedName == qualifiedName && it.annotationMirrors == annotationMirrors
	} ?: false

	final override fun hashCode(): Int = Objects.hash(toString(), annotationMirrors)

	companion object
}

/** Represents the pseudo-type of a module */
abstract class KJModuleType : KJPseudoType(), HasQualifiedName, CanBeUnnamed {
	abstract override fun toJavaxTypeMirror(): NoType

	override fun toString(): String = qualifiedName

	final override fun equals(other: Any?): Boolean = (other as? KJModuleType)?.let {
		it.qualifiedName == qualifiedName && it.annotationMirrors == annotationMirrors
	} ?: false

	final override fun hashCode(): Int = Objects.hash(qualifiedName, annotationMirrors)

	companion object
}

/** Represents a type of unknown or unspecified kind */
abstract class KJUnknownType : KJTypeMirror() {
	companion object
}