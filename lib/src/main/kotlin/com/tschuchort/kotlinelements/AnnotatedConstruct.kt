package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.mixins.ConvertibleToElement
import com.tschuchort.kotlinelements.mixins.ConvertibleToTypeMirror
import com.tschuchort.kotlinelements.mixins.HasQualifiedName
import kotlinx.metadata.KmAnnotation
import kotlinx.metadata.KmAnnotationArgument
import sun.reflect.annotation.AnnotationInvocationHandler
import java.lang.reflect.Proxy
import java.security.AccessController
import java.security.PrivilegedAction
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

interface KJAnnotationMirror : ConvertibleToTypeMirror, ConvertibleToElement, HasQualifiedName {
    val annotationValues: Map<String, KJAnnotationValue<*>>
    fun asJavax(): AnnotationMirror

    override fun asTypeMirror(): KJDeclaredType
    override fun asElement(): KJAnnotationElement
    companion object
}

sealed class KJAnnotationValue<out T : Any> {
    abstract val value: T

    data class ByteValue(override val value: Byte) : KJAnnotationValue<Byte>()
    data class CharValue(override val value: Char) : KJAnnotationValue<Char>()
    data class ShortValue(override val value: Short) : KJAnnotationValue<Short>()
    data class IntValue(override val value: Int) : KJAnnotationValue<Int>()
    data class LongValue(override val value: Long) : KJAnnotationValue<Long>()
    data class FloatValue(override val value: Float) : KJAnnotationValue<Float>()
    data class DoubleValue(override val value: Double) : KJAnnotationValue<Double>()
    data class BooleanValue(override val value: Boolean) : KJAnnotationValue<Boolean>()

    data class UByteValue(override val value: Byte) : KJAnnotationValue<Byte>()
    data class UShortValue(override val value: Short) : KJAnnotationValue<Short>()
    data class UIntValue(override val value: Int) : KJAnnotationValue<Int>()
    data class ULongValue(override val value: Long) : KJAnnotationValue<Long>()

    data class StringValue(override val value: String) : KJAnnotationValue<String>()
    data class TypeMirrorValue(override val value: KJTypeMirror) : KJAnnotationValue<KJTypeMirror>()
    data class EnumValue(override val value: KJEnumConstantElement) : KJAnnotationValue<KJEnumConstantElement>()

    data class AnnotationMirrorValue(override val value: KJAnnotationMirror) : KJAnnotationValue<KJAnnotationMirror>()
    data class ArrayValue(override val value: List<KJAnnotationValue<*>>) : KJAnnotationValue<List<KJAnnotationValue<*>>>()

    companion object
}

interface KJAnnotatedConstruct {

    val annotations: List<KJAnnotationMirror>

    fun asJavax(): AnnotatedConstruct

    companion object
}

internal fun KJAnnotationValue.Companion.fromMetadata(kmAnnArg: KmAnnotationArgument<*>)
        : KJAnnotationValue<*> = when(kmAnnArg) {
    is KmAnnotationArgument.ByteValue -> KJAnnotationValue.ByteValue(kmAnnArg.value)
    is KmAnnotationArgument.CharValue -> KJAnnotationValue.CharValue(kmAnnArg.value)
    is KmAnnotationArgument.ShortValue -> KJAnnotationValue.ShortValue(kmAnnArg.value)
    is KmAnnotationArgument.IntValue -> KJAnnotationValue.IntValue(kmAnnArg.value)
    is KmAnnotationArgument.LongValue -> KJAnnotationValue.LongValue(kmAnnArg.value)
    is KmAnnotationArgument.FloatValue -> KJAnnotationValue.FloatValue(kmAnnArg.value)
    is KmAnnotationArgument.DoubleValue -> KJAnnotationValue.DoubleValue(kmAnnArg.value)
    is KmAnnotationArgument.BooleanValue -> KJAnnotationValue.BooleanValue(kmAnnArg.value)
    is KmAnnotationArgument.UByteValue -> KJAnnotationValue.UByteValue(kmAnnArg.value)
    is KmAnnotationArgument.UShortValue -> KJAnnotationValue.UShortValue(kmAnnArg.value)
    is KmAnnotationArgument.UIntValue -> KJAnnotationValue.UIntValue(kmAnnArg.value)
    is KmAnnotationArgument.ULongValue -> KJAnnotationValue.ULongValue(kmAnnArg.value)
    is KmAnnotationArgument.StringValue -> KJAnnotationValue.StringValue(kmAnnArg.value)
    is KmAnnotationArgument.KClassValue ->
    is KmAnnotationArgument.EnumValue ->
    is KmAnnotationArgument.AnnotationValue ->
    is KmAnnotationArgument.ArrayValue -> KJAnnotationValue.ArrayValue(
        kmAnnArg.value.map(KJAnnotationValue.Companion::fromMetadata)
    )
}

