package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.KotlinPackageMetadata
import me.eugeniomarletti.kotlin.metadata.jvm.jvmPackageModuleName
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement

/*class KotlinPackageElement internal constructor(
		private val element: PackageElement,
		metadata: KotlinPackageMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinElement(processingEnv), PackageElement {

	protected val protoPackage: ProtoBuf.Package = metadata.data.packageProto
	protected val protoNameResolver: NameResolver = metadata.data.nameResolver

	val jvmPackageModuleName: String? = protoPackage.jvmPackageModuleName?.let{ protoNameResolver.getString(it) }

	override fun isUnnamed(): Boolean = element.isUnnamed

	override fun getQualifiedName(): Name = element.qualifiedName

	companion object {
		fun get(element: PackageElement, processingEnv: ProcessingEnvironment): KotlinPackageElement? {
			return if (element is KotlinPackageElement)
				element
			else
				(element.kotlinMetadata as? KotlinPackageMetadata)?.let { metadata ->
					KotlinPackageElement(element, metadata, processingEnv)
				}
		}
	}

	override fun toString() = element.toString()
	override fun equals(other: Any?) = element.equals(other)
	override fun hashCode() = element.hashCode()
}*/