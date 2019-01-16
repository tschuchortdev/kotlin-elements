package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.jvm.jvmPackageModuleName
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

/**
 * A package that contains Kotlin code
 *
 * It doesn't necessarily contain _only_ Kotlin code
 */
class KotlinPackageElement internal constructor(
		val javaElement: PackageElement,
		metadata: KotlinPackageMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinElement(), AnnotatedConstruct by javaElement, KotlinQualifiedNameable,
	EnclosesKotlinElements, EnclosesKotlinTypes, EnclosesKotlinPackages, EnclosesKotlinFunctions,
	EnclosesKotlinProperties, EnclosesKotlinTypeAliases {

	val isUnnamed: Boolean = javaElement.isUnnamed

	override val qualifiedName: Name = javaElement.qualifiedName

	override val simpleName: Name = javaElement.simpleName

	override val enclosingElement: KotlinElement? by lazy {
		javaElement.enclosingElement?.run { asKotlin(processingEnv) as KotlinElement }
	}

	/**
	 * Elements enclosed by this package that aren't Kotlin elements
	 */
	val enclosedJavaElements: Set<Element> by lazy {
		javaElement.enclosedElements.asSequence().filter { !it.originatesFromKotlinCode() }.toSet()
	}

	val javaPackages: Set<PackageElement> by lazy {
		enclosedJavaElements.filter { it.kind == ElementKind.PACKAGE }
				.castList<PackageElement>().toSet()
	}

	val javaTypes: Set<TypeElement> by lazy {
		enclosedJavaElements.mapNotNull { it.asTypeElement() }.toSet()
	}

	override val enclosedKotlinElements: Set<KotlinElement>
		get() = enclosedElementsDelegate.kotlinElements

	override val functions: Set<KotlinFunctionElement>
		get() = enclosedElementsDelegate.functions

	override val properties: Set<KotlinPropertyElement>
		get() = enclosedElementsDelegate.properties

	override val types: Set<KotlinTypeElement>
		get() = enclosedElementsDelegate.types

	override val kotlinPackages: Set<KotlinPackageElement>
		get() = enclosedElementsDelegate.packages

	override val typeAliases: Set<KotlinTypeAliasElement>
		get() = enclosedElementsDelegate.typeAliases

	override fun asType(): TypeMirror = javaElement.asType()

	override fun toString() = javaElement.toString()
	override fun equals(other: Any?) = javaElement.equals(other)
	override fun hashCode() = javaElement.hashCode()

	private val protoPackage: ProtoBuf.Package = metadata.data.packageProto
	private val protoTypeTable: ProtoBuf.TypeTable = protoPackage.typeTable
	private val protoNameResolver: NameResolver = metadata.data.nameResolver

	private val enclosedElementsDelegate = EnclosedElementsDelegate(
			enclosingKtElement = this,
			protoTypeAliases = protoPackage.typeAliasList,
			protoProps = protoPackage.propertyList,
			protoCtors = emptyList(),
			protoFunctions = protoPackage.functionList,
			companionSimpleName = null,
			enclosedJavaElems = javaElement.enclosedElements,
			protoNameResolver = protoNameResolver,
			protoTypeTable = protoTypeTable,
			processingEnv = processingEnv
	)

	private val jvmModuleName: String? = protoPackage.jvmPackageModuleName?.let{ protoNameResolver.getString(it) }
}