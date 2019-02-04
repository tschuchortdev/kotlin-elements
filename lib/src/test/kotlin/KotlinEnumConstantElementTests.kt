package com.tschuchort.kotlinelements

import com.tschuchort.compiletesting.KotlinCompilation
import org.assertj.core.api.Assertions.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

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
}