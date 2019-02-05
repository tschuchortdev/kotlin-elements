package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.load.java.JvmAbi
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import mixins.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*

object KotlinElementFactory {
    /** Already converted elements */
    private val convertedElements = mutableMapOf<Element, KotlinRelatedElement>()

    @Synchronized
    fun convertToKotlin(elem: Element, processingEnv: ProcessingEnvironment): KotlinRelatedElement? {
        convertedElements[elem]?.let { return it }

        if(!elem.originatesFromKotlinCode())
            return null

        try {
            val convertedElem = when (elem.kind!!) {
                ElementKind.CLASS,
                ElementKind.ENUM,
                ElementKind.INTERFACE,
                ElementKind.ANNOTATION_TYPE -> convertType(elem as TypeElement, processingEnv)

                ElementKind.CONSTRUCTOR -> convertConstructor(elem as ExecutableElement, processingEnv)

                ElementKind.METHOD -> convertMethod(elem as ExecutableElement, processingEnv)

                ElementKind.INSTANCE_INIT,
                ElementKind.STATIC_INIT -> throw AssertionError(
                    "Element originating from Kotlin code should never be of kind INSTANCE_INIT or STATIC_INIT."
                )

                ElementKind.TYPE_PARAMETER -> convertTypeParam(elem as TypeParameterElement, processingEnv)

                ElementKind.PARAMETER -> convertParam(elem as VariableElement, processingEnv)

                ElementKind.FIELD -> convertField(elem as VariableElement, processingEnv)

                ElementKind.ENUM_CONSTANT -> KotlinEnumConstantElement(elem as VariableElement, processingEnv)

                ElementKind.RESOURCE_VARIABLE,
                ElementKind.EXCEPTION_PARAMETER,
                ElementKind.LOCAL_VARIABLE -> throw AssertionError(
                    "Element to be converted is local but this library was written under the assumption " +
                            "that it is impossible to get a local Element during annotation processing (which will probably " +
                            "change in the future)."
                )

                ElementKind.MODULE -> KotlinModuleElement(elem as ModuleElement, processingEnv)

                ElementKind.PACKAGE -> KotlinPackageElement(elem as PackageElement, processingEnv)

                ElementKind.OTHER -> throw UnsupportedOperationException(
                    "Can not convert element $this of unknown kind ${elem.kind} to Kotlin.")

                else -> throw UnsupportedOperationException(
                    "Can not convert element $this of unsupported kind ${elem.kind} to Kotlin.\n" +
                            "Element kind was probably added to the Java language at a later date.")
            }

            convertedElements[elem] = convertedElem
            return convertedElem
        }
        catch(e: Exception) {
            throw KotlinElementConversionException(elem, e)
        }
    }

    private fun convertType(elem: TypeElement, processingEnv: ProcessingEnvironment): KotlinRelatedElement
            = when (val metadata = elem.kotlinMetadata!!) {
        is KotlinClassMetadata -> when (elem.kind!!) {
            ElementKind.ENUM -> KotlinEnumElement(elem, metadata, processingEnv)
            ElementKind.CLASS -> if(metadata.data.classProto.classKind == ProtoBuf.Class.Kind.COMPANION_OBJECT
                    || metadata.data.classProto.classKind == ProtoBuf.Class.Kind.OBJECT)
                KotlinObjectElement(elem, metadata, processingEnv)
            else
                KotlinClassElement(elem, metadata, processingEnv) as KotlinElement
            ElementKind.INTERFACE -> KotlinInterfaceElement(elem, metadata, processingEnv)
            ElementKind.ANNOTATION_TYPE -> KotlinAnnotationElement(elem, metadata, processingEnv)
            else -> throw AssertionError("Only TypeElements should have Kotlin metadata")
        }
        is KotlinFileMetadata -> KotlinFileFacadeElement(elem, metadata, processingEnv)
        is KotlinMultiFileClassFacadeMetadata -> KotlinFileFacadeElement(elem, metadata, processingEnv)
        is KotlinMultiFileClassPartMetadata -> throw AssertionError(
            "Element $this is a MultiFileClassPart but this shouldn't be possible " +
                    "because MultiFileClassParts are synthetic and thus are never loaded " +
                    "by the annotation processor."
        )
        is KotlinSyntheticClassMetadata ->
            if (elem.simpleName.toString() == JvmAbi.DEFAULT_IMPLS_CLASS_NAME)
                KotlinInterfaceDefaultImplElement(elem, metadata, processingEnv)
            else
                UnspecifiedKotlinCompatElement(elem, elem.enclosingElement.asKotlin(processingEnv)!!)
        is KotlinUnknownMetadata -> throw AssertionError("Element $elem has unknown kotlin metadata: $metadata.")
    }

    private fun convertConstructor(elem: ExecutableElement, processingEnv: ProcessingEnvironment): KotlinRelatedElement {
        assert(elem.kind == ElementKind.CONSTRUCTOR)

        val constructors = (elem.enclosingElement.asKotlin(processingEnv) as EnclosesKotlinConstructors).constructors

        return constructors.atMostOne { it.javaElement == elem }
                as KotlinRelatedElement? // unnecessary cast to prevent the compiler from inferring the wrong type
            ?: constructors.flatMap { it.javaOverloads }.atMostOne { it.javaElement == elem }
            ?: throw IllegalStateException("Can not convert this $this to Kotlin: ElementKind is CONSTRUCTOR but" +
                    "does not belong to any Kotlin constructor of its enclosing this.")
    }

