package com.tschuchort.kotlinelements

@ClassAnnotation
data class A<out T, in S, U, V, W>(val a: Integer) {
	companion object Comp {

	}
}

data class B(val a: Integer)

@FunctionAnnotation
inline fun <reified T, S, U> foo() {}

fun bar() {
	@FunctionAnnotation
	fun innerBar() {}
}

var x: @TypeAnnotation Integer? = null

fun main(args: Array<String>) {
	println("hello")
	A<Int, Int, Int, Int, Int>(Integer(1))
	x = null
}