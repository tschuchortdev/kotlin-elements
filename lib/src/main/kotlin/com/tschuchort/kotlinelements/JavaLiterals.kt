@file:Suppress("MemberVisibilityCanBePrivate")

package com.tschuchort.kotlinelements

internal object JavaLiterals {
    fun toSourceCodeLiteral(clazz: Class<*>): String {
        fun rec(clazz: Class<*>): String =
            if (clazz.isArray) rec(clazz.componentType) + "[]"
            else clazz.name

        return rec(clazz) + ".class"
    }

    fun toSourceCodeLiteral(f: Float): String = when {
        f.isFinite() -> "${f}f"
        f.isInfinite() -> if (f < 0.0f) "-1.0f/0.0f" else "1.0f/0.0f"
        else -> "0.0f/0.0f"
    }

    fun toSourceCodeLiteral(d: Double): String = when {
        d.isFinite() -> d.toString()
        d.isInfinite() -> if (d < 0.0f) "-1.0/0.0" else "1.0/0.0"
        else -> "0.0/0.0"
    }

    fun toSourceCodeLiteral(c: Char): String
            = "\'" + (if (c == '\'') "\\'" else "$c") + "\'"

    fun toSourceCodeLiteral(l: Long): String = "${l}L"

    fun toSourceCodeLiteral(s: String): String
            = "\"" + s.replace("\"", "\\\"") + "\""


    fun toSourceCodeLiteral(values: ByteArray): String
            = values.joinToString(", ", "{", "}") { it.toString() }

    fun toSourceCodeLiteral(values: CharArray): String
        = values.joinToString(", ", "{", "}") { toSourceCodeLiteral(it) }

    fun toSourceCodeLiteral(values: FloatArray): String
            = values.joinToString(", ", "{", "}") { toSourceCodeLiteral(it) }

    fun toSourceCodeLiteral(values: DoubleArray): String
            = values.joinToString(", ", "{", "}") { it.toString() }

    fun toSourceCodeLiteral(values: ShortArray): String
            = values.joinToString(", ", "{", "}") { it.toString() }

    fun toSourceCodeLiteral(values: IntArray): String
            = values.joinToString(", ", "{", "}") { it.toString() }

    fun toSourceCodeLiteral(values: LongArray): String
            = values.joinToString(", ", "{", "}") { it.toString() }

    fun toSourceCodeLiteral(values: BooleanArray): String
            = values.joinToString(", ", "{", "}") { it.toString() }

    fun toSourceCodeLiteral(values: Array<String>): String
            = values.joinToString(", ", "{", "}") { it }

    fun toSourceCodeLiteral(values: Array<*>): String
            = values.joinToString(", ", "{", "}") { it.toString() }
}