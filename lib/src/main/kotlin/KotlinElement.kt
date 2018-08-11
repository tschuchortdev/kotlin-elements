package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.KotlinPackageMetadata
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.lang.model.element.Element

open class KotlinElement internal constructor(
		element: Element,
		protected val protoNameResolver: NameResolver) : Element by element {

	companion object {
		fun get(element: Element): KotlinElement? {
			return getNameResolver(element)?.let { nameResolver -> KotlinElement(element, nameResolver) }
		}

		internal fun getNameResolver(elem: Element): NameResolver? {
			val metadata = elem.kotlinMetadata
			return when(metadata) {
				is KotlinPackageMetadata -> metadata.data.nameResolver
				is KotlinClassMetadata -> metadata.data.nameResolver
				else -> elem.enclosingElement?.let(::getNameResolver)
			}
		}
	}
}

fun Element.isKotlinElement() = KotlinElement.getNameResolver(this) != null