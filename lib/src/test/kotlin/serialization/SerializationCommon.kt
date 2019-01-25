@file:Suppress("MemberVisibilityCanBePrivate")

package serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.KryoException
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.SerializerFactory
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.CollectionSerializer
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy
import com.esotericsoftware.minlog.Log
import de.javakaffee.kryoserializers.*
import org.objenesis.strategy.StdInstantiatorStrategy
import java.io.Serializable
import java.lang.IllegalStateException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*
import javax.lang.model.type.*
import kotlin.reflect.KClass


fun getKryo(): Kryo {
    Log.WARN()

    return Kryo().apply {
        references = true
        isRegistrationRequired = false

        instantiatorStrategy = DefaultInstantiatorStrategy(StdInstantiatorStrategy())

        register(Arrays.asList("").javaClass, ArraysAsListSerializer())
        register(Collections.EMPTY_LIST.javaClass, CollectionsEmptyListSerializer())
        register(Collections.EMPTY_MAP.javaClass, CollectionsEmptyMapSerializer())
        register(Collections.EMPTY_SET.javaClass, CollectionsEmptySetSerializer())
        register(Collections.singletonList("").javaClass, CollectionsSingletonListSerializer())
        register(Collections.singleton("").javaClass, CollectionsSingletonSetSerializer())
        register(Collections.singletonMap("", "").javaClass, CollectionsSingletonMapSerializer())
        register(GregorianCalendar::class.java, GregorianCalendarSerializer())
        register(InvocationHandler::class.java, JdkProxySerializer())
        UnmodifiableCollectionsSerializer.registerSerializers(this)
        SynchronizedCollectionsSerializer.registerSerializers(this)

        addJavaLangModelSerializers(
            serializeEnclosingPackages = true,
            serializeDeclaredTypeAsElement = false)
    }
}

fun Kryo.addJavaLangModelSerializers(serializeEnclosingPackages: Boolean, serializeDeclaredTypeAsElement: Boolean) {
    addDefaultSerializer(List::class.java, JavacAwareListSerializerFactory())

    addDefaultSerializer(AnnotatedConstruct::class.java, ImmutableInterfaceSerializer(AnnotatedConstruct::class))

    addDefaultSerializer(Element::class.java, ElementSerializer(Element::class, serializeEnclosingPackages))
    addDefaultSerializer(ExecutableElement::class.java, ElementSerializer(ExecutableElement::class, serializeEnclosingPackages))
    addDefaultSerializer(ModuleElement::class.java, ElementSerializer(ModuleElement::class, serializeEnclosingPackages))
    addDefaultSerializer(PackageElement::class.java, ElementSerializer(PackageElement::class, serializeEnclosingPackages))
    addDefaultSerializer(TypeElement::class.java, ElementSerializer(TypeElement::class, serializeEnclosingPackages))
    addDefaultSerializer(TypeParameterElement::class.java, ElementSerializer(TypeParameterElement::class, serializeEnclosingPackages))
    addDefaultSerializer(VariableElement::class.java, ElementSerializer(VariableElement::class, serializeEnclosingPackages))
    addDefaultSerializer(AnnotationMirror::class.java, ImmutableInterfaceSerializer(AnnotationMirror::class))
    addDefaultSerializer(ModuleElement.Directive::class.java, ImmutableInterfaceSerializer(ModuleElement.Directive::class))

    addDefaultSerializer(TypeMirror::class.java, TypeMirrorSerializer(TypeMirror::class))
    addDefaultSerializer(ExecutableType::class.java, TypeMirrorSerializer(ExecutableType::class))
    addDefaultSerializer(IntersectionType::class.java, TypeMirrorSerializer(IntersectionType::class))
    addDefaultSerializer(NoType::class.java, TypeMirrorSerializer(NoType::class))
    addDefaultSerializer(PrimitiveType::class.java, TypeMirrorSerializer(PrimitiveType::class))
    addDefaultSerializer(ReferenceType::class.java, TypeMirrorSerializer(ReferenceType::class))
    addDefaultSerializer(ArrayType::class.java, TypeMirrorSerializer(ArrayType::class))
    addDefaultSerializer(DeclaredType::class.java, DeclaredTypeSerializer(DeclaredType::class, serializeDeclaredTypeAsElement))
    addDefaultSerializer(ErrorType::class.java, DeclaredTypeSerializer(ErrorType::class, serializeDeclaredTypeAsElement))
    addDefaultSerializer(NullType::class.java, TypeMirrorSerializer(NullType::class))
    addDefaultSerializer(TypeVariable::class.java, TypeMirrorSerializer(TypeVariable::class))
    addDefaultSerializer(UnionType::class.java, TypeMirrorSerializer(UnionType::class))
    addDefaultSerializer(WildcardType::class.java, TypeMirrorSerializer(WildcardType::class))

    addDefaultSerializer(Name::class.java, NameSerializer())
    addDefaultSerializer(AnnotationValue::class.java, AnnotationValueSerializer())
}

