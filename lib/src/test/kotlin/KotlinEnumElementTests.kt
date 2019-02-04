package com.tschuchort.kotlinelements

import com.tschuchort.compiletesting.KotlinCompilation
import mixins.KotlinModality
import org.assertj.core.api.Assertions.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class KotlinEnumElementTests {
	@Rule
	@JvmField
	val temporaryFolder = TemporaryFolder()

	private val elementTester = ElementTester(temporaryFolder)

	@Test
	fun `Can be converted to KotlinElement`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinEnumElement::class,
				KotlinCompilation.SourceFile(
						"Enum.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            enum class Enum
        """.trimIndent()
				)
		)

		assertThat(elem.simpleName.toString()).isEqualTo("Enum")
	}

	@Test
	fun `Modality is final`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinEnumElement::class,
				KotlinCompilation.SourceFile(
						"Enum.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            enum class Enum
        """.trimIndent()
				)
		)

		assertThat(elem.modality).isEqualTo(KotlinModality.FINAL)
	}

	@Test
	fun `Constructors can be converted`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinEnumElement::class,
				KotlinCompilation.SourceFile(
						"Enum.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            enum class Enum(val i: O, val j: P) {
				;
				constructor() : this(O(), P())
			}
        """.trimIndent()
				)
		)

		assertThat(elem.constructors).hasSize(2)

		assertThat(elem.constructors.map{ it.parameters.map { it.simpleName.toString() } })
				.containsExactly(listOf("i", "j"), listOf())

		assertThat(elem.constructors.map{ it.parameters.map { it.asType().toString() } })
				.containsExactly(listOf(O::class.qualifiedName!!, P::class.qualifiedName!!), listOf())
	}

	@Test
	fun `Has enum constants`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinEnumElement::class,
				KotlinCompilation.SourceFile(
						"Enum.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            enum class Enum {
				A {
					override fun foo() {}
				},

				B {
					override fun foo() {}
				};

				abstract fun foo()
			}
        """.trimIndent()
				)
		)

		assertThat(elem.enumConstants.map { it.simpleName.toString() })
				.containsExactlyInAnyOrder("A", "B")
	}

	@Test
	fun `Qualified name is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinEnumElement::class,
				KotlinCompilation.SourceFile(
						"Enum.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            enum class Enum
        """.trimIndent()
				)
		)

		assertThat(elem.qualifiedName.toString())
				.isEqualTo("com.tschuchort.kotlinelements.Enum")
	}

	@Test
	fun `Has companion`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinEnumElement::class,
				KotlinCompilation.SourceFile(
						"Enum.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            enum class Enum {
				;
				companion object
			}
        """.trimIndent()
				)
		)

		assertThat(elem.companion).isNotNull
	}

	@Test
	fun `Encloses nothing but default constructor`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinEnumElement::class,
				KotlinCompilation.SourceFile(
						"Enum.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            enum class Enum
        """.trimIndent()
				)
		)

		assertThat(elem.enclosedKotlinElements).hasSize(1)
		assertThat(elem.enclosedKotlinElements.first()).isInstanceOf(KotlinConstructorElement::class.java)
	}

	@Test
	fun `Has method`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"Enum.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            class Enum {
				;
				fun foo() {}
			}
        """.trimIndent()
				)
		)

		assertThat(elem.functions).hasSize(1)
		assertThat(elem.functions.first().simpleName.toString()).isEqualTo("foo")
	}

	@Test
	fun `Has property`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinEnumElement::class,
				KotlinCompilation.SourceFile(
						"Enum.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            enum class Enum {
				;
				val bar = 3
			}
        """.trimIndent()
				)
		)

		assertThat(elem.properties).hasSize(1)
		assertThat(elem.properties.first().simpleName.toString()).isEqualTo("bar")
	}

	@Test
	fun `Has nested class`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinEnumElement::class,
				KotlinCompilation.SourceFile(
						"Enum.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            enum class Enum {
				;
				class Nested
			}
        """.trimIndent()
				)
		)

		assertThat(elem.kotlinTypes).hasSize(1)
		assertThat(elem.kotlinTypes.first().simpleName.toString()).isEqualTo("Nested")
	}

}