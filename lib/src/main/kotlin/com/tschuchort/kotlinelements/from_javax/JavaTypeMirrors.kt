package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.KJVariance
import com.tschuchort.kotlinelements.removeTypeArgs
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.type.*

internal class JavaPrimitiveType(private val javaxType: PrimitiveType)
	: KJPrimitiveType(), AnnotatedConstruct by javaxType {

	override val nullable: Boolean? = false
	override fun toJavaxTypeMirror(): PrimitiveType = javaxType

	override val kind: Kind = when(javaxType.kind!!) {
		TypeKind.BOOLEAN -> Kind.BOOLEAN
		TypeKind.BYTE    -> Kind.BYTE
		TypeKind.SHORT   -> Kind.SHORT
		TypeKind.INT     -> Kind.INT
		TypeKind.LONG    -> Kind.LONG
		TypeKind.CHAR    -> Kind.CHAR
		TypeKind.FLOAT   -> Kind.FLOAT
		TypeKind.DOUBLE  -> Kind.DOUBLE
		TypeKind.VOID    -> Kind.UNIT
		else             -> throw IllegalArgumentException(
				"Constructor argument javaxType has illegal TypeKind ${javaxType.kind}"
		)
	}
}

internal class JavaBoxedPrimitiveType private constructor(
		private val javaxType: DeclaredType,
		override val nullable: Boolean?,
		override val kind: Kind
) : KJPrimitiveType(), AnnotatedConstruct by javaxType {

	override fun toJavaxTypeMirror(): DeclaredType = javaxType

	internal companion object {
		fun createOrNull(javaxType: DeclaredType, isTypeArg: Boolean): JavaBoxedPrimitiveType? {
			val kind = Kind.values().singleOrNull {
				javaxType.toString() == it.javaBoxedQualifiedName
			}

			val nullable: Boolean? = if(isTypeArg) null else true

			return if(kind != null)
				JavaBoxedPrimitiveType(javaxType, nullable, kind)
			else
				null
		}
	}

}

internal class JavaReferenceArrayType(
		private val javaxType: ArrayType,
		private val processingEnv: ProcessingEnvironment
) : KJReferenceArrayType(), AnnotatedConstruct by javaxType {

	override val nullable: Boolean? = null

	override val componentType: KJTypeMirror = ComponentKJWildCardType()

	override fun toJavaxTypeMirror(): ArrayType = javaxType

	inner class ComponentKJWildCardType
		: KJWildcardType(), AnnotatedConstruct by javaxType.componentType {

		override val kind: Kind = Kind.Bounded.Out(
				bound = javaxType.componentType.toKJTypeMirror(processingEnv)!!,
				varianceInferredFromPlatformType = true
		)

		override fun toJavaxTypeMirror(): TypeMirror = javaxType.componentType
	}
}

internal class JavaPrimitiveArrayType(
		private val javaxType: ArrayType,
		private val processingEnv: ProcessingEnvironment
) : KJPrimitiveArrayType(), AnnotatedConstruct by javaxType {

	override val nullable: Boolean? = null

	override val componentType: KJPrimitiveType
			= javaxType.componentType.toKJTypeMirror(processingEnv) as KJPrimitiveType

	override fun toJavaxTypeMirror(): ArrayType = javaxType
}

internal class JavaErrorType(
		private val javaxType: ErrorType,
		private val processingEnv: ProcessingEnvironment
) : KJErrorType(), AnnotatedConstruct by javaxType {

	override val nullable: Boolean? = null

	override fun toString(): String = javaxType.toString()

	override fun equals(other: Any?): Boolean = (other as? JavaErrorType)?.let {
		it.javaxType == javaxType && it.nullable == nullable
				&& it.annotationMirrors == annotationMirrors
	} ?: false

	override fun hashCode(): Int
			= Objects.hash(javaxType, nullable, annotationMirrors)

	override val enclosingType: KJTypeMirror?
		get() = javaxType.enclosingType?.toKJTypeMirror(processingEnv)

	override fun asElement(): KJTypeElement
			= javaxType.asElement().toKJElement(processingEnv) as KJTypeElement

	override fun toJavaxTypeMirror(): ErrorType = javaxType

	override val typeArguments: List<KJTypeMirror>
		get() = javaxType.typeArguments.map { it.toKJTypeMirror(processingEnv, isTypeArg = true)!! }

	override val simpleName: String
		get() = javaxType.asElement().simpleName.toString()

	override val qualifiedName: String
		get() = removeTypeArgs(javaxType.toString())
}

