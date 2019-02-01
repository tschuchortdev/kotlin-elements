@file:Suppress("MemberVisibilityCanBePrivate")

package com.tschuchort.kotlinelements

import com.esotericsoftware.kryo.io.Input
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.TeeOutputStream
import com.tschuchort.compiletesting.getJdkHome
import com.tschuchort.compiletesting.isJdk9OrLater
import okio.Buffer
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.`when`
import serialization.*
import java.io.File
import java.io.PrintStream
import java.net.URLClassLoader
import java.util.*
import javax.annotation.processing.Processor
import javax.lang.model.element.*
import kotlin.reflect.jvm.javaMethod

class KotlinElementsTests {
    @Rule
    @JvmField val temporaryFolder = TemporaryFolder()

    val serializeProcService = KotlinCompilation.Service(Processor::class, SerializeAnnotatedElementProcessor::class)

    final class X {
        final fun foo() = 3
    }

    @Test
    fun `Elements can be serialized`() {
        val source = KotlinCompilation.SourceFile("Source.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElem
            class Source {
                fun foo(s : Source) {}
            }
        """.trimIndent())

        val mockx = mock<X>()
        given(mockx.foo()).willReturn(5)

        //given(mockx.hashCode()).willReturn(0)
        //given(mockx.equals(Unit)).willReturn(false)

        val result = compilationPreset().copy(
            sources = listOf(source),
            services = listOf(serializeProcService),
            inheritClassPath = true
        ).compile_()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        SerializedMsgOverStdout.parseAllIn(result.systemOut).forEach { msg ->
            val base64Encoded = msg.content
            val inp = Input(Base64.getDecoder().decode(base64Encoded))
            val elem = getKryo().readClassAndObject(inp)
            println()
        }
    }


    private fun compilationPreset(): KotlinCompilation {
        val jdkHome = getJdkHome()

        return KotlinCompilation(
            workingDir = temporaryFolder.root,
            jdkHome = jdkHome,
            toolsJar = if(isJdk9OrLater())
                null
            else
                File(jdkHome, "lib\\tools.jar"),
            inheritClassPath = false,
            skipRuntimeVersionCheck = true,
            correctErrorTypes = true,
            verbose = true,
            reportOutputFiles = true
        )
    }

    private fun KotlinCompilation.compile_() = run {
        val systemOutBuffer = Buffer()
        val result = copy(systemOut = PrintStream(
                TeeOutputStream(System.out, systemOutBuffer.outputStream()))
        ).compile()

        return@run object {
            val exitCode = result.exitCode
            val classLoader = URLClassLoader(
                arrayOf(result.outputDirectory.toURI().toURL()),
                this::class.java.classLoader)
            val systemOut = systemOutBuffer.readUtf8()
        }
    }
}

