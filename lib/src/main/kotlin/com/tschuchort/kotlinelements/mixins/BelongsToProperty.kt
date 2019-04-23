package com.tschuchort.kotlinelements.mixins

import com.tschuchort.kotlinelements.KJPropertyElement

interface BelongsToProperty {
	val correspondingProperty: KJPropertyElement
}