internal class JavaMappedCollectionType private constructor(
		private val javaxType: DeclaredType,
		private val processingEnv: ProcessingEnvironment,
		override val kind: Kind
) : KJMappedCollectionType(), AnnotatedConstruct by javaxType {

	override val nullable: Boolean? = null

	override fun toJavaxTypeMirror(): TypeMirror = javaxType

	override val enclosingType: KJTypeMirror?
		get() = javaxType.enclosingType.toKJTypeMirror(processingEnv)

	override val mutable: Boolean? = null

	override val typeArguments: List<KJTypeMirror>
		get() = javaxType.typeArguments.map { it.toKJTypeMirror(processingEnv, isTypeArg = true)!! }

	internal companion object {
		private fun matchingKindForName(qualifiedName: String): Kind? {
			val rawName = removeTypeArgs(qualifiedName)
			return Kind.values().singleOrNull { it.javaQualifiedName == rawName }
		}

		fun createOrNull(javaxType: DeclaredType, processingEnv: ProcessingEnvironment)
				: JavaMappedCollectionType? {
			return matchingKindForName(javaxType.toString())?.let { kind ->
				JavaMappedCollectionType(javaxType, processingEnv, kind)
			}
		}
	}
}

internal class JavaMappedNonPrimitiveBuiltInType private constructor(
		private val javaxType: DeclaredType,
		private val processingEnv: ProcessingEnvironment
) : KJMappedBuiltInType(), AnnotatedConstruct by javaxType {
	override fun asElement(): KJElement {
	}

	override val nullable: Boolean?
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val simpleName: String
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val qualifiedName: String
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val typeArguments: List<KJTypeMirror>
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

	override fun toJavaxTypeMirror(): TypeMirror = javaxType

	internal companion object {
		fun createOrNull(javaxType: DeclaredType, processingEnv: ProcessingEnvironment)
				: JavaMappedNonPrimitiveBuiltInType? {
		}
	}
}

internal class JavaRegularDeclaredType(
		private val javaxType: DeclaredType,
		private val processingEnv: ProcessingEnvironment
) : KJDeclaredType(), AnnotatedConstruct by javaxType {

	override val nullable: Boolean? = null

	override val typeArguments: List<KJTypeMirror>
		get() = javaxType.typeArguments.map { it.toKJTypeMirror(processingEnv, isTypeArg = true)!! }

	override val enclosingType: KJTypeMirror?
		get() = javaxType.enclosingType.toKJTypeMirror(processingEnv)

	override fun asElement(): KJTypeElement
			= javaxType.asElement().toKJElement(processingEnv) as KJTypeElement

	override fun toJavaxTypeMirror(): DeclaredType = javaxType

	override val simpleName: String
		get() = javaxType.asElement().simpleName.toString()

	override val qualifiedName: String
		get() = removeTypeArgs(javaxType.toString())
}

internal class JavaWildcardType(
		private val javaxType: WildcardType,
		private val processingEnv: ProcessingEnvironment
) : KJWildcardType(), AnnotatedConstruct by javaxType {

	override val kind: Kind = when {
		javaxType.superBound != null -> Kind.Bounded.In(
				javaxType.superBound.toKJTypeMirror(processingEnv)!!
		)
		javaxType.extendsBound != null -> Kind.Bounded.Out(
				javaxType.extendsBound.toKJTypeMirror(processingEnv)!!
		)
		else -> Kind.Star
	}

	override fun toJavaxTypeMirror(): WildcardType = javaxType
}

