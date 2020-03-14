package com.tschuchort.kotlinelements.from_metadata

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.mixins.KJVariance
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.Element
import javax.lang.model.type.*

internal class KotlinBasicType(
		override val kind: Kind,
		override val nullable: Boolean,
		private val annotationsDelegate: AnnotatedConstruct,
		val processingEnv: ProcessingEnvironment
) : KJBasicType(), AnnotatedConstruct by annotationsDelegate {

	override fun toJavaxTypeMirror(): TypeMirror {
		val primitiveType = processingEnv.typeUtils.getPrimitiveType(kind.javaxPrimitiveTypeKind)

		return if (!nullable)
			primitiveType
		else
			processingEnv.typeUtils.boxedClass(primitiveType).asType()
	}

	companion object {
		fun createOrNull(
				qualifiedName: String, nullable: Boolean,
				annotationsDelegate: AnnotatedConstruct,
				processingEnv: ProcessingEnvironment
		): KotlinBasicType? {
			val kind = Kind.values().singleOrNull {
				it.kotlinQualifiedName == qualifiedName
			}

			return if(kind != null)
				KotlinBasicType(kind, nullable, annotationsDelegate, processingEnv)
			else
				null
		}
	}
}

internal class KotlinTypeVariable(
		private val javaxType: TypeVariable? = null,
		override val simpleName: String,
		override val variance: KJVariance,
		upperBound: KJTypeMirror?,
		lowerBound: KJTypeMirror?,
		private val annotationsDelegate: AnnotatedConstruct,
		val processingEnv: ProcessingEnvironment
) : KJTypeVariable(), AnnotatedConstruct by annotationsDelegate {

	override fun asElement(): KJTypeParameterElement {
	}

	override fun toJavaxTypeMirror(): TypeVariable = javaxType
		?: object : TypeVariable, AnnotatedConstruct by annotationsDelegate {
			override fun getUpperBound(): TypeMirror
					= this@KotlinTypeVariable.upperBound.toJavaxTypeMirror()

			override fun getLowerBound(): TypeMirror
					= this@KotlinTypeVariable.upperBound.toJavaxTypeMirror()

			override fun getKind() = TypeKind.TYPEVAR

			override fun <R : Any?, P : Any?> accept(v: TypeVisitor<R, P>, p: P): R
					= v.visitTypeVariable(this, p)

			override fun asElement(): Element {
			}
		}
}

internal class KotlinWildcardType(
		private val javaxType: WildcardType? = null,
		override val kind: Kind,
		annotationsDelegate: AnnotatedConstruct,
		val processingEnv: ProcessingEnvironment
) : KJWildcardType(), AnnotatedConstruct by annotationsDelegate {

	override fun toJavaxTypeMirror(): TypeMirror = javaxType
		?: processingEnv.typeUtils.getWildcardType(
				(kind as? Kind.Bounded.Out)?.bound?.toJavaxTypeMirror(),
				(kind as? Kind.Bounded.In)?.bound?.toJavaxTypeMirror()
		)
}

internal class KotlinIntersectionType(
		private val javaxType: IntersectionType? = null,
		override val bounds: List<KJTypeMirror>,
		private val annotationsDelegate: AnnotatedConstruct,
		private val processingEnv: ProcessingEnvironment
) : KJIntersectionType(), AnnotatedConstruct by annotationsDelegate {

	override fun toJavaxTypeMirror(): IntersectionType = javaxType
		?: object : IntersectionType, AnnotatedConstruct by annotationsDelegate {
			override fun getKind() = TypeKind.INTERSECTION

			override fun <R : Any?, P : Any?> accept(v: TypeVisitor<R, P>, p: P): R = v.visitIntersection(this, p)

			override fun getBounds(): List<TypeMirror> =
				this@KotlinIntersectionType.bounds.map { it.toJavaxTypeMirror() }
		}
}

internal class KotlinTypeAlias(
		override val nullable: Boolean,
		override val typeArguments: List<KJTypeMirror>,

		annotationsDelegate: AnnotatedConstruct,
		val processingEnv: ProcessingEnvironment) : KJTypeAlias(), AnnotatedConstruct by annotationsDelegate {

	override fun asElement(): KJTypeElement {
	}

	override fun toJavaxTypeMirror(): DeclaredType {
	}

	override val underlyingType: KJTypeMirror
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

	override val expandedType: KJTypeMirror
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

	override val simpleName: String
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

	override val qualifiedName: String
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

}