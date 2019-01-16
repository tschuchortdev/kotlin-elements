@file:Suppress("UNCHECKED_CAST")

package serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import java.util.*
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

/**
 * Serializers for the package javax.lang.model.element
 */


class ElementSerializer : Serializer<Element>() {
    private val annotConstrSerializer = AnnotatedConstructSerializer()

    override fun write(kryo: Kryo, output: Output, obj: Element) = with(kryo) {
        annotConstrSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.simpleName)
        writeClassAndObject(output, obj.kind)
        //writeClassAndObject(output, obj.enclosingElement)
        writeClassAndObject(output, obj.enclosedElements.toList())
        writeClassAndObject(output, obj.modifiers.toList())
        writeClassAndObject(output, obj.asType())
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out Element>)
        : Element = with(kryo) {

        val annotConstr = annotConstrSerializer.read(kryo, input, type)

        val simpleName = readClassAndObject(input) as Name
        val kind = readClassAndObject(input) as ElementKind
        //val enclosingElement = readClassAndObject(input) as Element?
        val enclosedElems = readClassAndObject(input) as List<Element>
        val modifiers = readClassAndObject(input) as List<Modifier>
        val asType = readClassAndObject(input) as TypeMirror

        return object : Element, AnnotatedConstruct by annotConstr {
            override fun getSimpleName(): Name = simpleName

            override fun getKind(): ElementKind = kind

            override fun getEnclosingElement(): Element? = throw NotSerializedException()

            override fun getEnclosedElements(): List<Element> = enclosedElems

            override fun getModifiers(): Set<Modifier> = modifiers.toSet()

            override fun asType(): TypeMirror = asType

            override fun <R : Any?, P : Any?> accept(v: ElementVisitor<R, P>?, p: P): R {
                throw NotSerializedException()
            }
        }
    }
}

class ExecutableElementSerializer : Serializer<ExecutableElement>() {
    private val elemSerializer = ElementSerializer()

    override fun write(kryo: Kryo, output: Output, obj: ExecutableElement) = with(kryo) {
        elemSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.defaultValue)
        writeClassAndObject(output, obj.returnType)
        writeClassAndObject(output, obj.receiverType)
        writeClassAndObject(output, obj.thrownTypes.toList())
        writeClassAndObject(output, obj.typeParameters.toList())
        writeClassAndObject(output, obj.parameters.toList())
        writeClassAndObject(output, obj.isVarArgs)
        writeClassAndObject(output, obj.isDefault)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out ExecutableElement>)
        : ExecutableElement = with(kryo) {

        val elem = elemSerializer.read(kryo, input, type)

        val defaultVal = readClassAndObject(input) as AnnotationValue?
        val returnType = readClassAndObject(input) as TypeMirror?
        val receiverType = readClassAndObject(input) as TypeMirror?
        val thrownTypes = readClassAndObject(input) as List<TypeMirror>
        val typeParams = readClassAndObject(input) as List<TypeParameterElement>
        val params = readClassAndObject(input) as List<VariableElement>
        val isVarArgs = readClassAndObject(input) as Boolean
        val isDefault = readClassAndObject(input) as Boolean

        return object : ExecutableElement, Element by elem {
            override fun getDefaultValue(): AnnotationValue? = defaultVal

            override fun getReturnType(): TypeMirror? = returnType

            override fun getReceiverType(): TypeMirror? = receiverType

            override fun getThrownTypes(): List<TypeMirror> = thrownTypes

            override fun getTypeParameters(): List<TypeParameterElement> = typeParams

            override fun getParameters(): List<VariableElement> = params

            override fun isVarArgs(): Boolean = isVarArgs

            override fun isDefault(): Boolean = isDefault
        }
    }
}

class ModuleElementSerializer : Serializer<ModuleElement>() {
    private val elemSerializer = ElementSerializer()

    override fun write(kryo: Kryo, output: Output, obj: ModuleElement) = with(kryo) {
        elemSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.isUnnamed)
        writeClassAndObject(output, obj.qualifiedName)
        writeClassAndObject(output, obj.directives.toList())
        writeClassAndObject(output, obj.isOpen)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out ModuleElement>)
        : ModuleElement = with(kryo) {

        val elem = elemSerializer.read(kryo, input, type)

        val isUnnamed = readClassAndObject(input) as Boolean
        val qualName = readClassAndObject(input) as Name
        val directives = readClassAndObject(input) as List<ModuleElement.Directive>
        val isOpen = readClassAndObject(input) as Boolean

        return object : ModuleElement, Element by elem {
            override fun isUnnamed(): Boolean = isUnnamed

            override fun getQualifiedName(): Name = qualName

            override fun getDirectives(): List<ModuleElement.Directive> = directives

            override fun isOpen(): Boolean = isOpen
        }
    }
}

class PackageElementSerializer : Serializer<PackageElement>() {
    private val elemSerializer = ElementSerializer()

