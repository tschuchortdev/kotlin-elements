package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.load.java.JvmAbi
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*

/**
 * returns a [KotlinElement] if this [Element] has a 1:1 correspondence with
 * a syntactic construct in Kotlin source code, a [KotlinCompatElement] if
 * it was generated by the compiler for Java-comparability or implementation
 * reasons and is not accessible from Kotlin code or null if it is a Java element
 * completely unrelated to Kotlin
 */
fun Element.asKotlin(processingEnv: ProcessingEnvironment): KotlinRelatedElement? {
	fun TypeElement.toKotlin(): KotlinRelatedElement {
		val metadata = kotlinMetadata!!

		return when(metadata) {
			is KotlinClassMetadata -> when(kind) {
				ElementKind.ENUM -> KotlinEnumElement(this, metadata, processingEnv)
				ElementKind.CLASS -> KotlinClassElement(this, metadata, processingEnv)
				ElementKind.INTERFACE -> KotlinInterfaceElement(this, metadata, processingEnv)
				ElementKind.ANNOTATION_TYPE -> KotlinAnnotationElement(this, metadata, processingEnv)
				else -> throw AssertionError("Only TypeElements should have Kotlin metadata")
			}
			is KotlinFileMetadata -> KotlinFileFacadeElement(this, metadata, processingEnv)
			is KotlinMultiFileClassFacadeMetadata -> KotlinFileFacadeElement(this, metadata, processingEnv)
			is KotlinMultiFileClassPartMetadata -> throw AssertionError(
					"Element $this is a MultiFileClassPart but this shouldn't be possible" +
					"because MultiFileClassParts are synthetic and thus are never loaded" +
					"by the annotation processor")
			is KotlinSyntheticClassMetadata ->
				if (simpleName.toString() == JvmAbi.DEFAULT_IMPLS_CLASS_NAME)
					KotlinInterfaceDefaultImplElement(this, metadata, processingEnv)
				else
					UnspecifiedKotlinCompatElement(this, processingEnv)
			is KotlinUnknownMetadata -> throw AssertionError("Element $this has unknown kotlin metadata: $metadata")
		}
	}

	return if(this is KotlinRelatedElement)
		this
	else if(!originatesFromKotlinCode())
		null
	else when (kind) {
		ElementKind.CLASS,
		ElementKind.ENUM,
		ElementKind.INTERFACE,
		ElementKind.ANNOTATION_TYPE -> (this as TypeElement).toKotlin()

		ElementKind.CONSTRUCTOR -> {
			val constructors = (enclosingElement.asKotlin(processingEnv) as EnclosesKotlinConstructors).constructors

			constructors.atMostOne { it.javaElement == this }
					as KotlinRelatedElement? // unnecessary cast to prevent the compiler from inferring the wrong type
			?: constructors.flatMap { it.javaOverloads }.atMostOne { it.javaElement == this }
			?: throw IllegalStateException("Can not convert element $this to Kotlin: ElementKind is CONSTRUCTOR but" +
											"does not belong to any Kotlin constructor of its enclosing element")

		}

		ElementKind.METHOD -> {
			val enclosingElem = enclosingElement.asKotlin(processingEnv)
			val functions = (enclosingElem as? EnclosesKotlinFunctions)?.functions
			val properties = (enclosingElem as? EnclosesKotlinProperties)?.properties
			val typeAliases = (enclosingElem as? EnclosesKotlinTypeAliases)?.typeAliases
			val annotationParams = (enclosingElem as? KotlinAnnotationElement)?.parameters

			functions?.atMostOne { it.javaElement == this }
					as KotlinRelatedElement? // unnecessary cast to prevent the compiler from inferring the wrong type
			?: functions?.flatMap { it.javaOverloads }?.atMostOne { it.javaElement == this }
			?: properties?.mapNotNull { it.getter }?.atMostOne { it.javaElement == this }
			?: properties?.mapNotNull { it.setter }?.atMostOne { it.javaElement == this }
			?: properties?.atMostOne { it.javaAnnotationHolderElement == this }
			?: typeAliases?.atMostOne { it.javaAnnotationHolderElement == this }
			?: annotationParams?.atMostOne { it.javaElement == this }
			?: throw IllegalStateException(
					"Can not convert element $this to Kotlin: ElementKind is METHOD but does not belong to " +
					"any Kotlin function, overload, getter, setter, property annotation holder, type alias annotation" +
					"holder or annotation class parameter of its enclosing element")

		}

		ElementKind.INSTANCE_INIT,
		ElementKind.STATIC_INIT -> throw AssertionError(
				"Element originating from Kotlin code should never be of kind INSTANCE_INIT or STATIC_INIT"
		)

		ElementKind.TYPE_PARAMETER -> {
			//TODO("handle type parameters of KotlinCompatElements")
			val enclosingElem = enclosingElement.asKotlin(processingEnv)

			if(enclosingElem is KotlinParameterizable)
				enclosingElem.typeParameters.single { it.javaElement == this }
			else
				throw AssertionError("enclosing element of element $this with kind TYPE_PARAMETER originating " +
									 "from Kotlin is not KotlinParameterizable")
		}

		ElementKind.PARAMETER -> {
			//TODO("handle annotated receiver parameter")
			//TODO("handle parameters of java overloads")
			(enclosingElement.asKotlin(processingEnv) as KotlinExecutableElement)
					.parameters.single { it.simpleName == simpleName }
		}

		ElementKind.FIELD -> {
			val properties = (enclosingElement.asKotlin(processingEnv) as EnclosesKotlinProperties).properties

			(properties.mapNotNull { it.backingField } + properties.mapNotNull { it.delegateField })
					.single { it == this }
		}

		ElementKind.ENUM_CONSTANT -> TODO("handle enum constants")

		ElementKind.RESOURCE_VARIABLE,
		ElementKind.EXCEPTION_PARAMETER,
		ElementKind.LOCAL_VARIABLE -> throw AssertionError(
				"Element to be converted is local but this library was written under the assumption " +
				"that it is impossible to get a local Element during annotation processing (which will probably " +
				"change in the future)"
		)

		ElementKind.MODULE -> KotlinModuleElement(this as ModuleElement, processingEnv)

		ElementKind.PACKAGE -> KotlinPackageElement(this as PackageElement, kotlinMetadata as KotlinPackageMetadata, processingEnv)

		ElementKind.OTHER -> throw UnsupportedOperationException(
				"Can not convert element \"$this\" of unknown kind \"$kind\" to Kotlin")

		null -> throw NullPointerException("Can not convert element to Kotlin: Kind of element \"$this\" was null")

		else -> throw UnsupportedOperationException(
				"Can not convert element \"$this\" of unsupported kind \"$kind\" to Kotlin.\n" +
				"Element kind was probably added to the Java language at a later date")
	}
}

/**
 * Whether this element originates from Kotlin code. It doesn't actually have to be
 * a [KotlinElement], it may as well be a [KotlinCompatElement] that was generated
 * by the compiler for Java-interop
 */
//TODO("make internal")
fun Element.originatesFromKotlinCode(): Boolean {
	return if(kotlinMetadata != null)
		true
	else when(this.kind) {
		ElementKind.CLASS,
		ElementKind.ENUM,
		ElementKind.INTERFACE,
		ElementKind.ANNOTATION_TYPE,
		ElementKind.PACKAGE -> false

		ElementKind.MODULE -> enclosedElements.any { originatesFromKotlinCode() }  //TODO("check module metadata instead")

		ElementKind.OTHER -> throw UnsupportedOperationException("Encountered unknown element kind")

		else -> enclosingElement?.originatesFromKotlinCode() ?: false
	}
}