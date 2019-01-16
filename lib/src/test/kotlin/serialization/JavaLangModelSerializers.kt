package serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.AnnotationMirror

/**
 * Serializers for the package javax.lang.model
 */

@Suppress("UNCHECKED_CAST")
class AnnotatedConstructSerializer : Serializer<AnnotatedConstruct>() {
    override fun write(kryo: Kryo, output: Output, obj: AnnotatedConstruct) = with(kryo) {
        writeClassAndObject(output, obj.annotationMirrors.toList())
        writeClassAndObject(output, obj.toString())
        writeClassAndObject(output, obj.hashCode())
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out AnnotatedConstruct>)
            : AnnotatedConstruct = with(kryo) {

        val annotationMirrors = readClassAndObject(input) as List<AnnotationMirror>
        val toString = readClassAndObject(input) as String
        val hashCode = readClassAndObject(input) as Int

        return object : AnnotatedConstruct {
            override fun getAnnotationMirrors(): List<AnnotationMirror> = annotationMirrors

            override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A {
                throw NotSerializedException()
            }

            override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>?): Array<A> {
                throw NotSerializedException()
            }

            override fun toString(): String = toString
            override fun hashCode(): Int = hashCode

            override fun equals(other: Any?): Boolean {
                return other.hashCode() == hashCode() && other.toString() == toString()
            }
        }
    }
}