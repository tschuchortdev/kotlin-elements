package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.java.JavaAnnotationElement
import com.tschuchort.kotlinelements.java.JavaClassElement
import com.tschuchort.kotlinelements.java.JavaEnumElement
import com.tschuchort.kotlinelements.java.JavaFieldElement
import com.tschuchort.kotlinelements.java.JavaModuleElement
import com.tschuchort.kotlinelements.java.JavaPackageElement
import com.tschuchort.kotlinelements.kotlin.*
import com.tschuchort.kotlinelements.kotlin.EnclosesKotlinElements
import com.tschuchort.kotlinelements.kotlin.KotlinClassElement
import com.tschuchort.kotlinelements.kotlin.getKotlinMetadata
import com.tschuchort.kotlinelements.kotlin.originatesFromKotlin
import kotlinx.metadata.Flag
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*

object KJElementFactory {

    fun convert(elem: Element, processingEnv: ProcessingEnvironment): KJElement {
        try {
            return if (!elem.originatesFromKotlin())
                convertJavaElement(elem, processingEnv)
            else
                convertKotlinElement(elem, processingEnv)
        }
        catch(e: Exception) {
            throw KotlinElementConversionException(elem, e)
        }
    }

    private fun convertJavaElement(elem: Element, processingEnv: ProcessingEnvironment)
            : KJElement = when(elem.kind!!) {
        ElementKind.PACKAGE             -> JavaPackageElement(elem as PackageElement, processingEnv)
        ElementKind.ENUM                -> JavaEnumElement(elem as TypeElement, processingEnv)
        ElementKind.CLASS               -> JavaClassElement(elem as TypeElement, processingEnv)
        ElementKind.ANNOTATION_TYPE     -> JavaAnnotationElement(elem as TypeElement, processingEnv)
        ElementKind.INTERFACE           -> TODO()
        ElementKind.ENUM_CONSTANT       -> TODO()
        ElementKind.FIELD               -> JavaFieldElement(elem as VariableElement, processingEnv)
        ElementKind.PARAMETER           -> TODO()
        ElementKind.LOCAL_VARIABLE      -> TODO()
        ElementKind.EXCEPTION_PARAMETER -> TODO()
        ElementKind.METHOD              -> TODO()
        ElementKind.CONSTRUCTOR         -> TODO()
        ElementKind.STATIC_INIT         -> TODO()
        ElementKind.INSTANCE_INIT       -> TODO()
        ElementKind.TYPE_PARAMETER      -> TODO()
        ElementKind.OTHER               -> TODO()
        ElementKind.RESOURCE_VARIABLE   -> TODO()
        ElementKind.MODULE              -> JavaModuleElement(elem as ModuleElement, processingEnv)
    }

    private fun convertKotlinElement(elem: Element, processingEnv: ProcessingEnvironment): KJElement =
        when (elem.kind!!) {
            ElementKind.CLASS,
            ElementKind.ENUM,
            ElementKind.INTERFACE,
            ElementKind.ANNOTATION_TYPE -> convertKotlinType(elem as TypeElement, processingEnv)

            ElementKind.CONSTRUCTOR,
            ElementKind.METHOD,
            ElementKind.FIELD,
            ElementKind.INSTANCE_INIT,
            ElementKind.STATIC_INIT     -> {
                (elem.enclosingElement.toKJElement(processingEnv) as EnclosesKotlinElements)
                        .lookupEnclosedKJElementFor(elem)
            }

            ElementKind.TYPE_PARAMETER  -> convertKotlinTypeParam(elem as TypeParameterElement, processingEnv)

            ElementKind.PARAMETER       -> convertKotlinParam(elem as VariableElement, processingEnv)

            ElementKind.ENUM_CONSTANT   -> KotlinEnumConstantElement(elem as VariableElement, processingEnv)

            ElementKind.RESOURCE_VARIABLE,
            ElementKind.EXCEPTION_PARAMETER,
            ElementKind.LOCAL_VARIABLE  -> throw AssertionError(
                    "Element to be converted is local but this library was written under the assumption " +
                            "that it is impossible to get a local Element during annotation processing (which will probably " +
                            "change in the future)."
            )

            ElementKind.MODULE          -> KotlinModuleElement(elem as ModuleElement, processingEnv)

            ElementKind.PACKAGE         -> KotlinPackageElement(elem as PackageElement, processingEnv)

            ElementKind.OTHER           -> throw UnsupportedOperationException(
                    "Can not convert element $this of unknown kind ${elem.kind} to Kotlin."
            )

            else                        -> throw UnsupportedOperationException(
                    "Can not convert element $this of unsupported kind ${elem.kind} to Kotlin.\n" +
                            "Element kind was probably added to the Java language at a later date."
            )
        }

    private fun convertKotlinType(elem: TypeElement, processingEnv: ProcessingEnvironment): KJElement
            = when (val metadata = elem.getKotlinMetadata()!!) {
        is KotlinClassMetadata.Class -> when (elem.kind!!) {
            ElementKind.ENUM -> KotlinEnumElement(elem, metadata.toKmClass(), processingEnv)
            ElementKind.CLASS -> {
                val kmClass = metadata.toKmClass()
                if (Flag.Class.IS_COMPANION_OBJECT(kmClass.flags) || Flag.Class.IS_OBJECT(kmClass.flags))
                    KotlinObjectElement(elem, kmClass, processingEnv)
                else
                    KotlinClassElement(elem, kmClass, processingEnv)
            }
            ElementKind.INTERFACE -> KotlinInterfaceElement(elem, metadata.toKmClass(), processingEnv)
            ElementKind.ANNOTATION_TYPE -> KotlinAnnotationElement(elem, metadata.toKmClass(), processingEnv)
            else -> throw AssertionError("Only TypeElements should have Kotlin metadata")
        }
        is KotlinClassMetadata.FileFacade -> KotlinFileFacadeElement(elem, metadata.toKmPackage(), processingEnv)
        is KotlinClassMetadata.MultiFileClassFacade -> KotlinMultiFileClassFacadeElement(elem, metadata.partClassNames, processingEnv)
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

    private fun convertParam(elem: VariableElement, processingEnv: ProcessingEnvironment): KJElement {
        //TODO("handle annotated receiver parameter")
        //TODO("handle parameters of java overloads")
        val enclosingKotlinElem = elem.enclosingElement.asKotlin(processingEnv) as? KotlinExecutableElement
            ?: throw AssertionError("Enclosing element of $elem with kind PARAMETER originating " +
                    "from Kotlin is not of type KotlinExecutableElement.")

        return enclosingKotlinElem.parameters.single { it.simpleName == elem.simpleName }
    }

    private fun convertField(elem: VariableElement, processingEnv: ProcessingEnvironment): KJElement {

    }
}


/**
 * returns a [KotlinElement] if this [Element] has a 1:1 correspondence with
 * a syntactic construct in Kotlin source code, a [KotlinCompatElement] if
 * it was generated by the compiler for Java-comparability or implementation
 * reasons and is not accessible from Kotlin code or null if it is a Java element
 * completely unrelated to Kotlin
 */
fun Element.toKJElement(processingEnv: ProcessingEnvironment): KJElement
        = KJElementFactory.convert(this, processingEnv)


