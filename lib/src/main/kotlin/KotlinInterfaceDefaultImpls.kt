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
/*class KotlinInterfaceDefaultImpls internal constructor(
		protected val javaElement: TypeElement,
		protected val interfaceElement: KotlinClassOrInterfaceElement,
		processingEnv: ProcessingEnvironment
) : KotlinImplicitElement(javaElement, processingEnv), TypeElement {

	companion object {
		fun get(javaElement: TypeElement, processingEnv: ProcessingEnvironment): KotlinInterfaceDefaultImpls? {
			val enclosingElem = KotlinSyntacticElement.get(javaElement.enclosingElement, processingEnv) as? KotlinClassOrInterfaceElement

			return if(javaElement.qualifiedName.toString() == enclosingElem?.qualifiedName.toString() + ".DefaultImpl"
			   && enclosingElem?.kind == ElementKind.INTERFACE) {
				KotlinInterfaceDefaultImpls(javaElement, enclosingElem, processingEnv)
			}
			else
				null
		}
	}

	//TODO("return Kotlin Superclass")
	override fun getSuperclass(): TypeMirror = javaElement.superclass

	override fun getInterfaces(): List<Nothing> {
		// the generated DefaultImpl class should never implement any interfaces
		assert(javaElement.interfaces.isEmpty())
		return emptyList()
	}

	override fun getNestingKind(): NestingKind = javaElement.nestingKind

	//TODO("return Kotlin qualified name")
	override fun getQualifiedName(): Name = javaElement.qualifiedName

	override fun getTypeParameters(): List<Nothing> {
		// the generated DefaultImpl class never has type parameters. Instead each of it's functions
		// receives an instance of the enclosing interface
		assert(javaElement.typeParameters.isEmpty())
		return emptyList()
	}
}*/