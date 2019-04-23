package com.tschuchort.kotlinelements.mixins

import com.tschuchort.kotlinelements.*
import com.tschuchort.kotlinelements.kotlin.KotlinTypeParameterElement
import com.tschuchort.kotlinelements.kotlin.MetadataContext
import com.tschuchort.kotlinelements.zipWith
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeParameterElement


/**
 * A Kotlin element that can have type parameters
 */
interface HasTypeParameters {
	val typeParameters: List<KJTypeParameterElement>
}
