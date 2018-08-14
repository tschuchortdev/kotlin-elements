package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.isPrimary
import me.eugeniomarletti.kotlin.metadata.isSecondary
import me.eugeniomarletti.kotlin.metadata.jvm.getJvmConstructorSignature
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.metadata.visibility
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement

class KotlinConstructorElement internal constructor(
		private val element: ExecutableElement,
		private val protoConstructor: ProtoBuf.Constructor,
		protoNameResolver: NameResolver,
		processingEnv: ProcessingEnvironment
) : KotlinExecutableElement(element, processingEnv) {

	val isPrimary: Boolean
		get() = protoConstructor.isPrimary.also { primary ->
			assert(primary != protoConstructor.isSecondary)
		}

	//TODO("constructor visibility")
	val visibility: ProtoBuf.Visibility = protoConstructor.visibility!!

	companion object {
		fun get(element: ExecutableElement, processingEnv: ProcessingEnvironment): KotlinConstructorElement? {
			return if(element is KotlinConstructorElement)
				element
			else {
				// to construct the KotlinConstructorElement, metadata of the parent element is needed
				// so the construction is delegated to the parent element
				(element.enclosingElement.toKotlinElement(processingEnv)
						as? KotlinTypeElement)
						?.getKotlinConstructor(element)
				?: throw IllegalStateException(
						"Could not convert $element to KotlinTypeParameterElement even " +
						"though it is apparently a Kotlin element")
			}
		}
	}

	override fun getTypeParameters(): List<KotlinTypeParameterElement> {
		// a constructor should never have type parameters
		assert(element.typeParameters.isEmpty())
		return emptyList()
	}
}

internal fun ProcessingEnvironment.findMatchingProtoConstructor(
		constructorElement: ExecutableElement,
		protoConstructors: List<ProtoBuf.Constructor>,
		nameResolver: NameResolver,
		typeTable: ProtoBuf.TypeTable
): ProtoBuf.Constructor? {
	val matchingProtoCtors = protoConstructors.filter {
		doConstructorsMatch(constructorElement, it, nameResolver, typeTable)
	}

	return when(matchingProtoCtors.size) {
		0 -> null
		1 -> matchingProtoCtors.single()
		else -> throw IllegalStateException(
				"More than one element in the list of protoConstructors matches the executable element's signature")
	}
}

internal fun ProcessingEnvironment.findMatchingConstructorElement(
		protoConstructor: ProtoBuf.Constructor,
		functionElements: List<ExecutableElement>,
		nameResolver: NameResolver,
		typeTable: ProtoBuf.TypeTable
): ExecutableElement? = with(this.kotlinMetadataUtils) {
	val matchingCtorElems = functionElements.filter {
		doConstructorsMatch(it, protoConstructor, nameResolver, typeTable)
	}

	return when(matchingCtorElems.size) {
		0 -> null
		1 -> matchingCtorElems.single()
		else -> throw IllegalStateException(
				"More than one element in the list of functionElements matches the protoConstructor's signature")
	}
}

internal fun ProcessingEnvironment.doConstructorsMatch(
		constructorElement: ExecutableElement,
		protoConstructor: ProtoBuf.Constructor,
		nameResolver: NameResolver,
		typeTable: ProtoBuf.TypeTable
): Boolean = with(this.kotlinMetadataUtils) {
	constructorElement.jvmMethodSignature == protoConstructor.getJvmConstructorSignature(nameResolver, typeTable)
}
