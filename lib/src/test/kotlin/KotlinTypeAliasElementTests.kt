package com.tschuchort.kotlinelements

import com.tschuchort.compiletesting.KotlinCompilation
import mixins.KotlinVisibility
import org.assertj.core.api.Assertions.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

internal class KotlinTypeAliasElementTests {
	@Rule
	@JvmField
	val temporaryFolder = TemporaryFolder()

	private val elementTester = ElementTester(temporaryFolder)

	@Test
	fun `Can be converted to KotlinElement`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinTypeAliasElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
			typealias Alias = Any
        """.trimIndent()))

		assertThat(elem.simpleName.toString()).isEqualTo("Alias")
	}

	@Test
	fun `Has annotation`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinTypeAliasElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
			typealias Alias = Any
        """.trimIndent()))

		assertThat(elem.annotationMirrors.map { it.annotationType.toString() })
				.containsExactlyInAnyOrder(SerializeElemForTesting::class.qualifiedName)
	}

	@Test
	fun `Underlying type is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinTypeAliasElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
			typealias Alias = O
        """.trimIndent()))

		assertThat(elem.underlyingType.toString()).isEqualTo(O::class.qualifiedName)
	}

	@Test
	fun `Expanded type is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinTypeAliasElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
			typealias Alias = Any
        """.trimIndent()))

		assertThat(elem.expandedType.toString()).isEqualTo("com.tschuchort.kotlinelements.Alias")
	}

	@Test
	fun `Default visibility is public`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinTypeAliasElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
			typealias Alias = Any
        """.trimIndent()))

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PUBLIC)
	}

	@Test
	fun `Visibility is internal`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinTypeAliasElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
			internal typealias Alias = Any
        """.trimIndent()))

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.INTERNAL)
	}

	@Test
	fun `Visibility is private`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinTypeAliasElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
			private typealias Alias = Any
        """.trimIndent()))

		assertThat(elem.visibility).isEqualTo(KotlinVisibility.PRIVATE)
	}

	@Test
	fun `Has type parameters`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinTypeAliasElement::class,
				KotlinCompilation.SourceFile(
						"KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
			typealias Alias<T, S> = List<T>
        """.trimIndent()))

		assertThat(elem.typeParameters.map { it.simpleName.toString() })
				.containsExactlyInAnyOrder("T", "S")
	}
}


