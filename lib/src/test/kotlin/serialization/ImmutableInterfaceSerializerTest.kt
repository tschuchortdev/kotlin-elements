@file:Suppress("MemberVisibilityCanBePrivate")

package com.tschuchort.kotlinelements.serialization

import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.tschuchort.kotlinelements.JavaReflectImmutableInterfaceSerializer
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.Test
import serialization.getKryo
import serialization.NotSerializedException


class ImmutableInterfaceSerializerTest {

    val kryo = getKryo().apply {
        addDefaultSerializer(A::class.java, JavaReflectImmutableInterfaceSerializer(A::class))
    }

    interface A {
        val immutableProperty: Int
        var mutableProperty: Int

        fun noArgMethod(): Int
        fun argMethod(x: Int)

        var child: A?
        var parent: A?
    }

    class AImpl : A {
        override val immutableProperty: Int = 1
        override var mutableProperty: Int = 2

        override fun noArgMethod(): Int {
            return 3
        }

        override fun argMethod(x: Int) {
        }

        override var child: A? = null
        override var parent: A? = null
    }

    @Test
    fun `Serializes vals`() {
        val a = AImpl()
        val o = serializeAndDeserialize(a)

        assertThat(o).isNotNull
        assertThat(o!!.immutableProperty).isEqualTo(a.immutableProperty)
    }

    @Test
    fun `Serializes vars`() {
        val a = AImpl()
        val o = serializeAndDeserialize(a)

        assertThat(o).isNotNull
        assertThat(o!!.mutableProperty).isEqualTo(a.mutableProperty)
    }

    @Test
    fun `Serializes no-arg methods`() {
        val a = AImpl()
        val o = serializeAndDeserialize(a)

        assertThat(o).isNotNull
        assertThat(o!!.noArgMethod()).isEqualTo(a.noArgMethod())
    }

    @Test
    fun `Doesn't serialize methods with args`() {
        val a = AImpl()
        val o = serializeAndDeserialize(a)

        assertThat(o).isNotNull
        val thrown = catchThrowable { o!!.argMethod(1) }
        assertThat(thrown).isInstanceOf(NotSerializedException::class.java)
    }

    @Test
    fun `Supports cyclic references`() {
        val a1 = AImpl()
        val a2 = AImpl()
        a1.child = a2
        a2.parent = a1
        val o = serializeAndDeserialize(a1)

        assertThat(o).isNotNull
        assertThat(o!!.child).isNotNull
        assertThat(o.child!!.parent === o).isTrue()
    }

    @Test
    fun `Serializes hashCode and toString`() {
        val a = AImpl()
        val o = serializeAndDeserialize(a)

        assertThat(o).isNotNull
        assertThat(o.toString()).isEqualTo(a.toString())
        assertThat(o.hashCode()).isEqualTo(a.hashCode())
    }

    @Test
    fun `Doesn't serialize equals`() {
        val a = AImpl()
        val o = serializeAndDeserialize(a)

        assertThat(o).isNotNull

        val thrown = catchThrowable { o!!.equals(null) }
        assertThat(thrown).isInstanceOf(NotSerializedException::class.java)
    }

    fun serializeAndDeserialize(a: A?): A? = with(kryo) {
        val buffer = Buffer()
        val out = Output(buffer.outputStream())

        writeClassAndObject(out, a)
        out.close()

        val inp = Input(buffer.inputStream())
        return readClassAndObject(inp) as A?
    }
}