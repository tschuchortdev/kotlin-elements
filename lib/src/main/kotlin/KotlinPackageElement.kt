package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.KotlinPackageMetadata
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement

open class KotlinPackageElement internal constructor(
		private val element: PackageElement,
		metadata: KotlinPackageMetadata,
		processingEnv: ProcessingEnvironment)
	: KotlinElement(element, metadata.data.nameResolver, processingEnv), PackageElement {

	protected val protoPackage: ProtoBuf.Package = metadata.data.packageProto

	override fun isUnnamed(): Boolean = element.isUnnamed

	override fun getQualifiedName(): Name = element.qualifiedName

	companion object {
		fun get(element: PackageElement, processingEnv: ProcessingEnvironment): KotlinPackageElement?
				= if (element is KotlinPackageElement)
					element
				else
					(element.kotlinMetadata as? KotlinPackageMetadata)?.let { metadata ->
						KotlinPackageElement(element, metadata, processingEnv)
					}
	}

	override fun toString() = element.toString()
}