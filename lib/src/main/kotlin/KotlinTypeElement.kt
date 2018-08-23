package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.jvm.getJvmConstructorSignature
import me.eugeniomarletti.kotlin.metadata.jvm.getJvmFieldSignature
import me.eugeniomarletti.kotlin.metadata.jvm.getJvmMethodSignature
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

class KotlinTypeElement internal constructor(
		val javaElement: TypeElement,
		metadata: KotlinClassMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinElement(processingEnv), TypeElement by javaElement, KotlinParameterizable, HasKotlinModality {

	protected val protoClass: ProtoBuf.Class = metadata.data.classProto
	protected val protoNameResolver: NameResolver = metadata.data.nameResolver
	protected val protoTypeTable: ProtoBuf.TypeTable = protoClass.typeTable

	init {
		val fqName: String = protoNameResolver.getString(protoClass.fqName).replace('/', '.')
		val elementQualifiedName = javaElement.qualifiedName.toString()

		assert(fqName == elementQualifiedName)
	}

	val packageName: String = protoNameResolver.getString(protoClass.fqName).substringBeforeLast('/').replace('/', '.')

	//TODO("replace Visibility with own enum")
	val visibility: ProtoBuf.Visibility = protoClass.visibility!!

	/**
	 * Whether this is a (possibly anonymous) singleton class of the kind denoted by the `object` keyword
	 */
	val isObject: Boolean = (protoClass.classKind == ProtoBuf.Class.Kind.OBJECT)

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

	/**
	 * Whether this class has the `external` keyword
	 *
	 * An external class is a class declaration with the actual definition in native
	 * code, similar to the `native` keyword in Java
	 */
	val isExternalClass: Boolean = protoClass.isExternalClass

	val isDataClass: Boolean = protoClass.isDataClass

	/**
	 * class modality
	 * one of: [Modality.FINAL], [Modality.OPEN], [Modality.ABSTRACT], [Modality.ABSTRACT], [Modality.NONE]
	 */
	override val modality: Modality = when(protoClass.modality) {
		ProtoBuf.Modality.FINAL -> Modality.FINAL
		ProtoBuf.Modality.ABSTRACT -> Modality.ABSTRACT
		ProtoBuf.Modality.OPEN -> Modality.OPEN
		ProtoBuf.Modality.SEALED -> Modality.SEALED
		null -> Modality.NONE
	}

	/**
	 * the companion object of this element if it has one
	 */
	val companionObject: KotlinTypeElement? by lazy {
		getCompanionSimpleName()?.let { companionSimpleName ->
			val companionElement = javaElement.enclosedElements.single {
						it.kind == ElementKind.CLASS
						&& (it as TypeElement).simpleName.toString() == companionSimpleName
			} as TypeElement

			(companionElement.kotlinMetadata as? KotlinClassMetadata)?.let { metadata ->
				KotlinTypeElement(companionElement, metadata, processingEnv)
			}
			?: throw IllegalStateException(
					"element \"$companionElement\" for companion object with simple name " +
					"\"$companionSimpleName\" of \"$this\" doesn't have KotlinClassMetadata")
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
	override fun getSuperclass(): TypeMirror = javaElement.superclass

	override fun getTypeParameters(): List<KotlinTypeParameterElement>
			= protoClass.typeParameterList.zipWith(javaElement.typeParameters) { protoTypeParam, javaTypeParam ->
		if(doTypeParamsMatch(javaTypeParam, protoTypeParam, protoNameResolver))
			KotlinTypeParameterElement(javaTypeParam, protoTypeParam, processingEnv)
		else
			throw AssertionError(
					"Kotlin ProtoBuf.TypeParameters should always match up with Java TypeParameterElements")
	}

	/**
	 * Returns a [KotlinTypeParameterElement] for this [TypeParameterElement] if it's a type parameter
	 * of this class or null otherwise
	 *
	 * this function is mostly necessary to be used when finding the corresponding [KotlinElement] for
	 * some arbitrary Java [Element] since only the surrounding element of the type parameter has enough
	 * information to construct it
	 */
	internal fun getKotlinTypeParameter(typeParamElem: TypeParameterElement): KotlinTypeParameterElement?
			= protoClass.typeParameterList.filter { doTypeParamsMatch(typeParamElem, it, protoNameResolver) }
			.singleOrNull()
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
			?: if(kind != ElementKind.ANNOTATION_TYPE)
					throw IllegalStateException(
					"Could not find matching ExecutableElement for ProtoBuf.Constructor \"${protoCtor.jvmSignature()}\"" +
					" which is a sub-element of \"$this\"")
			else
				null
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
	 * methods declared within this class
	 */
	val declaredMethods: List<KotlinFunctionElement> by lazy {
		// a Kotlin TypeElement should never have initializers
		assert(javaElement.enclosedElements.none { it.kind == ElementKind.INSTANCE_INIT
												   || it.kind == ElementKind.STATIC_INIT })

		val methodElems = javaElement.enclosedElements.filter { it.kind == ElementKind.METHOD }
				.castList<ExecutableElement>()

		/*
		When @JvmOverloads was used, there may be multiple executable elements correspond
		to this Kotlin method but have different JVM signatures (with only a subset of the parameters)

		First find all the Java executable elements that match the JVM signature of a ProtoBuf.Function
		perfectly.
		All the remaining Java executable elements must then be JVM-overloads of one of the matched
		method elements.
		 */
		val matchedMethodElems = mutableListOf<Pair<ExecutableElement, ProtoBuf.Function>>()
		val unmatchedMethodElems = mutableListOf<ExecutableElement>()

		for(methodElem in methodElems) {
			val matchingProtoMethod = protoClass.functionList.firstOrNull {
				methodElem.jvmSignature() ==  it.jvmSignature()
			}

			if(matchingProtoMethod != null)
				matchedMethodElems += Pair(methodElem, matchingProtoMethod)
			else
				unmatchedMethodElems += methodElem
		}


		val kotlinMethodElements = matchedMethodElems.map { (matchedMethodElem, protoMethod) ->
			open class Parameter(param: VariableElement) {
				val simpleName = param.simpleName.toString()
				val type = param.asType()

				override fun equals(other: Any?) =
						if(other is Parameter)
							other.simpleName == simpleName
							&& processingEnv.typeUtils.isSameType(type, other.type)
						else
							false

				override fun hashCode() = Objects.hash(simpleName, type)
			}

			/*
			parameters of the method element and protoMethod should be in the exact same order,
			so we can zip them together.
			But better assert that they have the same name just to be sure
			 */
			val params = matchedMethodElem.parameters.zipWith(protoMethod.valueParameterList) { paramElem, protoParam ->
				assert(paramElem.simpleName.toString() == protoNameResolver.getString(protoParam.name))

				object : Parameter(paramElem) {
					val required = protoParam.declaresDefaultValue
				}
			}

			val methodSimpleName = protoNameResolver.getString(protoMethod.name)

			// now find those other Java executable elements that are generated by @JvmOverloads
			// and belong to this Kotlin method
			val jvmOverloadElems = unmatchedMethodElems.filter { unmatchedElem ->
				val unmatchedElemParams = unmatchedElem.parameters.map { Parameter(it) }

				// overload executable element must have...
				unmatchedElem.simpleName.toString() == methodSimpleName // ...the same name
				&& params.containsAll(unmatchedElemParams) // ...a subset of the parameters
				&& unmatchedElemParams.containsAll(params.filter { it.required }) // ...all the required (non-default) parameters
			}

			KotlinFunctionElement(matchedMethodElem, jvmOverloadElems, protoMethod, protoNameResolver, processingEnv)
		}

		return kotlinMethodElements
	}


	fun findCorrespondingExecutableElements(protoJvmSignature: String, protoParams: List<ProtoBuf.ValueParameter>,
											executableElements: List<ExecutableElement>) {
		/*
		When @JvmOverloads was used, there may be multiple executable elements which correspond
		to this Kotlin method/constructor but have different JVM signatures (with only a subset of the parameters)

		First find all the Java executable elements that match the JVM signature of the ProtoBuf method perfecty

		All the remaining Java executable elements must then be JVM-overloads of one of the matched
		executable elements.
		 */
		val matchedMethodElems = mutableListOf<Pair<ExecutableElement, ProtoBuf.Function>>()
		val unmatchedMethodElems = mutableListOf<ExecutableElement>()

		for(execElem in executableElements) {
			val matchingProtoMethod = protoClass.functionList.firstOrNull {
				execElem.jvmSignature() ==  it.jvmSignature()
			}

			if(matchingProtoMethod != null)
				matchedMethodElems += Pair(execElem, matchingProtoMethod)
			else
				unmatchedMethodElems += execElem
		}


		matchedMethodElems.map { (matchedMethodElem, protoMethod) ->
			open class Parameter(param: VariableElement) {
				val simpleName = param.simpleName.toString()
				val type = param.asType()

				override fun equals(other: Any?) =
						if(other is Parameter)
							other.simpleName == simpleName
							&& processingEnv.typeUtils.isSameType(type, other.type)
						else
							false

				override fun hashCode() = Objects.hash(simpleName, type)
			}

			/*
			parameters of the method element and protoMethod should be in the exact same order,
			so we can zip them together.
			But better assert that they have the same name just to be sure
			 */
			val params = matchedMethodElem.parameters.zipWith(protoMethod.valueParameterList) { paramElem, protoParam ->
				assert(paramElem.simpleName.toString() == protoNameResolver.getString(protoParam.name))

				object : Parameter(paramElem) {
					val required = protoParam.declaresDefaultValue
				}
			}

			val methodSimpleName = protoNameResolver.getString(protoMethod.name)

			// now find those other Java executable elements that are generated by @JvmOverloads
			// and belong to this Kotlin method
			val jvmOverloadElems = unmatchedMethodElems.filter { unmatchedElem ->
				val unmatchedElemParams = unmatchedElem.parameters.map { Parameter(it) }

				// overload executable element must have...
				unmatchedElem.simpleName.toString() == methodSimpleName // ...the same name
				&& params.containsAll(unmatchedElemParams) // ...a subset of the parameters
				&& unmatchedElemParams.containsAll(params.filter { it.required }) // ...all the required (non-default) parameters
			}
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


	//TODO(return kotlin interfaces)
	override fun getInterfaces(): List<TypeMirror> = javaElement.interfaces

	override fun getEnclosedElements(): List<KotlinElement> {
		TODO("type enclosed elements")
	}

	protected fun ProtoBuf.Constructor.jvmSignature() = with(processingEnv.kotlinMetadataUtils) {
		val signature = this@jvmSignature.getJvmConstructorSignature(protoNameResolver, protoTypeTable)
						?: throw IllegalArgumentException("could not get JVM signature for ProtoBuf.Constructor")

		if(this@KotlinTypeElement.kind == ElementKind.ENUM) {
			/* for some reason the Kotlin compiler adds an implicit String and Int argument
			to enum constructors (probably to call it's implicit super constructor
			`Enum::<init>(name: String, ordinal: Int)`). The `ExecutableElement` that is
			the actual constructor won't have those arguments, so we need to remove them
			from the signature so they will match */
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

	override fun toString() = javaElement.toString()
	override fun hashCode() = javaElement.hashCode()
	override fun equals(other: Any?) = javaElement.equals(other)
}

fun TypeElement.isKotlinClass() = kotlinMetadata is KotlinClassMetadata