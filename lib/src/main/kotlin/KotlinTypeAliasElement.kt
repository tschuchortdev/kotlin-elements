package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.hasAnnotations
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.metadata.visibility
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Name
import javax.lang.model.type.TypeMirror

/**
 * A kotlin type alias
 */
class KotlinTypeAliasElement internal constructor(
		/**
		 * If the Kotlin type alias has annotations the Kotlin compiler will generate
		 * an empty parameterless void-returning synthetic method named
		 * "aliasName$annotations" to hold the annotations
		 */
		val javaAnnotationHolderElement: ExecutableElement?,
		protoTypeAlias: ProtoBuf.TypeAlias,
		private val protoTypeTable: ProtoBuf.TypeTable,
		private val protoNameResolver: NameResolver,
		override val enclosingElement: KotlinElement,
		processingEnv: ProcessingEnvironment
) : KotlinElement(processingEnv), HasKotlinVisibility, KotlinParameterizable {

	init {
		assert(protoTypeAlias.hasAnnotations == (javaAnnotationHolderElement != null))
	}

	val underlyingType: TypeMirror = TODO("alias underlying type")

	val expandedType: TypeMirror = TODO("alias expanded type")

	override val visibility: KotlinVisibility = KotlinVisibility.fromProtoBuf(protoTypeAlias.visibility!!)

	override val simpleName: Name = processingEnv.elementUtils.getName(protoNameResolver.getString(protoTypeAlias.name))

	override val typeParameters: List<KotlinTypeParameterElement>
		get() = TODO("type alias type parameters")

	override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>): Array<A> {
		return javaAnnotationHolderElement?.getAnnotationsByType(annotationType)
			   ?: java.lang.reflect.Array.newInstance(annotationType, 0) as Array<A>
	}

	override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A? {
		return javaAnnotationHolderElement?.getAnnotation(annotationType)
	}

	override fun getAnnotationMirrors(): List<AnnotationMirror> {
		return javaAnnotationHolderElement?.annotationMirrors
			   ?: emptyList()
	}

	override fun asType(): TypeMirror = underlyingType

	override fun equals(other: Any?): Boolean
			= if(other is KotlinTypeAliasElement)
		other.enclosingElement == enclosingElement && other.simpleName == simpleName
	else
		false

	override fun hashCode(): Int = Objects.hash(enclosingElement, simpleName)

	override fun toString(): String
			= simpleName.toString() + typeParameters.joinToString(", ", "<", ">")

}