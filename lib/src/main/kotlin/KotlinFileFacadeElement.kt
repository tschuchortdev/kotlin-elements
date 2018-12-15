package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

class KotlinFileFacadeElement private constructor(
		javaElement: TypeElement,
		private val packageData: PackageData?,
		processingEnv: ProcessingEnvironment
) : KotlinCompatElement(javaElement, processingEnv), EnclosesKotlinElements,
	EnclosesKotlinProperties, EnclosesKotlinFunctions, EnclosesKotlinTypes, TypeElement by javaElement {

	internal constructor(javaElement: TypeElement, metadata: KotlinFileMetadata,
						 processingEnv: ProcessingEnvironment)
			: this(javaElement, metadata.data, processingEnv)

	internal constructor(javaElement: TypeElement, metadata: KotlinMultiFileClassFacadeMetadata,
						 processingEnv: ProcessingEnvironment)
			: this(javaElement, null, processingEnv)

	val isMultiFileClassFacade: Boolean = (packageData == null)

	override val enclosingElement: KotlinPackageElement
		get() = super.enclosingElement as KotlinPackageElement

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
		element: TypeElement,
		metadata: KotlinMultiFileClassPartMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinCompatElement(element, processingEnv), TypeElement by element