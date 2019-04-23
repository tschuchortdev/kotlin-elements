package com.tschuchort.kotlinelements.mixins

import com.tschuchort.kotlinelements.KJTypeMirror

interface HasReceiver {
	val receiverType: KJTypeMirror?
}