//TODO("handle JvmName for Kotlin files
@file:JvmName("RenamedFile")

package com.tschuchort.kotlinelements

//TODO("test inline classes")
//TODO("test functions accepting inline classes")

/*@ClassAnnotation
data class A<out T, in S, U : Integer, V : Int, W>(val a: Integer) {
	companion object Comp {
	}
}

*/

class A {
}

interface I {
	fun bar(): String
}

@ClassAnnotation
enum class E(val prop: A) : I {
	E1(A()) {
		@FunctionAnnotation
		override fun bar() = "E1"
	}, E2(A()) {
		override fun bar() = "E1"
	};

	fun foo() {}
}

/*
//TODO("handle illegal java names")
fun `illegal$name`() {}


@ClassAnnotation
open class B {

	//@PropertyAnnotation
	var y = 4
		@Deprecated("", level = DeprecationLevel.HIDDEN) get
		set(value) {}
}


@ClassAnnotation
class O<T>(t: T) {
	//@PropertyAnnotation
	private val t: T = t // visibility for t is PRIVATE_TO_THIS

	private var x = 3

	//@FunctionAnnotation
	private fun foo():T = t

	//@FunctionAnnotation
	fun bar():T = t
}

@ClassAnnotation
class C : B(Integer(1)) {

	//@ClassAnnotation
	inner class InnerClass

	//@FunctionAnnotation
	//@JvmName("differentJvmFunName")
	//fun kotlinFunName() {}

	//@PropertyAnnotation
	//var normalProp = 4
		//@JvmName("renamedSetter") set(value) {}

	@PropertyAnnotation
	private var z = "hello"

	@PropertyAnnotation
	inline private var z2
		get() = "hello"
		inline set(value) {}

	@PropertyAnnotation
	private var z3 = "hello"
		set


	//@get:GetterAnnotation
	@get:JvmName("differentJvmGetterName")
	var kotlinPropName
		set(value) {}
		get() = 3
}

fun five() = 5

@ClassAnnotation
class Q {
	init {

	}

	 companion object {
		 val x = 3
		 init {

		 }
	 }
}

@ClassAnnotation
class InternalMembers internal constructor(s: String) {
	internal fun internalFoo() {}

	internal val internalProp
		get() = 3
}

@ClassAnnotation
class D {
	companion object {
		@JvmStatic
		fun get(f: Float): D = D()
	}

	val x
		get() = five()


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

@ClassAnnotation
class E constructor(x: String, y: SomeClass){
	fun foo(x: String, y: GenericClass<Int>, z: GenericClass<*>) {}
}

open class SomeClass {
	//@PropertyAnnotation
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

//@ClassAnnotation
class GenericClass<T>

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
*/
fun main(args: Array<String>) {
	println("hello")
}