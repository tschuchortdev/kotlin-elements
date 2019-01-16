package serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.tschuchort.kotlinelements.KotlinElement
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Name
import javax.lang.model.type.TypeMirror

class KotlinElementSerializer : Serializer<KotlinElement>() {
    val nameSerializer = NameSerializer()
    override fun write(kryo: Kryo, output: Output, obj: KotlinElement) = with(kryo) {
        nameSerializer.write(kryo, output, obj.simpleName)
        writeClassAndObject(output, obj.toString())
        writeClassAndObject(output, obj.hashCode())

        println("write KotlinElement")
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out KotlinElement>)
        : KotlinElement = with(kryo) {

        val name = nameSerializer.read(kryo, input, Name::class.java)
        val toString = readClassAndObject(input) as String
        val hashCode = readClassAndObject(input) as Int

        return object : KotlinElement() {
            override val enclosingElement: KotlinElement?
                get() = throw NotSerializedException()

            override val simpleName: Name
                get() = name

            override fun asType(): TypeMirror {
                throw NotSerializedException()
            }

            override fun toString(): String = toString

            override fun equals(other: Any?): Boolean {
                throw NotSerializedException()
            }

            override fun hashCode(): Int = hashCode

            override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>?): Array<A> {
                throw NotSerializedException()
            }

            override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A {
                throw NotSerializedException()
            }

            override fun getAnnotationMirrors(): MutableList<out AnnotationMirror> {
                throw NotSerializedException()
            }

        }
    }
}