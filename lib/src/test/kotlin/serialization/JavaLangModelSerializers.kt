@file:Suppress("UNCHECKED_CAST", "JoinDeclarationAndAssignment")

package serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.lang.reflect.Method
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaMethod

/**
 * Serializers for the package javax.lang.model
 */

open class ElementSerializer<T : Element>(
    /** The class to be serialized */
    clazz: KClass<T>,
    /**
     * Whether the [Element.getEnclosingElement] method should be serialized
     * if it's a package.
     * Disabling this can be useful for performance reasons.
     */
    private val serializeEnclosingPackage: Boolean = true
) : ImmutableInterfaceSerializer<T>(clazz, true) {

    override fun serializeReturnValue(
        kryo: Kryo, output: Output, obj: T,
        method: Method, value: SerializedReturnValue
    ) {
        // don't serialize packages for performance reasons
        if(!serializeEnclosingPackage && method.name == Element::getEnclosingElement.name
                && obj.enclosingElement is PackageElement)
            super.serializeReturnValue(kryo, output, obj, method, SerializedReturnValue.NotSerialized)
        else
            super.serializeReturnValue(kryo, output, obj, method, value)
    }

    override fun onProxyMethodCall(
        obj: T, method: Method, args: Array<Any?>,
        serializedReturnValue: SerializedReturnValue?
    ): Any? {
        if(Element::equals.javaMethod == method)
            return args[0].hashCode() == obj.hashCode()
                    && (args[0] as Element).simpleName == obj.simpleName

        return super.onProxyMethodCall(obj, method, args, serializedReturnValue)
    }
}

open class TypeMirrorSerializer<T : TypeMirror>(clazz: KClass<T>)
    : ImmutableInterfaceSerializer<T>(clazz, true) {

    override fun onProxyMethodCall(
        obj: T, method: Method, args: Array<Any?>,
        serializedReturnValue: SerializedReturnValue?
    ): Any? {
        if(Element::equals.javaMethod == method)
            return args[0].hashCode() == obj.hashCode()
                    && (args[0] as TypeMirror).toString() == obj.toString()

        return super.onProxyMethodCall(obj, method, args, serializedReturnValue)
    }
}

open class DeclaredTypeSerializer<T : DeclaredType>(
    /** The class to be serialized */
    clazz: KClass<T>,
    /**
     * Whether the [DeclaredType.asElement] method should be serialized.
     * Disabling this can be useful for performance reasons.
     */
    private val serializeAsElement: Boolean = true
) : TypeMirrorSerializer<T>(clazz) {

    override fun serializeReturnValue(
        kryo: Kryo, output: Output, obj: T,
        method: Method, value: SerializedReturnValue
    ) {
        // don't serialize element for performance reasons
        if(!serializeAsElement && method.name == DeclaredType::asElement.name)
            super.serializeReturnValue(kryo, output, obj, method, SerializedReturnValue.NotSerialized)
        else
            super.serializeReturnValue(kryo, output, obj, method, value)
    }
}

class AnnotationValueSerializer : Serializer<AnnotationValue>() {
    override fun write(kryo: Kryo, output: Output, obj: AnnotationValue) = with(kryo) {
        if(obj.value is List<*>)
            writeClassAndObject(output, (obj.value as List<*>).toList())
        else
            writeClassAndObject(output, obj.value)

        writeClassAndObject(output, obj.toString())
        writeClassAndObject(output, obj.hashCode())
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out AnnotationValue>)
        : AnnotationValue = with(kryo) {

        var value: Any? = null
        lateinit var toString: String
        var hashCode: Int = 0

        val o = object : AnnotationValue {
            override fun <R : Any?, P : Any?> accept(v: AnnotationValueVisitor<R, P>?, p: P): R {
                throw NotSerializedException()
            }

            override fun getValue(): Any? = value

            override fun toString(): String = toString

            override fun hashCode(): Int = hashCode

            override fun equals(other: Any?): Boolean {
                return other.toString() == toString() && other.hashCode() == hashCode()
            }
        }

        reference(o)

        value = readClassAndObject(input) as Any?
        toString = readClassAndObject(input) as String
        hashCode = readClassAndObject(input) as Int

        return o
    }
}

class NameSerializer : Serializer<Name>() {
    override fun write(kryo: Kryo, output: Output, obj: Name) = with(kryo) {
        writeClassAndObject(output, obj.hashCode())
        writeClassAndObject(output, obj.toString())
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out Name>)
        : Name = with(kryo) {

        var hashCode: Int = 0
        lateinit var toString: String

        val o = object : Name {
            override fun get(index: Int): Char = toString[index]

            override fun contentEquals(cs: CharSequence?): Boolean {
                return if(cs != null)
                    toString.contentEquals(cs)
                else
                    false
            }

            override val length: Int
                get() = toString.length

            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence
                    = toString.subSequence(startIndex, endIndex)

            override fun toString(): String = toString

            override fun hashCode(): Int = hashCode

            override fun equals(other: Any?): Boolean {
                throw NotSerializedException()
            }
        }

        reference(o)

        hashCode = readClassAndObject(input) as Int
        toString = readClassAndObject(input) as String

        return o
    }
}

