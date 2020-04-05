package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*

/** Converts any Javax [Element] to a corresponding [KJElement] */
fun Element.toKJElement(processingEnv: ProcessingEnvironment): KJElement = try {
	when (kind!!) {
		ElementKind.CLASS,
		ElementKind.ENUM,
		ElementKind.INTERFACE,
		ElementKind.ANNOTATION_TYPE -> KJTypeElementFactory.convertType(this as TypeElement, processingEnv).convertedType

		ElementKind.CONSTRUCTOR,
		ElementKind.METHOD,
		ElementKind.FIELD,
		ElementKind.INSTANCE_INIT,
		ElementKind.STATIC_INIT,
		ElementKind.ENUM_CONSTANT,
		ElementKind.RECORD_COMPONENT -> convertMember(this, processingEnv)

		ElementKind.TYPE_PARAMETER  -> convertTypeParameter(this as TypeParameterElement, processingEnv)

		ElementKind.PARAMETER       -> convertParameter(this as VariableElement, processingEnv)

		ElementKind.RESOURCE_VARIABLE,
		ElementKind.EXCEPTION_PARAMETER,
		ElementKind.LOCAL_VARIABLE  -> throw AssertionError(
				"Element to be converted is local but this library was written under the assumption " +
						"that it is impossible to get a local Element during annotation processing " +
						"(which will probably change in the future)."
		)

		ElementKind.MODULE          -> convertModule(this as ModuleElement, processingEnv)

		ElementKind.PACKAGE         -> convertPackage(this as PackageElement, processingEnv)

		ElementKind.OTHER           -> convertOther(this, processingEnv)
		else                        -> throw UnsupportedOperationException(
				"Can not convert element of unsupported kind $kind\n" +
						"Element kind was probably added to the Java language at a later date " +
						"and is unknown to this library version."
		)
	}
}
catch (e: Exception) {
	throw KJConversionException(this, e)
}

/**
 * Converts any Javax [TypeElement] to a corresponding [KJTypeElement].
 * Prefer this function over [Element.toKJElement] for type safety.
 */
fun TypeElement.toKJTypeElement(processingEnv: ProcessingEnvironment): KJTypeElement = try {
	KJTypeElementFactory.convertType(this, processingEnv).convertedType
}
catch (e: Exception) {
	throw KJConversionException(this, e)
}

/**
 * Converts any Javax [TypeParameterElement] to a corresponding [KJTypeParameterElement].
 * Prefer this function over [Element.toKJElement] for type safety.
 */
fun TypeParameterElement.toKJTypeParameterElement(processingEnv: ProcessingEnvironment): KJTypeParameterElement = try {
	convertTypeParameter(this, processingEnv)
}
catch (e: Exception) {
	throw KJConversionException(this, e)
}

/**
 * Converts any Javax [PackageElement] to a corresponding [KJPackageElement].
 * Prefer this function over [Element.toKJElement] for type safety.
 */
fun PackageElement.toKJPackageElement(processingEnv: ProcessingEnvironment): KJPackageElement = try {
	convertPackage(this, processingEnv)
}
catch (e: Exception) {
	throw KJConversionException(this, e)
}

/**
 * Converts any Javax [ModuleElement] to a corresponding [KJModuleElement].
 * Prefer this function over [Element.toKJElement] for type safety.
 */
fun ModuleElement.toKJModuleElement(processingEnv: ProcessingEnvironment): KJElement = try {
	convertModule(this, processingEnv)
}
catch (e: Exception) {
	throw KJConversionException(this, e)
}


internal fun KJElement.fromJavax(elem: Element, origin: KJOrigin, processingEnv: ProcessingEnvironment): KJElement =
	object : KJElement,
			 AnnotatedConstruct by elem {
		val ENCLOSING_ELEMENT: KJElement? by lazy {
			elem.enclosingElement?.toKJElement(processingEnv)
		}

		val ENCLOSED_ELEMENTS: Set<KJElement> by lazy {
			elem.enclosedElements.map { it.toKJElement(processingEnv) }.toSet()
		}

		override val simpleName: String
			get() = elem.simpleName.toString()

		override fun asTypeMirror(): KJTypeMirror? = elem.asType()!!.toKJTypeMirror(processingEnv)

		override fun asJavaxElement(): Element = elem

		override val origin: KJOrigin = origin
	}

