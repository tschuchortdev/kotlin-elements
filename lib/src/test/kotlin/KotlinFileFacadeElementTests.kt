package com.tschuchort.kotlinelements

import com.tschuchort.compiletesting.KotlinCompilation
import mixins.KotlinVisibility
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class KotlinFileFacadeElementTests {
	@Rule
	@JvmField
	val temporaryFolder = TemporaryFolder()

	private val elementTester = ElementTester(temporaryFolder)

	@Test
	fun `Simple name is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinFileFacadeElement::class,
				KotlinCompilation.SourceFile(
						"FileFacade.kt", """
            @file:SerializeElemForTesting
            package com.tschuchort.kotlinelements

			fun foo() {} // If the file is empty, the class won't be generated
        """.trimIndent()
				)
		)

		assertThat(elem.simpleName.toString()).isEqualTo("FileFacadeKt")
	}

	@Test
	fun `Qualified name is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinFileFacadeElement::class,
				KotlinCompilation.SourceFile(
						"FileFacade.kt", """
            @file:SerializeElemForTesting
            package com.tschuchort.kotlinelements

			fun foo() {} // If the file is empty, the class won't be generated
        """.trimIndent()
				)
		)

		assertThat(elem.qualifiedName.toString())
				.isEqualTo("com.tschuchort.kotlinelements.FileFacadeKt")
	}

	@Test
	fun `Has method`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinFileFacadeElement::class,
				KotlinCompilation.SourceFile(
						"FileFacade.kt", """
            @file:SerializeElemForTesting
            package com.tschuchort.kotlinelements

			fun method() {}
        """.trimIndent()
				)
		)

		assertThat(elem.functions).hasSize(1)
		assertThat(elem.functions.first().simpleName.toString()).isEqualTo("method")
	}

	@Test
	fun `Has property`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinFileFacadeElement::class,
				KotlinCompilation.SourceFile(
						"FileFacade.kt", """
            @file:SerializeElemForTesting
            package com.tschuchort.kotlinelements

			val prop: Int = 3
        """.trimIndent()
				)
		)

		assertThat(elem.properties).hasSize(1)
		assertThat(elem.properties.first().simpleName.toString()).isEqualTo("prop")
	}

	@Test
	fun `Enclosed elements are equal to property + function + typealias`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinFileFacadeElement::class,
				KotlinCompilation.SourceFile(
						"FileFacade.kt", """
            @file:SerializeElemForTesting
            package com.tschuchort.kotlinelements

			fun foo() {}
			val prop: Int = 3
			typealias Alias = O
			class Nested
        """.trimIndent()
				)
		)

		val expectedElems = with(elem) {
			@Suppress("UNCHECKED_CAST")
			(functions + properties + typeAliases) as Set<KotlinElement>
		}.map { it.simpleName.toString() }.distinct()

		assertThat(elem.enclosedKotlinElements.map {
			it.simpleName.toString()
		}).containsExactlyInAnyOrderElementsOf(expectedElems)
	}
}