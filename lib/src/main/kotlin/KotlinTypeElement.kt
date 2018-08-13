package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

open class KotlinTypeElement internal constructor(
		private val element: TypeElement,
		metadata: KotlinClassMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinElement(element, metadata.data.nameResolver, processingEnv), TypeElement {

	protected val protoClass: ProtoBuf.Class = metadata.data.classProto

	val packageName: String = protoNameResolver.getString(protoClass.fqName).substringBeforeLast('/').replace('/', '.')

	val classKind: ProtoBuf.Class.Kind = protoClass.classKind
	val visibility: ProtoBuf.Visibility? = protoClass.visibility

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
	 * one of: `FINAL`, `OPEN`, `ABSTRACT`, `SEALED`
	 */
	val modality: ProtoBuf.Modality? = protoClass.modality

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

	/**
	 * methods declared within this class
	 */
	val declaredMethods: List<KotlinExecutableElement> by lazy {
		enclosedElements.filter { it.kind == ElementKind.METHOD }.castList<ExecutableElement>()
				.map { methodElem ->
					val protoMethod = processingEnv.findMatchingProtoFunction(methodElem, protoClass.functionList, protoNameResolver)

					if(protoMethod == null)
						throw IllegalStateException(
								"Could not find matching ProtoBuf.Function for method element \"$methodElem\"" +
								"which is a method of \"$this\"")
					else
						KotlinExecutableElement(methodElem, protoMethod, protoNameResolver, processingEnv)
				}
	}

	init {
		val fqName: String = protoNameResolver.getString(protoClass.fqName).replace('/', '.')
		val elementQualifiedName = element.qualifiedName.toString()

		if(fqName != elementQualifiedName) {
			throw IllegalStateException("fully qualified name of Proto.Class and TypeElement don't match:\n" +
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
			= element.typeParameters.zip(protoClass.typeParameterList)
			.map { (typeParamElem, protoTypeParam) ->
				if (typeParamElem.simpleName.toString() == protoNameResolver.getString(protoTypeParam.name)) {
					KotlinTypeParameterElement(typeParamElem, protoTypeParam, protoNameResolver, processingEnv)
				}
				else {
					throw IllegalStateException("type parameter names for TypeElement \"$element\" don't match up with " +
												"the ProtoBuf TypeParameters in it's associated metadata ProtoBuf.Class:\n" +
												"	Java TypeElement type parameter: \"${typeParamElem.simpleName}\"\n" +
												"	ProtoBuf.TypeParameter: \"${protoTypeParam.name}\"")
				}
			}


	override fun getQualifiedName(): Name = element.qualifiedName

	//TODO(return kotlin interfaces)
	override fun getInterfaces(): MutableList<out TypeMirror> = element.interfaces

	override fun getNestingKind(): NestingKind = element.nestingKind

	override fun toString() = element.toString()
}

fun TypeElement.isKotlinClass() = kotlinMetadata is KotlinClassMetadata
