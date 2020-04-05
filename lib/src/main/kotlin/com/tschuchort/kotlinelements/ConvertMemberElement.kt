package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.KJTypeElementFactory.createDelegate
import com.tschuchort.kotlinelements.mixins.KJOrigin
import com.tschuchort.kotlinelements.mixins.KJVisibility
import kotlinx.metadata.*
import kotlinx.metadata.jvm.jvmFlags
import kotlinx.metadata.jvm.syntheticMethodForAnnotations
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

private typealias JvmSignature = String


internal sealed class MemberElement {
	abstract val elem: Element

	data class Variable(override val elem: VariableElement) : MemberElement() {
	}

	data class Executable(override val elem: ExecutableElement) : MemberElement() {
	}
}

internal fun convertKotlinMembers(enclosingElem: TypeElement,
								  kmTypeAliases: List<KmTypeAlias> = emptyList(),
								  kmProperties: List<KmProperty> = emptyList(),
								  kmConstructors: List<KmConstructor> = emptyList(),
								  kmFunctions: List<KmFunction> = emptyList(),
								  processingEnv: ProcessingEnvironment) {
	val convertedMembers = hashMapOf<Element, KJElement>()
	val unconvertedMembers = hashMapOf<JvmSignature, Element>()

	for (elem in enclosingElem.enclosedElements) {
		val signature = when (elem.kind!!) {
			ElementKind.METHOD,
			ElementKind.CONSTRUCTOR -> (elem as ExecutableElement).jvmMethodSignature(processingEnv.typeUtils)

			ElementKind.FIELD -> (elem as VariableElement).jvmFieldSignature(processingEnv.typeUtils)

			else -> null
		}

		if (signature != null)
			unconvertedMembers[signature] = elem

		for (kmTypeAlias in kmTypeAliases) {
			kmTypeAlias.name
		}
	}
}

internal fun KJTypeAliasElement.Companion.create(kmTypeAlias: KmTypeAlias,
									   processingEnv: ProcessingEnvironment): KJTypeAliasElement {

	val annotationsDelegate = object : AnnotatedConstruct {
		override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>?): Array<A> {
		}

		override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A {
		}

		override fun getAnnotationMirrors(): MutableList<out AnnotationMirror> {
		}
	}

	val visibility = KJVisibility.fromKm(kmTypeAlias.flags)

	return object : KJTypeAliasElement(), AnnotatedConstruct by annotationsDelegate {
		override val enclosingElement: KJFileFacadeElement
			get() = TODO("Not yet implemented")

		override val simpleName: String = kmTypeAlias.name

		override fun asTypeMirror(): KJTypeAlias {
			TODO("KmTypeAlias to KJTypeMirror")
		}

		override fun asJavaxElement() = object : Element, AnnotatedConstruct by annotationsDelegate {
			override fun getModifiers() = setOfNotNull(visibility.asJavaxModifier())

			override fun getSimpleName() = processingEnv.elementUtils.getName(kmTypeAlias.name)

			override fun getKind() = ElementKind.OTHER

			override fun asType(): TypeMirror {

			}

			override fun getEnclosingElement(): Element = TODO()

			override fun <R : Any?, P : Any?> accept(v: ElementVisitor<R, P>, p: P): R {
				return v.visitUnknown(this, p)
			}

			override fun getEnclosedElements() = emptyList<Element>()
		}

		override val typeParameters: List<KJTypeParameterElement>
			get() = TODO("Not yet implemented")


		override val qualifiedName: String
			get() = TODO("Not yet implemented")

		override val visibility: KJVisibility
			get() = TODO("Not yet implemented")

	}
}