    private fun convertMethod(elem: ExecutableElement, processingEnv: ProcessingEnvironment): KotlinRelatedElement {
        val enclosingKotlinElem = elem.enclosingElement.asKotlin(processingEnv)
        val functions = (enclosingKotlinElem as? EnclosesKotlinFunctions)?.functions
        val properties = (enclosingKotlinElem as? EnclosesKotlinProperties)?.properties
        val typeAliases = (enclosingKotlinElem as? EnclosesKotlinTypeAliases)?.typeAliases

        /**
         * A JVM method element can be any of the following:
         * - a Kotlin function
         * - Java overload of a Kotlin function
         * - getter of a Kotlin property
         * - setter of a Kotlin property
         * - synthetic annotation holder of a Kotlin property
         * - synthetic annotation holder of a Kotlin type alias
         * - parameter of a Kotlin annotation class
         *
         * We need to try all in order to find the corresponding Kotlin element
         */

        return if(enclosingKotlinElem is KotlinAnnotationElement)
            enclosingKotlinElem.parameters.single { it.javaElement == elem }
        else
            functions?.atMostOne { it.javaElement == elem }
                    as KotlinRelatedElement? // unnecessary cast to prevent the compiler from inferring the wrong type
                ?: functions?.flatMap { it.javaOverloads }?.atMostOne { it.javaElement == elem }
                ?: properties?.mapNotNull { it.getter }?.atMostOne { it.javaElement == elem }
                ?: properties?.mapNotNull { it.setter }?.atMostOne { it.javaElement == elem }
                ?: properties?.atMostOne { it.javaAnnotationHolderElement == elem }
                ?: typeAliases?.atMostOne { it.javaAnnotationHolderElement == elem }
                ?: throw IllegalStateException(
                        "Can not convert elem $elem to Kotlin: ElementKind is METHOD but does not belong to " +
                                "any Kotlin function, overload, getter, setter, property annotation holder, type alias annotation" +
                                "holder or annotation class parameter of its enclosing elem.")
    }

    private fun convertTypeParam(elem: TypeParameterElement, processingEnv: ProcessingEnvironment): KotlinRelatedElement {
        //TODO("handle type parameters of KotlinCompatElements")
        val enclosingKotlinElem = elem.enclosingElement.asKotlin(processingEnv) as? KotlinParameterizable
            ?: throw AssertionError("Enclosing element of $elem with kind TYPE_PARAMETER originating " +
                    "from Kotlin is not of type KotlinParameterizable.")


        return enclosingKotlinElem.typeParameters.single { it.javaElement == elem }
    }

    private fun convertParam(elem: VariableElement, processingEnv: ProcessingEnvironment): KotlinRelatedElement {
        //TODO("handle annotated receiver parameter")
        //TODO("handle parameters of java overloads")
        val enclosingKotlinElem = elem.enclosingElement.asKotlin(processingEnv) as? KotlinExecutableElement
            ?: throw AssertionError("Enclosing element of $elem with kind PARAMETER originating " +
                    "from Kotlin is not of type KotlinExecutableElement.")

        return enclosingKotlinElem.parameters.single { it.simpleName == elem.simpleName }
    }

    private fun convertField(elem: VariableElement, processingEnv: ProcessingEnvironment): KotlinRelatedElement {
        val enclosingKotlinElem = elem.enclosingElement.asKotlin(processingEnv) as? EnclosesKotlinProperties
            ?: throw AssertionError("Enclosing element of $elem with kind FIELD originating " +
                    "from Kotlin is not of type EnclosesKotlinProperties.")

        return (enclosingKotlinElem.properties.mapNotNull { it.backingField } +
                enclosingKotlinElem.properties.mapNotNull { it.delegateField })
            .single { it == elem }
    }
}

/**
 * returns a [KotlinElement] if this [Element] has a 1:1 correspondence with
 * a syntactic construct in Kotlin source code, a [KotlinCompatElement] if
 * it was generated by the compiler for Java-comparability or implementation
 * reasons and is not accessible from Kotlin code or null if it is a Java element
 * completely unrelated to Kotlin
 */
fun Element.asKotlin(processingEnv: ProcessingEnvironment): KotlinRelatedElement?
    = KotlinElementFactory.convertToKotlin(this, processingEnv)

/**
 * Checks whether this element originates from Kotlin code. It doesn't actually have to be
 * a [KotlinElement], it may as well be a [KotlinCompatElement] that was generated
 * by the compiler for Java-interop
 */
fun Element.originatesFromKotlinCode(): Boolean {
    return if(kotlinMetadata != null)
        true
    else when(this.kind) {
        ElementKind.CLASS,
        ElementKind.ENUM,
        ElementKind.INTERFACE,
        ElementKind.ANNOTATION_TYPE -> false

        ElementKind.PACKAGE,
        ElementKind.MODULE -> enclosedElements.any { it.originatesFromKotlinCode() }  //TODO("check module metadata instead")

        ElementKind.OTHER -> throw UnsupportedOperationException("Encountered unknown element kind.")

        else -> enclosingElement?.originatesFromKotlinCode() ?: false
    }
}