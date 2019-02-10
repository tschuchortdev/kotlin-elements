package com.tschuchort.kotlinelements

import com.tschuchort.compiletesting.KotlinCompilation
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.lang.model.element.PackageElement

class KotlinPackageElementTests {
	@Rule
	@JvmField
	val temporaryFolder = TemporaryFolder()

	private val packageInfoSrc = KotlinCompilation.SourceFile(
			"package-info.java", """
		@SerializePackageForTesting
		package testpackage;

		import com.tschuchort.kotlinelements.SerializePackageForTesting;
	""".trimIndent()
	)

	private val elementTester = ElementTester(temporaryFolder)

	@Test
	fun `Package without enclosed Kotlin elements is a Java package`() {
		val elem = elementTester.getSingleSerializedFrom(
				PackageElement::class,
				packageInfoSrc
		)
	}

	@Test
	fun `Package with enclosed Kotlin elements is a Kotlin package`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinPackageElement::class,
				listOf(
						packageInfoSrc,
						KotlinCompilation.SourceFile(
								"File.kt", """
							package testpackage
							class KClass
						""".trimIndent()
						)
				)
		)
	}

	@Test
	fun `Simple name is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinPackageElement::class,
				listOf(
						packageInfoSrc,
						KotlinCompilation.SourceFile(
								"File.kt", """
							package testpackage
							class KClass
						""".trimIndent()
						)
				)
		)

		assertThat(elem.simpleName.toString()).isEqualTo("testpackage")
	}

	@Test
	fun `Qualified name is correct`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinPackageElement::class,
				listOf(
						packageInfoSrc,
						KotlinCompilation.SourceFile(
								"File.kt", """
							package testpackage
							class KClass
						""".trimIndent()
						)
				)
		)

		assertThat(elem.qualifiedName.toString()).isEqualTo("testpackage")
	}

	@Test
	fun `Encloses method`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinPackageElement::class,
				listOf(
						packageInfoSrc,
						KotlinCompilation.SourceFile(
								"File.kt", """
							package testpackage
							
							fun foo() {}
						""".trimIndent()
						)
				)
		)

		assertThat(elem.functions).hasSize(1)
		assertThat(elem.functions.first().simpleName.toString()).isEqualTo("foo")
	}

	@Test
	fun `Encloses property`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinPackageElement::class,
				listOf(
						packageInfoSrc,
						KotlinCompilation.SourceFile(
								"File.kt", """
							package testpackage
							
							val prop: Int = 3
						""".trimIndent()
						)
				)
		)

		assertThat(elem.properties).hasSize(1)
		assertThat(elem.properties.first().simpleName.toString()).isEqualTo("prop")
	}

	@Test
	fun `Encloses type alias`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinPackageElement::class,
				listOf(
						packageInfoSrc,
						KotlinCompilation.SourceFile(
								"File.kt", """
							package testpackage
							
							typealias Alias = Int
						""".trimIndent()
						)
				)
		)

		assertThat(elem.typeAliases).hasSize(1)
		assertThat(elem.typeAliases.first().simpleName.toString()).isEqualTo("Alias")
	}

	@Test
	fun `Encloses Kotlin class`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinPackageElement::class,
				listOf(
						packageInfoSrc,
						KotlinCompilation.SourceFile(
								"File.kt", """
							package testpackage
							
							class KClass
						""".trimIndent()
						)
				)
		)

		assertThat(elem.kotlinTypes).hasSize(1)
		assertThat(elem.javaTypes).isEmpty()
		assertThat(elem.kotlinTypes.first().simpleName.toString()).isEqualTo("KClass")
	}

	@Test
	fun `Encloses Java class`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinPackageElement::class,
				listOf(
						packageInfoSrc,
						KotlinCompilation.SourceFile(
								"JClass.java", """
							package testpackage;
							
							class JClass {}
						""".trimIndent()
						),
						KotlinCompilation.SourceFile(
								"File.kt", """
							package testpackage

							class KClass
						""".trimIndent()
						)
				)
		)

		assertThat(elem.javaTypes).hasSize(1)
		assertThat(elem.kotlinTypes).hasSize(1)
		assertThat(elem.javaTypes.first().simpleName.toString()).isEqualTo("JClass")
	}

	@Test
	fun `Encloses nothing but single type`() {
		val elem = elementTester.getSingleSerializedFrom(
				KotlinPackageElement::class,
				listOf(
						packageInfoSrc,
						KotlinCompilation.SourceFile(
								"File.kt", """
							package testpackage

							class KClass
						""".trimIndent()
						)
				)
		)

		assertThat(elem.enclosedKotlinElements).hasSize(1)
		assertThat(elem.enclosedJavaElements).hasSize(0)
		assertThat(elem.enclosedKotlinElements.first()).isInstanceOf(KotlinClassElement::class.java)
	}
}