package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.KJOrigin
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.PackageElement

internal class JxPackageElement(
		private val javaxElem: PackageElement,
		override val origin: KJOrigin,
		private val processingEnv: ProcessingEnvironment
) : KJPackageElement(), AnnotatedConstruct by javaxElem {
	override val isUnnamed: Boolean = javaxElem.isUnnamed

	override fun asJavaxElement(): PackageElement = javaxElem

	override val qualifiedName: String = javaxElem.qualifiedName.toString()

	override val enclosingElement: KJElement? by lazy {
		javaxElem.toKJElement(processingEnv)
	}

	override val enclosedElements: Set<KJElement> by lazy {
		javaxElem.enclosedElements.map { it.toKJElement(processingEnv) }.toSet()
	}

	override val simpleName: String = javaxElem.simpleName.toString()

	override fun asTypeMirror(): KJPackageType
			= javaxElem.asType().toKJTypeMirror(processingEnv) as KJPackageType
}


