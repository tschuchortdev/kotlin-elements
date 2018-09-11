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

	private var x = 3

	@PropertyAnnotation
	var y = 4
	   @Deprecated("", level = DeprecationLevel.HIDDEN) get
	   set(value) {}

	init {
		val z = y
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

//@ClassAnnotation
class E constructor(x: String, y: SomeClass){
	fun foo(x: String, y: GenericClass<Int>, z: GenericClass<*>) {}
}

open class SomeClass {
	//@get:GetterAnnotation
	//@set:SetterAnnotation
	val varrr
		get() = 4

	//@PropertyAnnotation
	inline val inlineVal get() = 5

	//@PropertyAnnotation
	val delegatedVal by lazy { 6 }

	init {
		println("init SomeClass")
	}
}

open class GenericClass<T>

//@ClassAnnotation
enum class EnumClassWithCtor(x: SomeClass) {
}

//@ClassAnnotation
enum class EnumClass { }

//@ClassAnnotation
annotation class Ann(val x: String, val y: String) {
}

//@ClassAnnotation
interface Interf<in T, S : SomeClass> {
	fun foo()

	fun bar() {
		println("bar")
	}
}

//@FunctionAnnotation
inline fun <reified T, S : Integer, U : Int> foo() {}

//@FunctionAnnotation
fun bar(a: String, b: String = "test") {
	// local annotations don't get processed :/
	//@FunctionAnnotation
	fun localBar() {}

	//@ClassAnnotation
	class LocalClass {
	}
}

fun bar(a: String) {

}

@JvmOverloads
//@FunctionAnnotation
fun fooWithDefault(a: String, b: Int, y: String = "world") {

}

@JvmOverloads
//@FunctionAnnotation
fun fooWithDefault(a: String, y: Float = 2.0f) {

}

//@FunctionAnnotation
fun SomeClass.ext(a: String) {

}

//var x: @TypeAnnotation Integer? = null

fun main(args: Array<String>) {
	println("hello")
	A<Int, Int, Integer, Int, Int>(Integer(1))
}