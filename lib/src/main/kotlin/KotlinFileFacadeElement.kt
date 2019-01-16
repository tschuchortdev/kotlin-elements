package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

class KotlinFileFacadeElement private constructor(
		override val javaElement: TypeElement,
		private val packageData: PackageData?,
		val processingEnv: ProcessingEnvironment
) : KotlinCompatElement(javaElement), EnclosesKotlinElements,
	EnclosesKotlinProperties, EnclosesKotlinFunctions, EnclosesKotlinTypes {

	internal constructor(javaElement: TypeElement, metadata: KotlinFileMetadata,
						 processingEnv: ProcessingEnvironment)
			: this(javaElement, metadata.data, processingEnv)

	internal constructor(javaElement: TypeElement, metadata: KotlinMultiFileClassFacadeMetadata,
						 processingEnv: ProcessingEnvironment)
			: this(javaElement, null, processingEnv)

	val isMultiFileClassFacade: Boolean = (packageData == null)

	override val enclosingElement: KotlinPackageElement
		get() = javaElement.enclosingElement.asKotlin(processingEnv) as KotlinPackageElement

	override val enclosedKotlinElements: Set<KotlinElement>
		get() = enclosedElementsDelegate?.kotlinElements ?: emptySet()

	override val functions: Set<KotlinFunctionElement>
		get() = enclosedElementsDelegate?.functions ?: emptySet()

	override val properties: Set<KotlinPropertyElement>
		get() = enclosedElementsDelegate?.properties ?: emptySet()

	override val types: Set<KotlinTypeElement>
		get() = enclosedElementsDelegate?.types ?: emptySet()

	private val enclosedElementsDelegate = packageData?.packageProto?.let { facadeProto ->
		EnclosedElementsDelegate(
				enclosingKtElement = enclosingElement,
				protoTypeAliases = facadeProto.typeAliasList,
				protoProps = facadeProto.propertyList,
				protoCtors = emptyList(),
				protoFunctions = facadeProto.functionList,
				companionSimpleName = null,
				enclosedJavaElems = javaElement.enclosedElements,
				protoNameResolver = packageData.nameResolver,
				protoTypeTable = facadeProto.typeTable,
				processingEnv = processingEnv
		)
	}
}

/**
 * A synthetic class that holds all the elements for top-level properties, functions
 * and type aliases, since only classes can be top-level in Java
 */
class KotlinMultiFileClassPartElement(
	    override val javaElement: TypeElement,
		metadata: KotlinMultiFileClassPartMetadata,
		val processingEnv: ProcessingEnvironment
) : KotlinCompatElement(javaElement) {
	override val enclosingElement: KotlinRelatedElement
		get() = javaElement.enclosingElement.asKotlin(processingEnv)!!
}