package com.tschuchort.kotlinelements

//@ClassAnnotation
data class A<out T, in S, U : Integer, V : Int, W>(val a: Integer) {
	companion object Comp {
	}
}

//@ClassAnnotation
open class B(val a: Integer) {
	companion object {
	}
}

class C : B(Integer(1)) {

	//@ClassAnnotation
	inner class InnerClass
}

fun five() = 5

//@ClassAnnotation
class D {
	companion object {
		@JvmStatic
		fun get(f: Float): D = D()
	}

	val x
		get() = five()

	var y = 6

	var z: Int
		get() = 5
		set(@ParameterAnnotation value) {}

	var isXx: Int
		get() = 5
		set(value) {}

	var hasYy: Int
		get() = 5
		set(value) {}

	fun memberFoo(@ParameterAnnotation param: Int) {
		val localVar = 1

		fun localFoo(@ParameterAnnotation localParam: Int) {

		}
	}
}

class SomeClass

//@ClassAnnotation
enum class EnumClassWithCtor(x: SomeClass) {
}

//@ClassAnnotation
enum class EnumClass { }

@ClassAnnotation
annotation class Ann(val x: String)

//@FunctionAnnotation
inline fun <reified T, S : Integer, U : Int> foo() {}

//@FunctionAnnotation
fun bar() {
	// local annotations don't get processed :/
	//@FunctionAnnotation
	fun localBar() {}

	//@ClassAnnotation
	class LocalClass {
	}
}

var x: @TypeAnnotation Integer? = null

fun main(args: Array<String>) {
	println("hello")
	A<Int, Int, Integer, Int, Int>(Integer(1))
	x = null
}