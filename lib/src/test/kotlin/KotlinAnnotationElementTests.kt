package com.tschuchort.kotlinelements

import com.tschuchort.compiletesting.KotlinCompilation
import mixins.KotlinModality
import mixins.KotlinVisibility
import org.assertj.core.api.Assertions.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.lang.model.type.DeclaredType

class KotlinAnnotationElementTests {
	@Rule
	@JvmField
	val temporaryFolder = TemporaryFolder()

	private val elementTester = ElementTester(temporaryFolder)

	@Test
	fun `Can be converted to KotlinElement`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            annotation class Ann
        """.trimIndent()
				)
		)

		assertThat(elem.simpleName.toString()).isEqualTo("Ann")
	}

	@Test
	fun `Has parameters`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            annotation class Ann(val a: Int, val b: String)
        """.trimIndent()
				)
		)

		assertThat(elem.parameters.map { it.simpleName.toString() })
				.containsExactly("a", "b")

		//TODO("change type names after resolving types correctly")
		assertThat(elem.parameters.map { it.asType().toString() })
				.containsExactly("int", "java.lang.String")
	}

	@Test
	fun `Modality is final`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            annotation class Ann
        """.trimIndent()
				)
		)

		assertThat(elem.modality).isEqualTo(KotlinModality.FINAL)
	}

	@Test
	fun `Default visibility is public`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            annotation class Ann
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PUBLIC)
	}

	@Test
	fun `Visibility is internal`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            internal annotation class Ann
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.INTERNAL)
	}

	@Test
	fun `Visibility is protected`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

			class Outer {
            	@SerializeElemForTesting
            	protected annotation class Ann
			}
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PROTECTED)
	}

	@Test
	fun `Visibility is private`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            private annotation class Ann
        """.trimIndent()
				)
		)

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PRIVATE)
	}

	@Test
	fun `Is expect`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            expect annotation class Ann

			actual annotation class Ann
        """.trimIndent()
				)
		)

		assertThat(elem.isExpect).isEqualTo(true)
	}

	@Test
	fun `Simple name is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            annotation class Ann
        """.trimIndent()
				)
		)

		assertThat(elem.simpleName.toString()).isEqualTo("Ann")
	}

	@Test
	fun `Qualified name is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            annotation class Ann
        """.trimIndent()
				)
		)

		assertThat(elem.qualifiedName.toString())
				.isEqualTo("com.tschuchort.kotlinelements.Ann")
	}
	@Test
	fun `asType() is correct with type parameters`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            annotation class Ann<T,S>
        """.trimIndent()
				)
		)

		assertThat(elem.asType()).isInstanceOf(DeclaredType::class.java)
		assertThat((elem.asType() as DeclaredType).toString()).isEqualTo("com.tschuchort.kotlinelements.Ann<T,S>")
		assertThat((elem.asType() as DeclaredType).typeArguments.map { it.toString() })
				.containsExactly("T", "S")
	}

	@Test
	fun `Has type parameters`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

			@SerializeElemForTesting
            annotation class Ann<out T : O, S>
        """.trimIndent()
				)
		)

		assertThat(elem.typeParameters.map { it.simpleName.toString() }).containsExactly("T", "S")
		assertThat(elem.typeParameters.first().variance).isEqualTo(KotlinTypeParameterElement.Variance.OUT)
		assertThat(elem.typeParameters.first().bounds).hasSize(1)
		assertThat(elem.typeParameters.first().bounds.first().toString()).isEqualTo(O::class.qualifiedName)
	}
}