@file:Suppress("UNCHECKED_CAST")

package serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.Element
import javax.lang.model.type.*

/**
 * Serializers for package javax.lang.model.type
 */

class TypeMirrorSerializer : Serializer<TypeMirror>() {
    private val annotConstrSerializer = AnnotatedConstructSerializer()

    override fun write(kryo: Kryo, output: Output, obj: TypeMirror) = with(kryo) {
        annotConstrSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.kind)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out TypeMirror>)
        : TypeMirror = with(kryo) {

        val annotConstr = annotConstrSerializer.read(kryo, input, type)
        val kind = readClassAndObject(input) as TypeKind

        return object : TypeMirror, AnnotatedConstruct by annotConstr {
            override fun getKind(): TypeKind = kind

            override fun <R : Any?, P : Any?> accept(v: TypeVisitor<R, P>?, p: P): R {
                throw NotSerializedException()
            }
        }
    }
}

class ExecutableTypeSerializer : Serializer<ExecutableType>() {
    private val typeMirrorSerializer = TypeMirrorSerializer()

    override fun write(kryo: Kryo, output: Output, obj: ExecutableType) = with(kryo) {
        typeMirrorSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.returnType)
        writeClassAndObject(output, obj.receiverType)
        writeClassAndObject(output, obj.thrownTypes.toList())
        writeClassAndObject(output, obj.parameterTypes.toList())
        writeClassAndObject(output, obj.typeVariables.toList())
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out ExecutableType>)
        : ExecutableType = with(kryo) {

        val typeMirror = typeMirrorSerializer.read(kryo, input, type)

        val returnType = readClassAndObject(input) as TypeMirror?
        val receiverType = readClassAndObject(input) as TypeMirror?
        val thrownTypes = readClassAndObject(input) as List<TypeMirror>
        val parameterTypes = readClassAndObject(input) as List<TypeMirror>
        val typeVariables = readClassAndObject(input) as List<TypeVariable>

        return object : ExecutableType, TypeMirror by typeMirror {
            override fun getReturnType(): TypeMirror? = returnType

            override fun getReceiverType(): TypeMirror? = receiverType

            override fun getThrownTypes(): List<TypeMirror> = thrownTypes

            override fun getParameterTypes(): List<TypeMirror> = parameterTypes

            override fun getTypeVariables(): List<TypeVariable> = typeVariables
        }
    }
}

class IntersectionTypeSerializer : Serializer<IntersectionType>() {
    private val typeMirrorSerializer = TypeMirrorSerializer()

    override fun write(kryo: Kryo, output: Output, obj: IntersectionType) = with(kryo) {
        typeMirrorSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.bounds.toList())
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out IntersectionType>)
            : IntersectionType = with(kryo) {

        val typeMirror = typeMirrorSerializer.read(kryo, input, type)

        val bounds = readClassAndObject(input) as List<TypeMirror>

        return object : IntersectionType, TypeMirror by typeMirror {
            override fun getBounds(): List<TypeMirror> = bounds
        }
    }
}


class NoTypeSerializer : Serializer<NoType>() {
    private val typeMirrorSerializer = TypeMirrorSerializer()

    override fun write(kryo: Kryo, output: Output, obj: NoType) = with(kryo) {
        typeMirrorSerializer.write(kryo, output, obj)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out NoType>)
            : NoType = with(kryo) {

        val typeMirror = typeMirrorSerializer.read(kryo, input, type)

        return object : NoType, TypeMirror by typeMirror {
        }
    }
}

class PrimitiveTypeSerializer : Serializer<PrimitiveType>() {
    private val typeMirrorSerializer = TypeMirrorSerializer()

    override fun write(kryo: Kryo, output: Output, obj: PrimitiveType) = with(kryo) {
        typeMirrorSerializer.write(kryo, output, obj)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out PrimitiveType>)
            : PrimitiveType = with(kryo) {

        val typeMirror = typeMirrorSerializer.read(kryo, input, type)

        return object : PrimitiveType, TypeMirror by typeMirror {
        }
    }
}

class ReferenceTypeSerializer : Serializer<ReferenceType>() {
    private val typeMirrorSerializer = TypeMirrorSerializer()

    override fun write(kryo: Kryo, output: Output, obj: ReferenceType) = with(kryo) {
        typeMirrorSerializer.write(kryo, output, obj)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out ReferenceType>)
            : ReferenceType = with(kryo) {

        val typeMirror = typeMirrorSerializer.read(kryo, input, type)

        return object : ReferenceType, TypeMirror by typeMirror {
        }
    }
}

class ArrayTypeSerializer : Serializer<ArrayType>() {
    private val referenceTypeSerializer = ReferenceTypeSerializer()

