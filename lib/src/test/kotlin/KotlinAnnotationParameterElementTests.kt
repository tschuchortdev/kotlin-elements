package com.tschuchort.kotlinelements

import com.tschuchort.compiletesting.KotlinCompilation
import org.assertj.core.api.Assertions.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.PrimitiveType

class KotlinAnnotationParameterElementTests {
	@Rule
	@JvmField
	val temporaryFolder = TemporaryFolder()

	private val elementTester = ElementTester(temporaryFolder)

	@Test
	fun `Can be converted to KotlinElement`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationParameterElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

            annotation class Ann(@get:SerializeElemForTesting val a: Int)
        """.trimIndent()
				)
		)

		assertThat(elem.simpleName.toString()).isEqualTo("a")
	}

	@Test
	fun `Has correct default value`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationParameterElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

            annotation class Ann(@get:SerializeElemForTesting val a: Int = 42)
        """.trimIndent()
				)
		)

		assertThat(elem.defaultValue).isNotNull
		assertThat(elem.defaultValue!!.value).isEqualTo(42)
	}

	@Test
	fun `asType() is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationParameterElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

			annotation class Ann(@get:SerializeElemForTesting val a: Int)
        """.trimIndent()
				)
		)

		//TODO("change type names after resolving types correctly")
		assertThat(elem.asType()).isInstanceOf(PrimitiveType::class.java)
		assertThat((elem.asType() as PrimitiveType).toString()).isEqualTo("int")
	}

	@Test
	fun `toString() is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinAnnotationParameterElement::class,
				KotlinCompilation.SourceFile(
						"Ann.kt", """
            package com.tschuchort.kotlinelements

			annotation class Ann(@get:SerializeElemForTesting val a: Int)
        """.trimIndent()
				)
		)

		assertThat(elem.toString()).isEqualTo("a")
	}
}

