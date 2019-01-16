package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.jvm.jvmClassModuleName
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.Name
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror


/**
 * An element that declares a new type like a class, object, interface or annotation class
 */
abstract class KotlinTypeElement internal constructor(
		val javaElement: TypeElement,
		metadata: KotlinClassMetadata,
		processingEnv: ProcessingEnvironment
) : KotlinElement(), KotlinParameterizable, KotlinQualifiedNameable,
	HasKotlinVisibility, HasKotlinModality, AnnotatedConstruct by javaElement {

	protected val protoClass: ProtoBuf.Class = metadata.data.classProto
	protected val protoNameResolver: NameResolver = metadata.data.nameResolver
	protected val protoTypeTable: ProtoBuf.TypeTable = protoClass.typeTable

	protected val jvmModuleName: String? = protoClass.jvmClassModuleName?.let{ protoNameResolver.getString(it) }

	override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoClass.visibility!!)

	override val modality: KotlinModality = KotlinModality.fromProtoBuf(protoClass.modality!!)

	//TODO("check if objects are inner classes")
	val isInner: Boolean = protoClass.isInnerClass

	/**
	 * Whether this class has the `expect` keyword
	 *
	 * An expect class is a class declaration with actual definition in a different
	 * file, akin to a declaration in a header file in C++. They are used in multiplatform
	 * projects where different implementations are needed depending on target platform
	 */
	val isExpect: Boolean = protoClass.isExpectClass

	/**
	 * Whether this class has the `external` keyword
	 *
	 * An external class is a class declaration with the actual definition in native
	 * code, similar to the `native` keyword in Java
	 */
	val isExternal: Boolean = protoClass.isExternalClass

	override val enclosingElement: KotlinElement by lazy {
		javaElement.enclosingElement.asKotlin(processingEnv) as KotlinElement
	}

	/**
	 * The interfaces implemented by this type
	 */
	val interfaces: List<TypeMirror> = javaElement.interfaces

	/**
	 * The superclass extended by this type
	 */
	val superclass: TypeMirror = javaElement.superclass

	override val typeParameters: List<KotlinTypeParameterElement>
		get() = parameterizableDelegate.typeParameters

	override fun asType(): TypeMirror = javaElement.asType()

	override val qualifiedName: Name = javaElement.qualifiedName

	override val simpleName: Name = javaElement.simpleName

	override fun equals(other: Any?)
			= (other as? KotlinTypeElement)?.javaElement == javaElement

	override fun hashCode() = javaElement.hashCode()

	override fun toString() = javaElement.toString()

	private fun getCompanionSimpleName(): String? =
			if (protoClass.hasCompanionObjectName())
				protoNameResolver.getString(protoClass.companionObjectName)
			else
				null

	internal val enclosedElementsDelegate by lazy {
		// lazy to avoid leaking this in ctor
		EnclosedElementsDelegate(
				enclosingKtElement = this,
				protoTypeAliases = protoClass.typeAliasList,
				protoProps = protoClass.propertyList,
				protoCtors = protoClass.constructorList,
				protoFunctions = protoClass.functionList,
				companionSimpleName = getCompanionSimpleName(),
				enclosedJavaElems = javaElement.enclosedElements,
				protoNameResolver = protoNameResolver,
				protoTypeTable = protoTypeTable,
				processingEnv = processingEnv
		)
	}

	private val parameterizableDelegate by lazy {
		// lazy to avoid leaking this in ctor
		KotlinParameterizableDelegate(this, protoClass.typeParameterList,
				javaElement.typeParameters, protoNameResolver)
	}
}