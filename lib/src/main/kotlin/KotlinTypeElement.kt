package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.jvm.getJvmConstructorSignature
import me.eugeniomarletti.kotlin.metadata.jvm.getJvmFieldSignature
import me.eugeniomarletti.kotlin.metadata.jvm.getJvmMethodSignature
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
) : KotlinElement(element, processingEnv), TypeElement, KotlinParameterizable {

	protected val protoClass: ProtoBuf.Class = metadata.data.classProto
	protected val protoNameResolver: NameResolver = metadata.data.nameResolver
	protected val protoTypeTable: ProtoBuf.TypeTable = protoClass.typeTable

	val packageName: String = protoNameResolver.getString(protoClass.fqName).substringBeforeLast('/').replace('/', '.')

	//TODO("replace Visibility with own enum")
	val visibility: ProtoBuf.Visibility = protoClass.visibility!!

	/**
	 * Whether this is a (possibly anonymous) singleton class of the kind denoted by the `object` keyword
	 */
	val isObject: Boolean = protoClass.classKind == ProtoBuf.Class.Kind.OBJECT

	/**
	 * Whether or not the class is an inner class
	 *
	 * A class is an inner class if it has the keyword `inner` i.e. nested classes are not
	 * necessarily inner classes
	 */
	val isInnerClass: Boolean = protoClass.isInnerClass

	/** Whether this class has the `expect` keyword
	 *
	 * An expect class is a class declaration with actual definition in a different
	 * file, akin to a declaration in a header file in C++. They are used in multiplatform
	 * projects where different implementations are needed depending on target platform
	 */
	val isExpectClass: Boolean = protoClass.isExpectClass

	//TODO(docs)
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
	 * Returns a [KotlinTypeParameterElement] for this [TypeParameterElement] if it's a type parameter
	 * of this class or null otherwise
	 *
	 * this function is mostly necessary to be used by [KotlinTypeParameterElement.get] because only the
	 * enclosing class has enough information to create the [KotlinTypeParameterElement] and the factory
	 * function alone can not do it
	 */
	internal fun getKotlinTypeParameter(typeParamElem: TypeParameterElement): KotlinTypeParameterElement?
			= findMatchingProtoTypeParam(typeParamElem, protoClass.typeParameterList, protoNameResolver)
				?.let { protoTypeParam -> KotlinTypeParameterElement(typeParamElem, protoTypeParam, processingEnv) }


	val primaryConstructor: KotlinConstructorElement? by lazy {
		constructors.firstOrNull { it.isPrimary }
	}

	/**
	 * Secondary constructors declared within this type element
	 */
	val secondaryConstructors: List<KotlinConstructorElement> by lazy {
		constructors.filter { !it.isPrimary }
	}

	/**
	 * All constructors declared within this type element
	 *
	 * The primary constructor will be the first one in the list
	 */
	val constructors: List<KotlinConstructorElement> by lazy {
		protoClass.constructorList.map { protoCtor ->
			getKotlinConstructor(protoCtor)
			?: //if(kind != ElementKind.ENUM && kind != ElementKind.ANNOTATION_TYPE)
					throw IllegalStateException(
					"Could not find matching ExecutableElement for ProtoBuf.Constructor \"${protoCtor.jvmSignature()}\"" +
					" which is a sub-element of \"$this\"")
			//else
			//	 null
		}
				.filterNotNull()
				.sortedBy { !it.isPrimary } // sort list by inverse of isPrimary so that the primary ctor will come first
				.also {
					// check that the first ctor really is primary
					assert(it.firstOrNull()?.isPrimary ?: true)

					//check that the second ctor is secondary if there is one, since
					// there may be only one primary ctor
					assert(it.getOrNull(1)?.isPrimary?.not() ?: true)
				}
	}

	/**
	 * returns a [KotlinConstructorElement] for this [ExecutableElementElement] if it's a constructor
	 * of this type element or null otherwise
	 *
	 * this function is mostly necessary to be used by [KotlinConstructorElement.get] because only the
	 * enclosing class has enough information to create the [KotlinConstructorElement] and the factory
	 * function alone can not do it
	 */
	internal fun getKotlinConstructor(constructorElem: ExecutableElement): KotlinConstructorElement?
			= protoClass.constructorList.singleOrNull { protoCtor -> doConstructorsMatch(constructorElem, protoCtor) }
			?.let { protoCtor -> KotlinConstructorElement(constructorElem, protoCtor, protoNameResolver, processingEnv) }

	internal fun getKotlinConstructor(protoCtor: ProtoBuf.Constructor): KotlinConstructorElement?
			= element.enclosedElements.filter { it.kind == ElementKind.CONSTRUCTOR }.castList<ExecutableElement>()
			.singleOrNull { ctorElem -> doConstructorsMatch(ctorElem, protoCtor) }
			?.let { ctorElem -> KotlinConstructorElement(ctorElem, protoCtor, protoNameResolver, processingEnv) }

	/**
	 * methods declared within this class
	 */
	val declaredMethods: List<KotlinFunctionElement> by lazy {
		protoClass.functionList.map { protoMethod ->
			getKotlinMethod(protoMethod)
			?: throw IllegalStateException(
					"Could not find matching ExecutableElement for ProtoBuf.Function \"${protoMethod.jvmSignature()}\"" +
					"which is a sub-element of \"$this\"")
		}
	}

	/**
	 * returns a [KotlinFunctionElement] for this [ExecutableElement] if it's a method
	 * of this class or null otherwise
	 *
	 * this function is mostly necessary to be used by [KotlinFunctionElement.get] because only the
	 * enclosing class has enough information to create the [KotlinFunctionElement] and the factory
	 * function alone can not do it
	 */
	internal fun getKotlinMethod(methodElem: ExecutableElement): KotlinFunctionElement?
			= protoClass.functionList.singleOrNull { protoMethod -> doFunctionsMatch(methodElem, protoMethod) }
			?.let { protoMethod -> KotlinFunctionElement(methodElem, protoMethod, protoNameResolver, processingEnv) }

	internal fun getKotlinMethod(protoMethod: ProtoBuf.Function): KotlinFunctionElement?
			= element.enclosedElements.filter { kind == ElementKind.METHOD }.castList<ExecutableElement>()
			.singleOrNull { methodElem -> doFunctionsMatch(methodElem, protoMethod) }
			?.let { methodElem -> KotlinFunctionElement(methodElem, protoMethod, protoNameResolver, processingEnv) }

	override fun getQualifiedName(): Name = element.qualifiedName

	/*val properties: List<KotlinPropertyElement> by lazy {
		protoClass.propertyList
	}*/

	//TODO(return kotlin interfaces)
	override fun getInterfaces(): List<TypeMirror> = element.interfaces

	override fun getNestingKind(): NestingKind = element.nestingKind

	protected fun ProtoBuf.Constructor.jvmSignature() = with(processingEnv.kotlinMetadataUtils) {
		val signature = this@jvmSignature.getJvmConstructorSignature(protoNameResolver, protoTypeTable)
						?: throw IllegalArgumentException("could not get JVM signature for ProtoBuf.Constructor")

		if(this@KotlinTypeElement.kind == ElementKind.ENUM) {
			/* for some reason the Kotlin compiler adds an implicit String and Int argument
			to enum constructors (probably to call it's implicit super constructor
			`Enum::<init>(name: String, ordinal: Int)`). The `ExecutableElement` that is
			the actual constructor won't have those arguments, so we need to remove them
			from the signature so they will match
			 */
			assert(signature.startsWith("<init>(Ljava/lang/String;I"))
			signature.removeFirstOccurance("Ljava/lang/String;I")
		}
		else
			signature
	}

	protected fun ProtoBuf.Function.jvmSignature() = with(processingEnv.kotlinMetadataUtils) {
		this@jvmSignature.getJvmMethodSignature(protoNameResolver)
		?: throw IllegalArgumentException("could not get JVM signature for ProtoBuf.Function")
	}

	protected fun ProtoBuf.Property.fieldJvmSignature() = with(processingEnv.kotlinMetadataUtils) {
		this@fieldJvmSignature.getJvmFieldSignature(protoNameResolver, protoTypeTable)
		?: throw IllegalArgumentException("could not get JVM signature for ProtoBuf.Property field")
	}

	protected fun doFunctionsMatch(functionElement: ExecutableElement, protoFunction: ProtoBuf.Function): Boolean
			= functionElement.jvmSignature() == protoFunction.jvmSignature()

	protected fun doConstructorsMatch(constructorElement: ExecutableElement, protoConstructor: ProtoBuf.Constructor): Boolean
			= constructorElement.jvmSignature() == protoConstructor.jvmSignature()
}

fun TypeElement.isKotlinClass() = kotlinMetadata is KotlinClassMetadata