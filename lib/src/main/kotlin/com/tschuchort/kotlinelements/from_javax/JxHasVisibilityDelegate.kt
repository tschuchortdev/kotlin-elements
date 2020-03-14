package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.mixins.HasVisibility
import com.tschuchort.kotlinelements.mixins.KJVisibility
import javax.lang.model.element.Element

internal class JxHasVisibilityDelegate(private val javaxElem: Element) : HasVisibility {
	override val visibility: KJVisibility = KJVisibility.fromJavax(javaxElem)
}