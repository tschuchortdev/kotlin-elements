package serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy
import com.esotericsoftware.minlog.Log
import com.tschuchort.kotlinelements.KotlinElement
import de.javakaffee.kryoserializers.*
import org.objenesis.strategy.StdInstantiatorStrategy
import java.lang.reflect.InvocationHandler
import java.util.*
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*
import javax.lang.model.type.*

fun getKryo(): Kryo {
    Log.TRACE()

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

        addDefaultSerializer(AnnotatedConstruct::class.java, AnnotatedConstructSerializer())

        addDefaultSerializer(Element::class.java, ElementSerializer())
        addDefaultSerializer(ExecutableElement::class.java, ExecutableElementSerializer())
        addDefaultSerializer(ModuleElement::class.java, ModuleElementSerializer())
        addDefaultSerializer(PackageElement::class.java, PackageElementSerializer())
        addDefaultSerializer(TypeElement::class.java, TypeElementSerializer())
        addDefaultSerializer(TypeParameterElement::class.java, TypeParameterElementSerializer())
        addDefaultSerializer(VariableElement::class.java, VariableElementSerializer())
        addDefaultSerializer(AnnotationMirror::class.java, AnnotationMirrorSerializer())
        addDefaultSerializer(AnnotationValue::class.java, AnnotationValueSerializer())
        addDefaultSerializer(Name::class.java, NameSerializer())
        addDefaultSerializer(ModuleElement.Directive::class.java, DirectiveSerializer())

        addDefaultSerializer(TypeMirror::class.java, TypeMirrorSerializer())
        addDefaultSerializer(ExecutableType::class.java, ExecutableTypeSerializer())
        addDefaultSerializer(IntersectionType::class.java, IntersectionTypeSerializer())
        addDefaultSerializer(NoType::class.java, NoTypeSerializer())
        addDefaultSerializer(PrimitiveType::class.java, PrimitiveTypeSerializer())
        addDefaultSerializer(ReferenceType::class.java, ReferenceTypeSerializer())
        addDefaultSerializer(ArrayType::class.java, ArrayTypeSerializer())
        addDefaultSerializer(DeclaredType::class.java, DeclaredTypeSerializer())
        addDefaultSerializer(ErrorType::class.java, ErrorTypeSerializer())
        addDefaultSerializer(NullType::class.java, NullTypeSerializer())
        addDefaultSerializer(TypeVariable::class.java, TypeVariableSerializer())
        addDefaultSerializer(UnionType::class.java, UnionTypeSerializer())
        addDefaultSerializer(WildcardType::class.java, WildcardTypeSerializer())

        addDefaultSerializer(KotlinElement::class.java, KotlinElementSerializer())
    }
}

class NotSerializedException : UnsupportedOperationException(
    "This operation is not supported because the object couldn't be serialized completely."
)

data class SerializedMessage(val content: String) {
    fun print() = MSG_PREFIX + content + MSG_SUFFIX

    init {
        require(content.isNotEmpty())
    }

    companion object {
        private const val MSG_PREFIX = "begin_serialized{"
        private const val MSG_SUFFIX = "}end_serialized"

        fun parseAllIn(s: String): List<SerializedMessage> {
            val pattern = Regex(Regex.escape(MSG_PREFIX) + "(.+)?" + Regex.escape(MSG_SUFFIX))
            return pattern.findAll(s)
                .map { match ->
                    SerializedMessage(match.destructured.component1())
                }.toList()
        }
    }
}