package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.mixins.HasTypeParameters
import com.tschuchort.kotlinelements.from_javax.*
import com.tschuchort.kotlinelements.from_metadata.*
import com.tschuchort.kotlinelements.mixins.KJOrigin
import kotlinx.metadata.Flag
import kotlinx.metadata.jvm.KotlinClassMetadata
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*

/** Converts any Javax [Element] to a corresponding [KJElement] */
fun Element.toKJElement(processingEnv: ProcessingEnvironment, inferJavaProperties: Boolean = false): KJElement = try {
	when (kind!!) {
		ElementKind.CLASS,
		ElementKind.ENUM,
		ElementKind.INTERFACE,
		ElementKind.ANNOTATION_TYPE -> KJTypeElementFactory.convertType(this as TypeElement, processingEnv)

		ElementKind.CONSTRUCTOR,
		ElementKind.METHOD,
		ElementKind.FIELD,
		ElementKind.INSTANCE_INIT,
		ElementKind.STATIC_INIT,
		ElementKind.ENUM_CONSTANT   -> convertMember(this, processingEnv)

		ElementKind.TYPE_PARAMETER  -> convertTypeParameter(this as TypeParameterElement, processingEnv)

		ElementKind.PARAMETER       -> convertParameter(this as VariableElement, processingEnv)

		ElementKind.RESOURCE_VARIABLE,
		ElementKind.EXCEPTION_PARAMETER,
		ElementKind.LOCAL_VARIABLE  -> convertLocalVar(this as VariableElement, processingEnv)

		ElementKind.MODULE          -> convertModule(this as ModuleElement, processingEnv)

		ElementKind.PACKAGE         -> convertPackage(this as PackageElement, processingEnv)

		ElementKind.OTHER           -> throw UnsupportedOperationException(
				"Can not convert element unknown kind $kind"
		)

		else                        -> throw UnsupportedOperationException(
				"Can not convert element of unsupported kind $kind\n" +
						"Element kind was probably added to the Java language at a later date " +
						"and is unknown to this library version."
		)
	}
}
catch (e: Exception) {
	throw KotlinElementConversionException(this, e)
}


/**
 * Converts any Javax [TypeElement] to a corresponding [KJTypeElement].
 * Prefer this function over [Element.toKJElement] for type safety.
 */
fun TypeElement.toKJElement(processingEnv: ProcessingEnvironment): KJTypeElement
		= KJTypeElementFactory.convertType(this, processingEnv)

/**
 * Converts any Javax [TypeParameterElement] to a corresponding [KJTypeParameterElement].
 * Prefer this function over [Element.toKJElement] for type safety.
 */
fun TypeParameterElement.toKJElement(processingEnv: ProcessingEnvironment): KJTypeParameterElement
		= convertTypeParameter(this, processingEnv)

/**
 * Converts any Javax [PackageElement] to a corresponding [KJPackageElement].
 * Prefer this function over [Element.toKJElement] for type safety.
 */
fun PackageElement.toKJElement(processingEnv: ProcessingEnvironment): KJPackageElement
		= convertPackage(this, processingEnv)

/**
 * Converts any Javax [ModuleElement] to a corresponding [KJModuleElement].
 * Prefer this function over [Element.toKJElement] for type safety.
 */
fun ModuleElement.toKJElement(processingEnv: ProcessingEnvironment): KJElement
		= convertModule(this, processingEnv)

private object KJTypeElementFactory {
	private val convertedTypeElemsCache = Collections.synchronizedMap(LruCache<TypeElement, KJTypeElement>(255))

	fun convertType(elem: TypeElement, processingEnv: ProcessingEnvironment): KJTypeElement {
		/* Because TypeElements are required to convert their members and converting those
			members is expensive, we cache the converted TypeElements so that the user can
			simply iterate over members and convert them one-by-one without having to worry
			about re-converting the enclosing TypeElement every time */
		return convertedTypeElemsCache[elem]
			?: elem.getKotlinMetadata()?.let { convertKotlinType(elem, it, processingEnv) }
			?: convertJavaType(elem, processingEnv)
	}

	private fun convertJavaType(elem: TypeElement, processingEnv: ProcessingEnvironment): KJTypeElement
			= when (elem.kind!!) {
		ElementKind.CLASS -> JxClassElement(elem, processingEnv)
		ElementKind.INTERFACE -> JxInterfacElement(elem, processingEnv);
		ElementKind.ENUM -> JxEnumElement(elem, processingEnv)
		ElementKind.ANNOTATION_TYPE -> JxAnnotationElement(elem, processingEnv);
		else -> throw AssertionError("ElementKind is not a type: ${elem.kind}")
	}

