package com.tschuchort.kotlinelements.mixins

import com.tschuchort.kotlinelements.KJJvmOverloadElement
import javax.lang.model.element.ExecutableElement

interface HasJvmOverloads {
	val jvmOverloadElements: Set<KJJvmOverloadElement>
}