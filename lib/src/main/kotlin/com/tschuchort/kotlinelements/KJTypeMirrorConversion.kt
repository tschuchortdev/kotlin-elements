package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.from_javax.*
import com.tschuchort.kotlinelements.from_javax.JavaBoxedPrimitiveType
import com.tschuchort.kotlinelements.from_javax.JavaErrorType
import com.tschuchort.kotlinelements.from_javax.JavaMappedCollectionType
import com.tschuchort.kotlinelements.from_javax.JavaPrimitiveArrayType
import com.tschuchort.kotlinelements.from_javax.JavaReferenceArrayType
import com.tschuchort.kotlinelements.from_metadata.*
import com.tschuchort.kotlinelements.from_metadata.AnnotationsDelegate
import com.tschuchort.kotlinelements.from_metadata.KotlinTypeVariable
import com.tschuchort.kotlinelements.from_metadata.KotlinWildcardType
import com.tschuchort.kotlinelements.from_metadata.MetadataContext
import com.tschuchort.kotlinelements.mixins.KJVariance
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import java.lang.RuntimeException
import javax.annotation.processing.ProcessingEnvironment

import javax.lang.model.type.*

fun TypeMirror.toKJTypeMirror(processingEnv: ProcessingEnvironment, isTypeArg: Boolean = false)
		: KJTypeMirror? = when(kind!!) {
	TypeKind.BOOLEAN,
	TypeKind.BYTE,
	TypeKind.SHORT,
	TypeKind.INT,
	TypeKind.LONG,
	TypeKind.CHAR,
	TypeKind.FLOAT,
	TypeKind.DOUBLE,
	TypeKind.VOID         -> JavaPrimitiveType(this as PrimitiveType)
	TypeKind.NONE         -> null
	TypeKind.NULL         -> KJNullType
	TypeKind.ARRAY        -> with(this as ArrayType) {
		if(componentType.isPrimitive())
			JavaPrimitiveArrayType(this, processingEnv)
		else
			JavaReferenceArrayType(this, processingEnv)
	}
	TypeKind.DECLARED     -> with(this as DeclaredType) {
			JavaMappedCollectionType.createOrNull(this, processingEnv)
				?: JavaBoxedPrimitiveType.createOrNull(this, isTypeArg)
				?:
				?: JavaRegularDeclaredType(this, processingEnv)
	}
	TypeKind.ERROR        -> JavaErrorType(this as ErrorType, processingEnv)
	TypeKind.TYPEVAR      -> JavaTypeVariable(this as TypeVariable, processingEnv)
	TypeKind.WILDCARD     -> JavaWildcardType(this as WildcardType, processingEnv)
	TypeKind.PACKAGE      -> JavaPackageType(this as NoType)
	TypeKind.EXECUTABLE   -> JavaExecutableType(this as ExecutableType, processingEnv)
	TypeKind.OTHER        -> JavaOtherType(this)
	TypeKind.UNION        -> JavaUnionType(this as UnionType, processingEnv)
	TypeKind.INTERSECTION -> JavaIntersectionType(this as IntersectionType, processingEnv)
	TypeKind.MODULE       -> JavaModuleType(this as NoType)
}

internal fun ProtoBuf.Type.toKJTypeMirror(
		metadataContext: MetadataContext, processingEnv: ProcessingEnvironment,
		javaxType: TypeMirror? = null): KJTypeMirror {


	fun convertTypeArg(protoTypeArg: ProtoBuf.Type.Argument, javaxType: TypeMirror?): KJTypeMirror {
		return if (protoTypeArg.hasType()) {
			if(protoTypeArg.hasProjection())
				protoTypeArg.projection.name //TODO("check this")

			protoTypeArg.type.toKJTypeMirror(metadataContext, processingEnv)
		}
		else {
			// Star projection can not be annotated
			KotlinWildcardType(javaxType?.let { it as WildcardType }, KJWildcardType.Kind.Star,
					AnnotationsDelegate.None, processingEnv)
		}
	}

	fun convertTypeVariable(protoTypeParam: ProtoBuf.TypeParameter, javaxType: TypeVariable?): KJTypeVariable {
		val name = metadataContext.nameResolver.getString(protoTypeParam.name)
		val variance = KJVariance.fromProtoBuf(protoTypeParam.variance)
		val upperBounds = when(javaxType?.upperBound) {
			is IntersectionType -> {
				protoTypeParam.upperBoundList.map { protoBound ->
					(javaxType.upperBound as IntersectionType).bounds.singleOrNull { javaxBound ->
						protoBound.
					}
				}

			}
			null -> protoTypeParam.upperBoundList.map { Pair(it, null) }
			else -> {

			}
		}

		// TODO("handle annotations")
		return KotlinTypeVariable(name, variance, upperBound, null, processingEnv)
	}

	try {
		return when {
			// type is a type variable
			hasTypeParameter()     -> {
				val protoTypeParam = metadataContext.getTypeParameter(typeParameter)
					?: throw IllegalStateException("Could not get type parameter")

				if(javaxType != null)
					check(javaxType.kind == TypeKind.TYPEVAR)

				convertTypeVariable(protoTypeParam, javaxType?.let { it as TypeVariable })
			}

			// type is a type variable
			hasTypeParameterName() -> {
				val name = metadataContext.nameResolver.getString(typeParameterName)
				check(name.isNotBlank())

				val protoTypeParam = try {
					metadataContext.typeParameterList.single {
						check(it.hasName())
						metadataContext.nameResolver.getString(it.name) == name
					}
				}
				catch(e: Exception) {
					throw IllegalStateException("Could not get type parameter with name $name", e)
				}

				if(javaxType != null)
					check(javaxType.kind == TypeKind.TYPEVAR)

				convertTypeVariable(protoTypeParam, javaxType?.let { it as TypeVariable })
			}

			// A type is abbreviated if it contains type aliases
			hasAbbreviatedType()   -> {
				check(hasClassName())
				val aliasedTypeName = metadataContext.nameResolver.getString(className).replace("/", ".")
				check(abbreviatedType.hasTypeAliasName())
				val aliasName = metadataContext.nameResolver.getString(abbreviatedType.typeAliasName).replace("/", ".")

				val typeArguments = abbreviatedType.argumentList.map { convertTypeArg(it, null) }



				KotlinTypeAlias(nullable, typeArguments,) //TODO
			}

			// type is a regular type that may have arguments
			else                   -> {
				check(hasClassName())
				val name = metadataContext.nameResolver.getString(className).replace("/", ".")
				val typeArguments = argumentList.map { convertTypeArg(it, ) }

				val matchingTypeElems = processingEnv.elementUtils.getAllTypeElements(name)

				val rawTypeMirror = when {
					matchingTypeElems.isEmpty() -> throw IllegalStateException(
							"Could not find Javax TypeElement by name for '$name'")
					matchingTypeElems.size > 1 -> throw IllegalStateException(
							"Found matching Javax TypeElements for name '$name' in multiple modules. Can not decide which one to use.")
					else -> matchingTypeElems.first()!!
				}

				KotlinBasicType.createOrNull(name, nullable, AnnotationsDelegate.None, processingEnv)
						?.also { check(typeArguments.isEmpty()) }
					?:
					?: KotlinDeclaredType(null, typeArguments, name, "") //TODO
			}
		}
	}
	catch(t: Throwable) {
		throw KotlinTypeMirrorConversionException(this, metadataContext, t)
	}
}

class KJTypeMirrorConversionException(
	cause: Throwable
) : KJConversionException("", cause)