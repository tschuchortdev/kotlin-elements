package com.tschuchort.kotlinelements

import com.tschuchort.compiletesting.KotlinCompilation
import mixins.KotlinVisibility
import org.assertj.core.api.Assertions.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.NoType
import javax.lang.model.type.TypeKind

class KotlinConstructorElementTests {
	@Rule
	@JvmField
	val temporaryFolder = TemporaryFolder()

	private val elementTester = ElementTester(temporaryFolder)

	@Test
	fun `Simple name is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class KClass @SerializeElemForTesting constructor() {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.simpleName.toString()).isEqualTo("<init>")
	}

	@Test
	fun `Visibility is private`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class KClass @SerializeElemForTesting private constructor() {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PRIVATE)
	}

	@Test
	fun `Visibility is protected`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class KClass @SerializeElemForTesting protected constructor() {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PROTECTED)
	}

	@Test
	fun `Visibility is public by default`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class KClass @SerializeElemForTesting constructor() {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PUBLIC)
	}

	@Test
	fun `Visibility is internal`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class KClass @SerializeElemForTesting internal constructor() {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.INTERNAL)
	}

	@Test
	fun `Is primary`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class KClass @SerializeElemForTesting constructor() {
				constructor(x: Int) : this() {}
			}
        """.trimIndent()
				)
		)

		assertThat(elem.isPrimary).isTrue()
	}

	@Test
	fun `Is not primary`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class KClass constructor() {
				@SerializeElemForTesting
				constructor(x: Int) : this() {}
			}
        """.trimIndent()
				)
		)

		assertThat(elem.isPrimary).isFalse()
	}

	@Test
	fun `Has no parameters`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class KClass @SerializeElemForTesting constructor() {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.parameters).isEmpty()
	}

	@Test
	fun `Enum constructor has no parameters`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			enum class Enum @SerializeElemForTesting constructor() {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.parameters).isEmpty()
	}

	@Test
	fun `Has correct parameters`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class KClass @SerializeElemForTesting constructor(a: O, b: P) {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.parameters.map { it.simpleName.toString() }).containsExactly("a", "b")
		assertThat(elem.parameters.map { it.asType().toString() })
				.containsExactly(O::class.qualifiedName, P::class.qualifiedName)
	}

	@Test
	fun `Enum constructor has correct parameters`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			enum class Enum @SerializeElemForTesting constructor(a: O, b: P) {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.parameters.map { it.simpleName.toString() }).containsExactly("a", "b")
		assertThat(elem.parameters.map { it.asType().toString() })
				.containsExactly(O::class.qualifiedName, P::class.qualifiedName)
	}

	@Test
	fun `Has Java overloads`() {
		val elems = elementTester.getSerializedFrom(
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class KClass
				@SerializeElemForTesting
				@JvmOverloads
				constructor(a: O = O(), b: P = P()) {
			}
        """.trimIndent()
				)
		)

		val ctorElem = elems.singleOrNull { it is KotlinConstructorElement }
		val overloadElems = elems.mapNotNull { it as? KotlinExecutableElement.JavaOverload }

		assertThat(ctorElem).isNotNull
		assertThat(overloadElems).hasSize(2)

		assertThat(overloadElems.map { it.javaElement.parameters.map { it.simpleName.toString() } })
				.containsExactlyInAnyOrder(listOf("a"), emptyList())
	}

	@Test
	fun `Has vararg parameter`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class KClass @SerializeElemForTesting constructor(vararg a: O, b: Array<P>) {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.parameters).hasSize(2)
		assertThat(elem.parameters.map { it.simpleName.toString() })
				.containsExactly("a", "b")
		assertThat(elem.parameters.map { it.asType().toString() })
				.containsExactly(O::class.qualifiedName, Array<P>::class.java.canonicalName)
		assertThat(elem.parameters[0].isVararg).isTrue()
		assertThat(elem.parameters[1].isVararg).isFalse()
	}

	@Test
	fun `Outer class constructor has no receiver type`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class KClass @SerializeElemForTesting constructor() {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.receiverType).isInstanceOf(NoType::class.java)
		assertThat(elem.receiverType.kind).isEqualTo(TypeKind.NONE)

		assertThat((elem.asType() as ExecutableType).receiverType).isInstanceOf(NoType::class.java)
		assertThat((elem.asType() as ExecutableType).receiverType.kind).isEqualTo(TypeKind.NONE)
	}

	@Test
	fun `Inner class constructor has outer receiver type`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class Outer {
				inner class Inner @SerializeElemForTesting constructor() {
				}
			}
        """.trimIndent()
				)
		)

		assertThat(elem.receiverType.toString()).isEqualTo(elem.enclosingElement.asType().toString())
		assertThat((elem.asType() as ExecutableType).receiverType.toString())
				.isEqualTo(elem.enclosingElement.asType().toString())
	}

	@Test
	fun `Has correct return type`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinConstructorElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class KClass @SerializeElemForTesting constructor() {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.returnType).isInstanceOf(NoType::class.java)
		assertThat(elem.returnType.kind).isEqualTo(TypeKind.VOID)

		assertThat((elem.asType() as ExecutableType).returnType).isInstanceOf(NoType::class.java)
		assertThat((elem.asType() as ExecutableType).returnType.kind).isEqualTo(TypeKind.VOID)
	}
}
