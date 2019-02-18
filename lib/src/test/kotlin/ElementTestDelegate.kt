package com.tschuchort.kotlinelements

import com.esotericsoftware.kryo.io.Input
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.TeeOutputStream
import com.tschuchort.compiletesting.getJdkHome
import com.tschuchort.compiletesting.isJdk9OrLater
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement
import serialization.SerializedMsgOverStdout
import serialization.getKryo
import java.io.File
import java.io.PrintStream
import java.net.URLClassLoader
import java.util.*
import javax.annotation.processing.Processor
import kotlin.reflect.KClass

class ElementTester(private val temporaryFolder: TemporaryFolder) {

	private val serializeProcService = KotlinCompilation.Service(
			Processor::class, SerializeAnnotatedElementProcessor::class
	)

	fun getSerializedFrom(source: KotlinCompilation.SourceFile)
			= getSerializedFrom(listOf(source))

	fun getSerializedFrom(sources: List<KotlinCompilation.SourceFile>): List<Any?> {
		val result = compilationPreset().copy(
				sources = sources,
				services = listOf(serializeProcService),
				inheritClassPath = true
		).compile_()

		assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

		return SerializedMsgOverStdout.parseAllIn(result.systemOut).map { msg ->
			val base64Encoded = msg.content
			val inp = Input(Base64.getDecoder().decode(base64Encoded))
			getKryo().readClassAndObject(inp)
		}
	}

	fun <T : Any> getSingleSerializedFrom(type: KClass<T>, sources: List<KotlinCompilation.SourceFile>): T {
		val objects = getSerializedFrom(sources)
		assertThat(objects).hasSize(1)
		val obj = objects.first()
		assertThat(obj).isInstanceOf(type.java)
		return obj as T
	}

	fun <T : Any> getSingleSerializedFrom(type: KClass<T>, source: KotlinCompilation.SourceFile): T
			= getSingleSerializedFrom(type, listOf(source))

	private fun compilationPreset(): KotlinCompilation {
		val jdkHome = getJdkHome()

		return KotlinCompilation(
				workingDir = temporaryFolder.root,
				jdkHome = jdkHome,
				toolsJar = if(isJdk9OrLater())
					null
				else
					File(jdkHome, "lib\\tools.jar"),
				inheritClassPath = true,
				skipRuntimeVersionCheck = true,
				correctErrorTypes = true,
				verbose = true,
				reportOutputFiles = true,
				multiplatform = true
		)
	}

	private fun KotlinCompilation.compile_() = run {
		val systemOutBuffer = Buffer()
		val result = copy(systemOut = PrintStream(
				TeeOutputStream(System.out, systemOutBuffer.outputStream())
		)
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