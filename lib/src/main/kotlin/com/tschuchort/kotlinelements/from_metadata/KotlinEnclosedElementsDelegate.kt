package com.tschuchort.kotlinelements.from_metadata

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.asTypeElement
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmProperty
import kotlinx.metadata.KmTypeAlias
import kotlinx.metadata.internal.metadata.ProtoBuf
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import kotlin.collections.LinkedHashMap

internal class KotlinEnclosedElementsDelegate(
		enclosingKtElement: KJElement,
		kmTypeAliases: List<KmTypeAlias> = emptyList(),
		kmProperties: List<KmProperty> = emptyList(),
		kmConstructors: List<KmConstructor> = emptyList(),
		kmFunctions: List<KmFunction> = emptyList(),
		companionSimpleName: String? = null,
		javaxElements: List<Element>,
		private val processingEnv: ProcessingEnvironment
) : EnclosesKotlinElements {

	private val convertedElements: LinkedHashMap<Element, KJElement> by lazy {

		val unconvertedElements = hashMapOf<JvmSignature, Element>()
	}

	private val javaMethodElems = javaxElements.filter { it.kind == ElementKind.METHOD }
			.castList<ExecutableElement>()

	private val javaFieldElems = javaxElements.filter { it.kind == ElementKind.FIELD }
			.castList<VariableElement>()

	private val javaCtorElems = javaxElements.filter { it.kind == ElementKind.CONSTRUCTOR }
			.castList<ExecutableElement>()

	private val javaTypeElems = javaxElements.mapNotNull { it.asTypeElement() }

	private val javaPackageElems = javaxElements.filter { it.kind == ElementKind.PACKAGE }
			.castList<PackageElement>()

	private val javaModuleElems = javaxElements.filter { it.kind == ElementKind.MODULE }
			.castList<ModuleElement>()

	val companion: KJObjectElement? by lazy {
		companionSimpleName?.run {
			val companionElement = javaTypeElems.single {
				it.kind == ElementKind.CLASS
						&& it.simpleName.toString() == companionSimpleName
			}

			(companionElement.asKotlin(processingEnv) as KotlinObjectElement)
					.also { check(it.isCompanion) }
		}
	}

	val typeAliases: Set<KJTypeAliasElement> by lazy {
		protoTypeAliases.asSequence().map { protoTypeAlias ->
			try {
				check(protoTypeAlias.hasName())

				val typeAliasName = protoNameResolver.getString(protoTypeAlias.name)

				val annotHolderElem = if (protoTypeAlias.hasAnnotations) {
					javaMethodElems.single { typeAliasName == it.simpleName.toString() + ANNOTATIONS_SUFFIX }
				}
				else
					null

				KotlinTypeAliasElement(annotHolderElem, protoTypeAlias, protoTypeTable,
						protoNameResolver, enclosingKtElement, processingEnv.elementUtils, processingEnv.typeUtils)
			}
			catch (e : Exception) {
				throw KJConversionException(protoTypeAlias, protoNameResolver, e)
			}
		}.toSet()
	}

	val types: Set<KJTypeElement> by lazy {
		javaTypeElems.mapNotNull {
			it.asKotlin(processingEnv) as? KotlinTypeElement
		}.toSet()
	}

	val packages: Set<KJPackageElement> by lazy {
		javaPackageElems.asSequence().map {
			it.toKJElement(processingEnv) as KJPackageElement
		}.toSet()
	}

	val properties: Set<KJPropertyElement> by lazy {
		protoProps.asSequence().map { protoProperty ->
			try {
				val propertyJvmProtoSignature = protoProperty.jvmPropertySignature!!
				val propertyName = protoNameResolver.getString(protoProperty.name)

				val setterElem = if (propertyJvmProtoSignature.hasSetter()) {
					val setterJvmSignature = propertyJvmProtoSignature.setter.jvmSignatureString(protoNameResolver)
						?: throw IllegalStateException(
								"Property setter should always have jvm name if it exists")

					javaMethodElems.atMostOne { it.jvmSignature() == setterJvmSignature }
				}
				else {
					null
				}

				//TODO("handle the trick where people use a getter with DeprecationLevel.HIDDEN to create properties with inaccessible getter")
				val getterElem = if (propertyJvmProtoSignature.hasGetter()) {
					val getterJvmSignature = propertyJvmProtoSignature.getter.jvmSignatureString(protoNameResolver)
						?: throw IllegalStateException(
								"Property getter should always have jvm name if it exists")

					javaMethodElems.single { it.jvmSignature() == getterJvmSignature }
				}
				else {
					null
				}

				val fieldElem = if (propertyJvmProtoSignature.hasField()) {
					if (propertyJvmProtoSignature.field.hasName())
						throw IllegalStateException("Afaik the field should never have a name in JvmProtoBuf.JvmFieldSignature" +
								"because it must always be the same as the property name")

					javaFieldElems.single { it.simpleName.toString() == propertyName }
				}
				else {
					null
				}

				/* If the Kotlin property has annotations with target [AnnotationTarget.PROPERTY]
		 	the Kotlin compiler will generate an empty parameterless void-returning
		 	synthetic method named "propertyName$annotations" to hold the annotations that
		 	are targeted at the property and not backing field, getter or setter */
				val syntheticAnnotationHolderElem = if (propertyJvmProtoSignature.hasSyntheticMethod()) {
					val synthJvmSignature = propertyJvmProtoSignature.syntheticMethod.jvmSignatureString(protoNameResolver)
						?: throw IllegalStateException("Property synthetic annotation holder method " +
								"should always have jvm name if it exists")

					javaMethodElems.single { it.jvmSignature() == synthJvmSignature }
				}
				else {
					null
				}

				val delegateFieldElem = if (protoProperty.isDelegated) {
					javaFieldElems.single {
						propertyName == it.simpleName.toString() + JvmAbi.DELEGATED_PROPERTY_NAME_SUFFIX
					}
				}
				else {
					null
				}

				assert(arrayListOf(fieldElem, setterElem, getterElem).filterNotNull().isNotEmpty())

				KotlinPropertyElement(enclosingKtElement, fieldElem, setterElem, getterElem,
						syntheticAnnotationHolderElem, delegateFieldElem, protoProperty,
						protoNameResolver, processingEnv)
			}
			catch (e : Exception) {
				throw KJConversionException(
						protoProperty,
						protoNameResolver,
						protoTypeTable,
						e
				)
			}
		}.toSet()
	}

	/**
	 * All constructors enclosed within this type element
	 *
	 * The primary constructor will be the first one in the list
	 */
	val constructors: Set<KJConstructorElement> by lazy {
		/* If the enclosing element is an annotation class, the proto class will contain
		a constructor signature, but in reality annotations don't really have constructors
		so no corresponding ExecutableElement exists. We will ignore it here. */
		if(enclosingKtElement is KJAnnotationElement) {
			assert(protoCtors.size == 1)
			assert(javaCtorElems.isEmpty())
			return@lazy emptySet<KotlinConstructorElement>()
		}

		return@lazy protoCtors.asSequence().map { protoCtor ->
			try {
				val (element, overloadElements) = findCorrespondingExecutableElements(
						protoCtor.jvmSignature(
								isEnumConstructor = enclosingKtElement is KotlinEnumElement,
								isInnerClassConstructor = (enclosingKtElement as? com.tschuchort.kotlinelements.KotlinClassElement)?.isInner
									?: false),
						protoCtor.valueParameterList, javaCtorElems
				)

				KotlinConstructorElement(element, overloadElements, enclosingKtElement as KotlinTypeElement,
						protoCtor, protoNameResolver, processingEnv.elementUtils, processingEnv.typeUtils)
			}
			catch (t : Throwable) {
				throw KotlinElementConversionException(
						protoCtor, protoNameResolver, protoTypeTable, t
				)
			}
		}
				.sortedBy { !it.isPrimary }.toList() // sort list by inverse of isPrimary so that the primary ctor will come first
				.also {
					// check that the first ctor really is primary
					assert(it.firstOrNull()?.isPrimary ?: true)

					//check that the second ctor is secondary if there is one, since
					// there may be only one primary ctor
					assert(it.getOrNull(1)?.isPrimary?.not() ?: true)
				}
				.toSet()
	}

	/**
	 * functions enclosed within this element
	 */
	val functions: Set<KJFunctionElement> by lazy {
		protoFunctions.asSequence().map { protoFunc ->
			try {
				val (javaFunc, javaOverloads) = findCorrespondingExecutableElements(
						protoFunc.jvmSignature(), protoFunc.valueParameterList, javaMethodElems)

				KotlinFunctionElement(javaFunc, javaOverloads, enclosingKtElement, protoFunc,
						protoNameResolver, processingEnv.elementUtils, processingEnv.typeUtils)
			} catch (t : Throwable) {
				throw KotlinElementConversionException(
						protoFunc, protoNameResolver, protoTypeTable, t
				)
			}
		}.toSet()
	}

	/**
	 * Used for comparing parameters
	 *
	 * This should really be private in [findCorrespondingExecutableElements] but can't be
	 * because naturally the Kotlin compiler is buggy as hell and will crash (KT-26697).
	 *
	 * Also it can not be a an inner class or _once again_ the compiler will produce garbage
	 * (VerifyError: Bad type on operand stack). 😐🔫
	 */
	private open class JavaParameter(paramElem: VariableElement, val processingEnv: ProcessingEnvironment,
									 val required: Boolean? = null) {
		val simpleName = paramElem.simpleName.toString()
		val type = paramElem.asType()

		override fun equals(other: Any?) =
			if(other is JavaParameter)
				other.simpleName == simpleName
						&& processingEnv.typeUtils.isSameType(type, other.type)
			else
				false

		override fun hashCode() = Objects.hash(simpleName, type)
	}

	private fun findCorrespondingExecutableElements(protoJvmSignature: String, protoParams: List<ProtoBuf.ValueParameter>,
													executableElements: List<ExecutableElement>) = run {
		// use `run` for type inference :S
		/*
		When @JvmOverloads was used, there may be multiple executable elements which correspond
		to this Kotlin method/constructor but have different JVM signatures (with only a subset of the parameters)

		First find the the Java executable javaElement that matches the JVM signature of the ProtoBuf method perfectly

		Then find overload elements who must have the same name with a subset of the parameters
		 */

		val matchingElement = try {
			executableElements.single { it.jvmSignature() == protoJvmSignature }
		}
		catch (t: Throwable) {
			throw IllegalStateException("Could not get Java ExecutableElement " +
					"matching protoJvmSignature ($protoJvmSignature)", t)
		}

		/*
		parameters of the method javaElement and protoMethod should be in the exact same order,
		so we can zip them together.
		But better assert that they have the same name just to be sure
		 */
		val params = try {
			matchingElement.parameters.zipWith(protoParams) { paramElem, protoParam ->
				val javaParamName = paramElem.simpleName.toString()
				val protoParamName = protoNameResolver.getString(protoParam.name)

				assert(
						javaParamName == protoParamName
								// ugly edge case here where the java parameter name is "p0" on data class
								// generated equals method while Kotlin parameter name is "other"
								|| protoJvmSignature == "equals(Ljava/lang/Object;)Z"
				) {
					"Java parameter name ($javaParamName) and proto parameter name ($protoParamName) should be identical"
				}

				return@zipWith JavaParameter(
						paramElem,
						processingEnv,
						!protoParam.declaresDefaultValue
				)
			}
		}
		catch (t: Throwable) {
			throw IllegalStateException("Could not get paramaters for executable element", t)
		}


		// now find those other Java executable elements that are generated by @JvmOverloads
		// and belong to this Kotlin method
		val jvmOverloadElems = executableElements.filter { overloadElem ->
			val overloadElemParams = overloadElem.parameters.map {
				JavaParameter(it, processingEnv)
			}

			// overload executable javaElement must have...
			overloadElem.simpleName == matchingElement.simpleName // ...the same name
					&& overloadElem.jvmSignature() != protoJvmSignature // ...a different signature, or it would just be the matching javaElement
					&& params.containsAll(overloadElemParams) // ...a subset of the parameters
					&& overloadElemParams.containsAll(params.filter { it.required!! }) // ...all the required (non-default) parameters
		}

		return@run object {
			val element = matchingElement
			val overloadElements = jvmOverloadElems

			operator fun component1() = element
			operator fun component2() = overloadElements
		}
	}

	private fun ProtoBuf.Constructor.jvmSignature(isEnumConstructor: Boolean,
												  isInnerClassConstructor: Boolean)
			= with(processingEnv.kotlinMetadataUtils) {
		val signature = this@jvmSignature.getJvmConstructorSignature(protoNameResolver, protoTypeTable)
			?: throw IllegalArgumentException("could not get JVM signature for ProtoBuf.Constructor")

		if(isEnumConstructor) {
			/* for some reason the Kotlin compiler adds an implicit String and Int argument
			to enum constructors (probably to call it's implicit super constructor
			`Enum::<init>(name: String, ordinal: Int)`). The `ExecutableElement` that is
			the actual constructor won't have those arguments, so we need to remove them
			from the signature so they will match */
			check(signature.startsWith("<init>(Ljava/lang/String;I"))
			signature.removeFirstOccurance("Ljava/lang/String;I")
		}
		else if(isInnerClassConstructor) {
			/* If the constructor belongs to an inner class the first parameter will be
			a reference to the outer class. The JVM signature that we get doesn't have
			that parameter, so we need to remove it to match them later. */
			val (nameAndOpeningBracket, firstParameter, rest)
					= Regex("(<init>\\()" + "(L.*?;)(.*)") .matchEntire(signature)!!.destructured

			nameAndOpeningBracket + rest
		}
		else
			signature
	}

	/**
	 * Returns the JVM signature in the form "$Name$MethodDescriptor", for example: `equals(Ljava/lang/Object;)Z`.
	 *
	 * For reference, see the [JVM specification, section 4.3](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3).
	 */
	private fun ExecutableElement.jvmSignature() = getJvmMethodSignature(processingEnv)
}