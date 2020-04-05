package com.tschuchort.kotlinelements

import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.AnnotationMirror

internal object NoAnnotationsDelegate : AnnotatedConstruct {
	@Suppress("UNCHECKED_CAST")
	override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>?): Array<A> =
		java.lang.reflect.Array.newInstance(annotationType, 0) as Array<A>

	override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A? = null

	override fun getAnnotationMirrors(): List<AnnotationMirror> = emptyList()
}