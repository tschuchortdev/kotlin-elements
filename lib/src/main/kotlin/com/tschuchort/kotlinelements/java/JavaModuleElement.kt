package com.tschuchort.kotlinelements.java

import com.tschuchort.kotlinelements.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.ModuleElement

internal class JavaModuleElement(
		private val javaxElem: ModuleElement,
		private val processingEnv: ProcessingEnvironment
) : KJModuleElement(), AnnotatedConstruct by javaxElem {

	override val enclosingElement: KJElement?
		get() = javaxElem.toKJElement(processingEnv)

	override val simpleName: String = javaxElem.simpleName.toString()

	override fun asTypeMirror(): KJModuleType
			= javaxElem.asType().toKJTypeMirror(processingEnv) as KJModuleType

	override val origin: KJOrigin = KJOrigin.fromJavax(javaxElem, processingEnv)

	override val isOpen: Boolean = javaxElem.isOpen

	override val isUnnamed: Boolean = javaxElem.isUnnamed

	override val directives: List<ModuleElement.Directive> = javaxElem.directives

	override val enclosedElements: Set<KJPackageElement>
		get() = javaxElem.enclosedElements.map { it.toKJElement(processingEnv) as KJPackageElement }.toSet()

	override fun asJavaxElement(): ModuleElement = javaxElem

	override val qualifiedName: String = javaxElem.qualifiedName.toString()
}