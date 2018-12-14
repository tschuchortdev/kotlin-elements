package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.KotlinMultiFileClassPartMetadata
import me.eugeniomarletti.kotlin.metadata.KotlinSyntheticClassMetadata
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement


/**
 * A class that contains default implementations of methods
 * in a Kotlin interface
 */
class KotlinInterfaceDefaultImplElement(
		element: TypeElement,
		metadata: KotlinSyntheticClassMetadata
) : KotlinCompatElement(), TypeElement by element

/**
 * A synthetic class that holds all the elements for top-level properties, functions
 * and type aliases, since only classes can be top-level in Java
 */
class KotlinMultiFileClassPartElement(
		element: TypeElement,
		metadata: KotlinMultiFileClassPartMetadata
) : KotlinCompatElement(), TypeElement by element

/**
 * Unidentified [KotlinCompatElement]
 */
class OtherKotlinCompatElement(element: Element) : KotlinCompatElement(), Element by element