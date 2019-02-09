package com.tschuchort.kotlinelements

import com.tschuchort.compiletesting.KotlinCompilation
import mixins.KotlinModality
import mixins.KotlinVisibility
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.lang.model.type.DeclaredType

internal class KotlinInterfaceElementTests {
	@Rule
	@JvmField
	val temporaryFolder = TemporaryFolder()

	private val elementTester = ElementTester(temporaryFolder)

	@Test
	fun `Interface can be converted to KotlinElement`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            interface KInterface
        """.trimIndent()
				)
		)

		assertThat(elem.simpleName.toString()).isEqualTo("KInterface")
	}

	@Test
	fun `Interface is abstract`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            interface KInterface
        """.trimIndent()
				)
		)

		assertThat(elem.modality).isEqualTo(KotlinModality.ABSTRACT)
	}

	@Test
	fun `Default visibility is public`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            interface KInterface
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PUBLIC)
	}

	@Test
	fun `Visibility is internal`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            internal interface KInterface
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.INTERNAL)
	}

	@Test
	fun `Visibility is protected`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

			class Outer {
            	@SerializeElemForTesting
            	protected interface KInterface
			}
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PROTECTED)
	}

	@Test
	fun `Visibility is private`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            private interface KInterface
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PRIVATE)
	}


	@Test
	fun `Is expect`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            expect interface KInterface

			actual interface KInterface
        """.trimIndent()
				)
		)

		assertThat(elem.isExpect).isEqualTo(true)
	}

	@Test
	fun `Simple name is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            interface KInterface
        """.trimIndent()
				)
		)

		assertThat(elem.simpleName.toString()).isEqualTo("KInterface")
	}

	@Test
	fun `Qualified name is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            interface KInterface
        """.trimIndent()
				)
		)

		assertThat(elem.qualifiedName.toString())
				.isEqualTo("com.tschuchort.kotlinelements.KInterface")
	}

	@Test
	fun `Has companion`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            interface KInterface {
				companion object
			}
        """.trimIndent()
				)
		)

		assertThat(elem.companion).isNotNull
	}


	@Test
	fun `Has method`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            interface KInterface {
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
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            interface KInterface {
				val bar: Int
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
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            interface KInterface {
				class Nested
			}
        """.trimIndent()
				)
		)

		assertThat(elem.kotlinTypes).hasSize(1)
		assertThat(elem.kotlinTypes.first().simpleName.toString()).isEqualTo("Nested")
	}

	@Test
	fun `Empty interface encloses nothing`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            interface KInterface
        """.trimIndent()
				)
		)

		assertThat(elem.enclosedKotlinElements).hasSize(0)
	}

	@Test
	fun `Enclosed elements are equal to property + method + companion + nested class`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            interface KInterface {
				fun func() {}
				val prop: Int
				class Nested
				companion object
			}
        """.trimIndent()
				)
		)

		val expectedElems = with(elem) {
			listOfNotNull<KotlinElement>(companion) + functions + properties + kotlinTypes
		}.map { it.simpleName.toString() }.distinct()

		assertThat(elem.enclosedKotlinElements.map {
			it.simpleName.toString()
		}).containsExactlyInAnyOrderElementsOf(expectedElems)
	}

	@Test
	fun `Has type parameters`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            interface KInterface<out T : O, S>
        """.trimIndent()
				)
		)

		assertThat(elem.typeParameters.map { it.simpleName.toString() }).containsExactly("T", "S")
		assertThat(elem.typeParameters.first().variance).isEqualTo(KotlinTypeParameterElement.Variance.OUT)
		assertThat(elem.typeParameters.first().bounds).hasSize(1)
		assertThat(elem.typeParameters.first().bounds.first().toString()).isEqualTo(O::class.qualifiedName)
	}

	@Test
	fun `asType() is correct with type parameters`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinInterfaceElement::class,
				KotlinCompilation.SourceFile(
						"KInterface.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            interface KInterface <T,S> {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.asType()).isInstanceOf(DeclaredType::class.java)
		assertThat((elem.asType() as DeclaredType).toString()).isEqualTo("com.tschuchort.kotlinelements.KInterface<T,S>")
		assertThat((elem.asType() as DeclaredType).typeArguments.map { it.toString() })
				.containsExactly("T", "S")
	}
}