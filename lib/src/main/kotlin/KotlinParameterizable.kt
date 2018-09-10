package com.tschuchort.kotlinelements

import javax.lang.model.element.Parameterizable

interface KotlinParameterizable : Parameterizable {
	override fun getTypeParameters(): List<KotlinTypeParameterElement>
}