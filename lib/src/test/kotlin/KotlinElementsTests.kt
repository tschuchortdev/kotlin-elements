package com.tschuchort.kotlinelements

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.TeeOutputStream
import com.tschuchort.compiletesting.getJdkHome
import com.tschuchort.compiletesting.isJdk9OrLater
import okio.Buffer
import org.assertj.core.api.Assertions
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import serialization.*
import java.io.File
import java.io.PrintStream
import java.net.URLClassLoader
import java.util.*
import javax.annotation.processing.Processor
import javax.lang.model.element.*

class KotlinElementsTests {
    @Rule
    @JvmField val temporaryFolder = TemporaryFolder()

    val serializeProcService = KotlinCompilation.Service(Processor::class, SerializeAnnotatedElementProcessor::class)

    @Test
    fun `Elements can be serialized`() {
        val source = KotlinCompilation.SourceFile("Source.kt", """
            package com.tschuchort.kotlinelements

            @SerializeElem
            class Source {
                fun foo(s : Source) {}
            }
        """.trimIndent())

        val result = compilationPreset().copy(
            sources = listOf(source),
            services = listOf(serializeProcService),
            inheritClassPath = true
        ).compile_()

        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        SerializedMessage.parseAllIn(result.systemOut).forEach { msg ->
            val base64Encoded = msg.content
            val inp = Input(Base64.getDecoder().decode(base64Encoded))
            val elem = getKryo().readClassAndObject(inp) as Element
        }
    }

    @Test
    fun `test simple`() {
        with(getKryo()) {
            addDefaultSerializer(A::class.java, ASerializer())
            addDefaultSerializer(B::class.java, BSerializer())
            addDefaultSerializer(C::class.java, CSerializer())

            val buffer = Buffer()

            val out = Output(buffer.outputStream())
            val c = CImpl()
            val b = BImpl(c)
            c.circRef_ = b

            writeObject(out, b, BSerializer())
            out.close()

            val inp = Input(buffer.inputStream())
            val o = readObject(inp, B::class.java, BSerializer())
        }

    }

    interface A {
        val aField: List<Int>
    }

    interface B : A {
        val bField: Int
        val circRef: C
    }

    interface C {
        val circRef: B
    }

    class AImpl : A {
        override val aField: List<Int>
            get() = listOf(1,2,3)
    }

    class BImpl(override val circRef: C) : B {
        override val aField: List<Int>
            get() = listOf(4,5,6)

        override val bField: Int
            get() = 7

    }

    class CImpl() : C {
        var circRef_: B? = null

        override val circRef: B
            get() = circRef_!!
    }

    class ASerializer : Serializer<A>() {
        override fun write(kryo: Kryo, output: Output, obj: A) = with(kryo) {
            writeClassAndObject(output, obj.aField)
        }

        override fun read(kryo: Kryo, input: Input, type: Class<out A>)
                : A = with(kryo) {

            lateinit var aField: List<Int>

            val o = object : A {
                override val aField: List<Int>
                    get() = aField
            }

            reference(o)

            aField = readClassAndObject(input) as List<Int>

            return o
        }
    }

    class BSerializer : Serializer<B>() {
        val aSerializer = ASerializer()
        override fun write(kryo: Kryo, output: Output, obj: B) = with(kryo) {
            writeObject(output, obj, ASerializer())
            //aSerializer.write(kryo, output, obj)
            writeClassAndObject(output, obj.bField)
            writeClassAndObject(output, obj.circRef)
        }

        override fun read(kryo: Kryo, input: Input, type: Class<out B>)
                : B = with(kryo) {
            lateinit var a: A
            var bField: Int = 0
            lateinit var circRef: C

            val o = object : B, A by a {
                override val bField: Int
                    get() = bField

                override val circRef: C
                    get() = circRef
            }

            reference(o)

            //a = aSerializer.read(kryo, input, type)
            a = readObject(input, A::class.java, ASerializer())!! as A
            bField = readClassAndObject(input) as Int
            circRef = readClassAndObject(input) as C

            return o
        }
    }

    class CSerializer : Serializer<C>() {
        override fun write(kryo: Kryo, output: Output, obj: C) = with(kryo) {
            writeClassAndObject(output, obj.circRef)
        }

        override fun read(kryo: Kryo, input: Input, type: Class<out C>)
                : C = with(kryo) {

            lateinit var circRef: B

            val o =  object : C {
                override val circRef = circRef
            }

            reference(o)

            circRef = readClassAndObject(input) as B

            return o
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
        val result = copy(systemOut = PrintStream(TeeOutputStream(System.out, systemOutBuffer.outputStream())))
            .compile()

        return@run object {
            val exitCode = result.exitCode
            val classLoader = URLClassLoader(
                arrayOf(result.outputDirectory.toURI().toURL()),
                this::class.java.classLoader)
            val systemOut = systemOutBuffer.readUtf8()
        }
    }
}