    override fun write(kryo: Kryo, output: Output, obj: PackageElement) = with(kryo) {
        elemSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.isUnnamed)
        writeClassAndObject(output, obj.qualifiedName)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out PackageElement>)
        : PackageElement = with(kryo) {

        val elem = elemSerializer.read(kryo, input, type)

        val isUnnamed = readClassAndObject(input) as Boolean
        val qualName = readClassAndObject(input) as Name

        return object : PackageElement, Element by elem {
            override fun isUnnamed(): Boolean = isUnnamed

            override fun getQualifiedName(): Name = qualName
        }
    }
}

class TypeElementSerializer : Serializer<TypeElement>() {
    private val elemSerializer = ElementSerializer()

    override fun write(kryo: Kryo, output: Output, obj: TypeElement) = with(kryo) {
        elemSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.superclass)
        writeClassAndObject(output, obj.typeParameters.toList())
        writeClassAndObject(output, obj.qualifiedName)
        writeClassAndObject(output, obj.interfaces.toList())
        writeClassAndObject(output, obj.nestingKind)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out TypeElement>)
        : TypeElement = with(kryo) {

        val elem = elemSerializer.read(kryo, input, type)

        val superclass = readClassAndObject(input) as TypeMirror
        val typeParams = readClassAndObject(input) as List<TypeParameterElement>
        val qualName = readClassAndObject(input) as Name
        val interfaces = readClassAndObject(input) as List<TypeMirror>
        val nestingKind = readClassAndObject(input) as NestingKind

        return object : TypeElement, Element by elem {
            override fun getSuperclass(): TypeMirror = superclass

            override fun getTypeParameters(): List<TypeParameterElement> = typeParams

            override fun getQualifiedName(): Name = qualName

            override fun getInterfaces(): List<TypeMirror> = interfaces

            override fun getNestingKind(): NestingKind = nestingKind
        }
    }
}

class TypeParameterElementSerializer : Serializer<TypeParameterElement>() {
    private val elemSerializer = ElementSerializer()

    override fun write(kryo: Kryo, output: Output, obj: TypeParameterElement) = with(kryo) {
        elemSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.bounds.toList())
        writeClassAndObject(output, obj.genericElement)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out TypeParameterElement>)
        : TypeParameterElement = with(kryo) {

        val elem = elemSerializer.read(kryo, input, type)

        val bounds = readClassAndObject(input) as List<TypeMirror>
        val genericElem = readClassAndObject(input) as Element

        return object : TypeParameterElement, Element by elem {
            override fun getBounds(): List<TypeMirror> = bounds

            override fun getGenericElement(): Element = genericElem
        }
    }
}

class VariableElementSerializer : Serializer<VariableElement>() {
    private val elemSerializer = ElementSerializer()

    override fun write(kryo: Kryo, output: Output, obj: VariableElement) = with(kryo) {
        elemSerializer.write(kryo, output, obj)

        writeClassAndObject(output, obj.constantValue)
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out VariableElement>)
        : VariableElement = with(kryo) {

        val elem = elemSerializer.read(kryo, input, type)

        val constantVal = readClassAndObject(input) as Any?

        return object : VariableElement, Element by elem {
            override fun getConstantValue(): Any? = constantVal
        }
    }
}

class AnnotationMirrorSerializer : Serializer<AnnotationMirror>() {
    override fun write(kryo: Kryo, output: Output, obj: AnnotationMirror) = with(kryo) {
        writeClassAndObject(output, obj.annotationType)
        writeClassAndObject(output, obj.elementValues.toMap())
        writeClassAndObject(output, obj.toString())
        writeClassAndObject(output, obj.hashCode())
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out AnnotationMirror>)
        : AnnotationMirror = with(kryo) {

        val annoType = readClassAndObject(input) as DeclaredType
        val elemValues = readClassAndObject(input) as Map<ExecutableElement, AnnotationValue>
        val toString = readClassAndObject(input) as String
        val hashCode = readClassAndObject(input) as Int

        return object : AnnotationMirror {
            override fun getAnnotationType(): DeclaredType = annoType

            override fun getElementValues(): Map<ExecutableElement, AnnotationValue> = elemValues

            override fun toString(): String = toString

            override fun hashCode(): Int = hashCode

            override fun equals(other: Any?): Boolean {
                return other.toString() == toString() && other.hashCode() == hashCode()
            }
        }
    }
}

