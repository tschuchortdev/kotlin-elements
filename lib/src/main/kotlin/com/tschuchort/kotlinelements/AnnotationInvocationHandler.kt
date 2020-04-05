package com.tschuchort.kotlinelements

import java.io.Serializable
import java.lang.annotation.IncompleteAnnotationException
import java.lang.reflect.AccessibleObject
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.security.AccessController
import java.security.PrivilegedAction

internal class AnnotationInvocationHandler(
    private val clazz: Class<out Annotation>,
    private val methodResults: Map<String, MethodResult>
) : InvocationHandler, Serializable {

    sealed class MethodResult {
        class Value(val value: Any) : MethodResult() {
            override fun equals(other: Any?): Boolean {
                val otherValue = (other as? Value)?.value ?: return false
                return when {
                    otherValue is ByteArray && value is ByteArray -> otherValue.contentEquals(value)
                    otherValue is CharArray && value is CharArray -> otherValue.contentEquals(value)
                    otherValue is ShortArray && value is ShortArray -> otherValue.contentEquals(value)
                    otherValue is IntArray && value is IntArray -> otherValue.contentEquals(value)
                    otherValue is LongArray && value is LongArray -> otherValue.contentEquals(value)
                    otherValue is FloatArray && value is FloatArray -> otherValue.contentEquals(value)
                    otherValue is DoubleArray && value is DoubleArray -> otherValue.contentEquals(value)
                    otherValue is BooleanArray && value is BooleanArray -> otherValue.contentEquals(value)
                    otherValue is Array<*> && value is Array<*> -> otherValue.contentDeepEquals(value)
                    else -> otherValue == value
                }
            }

            override fun hashCode(): Int = when (value) {
                is ByteArray -> value.contentHashCode()
                is CharArray -> value.contentHashCode()
                is ShortArray -> value.contentHashCode()
                is IntArray -> value.contentHashCode()
                is LongArray -> value.contentHashCode()
                is FloatArray -> value.contentHashCode()
                is DoubleArray -> value.contentHashCode()
                is BooleanArray -> value.contentHashCode()
                is Array<*> -> value.contentDeepHashCode()
                else -> value.hashCode()
            }
        }

        class ExceptionThrown(val exception: RuntimeException) : MethodResult() {
            override fun equals(other: Any?)
                    = (other as? ExceptionThrown)?.exception == this.exception

            override fun hashCode(): Int = exception.hashCode()
        }
    }

    init {
        assert(clazz.isAnnotation && clazz.interfaces.size == 1 && clazz.interfaces.first() == Annotation::class.java) {
            "This InvocationHandler only implements Annotation interfaces"
        }
    }

    override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any = when (method.name) {
        "toString" -> toStringImpl()
        "hashCode" -> hashCodeImpl()
        "annotationType" -> clazz
        "equals" -> {
            require(method.parameterCount == 1 && method.parameterTypes.first() == Any::class.java) {
                "equals method must have a single java.lang.Object parameter"
            }

            equalsImpl(proxy, args.first())
        }
        else -> annotationMembersImpl(method.name)
    }

    private fun annotationMembersImpl(methodName: String): Any {
        val result = methodResults[methodName] ?: throw IncompleteAnnotationException(clazz, methodName)
        return when (result) {
            is MethodResult.Value ->
                if (result.value.javaClass.isArray)
                    cloneArray(result)
                else
                    result
            is MethodResult.ExceptionThrown -> throw result.exception
        }
    }

    private fun cloneArray(array: Any): Any = when (array.javaClass) {
        ByteArray::class.java -> (array as ByteArray).clone()
        CharArray::class.java -> (array as CharArray).clone()
        DoubleArray::class.java -> (array as DoubleArray).clone()
        FloatArray::class.java -> (array as FloatArray).clone()
        IntArray::class.java -> (array as IntArray).clone()
        LongArray::class.java -> (array as LongArray).clone()
        ShortArray::class.java -> (array as ShortArray).clone()
        BooleanArray::class.java -> (array as BooleanArray).clone()
        else -> (array as Array<*>).clone()
    }

    private fun toStringImpl(): String = methodResults.toList()
        .joinToString(", ", "@${clazz.name}(", ")") { (methodName, methodResult) ->
            "$methodName=${methodResultToSourceCodeLiteral(methodResult)}"
        }

    private fun equalsImpl(proxy: Any, other: Any): Boolean {
        if (proxy === other)
            return true

        if (!clazz.isInstance(other))
            return false

        for ((method, result) in annotMethodsWithResults.entries) {
            val otherResult = try {
                MethodResult.Value(method.invoke(other))
            } catch (e: InvocationTargetException) {
                return false
            } catch (e: RuntimeException) {
                MethodResult.ExceptionThrown(e)
            }

            when (otherResult) {
                is MethodResult.Value -> if (result != otherResult)
                    return false
                is MethodResult.ExceptionThrown -> if (result != otherResult)
                    throw otherResult.exception
            }
        }

        return true
    }

    private val annotMethodsWithResults: Map<Method, MethodResult> by lazy {
        AccessController.doPrivileged(PrivilegedAction {
            clazz.declaredMethods
                .filter { it.parameterCount == 0 }.toTypedArray()
                .also { AccessibleObject.setAccessible(it, true) }
                .mapNotNull { method ->
                    methodResults[method.name]?.let { value -> Pair(method, value) }
                }
                .toMap()
        })
    }

    private fun hashCodeImpl(): Int {
        // We use exactly this implementation to try to be compatible with the OpenJDK implementation
        return methodResults.entries.fold(0) { acc, (methodName, methodResult) ->
            acc + 127 * methodName.hashCode() xor methodResult.hashCode()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun methodResultToSourceCodeLiteral(result: MethodResult) = when (result) {
        is MethodResult.Value -> when (result.value.javaClass) {
            Class::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as Class<*>)
            String::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as String)
            Char::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as Char)
            Double::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as Double)
            Float::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as Float)
            Long::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as Long)
            ByteArray::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as ByteArray)
            CharArray::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as CharArray)
            ShortArray::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as ShortArray)
            IntArray::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as IntArray)
            LongArray::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as LongArray)
            BooleanArray::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as BooleanArray)
            Array<String>::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as Array<String>)
            Array<Any>::class.java -> JavaLiterals.toSourceCodeLiteral(result.value as Array<Any>)
            else -> result.value.toString()
        }
        is MethodResult.ExceptionThrown -> result.exception.toString()
    }
}