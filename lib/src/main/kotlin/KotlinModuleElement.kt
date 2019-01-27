package com.tschuchort.kotlinelements

import java.util.*
import java.util.stream.Collectors.toSet
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

internal class KotlinModuleElementImpl internal constructor(
		override val javaElement: ModuleElement,
		processingEnv: ProcessingEnvironment
) : KotlinModuleElement(), AnnotatedConstruct by javaElement {

	override val enclosingElement: Nothing? = null

	override val qualifiedName: Name = javaElement.qualifiedName

	override val simpleName: Name = javaElement.simpleName

	override val isOpen: Boolean = javaElement.isOpen

	override val isUnnamed: Boolean = javaElement.isUnnamed

	override val directives: List<ModuleElement.Directive> = javaElement.directives

	override val javaPackages: Set<PackageElement> by lazy {
		javaElement.enclosedElements
			.filter { !it.originatesFromKotlinCode() }
			.map {
				assert(it.kind == ElementKind.PACKAGE)
				it as PackageElement
			}
			.toSet()
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