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
		javaElement: ExecutableElement,
		jvmOverloadElements: List<ExecutableElement>,
		private val protoConstructor: ProtoBuf.Constructor,
		private val protoNameResolver: NameResolver,
		processingEnv: ProcessingEnvironment
) : KotlinExecutableElement(javaElement, jvmOverloadElements, processingEnv) {

	val isPrimary: Boolean
		get() = protoConstructor.isPrimary.also { primary ->
			assert(primary != protoConstructor.isSecondary)
		}

	//TODO("constructor visibility")
	val visibility: ProtoBuf.Visibility = protoConstructor.visibility!!

	override fun getParameters(): List<KotlinParameterElement>
			= protoConstructor.valueParameterList.zipWith(javaElement.parameters) { protoParam, javaParam ->

		if (doParametersMatch(javaParam, protoParam, protoNameResolver))
			KotlinParameterElement(javaParam, protoParam, processingEnv)
		else
			throw AssertionError("Kotlin ProtoBuf.Parameters should always " +
								 "match up with Java VariableElements")
	}
	override fun getTypeParameters(): List<Nothing> {
		// a constructor should never have type parameters
		assert(javaElement.typeParameters.isEmpty())
		return emptyList()
	}
}
