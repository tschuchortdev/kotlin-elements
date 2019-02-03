@file:Suppress("MemberVisibilityCanBePrivate")

package com.tschuchort.kotlinelements

import com.tschuchort.compiletesting.KotlinCompilation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import javax.lang.model.element.Element

internal class ElementSerializationTests {

    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    val elementTester = ElementTester(temporaryFolder)

    @Test
    fun `Java element can be serialized`() {
        val elem = elementTester.getSingleSerializedFrom(Element::class,
                KotlinCompilation.SourceFile("JClass.java", """
            package com.tschuchort.kotlinelements;

            @SerializeElemForTesting
            class JClass {
            }
        """.trimIndent()))

        assertThat(elem.simpleName.toString()).isEqualTo("JClass")
    }

    @Test
    fun `Kotlin element can be serialized`() {
        val elem = elementTester.getSingleSerializedFrom(
                KotlinElement::class,
                KotlinCompilation.SourceFile("KClass.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElemForTesting
            class KClass
        """.trimIndent()))

        assertThat(elem.simpleName.toString()).isEqualTo("KClass")
    }
}

