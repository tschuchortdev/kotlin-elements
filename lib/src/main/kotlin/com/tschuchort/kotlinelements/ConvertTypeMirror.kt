package com.tschuchort.kotlinelements

import com.tschuchort.kotlinelements.mixins.KJOrigin
import kotlinx.metadata.ClassName
import kotlinx.metadata.isLocal
import java.lang.IllegalArgumentException
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

fun TypeMirror.toKJTypeMirror(processingEnv: ProcessingEnvironment): KJTypeMirror? {

}

internal fun KJTypeMirror.Companion.fromKmClassName(className: ClassName): KJTypeMirror {
    if (className.isLocal)
        throw IllegalArgumentException("Can not find KJTypeMirror for class name ${className} because it's a local class")
    else
        return KJTypeMirror.fromQualifiedName(className.replace("/", "."))
}

fun KJTypeMirror.Companion.fromQualifiedName(qualifiedName: String): KJTypeMirror {

}

fun KJTypeMirror.Companion.fromJavaClass(clazz: Class<*>): KJTypeMirror {

}

fun KJTypeMirror.Companion.fromKotlinClass(clazz: KClass<*>): KJTypeMirror {

}