internal fun KJAnnotationMirror.Companion.fromMetadata(kmAnn: KmAnnotation, processingEnv: ProcessingEnvironment)
        = object : KJAnnotationMirror {
    override val annotationValues: Map<String, KJAnnotationValue<*>> by lazy {
        kmAnn.arguments.mapValues { (_, kmAnnArg) -> KJAnnotationValue.fromMetadata(kmAnnArg) }
    }

    override fun asJavax(): AnnotationMirror {
    }

    override fun asTypeMirror(): KJDeclaredType {
    }

    private val element_: KJAnnotationElement by lazy {
        val javaxTypeElem = processingEnv.elementUtils.getTypeElement(kmAnn.className.replace('/', '.'))
            ?: error("Could not find Javax TypeElement for KmAnnotation ${kmAnn.className}")

        javaxTypeElem.toKJTypeElement(processingEnv) as KJAnnotationElement
    }

    override fun asElement() = element_

    override val qualifiedName: String
        get() = kmAnn.className.replace('/', '.')

    override val simpleName: String
        get() = kmAnn.className.substringAfterLast('.',
            kmAnn.className.substringAfterLast('/', kmAnn.className))
}

internal fun KJAnnotatedConstruct.Companion.fromMetadata(kmAnnotations: List<KmAnnotation>,
                                                         processingEnv: ProcessingEnvironment)
        : KJAnnotatedConstruct = object : KJAnnotatedConstruct {

    private val annotationsByClass: Map<KClass<*>, KJAnnotationMirror> by lazy {
        kmAnnotations.map { KJAnnotationMirror.fromMetadata(it, processingEnv) }
            .associateBy {  }
    }

    override val annotations: List<KJAnnotationMirror> = annotationsByClass.values.toList()

    override fun asJavax(): AnnotatedConstruct {
        return annotatedConstructFromMetadata(kmAnnotations, processingEnv)
    }

}

internal fun annotatedConstructFromMetadata(annotations: List<KmAnnotation>,
                                            processingEnv: ProcessingEnvironment)
        : AnnotatedConstruct = object : AnnotatedConstruct {

    override fun <A : Annotation> getAnnotationsByType(annotationType: Class<A>): Array<A> {
        require(annotationType.isAnnotation) { "Not an annotation type: $annotationType" }
    }

    private fun instantiateAnnotationProxy() {

    }

    /**
     * This method, which clones its array argument, would not be necessary
     * if Cloneable had a public clone method.
     */
    private fun cloneArray(array: Any): Any? {
        val type: Class<*> = array.javaClass
        if (type == ByteArray::class.java) {
            val byteArray = array as ByteArray
            return byteArray.clone()
        }
        if (type == CharArray::class.java) {
            val charArray = array as CharArray
            return charArray.clone()
        }
        if (type == DoubleArray::class.java) {
            val doubleArray = array as DoubleArray
            return doubleArray.clone()
        }
        if (type == FloatArray::class.java) {
            val floatArray = array as FloatArray
            return floatArray.clone()
        }
        if (type == IntArray::class.java) {
            val intArray = array as IntArray
            return intArray.clone()
        }
        if (type == LongArray::class.java) {
            val longArray = array as LongArray
            return longArray.clone()
        }
        if (type == ShortArray::class.java) {
            val shortArray = array as ShortArray
            return shortArray.clone()
        }
        if (type == BooleanArray::class.java) {
            val booleanArray = array as BooleanArray
            return booleanArray.clone()
        }
        val objectArray = array as Array<*>
        return objectArray.clone()
    }


    /**
     * Implementation of dynamicProxy.toString()
     */
    private fun toStringImpl(): String? {
        val result = StringBuilder(128)
        result.append('@')
        result.append(type.getName())
        result.append('(')
        var firstMember = true
        for ((key, value) in memberValues.entries) {
            if (firstMember) firstMember = false else result.append(", ")
            result.append(key)
            result.append('=')
            result.append(AnnotationInvocationHandler.memberValueToString(value))
        }
        result.append(')')
        return result.toString()
    }

    override fun <A : Annotation> getAnnotation(annotationType: Class<A>): A? {
        require(annotationType.isAnnotation) { "Not an annotation type: $annotationType" }
        return annotations.firstOrNull { it.className.replace('/', '.') == annotationType.canonicalName }
            ?.let {


                AccessController.doPrivileged(PrivilegedAction {
                    Proxy.newProxyInstance(
                        annotationType.classLoader, arrayOf(annotationType)
                    ) { proxy, method, args ->
                        val member = method.name
                        val parameterCount = method.parameterCount

                        // Handle Object and Annotation methods

                        // Handle Object and Annotation methods
                        if (parameterCount == 1 && member === "equals" && method.parameterTypes[0] == Any::class.java) {
                            return equalsImpl(proxy, args[0])
                        }
                        if (parameterCount != 0) {
                            throw AssertionError("Too many parameters for an annotation method")
                        }

                        if (member === "toString") {
                            return toStringImpl()
                        } else if (member === "hashCode") {
                            return hashCodeImpl()
                        } else if (member === "annotationType") {
                            return type
                        }
                    } as Annotation
                })

                annotationType.cast()
            }
    }

    private val annotationMirrors_: List<AnnotationMirror> by lazy {
        annotations.map { annotationMirrorFromMetadata(it, processingEnv) }
    }

    override fun getAnnotationMirrors() = annotationMirrors_
}