class AnnotationValueSerializer : Serializer<AnnotationValue>() {
    override fun write(kryo: Kryo, output: Output, obj: AnnotationValue) = with(kryo) {
        writeClassAndObject(output, obj.value)
        writeClassAndObject(output, obj.toString())
        writeClassAndObject(output, obj.hashCode())
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out AnnotationValue>)
        : AnnotationValue = with(kryo) {

        val value = readClassAndObject(input) as Any?
        val toString = readClassAndObject(input) as String
        val hashCode = readClassAndObject(input) as Int

        return object : AnnotationValue {
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
    }
}

class NameSerializer : Serializer<Name>() {
    override fun write(kryo: Kryo, output: Output, obj: Name) = with(kryo) {
        writeClassAndObject(output, obj.hashCode())
        writeClassAndObject(output, obj.toString())
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out Name>)
        : Name = with(kryo) {

        val hashCode = readClassAndObject(input) as Int
        val toString = readClassAndObject(input) as String

        return object : Name {
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
    }
}

class DirectiveSerializer : Serializer<ModuleElement.Directive>() {
    override fun write(kryo: Kryo, output: Output, obj: ModuleElement.Directive) = with(kryo) {
        writeClassAndObject(output, obj.kind)
        writeClassAndObject(output, obj.toString())
        writeClassAndObject(output, obj.hashCode())

        when(obj.kind) {
            ModuleElement.DirectiveKind.EXPORTS -> {
                writeClassAndObject(output, (obj as ModuleElement.ExportsDirective).`package`)
                writeClassAndObject(output, obj.targetModules.toList())
            }
            ModuleElement.DirectiveKind.OPENS -> {
                writeClassAndObject(output, (obj as ModuleElement.OpensDirective).`package`)
                writeClassAndObject(output, obj.targetModules.toList())
            }
            ModuleElement.DirectiveKind.PROVIDES -> {
                writeClassAndObject(output, (obj as ModuleElement.ProvidesDirective).implementations.toList())
                writeClassAndObject(output, obj.service)
            }
            ModuleElement.DirectiveKind.REQUIRES -> {
                writeClassAndObject(output, (obj as ModuleElement.RequiresDirective).isStatic)
                writeClassAndObject(output, obj.isTransitive)
                writeClassAndObject(output, obj.dependency)
            }
            ModuleElement.DirectiveKind.USES -> {
                writeClassAndObject(output, (obj as ModuleElement.UsesDirective).service)
            }
            null -> {}
        }
    }

    override fun read(kryo: Kryo, input: Input, type: Class<out ModuleElement.Directive>)
        : ModuleElement.Directive = with(kryo) {

        val kind = readClassAndObject(input) as ModuleElement.DirectiveKind?
        val toString = readClassAndObject(input) as String
        val hashCode = readClassAndObject(input) as Int

        val directiveBase = object : ModuleElement.Directive {
            override fun getKind(): ModuleElement.DirectiveKind? = kind

            override fun <R : Any?, P : Any?> accept(v: ModuleElement.DirectiveVisitor<R, P>?, p: P): R {
                throw NotSerializedException()
            }

            override fun toString(): String = toString

            override fun hashCode(): Int = hashCode

            override fun equals(other: Any?): Boolean {
                return other.toString() == toString() && other.hashCode() == hashCode()
            }
        }

        return when(kind) {
            ModuleElement.DirectiveKind.EXPORTS -> object : ModuleElement.ExportsDirective,
                ModuleElement.Directive by directiveBase {
                private val pack = readClassAndObject(input) as PackageElement
                private val targetModules = readClassAndObject(input) as List<ModuleElement>

                override fun getPackage(): PackageElement = pack

                override fun getTargetModules(): List<ModuleElement> = targetModules
            }

            ModuleElement.DirectiveKind.OPENS -> object : ModuleElement.OpensDirective,
                ModuleElement.Directive by directiveBase {
                private val pack = readClassAndObject(input) as PackageElement
                private val targetModules = readClassAndObject(input) as List<ModuleElement>

                override fun getPackage(): PackageElement = pack

                override fun getTargetModules(): List<ModuleElement> = targetModules
            }

            ModuleElement.DirectiveKind.PROVIDES -> object : ModuleElement.ProvidesDirective,
                ModuleElement.Directive by directiveBase {
                private val impls = readClassAndObject(input) as List<TypeElement>
                private val service = readClassAndObject(input) as TypeElement

                override fun getImplementations(): List<TypeElement> = impls

                override fun getService(): TypeElement = service
            }

            ModuleElement.DirectiveKind.REQUIRES -> object : ModuleElement.RequiresDirective,
                ModuleElement.Directive by directiveBase {
                private val isStatic = readClassAndObject(input) as Boolean
                private val isTransitive = readClassAndObject(input) as Boolean
                private val dependency = readClassAndObject(input) as ModuleElement

                override fun isStatic(): Boolean = isStatic

                override fun isTransitive(): Boolean = isTransitive

                override fun getDependency(): ModuleElement = dependency

            }

            ModuleElement.DirectiveKind.USES -> object : ModuleElement.UsesDirective,
                ModuleElement.Directive by directiveBase {
                private val service = readClassAndObject(input) as TypeElement

                override fun getService(): TypeElement = service
            }

            null -> directiveBase
        }
    }
}

