package com.tschuchort.kotlinelements.kotlin

import javax.lang.model.element.Element
import com.tschuchort.kotlinelements.KJElement


/**
 * This interface is implemented by all Kotlin elements to provide functionality to
 * lookup the corresponding [KJElement] for an enclosed Javax [Element] because only
 * the enclosing element has enough information to construct them and we want to avoid
 * iterating through all enclosed elements when trying to find the corresponding one.
*/
internal interface EnclosesKotlinElements {
	val enclosedElements: Set<KJElement>
	fun lookupEnclosedKJElementFor(enclosedJavaxElem: Element): KJElement
}