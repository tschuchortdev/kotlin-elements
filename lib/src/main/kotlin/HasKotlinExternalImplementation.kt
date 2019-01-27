package com.tschuchort.kotlinelements

/**
 * Mixin interface for Kotlin declarations that can have an external implementation.
 */
interface HasKotlinExternalImplementation {
    /**
     * Whether this function has the `external` keyword
     *
     * An external element is an element declaration with the actual definition in native
     * or JavaScript code. They act as an "interface" to declarations in a different language.
     */
    val isExternal: Boolean
}