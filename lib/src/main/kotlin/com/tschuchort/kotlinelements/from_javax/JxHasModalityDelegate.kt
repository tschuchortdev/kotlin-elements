package com.tschuchort.kotlinelements.from_javax

import com.tschuchort.kotlinelements.mixins.HasModality
import com.tschuchort.kotlinelements.mixins.KJModality
import javax.lang.model.element.Element

internal class JxHasModalityDelegate(
		val javaxElem: Element
) : HasModality {
	override val modality: KJModality = KJModality.fromJavax(javaxElem)
}