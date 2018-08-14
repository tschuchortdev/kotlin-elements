package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

open class KotlinTypeElement internal constructor(
		private val element: TypeElement,
		metadata: KotlinClassMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinElement(element, processingEnv), TypeElement {

	protected val protoClass: ProtoBuf.Class = metadata.data.classProto
	protected val protoNameResolver: NameResolver = metadata.data.nameResolver

	val packageName: String = protoNameResolver.getString(protoClass.fqName).substringBeforeLast('/').replace('/', '.')

	//TODO("replace Visibility with own enum")
	val visibility: ProtoBuf.Visibility = protoClass.visibility!!

	/**
	 * whether this is a (possibly anonymous) singleton class of the kind denoted by the `object` keyword
	 */
	val isObject: Boolean = protoClass.classKind == ProtoBuf.Class.Kind.OBJECT

	/**
	 * Whether or not the class is an inner class
	 *
	 * A class is an inner class if it has the keyword `inner` i.e. nested classes are not
	 * necessarily inner classes
	 */
	val isInnerClass: Boolean = protoClass.isInnerClass

	//TODO(docs)
	val isExpectClass: Boolean = protoClass.isExpectClass
	val isExternalClass: Boolean = protoClass.isExternalClass

	val isDataClass: Boolean = protoClass.isDataClass

	/**
	 * class modality
	 * one of: [ProtoBuf.Modality.FINAL], [ProtoBuf.Modality.OPEN],
	 * [ProtoBuf.Modality.ABSTRACT], [ProtoBuf.Modality.SEALED]
	 * //TODO("replace Modality with custom enum")
	 */
	val modality: ProtoBuf.Modality = protoClass.modality!!

	/**
	 * the companion object of this element if it has one
	 */
	val companionObject: KotlinElement? by lazy {
		getCompanionSimpleName()?.let { companionSimpleName ->
			val matchingChildElements = element.enclosedElements.filter {
						it.kind == ElementKind.CLASS
						&& (it as TypeElement).simpleName.toString() == companionSimpleName
			}

			when(matchingChildElements.size) {
				1 -> {
					val companionElement = matchingChildElements.single() as TypeElement

					KotlinTypeElement.get(companionElement, processingEnv)
					?: throw IllegalStateException("could not construct KotlinTypeElement from companion " +
												   "TypeElement \"$companionElement\"")
				}

				0 -> throw IllegalStateException(
						"no enclosed element of the parent element \"$element\" matches kind and " +
						"qualified name of the companion object even though the metadata ProtoBuf.Class " +
						"indicated that one exists with the simple name \"$companionSimpleName\"")

				else -> throw IllegalStateException(
						"more than one enclosed element of the parent element \"$element\" matches kind and" +
						"simple name of the companion object.\n" +
						"	name: \"$companionSimpleName\"\n" +
						"	elements: ${matchingChildElements.joinToString(", ")}")
			}
		}
	}


	init {
		val fqName: String = protoNameResolver.getString(protoClass.fqName).replace('/', '.')
		val elementQualifiedName = element.qualifiedName.toString()

		if(fqName != elementQualifiedName) {
			throw IllegalStateException(
					"fully qualified name of Proto.Class and TypeElement don't match:\n" +
					"	Proto.Class fqName: $fqName\n" +
					"	TypeElement::qualifiedName: $elementQualifiedName")
		}
	}

	companion object {
		fun get(element: TypeElement, processingEnv: ProcessingEnvironment): KotlinTypeElement?
				= if(element is KotlinTypeElement)
					element
				else
					(element.kotlinMetadata as? KotlinClassMetadata)?.let { metadata ->
						KotlinTypeElement(element, metadata, processingEnv)
					}
	}

	private fun getCompanionSimpleName(): String? =
			if (protoClass.hasCompanionObjectName())
				protoNameResolver.getString(protoClass.companionObjectName)
			else
				null

	//TODO(return kotlin superclass)
	override fun getSuperclass(): TypeMirror = element.superclass

	override fun getTypeParameters(): List<KotlinTypeParameterElement>
			= element.typeParameters
			.map { typeParamElem ->
				getKotlinTypeParameter(typeParamElem)
				?: throw IllegalStateException(
						"Could not find matching ProtoBuf.TypeParameter for TypeParameterElement" +
						" \"$typeParamElem\" which is a sub-element of \"$this\"")
			}

	/**
	 * returns a [KotlinTypeParameterElement] for this [TypeParameterElement] if it's a type parameter
	 * of this class or null otherwise
	 *
	 * this function is mostly necessary to be used by [KotlinTypeParameterElement.get] because only the
	 * enclosing class has enough information to create the [KotlinTypeParameterElement] and the factory
	 * function alone can not do it
	 */
	internal fun getKotlinTypeParameter(typeParamElem: TypeParameterElement): KotlinTypeParameterElement?
			= findMatchingProtoTypeParam(typeParamElem, protoClass.typeParameterList, protoNameResolver)
				?.let { protoTypeParam -> KotlinTypeParameterElement(typeParamElem, protoTypeParam, processingEnv) }

	/**
	 * methods declared within this class
	 */
	val declaredMethods: List<KotlinFunctionElement> by lazy {
		enclosedElements.filter { it.kind == ElementKind.METHOD }.castList<ExecutableElement>()
				.map { methodElem ->
					getKotlinFunction(methodElem)
					?: throw IllegalStateException(
							"Could not find matching ProtoBuf.Function for method element \"$methodElem\"" +
							"which is a sub-element of \"$this\"")

				}
	}

	/**
	 * returns a [KotlinFunctionElement] for this [ExecutableElementElement] if it's a method
	 * of this class or null otherwise
	 *
	 * this function is mostly necessary to be used by [KotlinFunctionElement.get] because only the
	 * enclosing class has enough information to create the [KotlinFunctionElement] and the factory
	 * function alone can not do it
	 */
	internal fun getKotlinFunction(functionElem: ExecutableElement): KotlinFunctionElement?
			= processingEnv.findMatchingProtoFunction(functionElem, protoClass.functionList, protoNameResolver)
			?.let { protoFunc -> KotlinFunctionElement(functionElem, protoFunc, protoNameResolver, processingEnv) }


	override fun getQualifiedName(): Name = element.qualifiedName

	//TODO(return kotlin interfaces)
	override fun getInterfaces(): List<TypeMirror> = element.interfaces

	override fun getNestingKind(): NestingKind = element.nestingKind

	override fun toString() = element.toString()
	override fun equals(other: Any?) = element.equals(other)
	override fun hashCode() = element.hashCode()
}

fun TypeElement.isKotlinClass() = kotlinMetadata is KotlinClassMetadata
