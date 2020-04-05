package com.tschuchort.kotlinelements.mixins

import com.tschuchort.kotlinelements.KJElement

interface ConvertibleToElement {
    fun asElement(): KJElement
}