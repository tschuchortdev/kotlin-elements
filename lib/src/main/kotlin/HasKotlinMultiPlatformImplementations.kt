package com.tschuchort.kotlinelements

/**
 * Mixin interface for elements that can have different implementations
 * in multiplatform projects
 */
interface HasKotlinMultiPlatformImplementations {
    /** Whether this function has the `expect` keyword
     *
     * An expect element is an element declaration with actual definition in a different
     * file, akin to a declaration in a header file in C. They are used in multiplatform
     * projects where different implementations are needed depending on target platform.
     */
    val isExpect: Boolean
}