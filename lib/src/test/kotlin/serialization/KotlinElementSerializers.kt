package com.tschuchort.kotlinelements.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Output
import com.tschuchort.kotlinelements.KotlinCompatElement
import com.tschuchort.kotlinelements.KotlinElement
import com.tschuchort.kotlinelements.KotlinPackageElement
import com.tschuchort.kotlinelements.MockitoImmutableClassSerializer
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaGetter


/**
* Serializers for KotlinElements
*/
open class KotlinElementSerializer<T : KotlinElement>(
		/** The class to be serialized */
		clazz: KClass<T>,
		/**
		 * Whether the enclosing element should be serialized
		 * if it's a package or module.
		 * Disabling this can be useful for performance reasons.
		 */
		private val serializeEnclosingPackage: Boolean = true
) : MockitoImmutableClassSerializer<T>(clazz, true) {

	override fun serializeReturnValue(
			kryo: Kryo, output: Output, obj: T,
			method: Method, value: SerializedReturnValue
	) {
		// don't serialize packages for performance reasons
		if(!serializeEnclosingPackage
				&& method == KotlinElement::enclosingElement.javaGetter
				&& obj.enclosingElement is KotlinPackageElement
		)
			super.serializeReturnValue(kryo, output, obj, method, SerializedReturnValue.NotSerialized)
		else
			super.serializeReturnValue(kryo, output, obj, method, value)
	}
}

/**
 * Serializers for KotlinElements
 */
open class KotlinCompatElementSerializer<T : KotlinCompatElement>(
		/** The class to be serialized */
		clazz: KClass<T>,
		/**
		 * Whether the enclosing element should be serialized
		 * if it's a package or module.
		 * Disabling this can be useful for performance reasons.
		 */
		private val serializeEnclosingPackage: Boolean = true
) : MockitoImmutableClassSerializer<T>(clazz, true) {

	override fun serializeReturnValue(
			kryo: Kryo, output: Output, obj: T,
			method: Method, value: SerializedReturnValue
	) {
		// don't serialize packages for performance reasons
		if(!serializeEnclosingPackage
				&& method == KotlinCompatElement::enclosingElement.javaGetter
				&& obj.enclosingElement is KotlinPackageElement
		)
			super.serializeReturnValue(kryo, output, obj, method, SerializedReturnValue.NotSerialized)
		else
			super.serializeReturnValue(kryo, output, obj, method, value)
	}
}

