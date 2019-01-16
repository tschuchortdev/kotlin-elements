package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.jvm.getJvmConstructorSignature
import me.eugeniomarletti.kotlin.metadata.jvm.getJvmMethodSignature
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.lang.model.element.Element

/**
 * Exception thrown when a JVM-[Element] that originates from Kotlin could not be translated
 * to its corresponding [KotlinRelatedElement] or when the corresponding JVM-[Element] to
 * a ProtoBuf element extracted from a metadata annotation could not be found
 */
class KotlinElementConversionException private constructor(
		processedElement: ProcessedElement, cause: Exception
) : RuntimeException("Could not translate element to corresponding Kotlin element: $processedElement", cause) {

	constructor(javaElement: Element, cause: Exception)
			: this(ProcessedElement.JavaElement(javaElement), cause)

	constructor(protoBufClass: ProtoBuf.Class, nameResolver: NameResolver, cause: Exception)
			: this(ProcessedElement.ProtoBufClass(protoBufClass, nameResolver), cause)

	constructor(protoBufTypeAlias: ProtoBuf.TypeAlias, nameResolver: NameResolver, cause: Exception)
			: this(ProcessedElement.ProtoBufTypeAlias(protoBufTypeAlias, nameResolver), cause)

	constructor(protoBufConstructor: ProtoBuf.Constructor, nameResolver: NameResolver,
				typeTable: ProtoBuf.TypeTable, cause: Exception)
			: this(ProcessedElement.ProtoBufConstructor(protoBufConstructor, nameResolver, typeTable), cause)

	constructor(protoBufFunction: ProtoBuf.Function, nameResolver: NameResolver,
				typeTable: ProtoBuf.TypeTable, cause: Exception)
			: this(ProcessedElement.ProtoBufFunction(protoBufFunction, nameResolver, typeTable), cause)

	constructor(protoBufProperty: ProtoBuf.Property, nameResolver: NameResolver,
				typeTable: ProtoBuf.TypeTable, cause: Exception)
			: this(ProcessedElement.ProtoBufProperty(protoBufProperty, nameResolver, typeTable), cause)

	sealed class ProcessedElement {
		data class JavaElement(val javaElement: Element) : ProcessedElement()

		class ProtoBufClass(val protoClass: ProtoBuf.Class, nameResolver: NameResolver) : ProcessedElement() {
			operator fun component1() = protoClass

			private val name = nameResolver.getString(protoClass.fqName)

			override fun toString(): String = name
		}

		class ProtoBufTypeAlias(val protoTypeAlias: ProtoBuf.TypeAlias, nameResolver: NameResolver) : ProcessedElement() {
			operator fun component1() = protoTypeAlias

			private val name = nameResolver.getString(protoTypeAlias.name)

			override fun toString(): String = name
		}

		class ProtoBufConstructor(val protoCtor: ProtoBuf.Constructor,
								  nameResolver: NameResolver,
								  typeTable: ProtoBuf.TypeTable) : ProcessedElement() {
			operator fun component1() = protoCtor

			private val signature = protoCtor.getJvmConstructorSignature(nameResolver, typeTable) ?: "null"

			override fun toString(): String = signature
		}

		class ProtoBufFunction(val protoFunction: ProtoBuf.Function,
							   nameResolver: NameResolver,
							   typeTable: ProtoBuf.TypeTable) : ProcessedElement() {
			operator fun component1() = protoFunction

			private val signature = protoFunction.getJvmMethodSignature(nameResolver, typeTable) ?: "null"

			override fun toString(): String = signature
		}

		class ProtoBufProperty(val protoProperty: ProtoBuf.Property,
							   nameResolver: NameResolver,
							   typeTable: ProtoBuf.TypeTable) : ProcessedElement() {
			operator fun component1() = protoProperty

			private val name = nameResolver.getString(protoProperty.name)

			override fun toString(): String = name
		}

		abstract override fun toString(): String
	}
}