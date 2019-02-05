package com.tschuchort.kotlinelements

import com.tschuchort.compiletesting.KotlinCompilation
import mixins.KotlinModality
import mixins.KotlinVisibility
import org.assertj.core.api.Assertions.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.lang.model.type.DeclaredType

internal class KotlinClassElementTests {
	@Rule
	@JvmField
	val temporaryFolder = TemporaryFolder()

	private val elementTester = ElementTester(temporaryFolder)

	@Test
	fun `Class can be converted to KotlinElement`() {
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

		assertThat(elem.simpleName.toString()).isEqualTo("KClass")
	}

	@Test
	fun `Class is final by default`() {
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

		assertThat(elem.modality).isEqualTo(KotlinModality.FINAL)
	}

	@Test
	fun `Open class is open`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            open class KClass
        """.trimIndent()
				)
		)

		assertThat(elem.modality).isEqualTo(KotlinModality.OPEN)
	}

	@Test
	fun `Abstract class is abstract`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            abstract class KClass
        """.trimIndent()
				)
		)

		assertThat(elem.modality).isEqualTo(KotlinModality.ABSTRACT)
	}

	@Test
	fun `Sealed class is sealed`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            sealed class KClass
        """.trimIndent()
				)
		)

		assertThat(elem.modality).isEqualTo(KotlinModality.SEALED)
	}

	@Test
	fun `Data class is data`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            data class KClass(val a: Int)
        """.trimIndent()
				)
		)

		assertThat(elem.isDataClass).isEqualTo(true)
	}

	@Test
	fun `Default visibility is public`() {
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

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PUBLIC)
	}

	@Test
	fun `Visibility is internal`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            internal class KClass
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.INTERNAL)
	}

	@Test
	fun `Visibility is protected`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			class Outer {
            	@SerializeElemForTesting
            	protected class KClass
			}
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PROTECTED)
	}

	@Test
	fun `Visibility is private`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            private class KClass
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PRIVATE)
	}

	@Test
	fun `Is inner`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            class KClass {
				@SerializeElemForTesting
				inner class Inner
			}
        """.trimIndent()
				)
		)

		assertThat(elem.isInner).isEqualTo(true)
	}

	@Test
	fun `Is expect`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            expect class KClass

			actual class KClass
        """.trimIndent()
				)
		)

		assertThat(elem.isExpect).isEqualTo(true)
	}

	@Test
	fun `Simple name is correct`() {
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

		assertThat(elem.simpleName.toString()).isEqualTo("KClass")
	}

	@Test
	fun `Qualified name is correct`() {
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

		assertThat(elem.qualifiedName.toString())
				.isEqualTo("com.tschuchort.kotlinelements.KClass")
	}

	@Test
	fun `Has companion`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            class KClass {
				companion object
			}
        """.trimIndent()
				)
		)

		assertThat(elem.companion).isNotNull
	}

	@Test
	fun `Has default constructor`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            class KClass {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.constructors).hasSize(1)
		assertThat(elem.constructors.first().isPrimary).isTrue()
	}

	@Test
	fun `Has multiple constructors`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            class KClass(val i: Int) {
				constructor(j: Float) : this(1)
				constructor(b: Boolean) : this(2)
			}
        """.trimIndent()
				)
		)

		assertThat(elem.constructors).hasSize(3)
	}

	@Test
	fun `Has method`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            class KClass {
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
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            class KClass {
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
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            class KClass {
				class Nested
			}
        """.trimIndent()
				)
		)

		assertThat(elem.kotlinTypes).hasSize(1)
		assertThat(elem.kotlinTypes.first().simpleName.toString()).isEqualTo("Nested")
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
	fun `Enclosed elements are equal to property + constructor + method + companion + nested class`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            class KClass constructor() {
				fun func() {}
				val prop = 1
				class Nested
				companion object
			}
        """.trimIndent()
				)
		)

		val expectedElems = with(elem) {
			listOfNotNull<KotlinElement>(companion) + functions + properties + constructors + kotlinTypes
		}.map { it.simpleName.toString() }.distinct()

		assertThat(elem.enclosedKotlinElements.map {
			it.simpleName.toString()
		}).containsExactlyInAnyOrderElementsOf(expectedElems)
	}

	@Test
	fun `Has type parameters`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            class KClass<out T : O, S>
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
				KotlinClassElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            class KClass <T,S> {
			}
        """.trimIndent()
				)
		)

		assertThat(elem.asType()).isInstanceOf(DeclaredType::class.java)
		assertThat((elem.asType() as DeclaredType).toString()).isEqualTo("com.tschuchort.kotlinelements.KClass<T,S>")
		assertThat((elem.asType() as DeclaredType).typeArguments.map { it.toString() })
				.containsExactly("T", "S")
	}
}