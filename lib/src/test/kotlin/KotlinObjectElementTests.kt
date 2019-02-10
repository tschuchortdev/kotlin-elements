package com.tschuchort.kotlinelements

import com.tschuchort.compiletesting.KotlinCompilation
import mixins.KotlinModality
import mixins.KotlinVisibility
import org.assertj.core.api.Assertions.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

internal class KotlinObjectElementTests {
	@Rule
	@JvmField
	val temporaryFolder = TemporaryFolder()

	private val elementTester = ElementTester(temporaryFolder)

	@Test
	fun `Can be converted to Kotlin`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinObjectElement::class,
				KotlinCompilation.SourceFile(
						"KObject.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            object KObject
        """.trimIndent()
				)
		)

		assertThat(elem.simpleName.toString()).isEqualTo("KObject")
	}

	@Test
	fun `Is companion`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinObjectElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            class KClass {
				@SerializeElemForTesting
				companion object {
				}
			}
        """.trimIndent()
				)
		)

		assertThat(elem.simpleName.toString()).isEqualTo("Companion")
		assertThat(elem.isCompanion).isTrue()
	}

	@Test
	fun `Modality final by default`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinObjectElement::class,
				KotlinCompilation.SourceFile(
						"KObject.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            object KObject
        """.trimIndent()
				)
		)

		assertThat(elem.modality).isEqualTo(KotlinModality.FINAL)
	}

	@Test
	fun `Default visibility is public`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinObjectElement::class,
				KotlinCompilation.SourceFile(
						"KObject.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            object KObject
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PUBLIC)
	}

	@Test
	fun `Visibility is internal`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinObjectElement::class,
				KotlinCompilation.SourceFile(
						"KObject.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            internal object KObject
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.INTERNAL)
	}

	@Test
	fun `Visibility is private`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinObjectElement::class,
				KotlinCompilation.SourceFile(
						"KObject.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            private object KObject
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PRIVATE)
	}

	@Test
	fun `Has method`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinObjectElement::class,
				KotlinCompilation.SourceFile(
						"KObject.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            object KObject {
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
				KotlinObjectElement::class,
				KotlinCompilation.SourceFile(
						"KObject.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            object KObject {
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
				KotlinObjectElement::class,
				KotlinCompilation.SourceFile(
						"KObject.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            object KObject {
				class Nested
			}
        """.trimIndent()
				)
		)

		assertThat(elem.kotlinTypes).hasSize(1)
		assertThat(elem.kotlinTypes.first().simpleName.toString()).isEqualTo("Nested")
	}

	@Test
	fun `Simple name is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinObjectElement::class,
				KotlinCompilation.SourceFile(
						"KObject.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            object KObject
        """.trimIndent()
				)
		)

		assertThat(elem.simpleName.toString()).isEqualTo("KObject")
	}

	@Test
	fun `Qualified name is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinObjectElement::class,
				KotlinCompilation.SourceFile(
						"KObject.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            object KObject
        """.trimIndent()
				)
		)

		assertThat(elem.qualifiedName.toString())
				.isEqualTo("com.tschuchort.kotlinelements.KObject")
	}

	@Test
	fun `Encloses nothing but default constructor`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            class KClass
        """.trimIndent()
				)
		)

		assertThat(elem.enclosedKotlinElements).hasSize(1)
		assertThat(elem.enclosedKotlinElements.first()).isInstanceOf(KotlinConstructorElement::class.java)
	}

	@Test
	fun `Enclosed elements are equal to property + constructor + method + nested class`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinObjectElement::class,
				KotlinCompilation.SourceFile(
						"KObject.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            object KObject {
				fun func() {}
				val prop = 1
				object Nested
			}
        """.trimIndent()
				)
		)

		val expectedElems = with(elem) {
			@Suppress("UNCHECKED_CAST")
			(functions + properties + constructors + kotlinTypes) as Set<KotlinElement>
		}.map { it.simpleName.toString() }.distinct()

		assertThat(elem.enclosedKotlinElements.map {
			it.simpleName.toString()
		}).containsExactlyInAnyOrderElementsOf(expectedElems)
	}
}