/**
 * Exception thrown when calling a method on a deserialized object, where the method
 * could not be serialized.
 */
class NotSerializedException : UnsupportedOperationException(
    "This operation is not supported because the object wasn't serialized completely."
)

data class SerializedMsgOverStdout(val content: String) {
    fun print() = MSG_PREFIX + content + MSG_SUFFIX

    init {
        require(content.isNotEmpty())
    }

    companion object {
        private const val MSG_PREFIX = "begin_serialized{"
        private const val MSG_SUFFIX = "}end_serialized"

        fun parseAllIn(s: String): List<SerializedMsgOverStdout> {
            val pattern = Regex(Regex.escape(MSG_PREFIX) + "(.+)?" + Regex.escape(MSG_SUFFIX))
            return pattern.findAll(s)
                .map { match ->
                    SerializedMsgOverStdout(match.destructured.component1())
                }.toList()
        }
    }
}

open class ImmutableInterfaceSerializer<T : Any>(
    protected val clazz: KClass<T>,
    /**
     * Whether exceptions thrown by methods of the object should be serialized
     * as well or rethrown.
     */
    protected val serializeExceptions: Boolean = false
) : Serializer<T>() {

    init {
        require(clazz.java.isInterface)
    }

    private val methods = clazz.java.methods + Object::class.java.methods.filter {
        // the method list doesn't include those inherited by Object so we need
        // to add them manually
        it.name == "hashCode" || it.name == "toString"
    }

    private val noArgMethods = methods.filter { it.parameters.isEmpty() }
    private val argMethods = methods.filter { it.parameters.isNotEmpty() }

    final override fun write(kryo: Kryo, output: Output, obj: T) {
        noArgMethods.forEach {
            try {
                serializeReturnValue(kryo, output, obj, it)
            }
            catch (e: KryoException) {
                throw e.apply { addTrace(kryoExceptionTraceFor(it, obj.javaClass)) }
            } catch (t: Throwable) {
                throw KryoException(t).apply { addTrace(kryoExceptionTraceFor(it, obj.javaClass)) }
            }
        }
    }

    protected open fun serializeReturnValue(kryo: Kryo, output: Output, obj: T, method: Method) {
        try {
            val returnValue = method.invoke(obj)
            serializeReturnValue(kryo, output, obj, method, SerializedReturnValue.Value(returnValue))
        } catch (e: InvocationTargetException) {
            if (e.targetException == null || !serializeExceptions)
                throw e
            else
                serializeReturnValue(kryo, output, obj, method, SerializedReturnValue.Exception(e))
        }
    }

    protected open fun serializeReturnValue(kryo: Kryo, output: Output, obj: T,
                                            method: Method, value: SerializedReturnValue) {
        kryo.writeClassAndObject(output, value)
    }

    final override fun read(kryo: Kryo, input: Input, type: Class<out T>): T {
        val methodValueMap = mutableMapOf<Method, SerializedReturnValue>()
        var initialized = false


        val o = Proxy.newProxyInstance(clazz.java.classLoader, arrayOf(clazz.java)
        ) { proxy, method, args ->
            if (!initialized)
                throw IllegalStateException("Deserialized object not initialized yet." +
                        if(method.name == "toString") "\nProxying object of type: $clazz" else "")

            return@newProxyInstance onProxyMethodCall(proxy as T, method,
                args ?: emptyArray(), methodValueMap[method])
        } as T

        kryo.reference(o)

        noArgMethods.forEach {
            try {
                methodValueMap[it] = deserializeReturnValue(kryo, input, type, it)
            }
            catch (e: KryoException) {
                throw e.apply { addTrace(kryoExceptionTraceFor(it, type)) }
            } catch (t: Throwable) {
                throw KryoException(t).apply { addTrace(kryoExceptionTraceFor(it, type)) }
            }
        }

        initialized = true
        return o
    }

    protected open fun onProxyMethodCall(obj: T, method: Method, args: Array<Any?>,
                                         serializedReturnValue: SerializedReturnValue?): Any? {
        if (args.isNotEmpty())
            throw NotSerializedException()

        if (serializedReturnValue == null)
            throw IllegalStateException(
                "Couldn't get return value for method $method on deserialized interface."
            )

        return when (serializedReturnValue) {
            is SerializedReturnValue.Exception -> throw serializedReturnValue.exception
            is SerializedReturnValue.NotSerialized -> throw NotSerializedException()
            is SerializedReturnValue.Value -> serializedReturnValue.value
        }
    }

    protected open fun deserializeReturnValue(kryo: Kryo, input: Input, type: Class<out T>, method: Method)
            : SerializedReturnValue {
        return kryo.readClassAndObject(input) as SerializedReturnValue
    }


    /**
     * The return value of a method that will be serialized
     */
    protected sealed class SerializedReturnValue : Serializable {
        /** An exception the method to be serialized threw */
        data class Exception(val exception: Throwable) : SerializedReturnValue(), Serializable
        /** The return value of the method to be serialized */
        data class Value(val value: Any?) : SerializedReturnValue(), Serializable
        /** The method won't be serialized */
        object NotSerialized : SerializedReturnValue(), Serializable
    }

    private fun kryoExceptionTraceFor(method: Method, type: Class<*>)
            = method.name + " (" + type.name + ")"
}