	private fun convertKotlinType(
			elem: TypeElement, metadata: KotlinClassMetadata,
			processingEnv: ProcessingEnvironment
	): KJTypeElement = when (metadata) {
		is KotlinClassMetadata.Class -> when (elem.kind!!) {
			ElementKind.ENUM -> KmEnumElement(elem, metadata.toKmClass(), processingEnv)
			ElementKind.CLASS -> {
				val kmClass = metadata.toKmClass()
				if (Flag.Class.IS_COMPANION_OBJECT(kmClass.flags) || Flag.Class.IS_OBJECT(kmClass.flags))
					KmObjectElement(elem, kmClass, processingEnv)
				else
					KmClassElement(elem, kmClass, processingEnv)
			}
			ElementKind.INTERFACE -> KmInterfaceElement(elem, metadata.toKmClass(), processingEnv)
			ElementKind.ANNOTATION_TYPE -> KmAnnotationElement(elem, metadata.toKmClass(), processingEnv)
			else -> throw AssertionError("ElementKind is not a type: ${elem.kind}")
		}
		is KotlinClassMetadata.FileFacade -> KmFileFacadeElement(elem, metadata.toKmPackage(), processingEnv)
		is KotlinClassMetadata.MultiFileClassFacade -> KmMultiFileClassFacadeElement(elem, metadata.partClassNames, processingEnv)
		/*  Represents metadata of a class file containing a compiled multi-file class part, i.e. an internal class with method bodies
		* and their metadata, accessed only from the corresponding facade. */
		is KotlinClassMetadata.MultiFileClassPart -> throw AssertionError(
				"Element $this is a MultiFileClassPart but this shouldn't be possible " +
						"because MultiFileClassParts are synthetic and thus are never loaded " +
						"by the annotation processor."
		)
		/* Represents metadata of a class file containing a synthetic class, e.g. a class for lambda, `$DefaultImpls` class for interface
		 * method implementations, `$WhenMappings` class for optimized `when` over enums, etc. */
		is KotlinClassMetadata.SyntheticClass -> KotlinSyntheticClassElement()
		/* Represents metadata of an unknown class file. This class is used if an old version of this library is used against a new kind
		 * of class files generated by the Kotlin compiler, unsupported by this library. */
		is KotlinClassMetadata.Unknown -> throw AssertionError("Element $elem has unknown kotlin metadata: $metadata.")
	}
}

private fun convertTypeParameter(elem: TypeParameterElement, processingEnv: ProcessingEnvironment)
		: KJTypeParameterElement {
	assert(elem.kind == ElementKind.TYPE_PARAMETER)

	return if (elem.originatesFromKotlin()) {
		val methodElem = elem.enclosingElement.toKJElement(processingEnv) as HasTypeParameters

		/* This is an O(n^2) algorithm if the user iterates over TypeParameterElements
		and converts them all to KJTypeParameterElements, but we will accept this because it
		shouldn't happen often and the number of parameters per method is usually small. */
		methodElem.typeParameters.single {
			it.simpleName == elem.simpleName.toString()
		}
	}
	else {
		JxTypeParameterElement(elem, KJOrigin.fromJavax(elem, processingEnv), processingEnv)
	}
}

private fun convertParameter(elem: VariableElement, processingEnv: ProcessingEnvironment)
		: KJParameterElement {
	assert(elem.kind == ElementKind.PARAMETER)

	return if (elem.originatesFromKotlin()) {
		val methodElem = elem.enclosingElement.toKJElement(processingEnv) as KJExecutableElement

		/* This is an O(n^2) algorithm if the user iterates over ParameterElements
		and converts them all to KJParameterElements, but we will accept this because it
		shouldn't happen often and the number of parameters per method is usually small. */
		methodElem.parameters.single {
			it.simpleName == elem.simpleName.toString()
		}
	}
	else {
		JxParameterElement(elem, KJOrigin.fromJavax(elem, processingEnv), processingEnv)
	}
}

private fun convertMember(elem: Element, processingEnv: ProcessingEnvironment): KJElement {
	assert(elem.kind in setOf(ElementKind.CONSTRUCTOR, ElementKind.METHOD, ElementKind.FIELD,
			ElementKind.STATIC_INIT, ElementKind.INSTANCE_INIT, ElementKind.ENUM_CONSTANT))
	/* Conversion of members is relegated to the enclosing class because only the class has
	 metadata and knows about sibling members, so only the class knows enough to do the
	 conversion */
	val enclosingElement = elem.enclosingElement!!.toKJElement(processingEnv) as KJTypeElement
	return enclosingElement.lookupMemberKJElementFor(elem)
}

private fun convertPackage(elem: PackageElement, processingEnv: ProcessingEnvironment): KJPackageElement {
	assert(elem.kind == ElementKind.PACKAGE)

	val origin = if (elem.originatesFromKotlin())
		KJOrigin.Kotlin.Declared
	else
		KJOrigin.fromJavax(elem, processingEnv)

	return JxPackageElement(elem, origin, processingEnv)
}

private fun convertModule(elem: ModuleElement, processingEnv: ProcessingEnvironment): KJModuleElement {
	assert(elem.kind == ElementKind.MODULE)

	val origin = if (elem.originatesFromKotlin())
		KJOrigin.Kotlin.Declared
	else
		KJOrigin.fromJavax(elem, processingEnv)

	return JxModuleElement(elem, origin, processingEnv)
}

private fun convertLocalVar(elem: VariableElement, processingEnv: ProcessingEnvironment): KJElement {
	throw AssertionError(
			"Element to be converted is local but this library was written under the assumption " +
					"that it is impossible to get a local Element during annotation processing " +
					"(which will probably change in the future)."
	)
}
