package com.tschuchort.kotlinelements

import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.Element
import javax.lang.model.element.ModuleElement
import javax.lang.model.element.Name
import javax.lang.model.type.TypeMirror

/**
 * A [KotlinModuleElement] is a module that contains at least one [KotlinPackageElement]
 */
class KotlinModuleElement internal constructor(
		val javaElement: ModuleElement,
		processingEnv: ProcessingEnvironment
) : KotlinElement(), EnclosesKotlinPackages, KotlinQualifiedNameable,
	AnnotatedConstruct by javaElement {

	override val enclosingElement: Nothing? = null

	override val qualifiedName: Name = javaElement.qualifiedName

	override val simpleName: Name = javaElement.simpleName

	val isOpen: Boolean = javaElement.isOpen

	val isUnnamed: Boolean = javaElement.isUnnamed

	val directives: List<ModuleElement.Directive> = javaElement.directives

	/**
	 * Elements enclosed by this package that aren't Kotlin elements
	 */
	val enclosedJavaElements: Set<Element> by lazy {
		javaElement.enclosedElements.asSequence()
				.filter { !it.originatesFromKotlinCode() }.toSet()
	}

	override val enclosedKotlinElements: Set<KotlinPackageElement> get() = kotlinPackages

	override val kotlinPackages: Set<KotlinPackageElement> by lazy {
		javaElement.enclosedElements.mapNotNull {
			it.asKotlin(processingEnv) as? KotlinPackageElement
		}.toSet()
	}

	override fun asType(): TypeMirror = javaElement.asType()

	override fun equals(other: Any?): Boolean
			= (other as? KotlinModuleElement)?.javaElement == javaElement

	override fun hashCode(): Int = Objects.hash(javaElement)

	override fun toString(): String = javaElement.toString()
}