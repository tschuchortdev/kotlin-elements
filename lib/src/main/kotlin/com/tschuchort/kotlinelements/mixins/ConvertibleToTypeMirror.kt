package com.tschuchort.kotlinelements.mixins

import com.tschuchort.kotlinelements.KJTypeMirror

interface ConvertibleToTypeMirror {
    fun asTypeMirror(): KJTypeMirror?
}