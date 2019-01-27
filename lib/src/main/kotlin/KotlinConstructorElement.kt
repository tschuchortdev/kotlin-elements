package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.isPrimary
import me.eugeniomarletti.kotlin.metadata.isSecondary
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.metadata.visibility
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Name

/**
 * A constructor of a Kotlin class.
 *
 * Note that annotation classes do not actually have constructors
 */
class KotlinConstructorElementImpl internal constructor(
		javaElement: ExecutableElement,
		javaOverloadElements: List<ExecutableElement>,
		override val enclosingElement: KotlinTypeElement,
		private val protoConstructor: ProtoBuf.Constructor,
		private val protoNameResolver: NameResolver,
		processingEnv: ProcessingEnvironment
) : KotlinExecutableElement(javaElement, javaOverloadElements, enclosingElement), HasKotlinVisibility {

	/** Whether this constructor is the primary constructor of its class */
	val isPrimary: Boolean
		get() = protoConstructor.isPrimary.also { primary ->
			assert(primary != protoConstructor.isSecondary)
		}

	override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoConstructor.visibility!!)

	override val parameters: List<KotlinParameterElement> by lazy{
		protoConstructor.valueParameterList.zipWith(javaElement.parameters) { protoParam, javaParam ->
			if (doParametersMatch(javaParam, protoParam, protoNameResolver))
				KotlinParameterElement(javaParam, protoParam, this)
			else
				throw AssertionError("Kotlin ProtoBuf.Parameters should always " +
									 "match up with Java VariableElements")
		}
	}

	override val simpleName: Name = javaElement.simpleName

	override fun toString(): String = javaElement.toString()
}
