package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import mixins.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Name
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/** A Kotlin file facade. File facades are needed because Kotlin files can contain top-level
 * free functions, but Java doesn't support those, so a facade class is generated to hold them */
class KotlinFileFacadeElement private constructor(
		final override val javaElement: TypeElement,
		private val packageData: PackageData?,
		private val processingEnv: ProcessingEnvironment
) : KotlinCompatElement(javaElement), EnclosesKotlinElements, KotlinQualifiedNameable,
	EnclosesKotlinProperties, EnclosesKotlinFunctions, EnclosesKotlinTypeAliases {

	internal constructor(javaElement: TypeElement, metadata: KotlinFileMetadata,
						 processingEnv: ProcessingEnvironment)
			: this(javaElement, metadata.data, processingEnv)

	internal constructor(javaElement: TypeElement, metadata: KotlinMultiFileClassFacadeMetadata,
						 processingEnv: ProcessingEnvironment)
			: this(javaElement, null, processingEnv)

	val isMultiFileClassFacade: Boolean = (packageData == null)

	override val qualifiedName: Name = javaElement.qualifiedName

	override val enclosingElement: KotlinPackageElement
		get() = javaElement.enclosingElement.asKotlin(processingEnv) as KotlinPackageElement

	override val enclosedKotlinElements: Set<KotlinElement>
		get() = enclosedElementsDelegate?.kotlinElements ?: emptySet()

	override val functions: Set<KotlinFunctionElement>
		get() = enclosedElementsDelegate?.functions ?: emptySet()

	override val properties: Set<KotlinPropertyElement>
		get() = enclosedElementsDelegate?.properties ?: emptySet()

	override val typeAliases: Set<KotlinTypeAliasElement>
		get() = enclosedElementsDelegate?.typeAliases ?: emptySet()

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
	    final override val javaElement: TypeElement,
		metadata: KotlinMultiFileClassPartMetadata,
		val processingEnv: ProcessingEnvironment
) : KotlinCompatElement(javaElement), KotlinQualifiedNameable {

	override val qualifiedName: Name = javaElement.qualifiedName

	override val enclosingElement: KotlinRelatedElement
		get() = javaElement.enclosingElement.asKotlin(processingEnv)!!
}