internal fun annotationMirrorFromMetadata(ann: KmAnnotation, processingEnv: ProcessingEnvironment)
        = object : AnnotationMirror {
    private val typeElement by lazy {
        processingEnv.elementUtils.getTypeElement(ann.className.replace('/', '.'))
            .also { assert(it.kind == ElementKind.ANNOTATION_TYPE) }
    }

    override fun getAnnotationType(): DeclaredType {
    }

    private val elementValues_: Map<out ExecutableElement, AnnotationValue> by lazy {
        try {
            val executablesByName: Map<String, ExecutableElement> = typeElement.enclosedElements
                .mapNotNull { it.asExecutableElement() }
                .associateBy { it.simpleName.toString() }

            ann.arguments.entries.map { annArg ->
                val executable = executablesByName[annArg.key]
                    ?: error(
                        "Could not find executable element with the same name that represents the " +
                                "annotation property ${annArg.key} in the annotation class element " +
                                "$typeElement.\nLooked at the following executable elements: $executablesByName"
                    )

                val value = annotationValueFromMetadata(annArg.value)

                Pair(executable, value)
            }.toMap()
        }
        catch (t : Throwable) {
            throw KJConversionException("While trying to convert a KmAnnotation to Javax AnnotationMirror:\n$ann", t)
        }
    }

    override fun getElementValues() = elementValues_
}

internal fun annotationValueFromMetadata(annArg: KmAnnotationArgument<*>, processingEnv: ProcessingEnvironment): AnnotationValue
        = object : AnnotationValue {
    @Suppress("REDUNDANT_PROJECTION", "USELESS_CAST")
    override fun getValue(): Any? = try {
        when (annArg) {
            is KmAnnotationArgument.KClassValue -> TODO() as TypeMirror
            is KmAnnotationArgument.EnumValue -> {
                val javaxTypeElem =
                    processingEnv.elementUtils.getTypeElement(annArg.enumClassName.replace('/', '.'))
                        ?.also { assert(it.kind == ElementKind.ENUM) }
                        ?: error("Could not find Javax TypeElement for Enum class ${annArg.enumClassName}")

                javaxTypeElem.enclosedElements.single {
                    it.kind == ElementKind.ENUM_CONSTANT && it.simpleName.toString() == annArg.enumEntryName
                }
                        as VariableElement
            }
            is KmAnnotationArgument.AnnotationValue ->
                annotationMirrorFromMetadata(annArg.value, processingEnv) as AnnotationMirror
            is KmAnnotationArgument.ArrayValue ->
                annArg.value.map { annotationValueFromMetadata(it, processingEnv) } as List<out AnnotationValue>
            else -> annArg.value
        }
    } catch (t: Throwable) {
        throw KJConversionException("While trying to convert a KmAnnotationArgument ($annArg) to Javax AnnotationValue", t)
    }

    @Suppress("UNCHECKED_CAST")
    override fun toString(): String = when (annArg) {
        is KmAnnotationArgument.KClassValue -> TODO()
        is KmAnnotationArgument.EnumValue -> (value as VariableElement).toString()
        is KmAnnotationArgument.AnnotationValue -> (value as AnnotationMirror).toString()
        is KmAnnotationArgument.ArrayValue -> (value as List<AnnotationValue>).joinToString(", ", "{", "}")
        else -> annArg.value.toString()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R : Any?, P : Any?> accept(v: AnnotationValueVisitor<R, P>, p: P?): R? = when (annArg) {
        is KmAnnotationArgument.UByteValue,
        is KmAnnotationArgument.ByteValue -> v.visitByte(value as Byte, p)
        is KmAnnotationArgument.CharValue -> v.visitChar(value as Char, p)
        is KmAnnotationArgument.UShortValue,
        is KmAnnotationArgument.ShortValue -> v.visitShort(value as Short, p)
        is KmAnnotationArgument.UIntValue,
        is KmAnnotationArgument.IntValue -> v.visitInt(value as Int, p)
        is KmAnnotationArgument.ULongValue,
        is KmAnnotationArgument.LongValue -> v.visitLong(value as Long, p)
        is KmAnnotationArgument.FloatValue -> v.visitFloat(value as Float, p)
        is KmAnnotationArgument.DoubleValue -> v.visitDouble(value as Double, p)
        is KmAnnotationArgument.BooleanValue -> v.visitBoolean(value as Boolean, p)
        is KmAnnotationArgument.StringValue -> v.visitString(value as String, p)
        is KmAnnotationArgument.KClassValue -> v.visitType(value as TypeMirror, p)
        is KmAnnotationArgument.EnumValue -> v.visitEnumConstant(value as VariableElement, p)
        is KmAnnotationArgument.AnnotationValue -> v.visitAnnotation(value as AnnotationMirror, p)
        is KmAnnotationArgument.ArrayValue -> v.visitArray(value as List<AnnotationValue>, p)
    }
}