/**
 * A generic throwable serializer for exceptions that can not be serialized
 * with other serializers.
 */
class GenericThrowableSerializer : Serializer<Throwable>() {
    override fun write(kryo: Kryo, output: Output, obj: Throwable) = with(kryo) {
        writeClassAndObject(output, obj.cause)
        writeClassAndObject(output, obj.message)
        writeClassAndObject(output, obj.stackTrace)
    }

    @Suppress("LocalVariableName")
    override fun read(kryo: Kryo, input: Input, type: Class<out Throwable>)
        : Throwable = with(kryo) {

        var cause_: Throwable? = null
        var message_: String? = null

        val o = object : Throwable() {
            override val cause: Throwable? = cause_
            override val message: String? = message_
        }

        cause_ = readClassAndObject(input) as Throwable?
        message_ = "Deserialized exception of type: $type with message: " + readClassAndObject(input) as String?
        o.stackTrace = readClassAndObject(input) as Array<StackTraceElement>

        return o
    }
}

/**
 * A serializer factory that returns a special serializer for [com.sun.tools.javac.util.List]
 * because it can not be serialized with the default [CollectionSerializer]. A default
 * serializer for [com.sun.tools.javac.util.List] can not be set by using [Kryo.addDefaultSerializer]
 * because [com.sun.tools.javac.util.List] can not be accessed since it's not exported from the
 * module.
 */
class JavacAwareListSerializerFactory(
    private val listSerializer: Serializer<List<*>> = CollectionSerializer<List<*>>()
) : SerializerFactory<Serializer<List<*>>> {
    private val javacListSerializer = SunJavacListSerializer()

    override fun newSerializer(kryo: Kryo, type: Class<*>): Serializer<List<*>> {
        return if(type.canonicalName == "com.sun.tools.javac.util.List")
            javacListSerializer
        else
            listSerializer
    }

    override fun isSupported(type: Class<*>): Boolean {
        return List::class.java.isAssignableFrom(type)
    }

    /**
     * A serializer for [com.sun.tools.javac.util.List] that simply converts it to a regular
     * Java list.
     */
    private class SunJavacListSerializer : Serializer<List<*>>() {
        override fun write(kryo: Kryo, output: Output, obj: List<*>?) {
            kryo.writeClassAndObject(output, obj?.toList())
        }

        override fun read(kryo: Kryo, input: Input, type: Class<out List<*>>?): List<*> {
            return kryo.readClassAndObject(input) as List<*>
        }
    }
}