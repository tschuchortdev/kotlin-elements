package com.tschuchort.kotlinelements

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Name
import javax.lang.model.element.NestingKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * For interfaces with default implementations Kotlin implicitly generates
 * a public static inner class `DefaultImpls` with a static method for each
 * default method implementation
 */
class KotlinInterfaceDefaultImpls internal constructor(
		protected val element: TypeElement,
		protected val interfaceElement: KotlinTypeElement,
		processingEnv: ProcessingEnvironment
) : KotlinImplicitElement(element, processingEnv), TypeElement {

	companion object {
		fun get(element: TypeElement, processingEnv: ProcessingEnvironment): KotlinInterfaceDefaultImpls? {
			val enclosingElem = KotlinSyntacticElement.get(element.enclosingElement, processingEnv) as? KotlinTypeElement

			return if(element.qualifiedName.toString() == enclosingElem?.qualifiedName.toString() + ".DefaultImpl"
			   && enclosingElem?.kind == ElementKind.INTERFACE) {
				KotlinInterfaceDefaultImpls(element, enclosingElem, processingEnv)
			}
			else
				null
		}
	}

	//TODO("return Kotlin Superclass")
	override fun getSuperclass(): TypeMirror = element.superclass

	override fun getInterfaces(): List<Nothing> {
		// the generated DefaultImpl class should never implement any interfaces
		assert(element.interfaces.isEmpty())
		return emptyList()
	}

	override fun getNestingKind(): NestingKind = element.nestingKind

	//TODO("return Kotlin qualified name")
	override fun getQualifiedName(): Name = element.qualifiedName

	override fun getTypeParameters(): List<Nothing> {
		// the generated DefaultImpl class never has type parameters. Instead each of it's functions
		// receives an instance of the enclosing interface
		assert(element.typeParameters.isEmpty())
		return emptyList()
	}
}