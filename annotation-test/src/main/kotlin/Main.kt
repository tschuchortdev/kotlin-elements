package com.tschuchort.kotlinelements

//@ClassAnnotation
data class A<out T, in S, U : Integer, V : Int, W>(val a: Integer) {
	companion object Comp {
	}
}

//@ClassAnnotation
data class B(val a: Integer) {
	companion object {
	}
}

//@ClassAnnotation
class C {

	//@ClassAnnotation
	inner class InnerClass
}

@ClassAnnotation class D {
	fun memberFoo(@ParameterAnnotation param: Int) {
		val localVar = 1

		fun localFoo(@ParameterAnnotation localParam: Int) {

		}
	}
}

@ClassAnnotation
enum class EnumClass { ENUM_A, ENUM_B }

@FunctionAnnotation
inline fun <reified T, S : Integer, U : Int> foo() {}

@FunctionAnnotation
fun bar() {
	// local annotations don't get processed :/
	@FunctionAnnotation
	fun localBar() {}

	@ClassAnnotation
	class LocalClass {
	}
}

var x: @TypeAnnotation Integer? = null

fun main(args: Array<String>) {
	println("hello")
	A<Int, Int, Integer, Int, Int>(Integer(1))
	x = null
}