internal class JavaTypeVariable(
		private val javaxType: TypeVariable,
		private val processingEnv: ProcessingEnvironment
) : KJTypeVariable(), AnnotatedConstruct by javaxType {

	override val upperBound: KJTypeMirror
		get() = javaxType.upperBound.toKJTypeMirror(processingEnv)!!

	override val lowerBound: KJTypeMirror
		get() = javaxType.lowerBound.toKJTypeMirror(processingEnv)!!

	override val variance: KJVariance
		get() = (asElement() as? KJTypeParameterElement)?.variance ?: KJVariance.INVARIANT

	override fun asElement(): KJElement = javaxType.asElement().toKJElement(processingEnv)

	override fun toJavaxTypeMirror(): TypeVariable = javaxType

	override val simpleName: String = javaxType.asElement().simpleName.toString()
}

internal class JavaExecutableType(
		private val javaxType: ExecutableType,
		private val processingEnv: ProcessingEnvironment
) : KJExecutableType(), AnnotatedConstruct by javaxType {

	override val typeVariables: List<KJTypeVariable>
		get() = javaxType.typeVariables.map { it.toKJTypeMirror(processingEnv) as KJTypeVariable }

	override val parameterTypes: List<KJTypeMirror>
		get() = javaxType.parameterTypes.map { it.toKJTypeMirror(processingEnv)!! }

	override val receiverType: KJTypeMirror?
		get() = javaxType.receiverType.toKJTypeMirror(processingEnv)

	override val returnType: KJTypeMirror?
		get() = javaxType.returnType.toKJTypeMirror(processingEnv)

	override val thrownTypes: List<KJTypeMirror>
		get() = javaxType.thrownTypes.map { it.toKJTypeMirror(processingEnv)!! }

	override fun toJavaxTypeMirror(): ExecutableType = javaxType
}

internal class JavaPackageType(private val javaxType: NoType)
	: KJPackageType(), AnnotatedConstruct by javaxType {

	init {
		require(javaxType.kind == TypeKind.PACKAGE)
	}

	// TODO("Check if PackageType has qualified name")

	override fun toJavaxTypeMirror(): NoType = javaxType

	override fun toString(): String = javaxType.toString()
}

internal class JavaModuleType(private val javaxType: NoType)
	: KJModuleType(), AnnotatedConstruct by javaxType {

	init {
		require(javaxType.kind == TypeKind.MODULE)
	}

	override fun toJavaxTypeMirror(): NoType = javaxType

	override fun toString(): String = javaxType.toString()
}

internal class JavaOtherType(private val javaxType: TypeMirror)
	: KJUnkownType(), AnnotatedConstruct by javaxType {

	override fun toJavaxTypeMirror(): TypeMirror = javaxType

	override fun toString(): String = javaxType.toString()

	override fun equals(other: Any?): Boolean
			= (other as? JavaOtherType)?.toJavaxTypeMirror() == toJavaxTypeMirror()

	override fun hashCode(): Int = Objects.hash(toJavaxTypeMirror())
}

internal class JavaIntersectionType(
		private val javaxType: IntersectionType,
		private val processingEnv: ProcessingEnvironment
) : KJIntersectionType(), AnnotatedConstruct by javaxType {
	override val bounds: List<KJTypeMirror>
		get() = javaxType.bounds.map { it.toKJTypeMirror(processingEnv)!! }

	override fun toJavaxTypeMirror(): IntersectionType = javaxType
}

internal class JavaUnionType(
		private val javaxType: UnionType,
		private val processingEnv: ProcessingEnvironment
) : KJUnionType(), AnnotatedConstruct by javaxType {
	override val alternatives: List<KJTypeMirror>
		get() = javaxType.alternatives.map { it.toKJTypeMirror(processingEnv)!! }

	override fun toJavaxTypeMirror(): UnionType = javaxType
}