    override fun write(kryo: Kryo, output: Output, obj: ArrayType) = with(kryo) {
        referenceTypeSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.componentType)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out ArrayType>)
            : ArrayType = with(kryo) {

        val referenceType = referenceTypeSerializer.read(kryo, input, type)

        val componentType = readClassAndObject(input) as TypeMirror

        return object : ArrayType, ReferenceType by referenceType {
            override fun getComponentType(): TypeMirror = componentType
        }
    }
}

class DeclaredTypeSerializer : Serializer<DeclaredType>() {
    private val referenceTypeSerializer = ReferenceTypeSerializer()

    override fun write(kryo: Kryo, output: Output, obj: DeclaredType) = with(kryo) {
        referenceTypeSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.typeArguments.toList())
        //writeClassAndObject(output, obj.asElement())
        writeClassAndObject(output, obj.enclosingType)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out DeclaredType>)
            : DeclaredType = with(kryo) {

        val referenceType = referenceTypeSerializer.read(kryo, input, type)

        val typeArgs = readClassAndObject(input) as List<TypeMirror>
        //val asElement = readClassAndObject(input) as Element
        val enclosingType = readClassAndObject(input) as TypeMirror

        return object : DeclaredType, ReferenceType by referenceType {
            override fun getTypeArguments(): List<TypeMirror> = typeArgs

            override fun asElement(): Element = throw NotSerializedException()

            override fun getEnclosingType(): TypeMirror = enclosingType
        }
    }
}

class ErrorTypeSerializer : Serializer<ErrorType>() {
    private val declaredTypeSerializer = DeclaredTypeSerializer()

    override fun write(kryo: Kryo, output: Output, obj: ErrorType) = with(kryo) {
        declaredTypeSerializer.write(kryo, output, obj)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out ErrorType>)
            : ErrorType = with(kryo) {

        val declaredType = declaredTypeSerializer.read(kryo, input, type)

        return object : ErrorType, DeclaredType by declaredType {
        }
    }
}

class NullTypeSerializer : Serializer<NullType>() {
    private val referenceTypeSerializer = ReferenceTypeSerializer()

    override fun write(kryo: Kryo, output: Output, obj: NullType) = with(kryo) {
       referenceTypeSerializer.write(kryo, output, obj)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out NullType>)
            : NullType = with(kryo) {

        val referenceType = referenceTypeSerializer.read(kryo, input, type)

        return object : NullType, ReferenceType by referenceType {
        }
    }
}

class TypeVariableSerializer : Serializer<TypeVariable>() {
    private val referenceTypeSerializer = ReferenceTypeSerializer()

    override fun write(kryo: Kryo, output: Output, obj: TypeVariable) = with(kryo) {
       referenceTypeSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.upperBound)
        writeClassAndObject(output, obj.lowerBound)
        writeClassAndObject(output, obj.asElement())
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out TypeVariable>)
            : TypeVariable = with(kryo) {

        val referenceType = referenceTypeSerializer.read(kryo, input, type)

        val upperBound = readClassAndObject(input) as TypeMirror
        val lowerBound = readClassAndObject(input) as TypeMirror
        val asElement = readClassAndObject(input) as Element

        return object : TypeVariable, ReferenceType by referenceType {
            override fun getUpperBound(): TypeMirror = upperBound

            override fun getLowerBound(): TypeMirror = lowerBound

            override fun asElement(): Element = asElement
        }
    }
}

class UnionTypeSerializer : Serializer<UnionType>() {
    private val typeMirrorSerializer = TypeMirrorSerializer()

    override fun write(kryo: Kryo, output: Output, obj: UnionType) = with(kryo) {
        typeMirrorSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.alternatives.toList())
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out UnionType>)
            : UnionType = with(kryo) {

        val typeMirror = typeMirrorSerializer.read(kryo, input, type)

        val alternatives = readClassAndObject(input) as List<TypeMirror>

        return object : UnionType, TypeMirror by typeMirror {
            override fun getAlternatives(): List<TypeMirror> = alternatives
        }
    }
}

class WildcardTypeSerializer : Serializer<WildcardType>() {
    private val typeMirrorSerializer = TypeMirrorSerializer()

    override fun write(kryo: Kryo, output: Output, obj: WildcardType) = with(kryo) {
        typeMirrorSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.superBound)
        writeClassAndObject(output, obj.extendsBound)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out WildcardType>)
            : WildcardType = with(kryo) {

        val typeMirror = typeMirrorSerializer.read(kryo, input, type)

        val superBound = readClassAndObject(input) as TypeMirror
        val extendsBound = readClassAndObject(input) as TypeMirror

        return object : WildcardType, TypeMirror by typeMirror {
            override fun getSuperBound(): TypeMirror = superBound

            override fun getExtendsBound(): TypeMirror = extendsBound
        }
    }
}