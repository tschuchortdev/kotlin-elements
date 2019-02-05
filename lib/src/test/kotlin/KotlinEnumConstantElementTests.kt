package com.tschuchort.kotlinelements

import com.tschuchort.compiletesting.KotlinCompilation
import org.assertj.core.api.Assertions.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.lang.model.type.DeclaredType

class KotlinEnumConstantElementTests {
	@Rule
	@JvmField
	val temporaryFolder = TemporaryFolder()

	private val elementTester = ElementTester(temporaryFolder)

	@Test
	fun `Can be converted to KotlinElement`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinEnumConstantElement::class,
				KotlinCompilation.SourceFile(
						"Enum.kt", """
            package com.tschuchort.kotlinelements

            enum class Enum {
				@SerializeElemForTesting
				A
			}
        """.trimIndent()
				)
		)

		assertThat(elem.simpleName.toString()).isEqualTo("A")
	}

	@Test
	fun `asType() is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinEnumConstantElement::class,
				KotlinCompilation.SourceFile(
						"Enum.kt", """
            package com.tschuchort.kotlinelements

            enum class Enum {
				@SerializeElemForTesting
				A
			}
        """.trimIndent()
				)
		)

		assertThat(elem.asType()).isInstanceOf(DeclaredType::class.java)
		assertThat((elem.asType() as DeclaredType).toString()).isEqualTo("com.tschuchort.kotlinelements.Enum")
	}
}