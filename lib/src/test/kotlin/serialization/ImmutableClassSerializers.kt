package com.tschuchort.kotlinelements

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.KryoException
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import javassist.util.proxy.ProxyFactory
import org.mockito.Mockito
import org.objenesis.ObjenesisStd
import serialization.NotSerializedException
import java.io.Serializable
import java.lang.IllegalStateException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

/**
 * Base class for a serializer that can serialize immutable record classes by
 * serializing the return values of all no-arg functions and then proxying the
 * object.
 */
@Suppress("MoveLambdaOutsideParentheses")
abstract class ImmutableClassSerializer<T : Any>(
		protected val clazz: KClass<T>,
		/**
		 * Whether exceptions thrown by methods of the object should be serialized
		 * as well or rethrown.
		 */
		protected val serializeExceptions: Boolean = false
) : Serializer<T>() {

	protected val methods = clazz.java.methods + Object::class.java.methods.filter {
		// the method list doesn't include those inherited by Object so we need
		// to add them manually
		it.name == "hashCode" || it.name == "toString"
	}

	private val noArgMethods = methods.filter { it.parameters.isEmpty() }

	final override fun write(kryo: Kryo, output: Output, obj: T) {
		noArgMethods.forEach {
			try {
				serializeReturnValue(kryo, output, obj, it)
			}
			catch (e: KryoException) {
				throw e.apply { addTrace(kryoExceptionTraceFor(it, obj.javaClass)) }
			}
			catch (t: Throwable) {
				throw KryoException(t).apply { addTrace(kryoExceptionTraceFor(it, obj.javaClass)) }
			}
		}
	}

	protected open fun serializeReturnValue(kryo: Kryo, output: Output, obj: T, method: Method) {
		try {
			val returnValue = method.invoke(obj)
			serializeReturnValue(kryo, output, obj, method, SerializedReturnValue.Value(returnValue))
		} catch (e: InvocationTargetException) {
			if (e.targetException == null || !serializeExceptions)
				throw e
			else
				serializeReturnValue(kryo, output, obj, method, SerializedReturnValue.Exception(e))
		}
	}

	protected open fun serializeReturnValue(kryo: Kryo, output: Output, obj: T,
											method: Method, value: SerializedReturnValue) {
		kryo.writeClassAndObject(output, value)
	}

	protected interface SerializedData {
		fun getSerializedValue(method: Method): SerializedReturnValue
		val initialized: Boolean
	}

	protected abstract fun createProxyInstance(serializedData: SerializedData): T

	final override fun read(kryo: Kryo, input: Input, type: Class<out T>): T {
		val serializedData = object : SerializedData {
			val methodValueMap = mutableMapOf<Method, SerializedReturnValue>()
			override var initialized = false

			override fun getSerializedValue(method: Method): SerializedReturnValue {
				if(!initialized)
					throw IllegalStateException("Deserialized object not initialized yet." +
							if(method.name == "toString") "\nProxying object of type: $clazz" else "")
				else {
					return methodValueMap[method] ?: throw IllegalStateException(
							"Couldn't get return value for method $method on deserialized interface."
					)
				}
			}
		}

		val proxyInstance = createProxyInstance(serializedData)
		kryo.reference(proxyInstance)

		noArgMethods.forEach {
			try {
				serializedData.methodValueMap[it] = deserializeReturnValue(kryo, input, type, it)
			}
			catch (e: KryoException) {
				throw e.apply { addTrace(kryoExceptionTraceFor(it, type)) }
			}
			catch (t: Throwable) {
				throw KryoException(t).apply { addTrace(kryoExceptionTraceFor(it, type)) }
			}
		}

		serializedData.initialized = true
		return proxyInstance
	}

	protected open fun deserializeReturnValue(kryo: Kryo, input: Input, type: Class<out T>, method: Method)
			: SerializedReturnValue {
		return kryo.readClassAndObject(input) as SerializedReturnValue
	}

	/**
	 * The return value of a method that will be serialized
	 */
	protected sealed class SerializedReturnValue : Serializable {
		/** An exception the method to be serialized threw */
		data class Exception(val exception: Throwable) : SerializedReturnValue(), Serializable
		/** The return value of the method to be serialized */
		data class Value(val value: Any?) : SerializedReturnValue(), Serializable
		/** The method won't be serialized */
		object NotSerialized : SerializedReturnValue(), Serializable
	}

	private fun kryoExceptionTraceFor(method: Method, type: Class<*>)
			= method.name + " (" + type.name + ")"
}

/**
 * A java.reflect based serializer for interfaces that act like immutable value classes
 */
open class JavaReflectImmutableInterfaceSerializer<T : Any>(
		clazz: KClass<T>, serializeExceptions: Boolean = true
) : ImmutableClassSerializer<T>(clazz, serializeExceptions) {

	init {
		require(clazz.java.isInterface)
	}

	@Suppress("UNCHECKED_CAST")
	override fun createProxyInstance(serializedData: SerializedData): T
			= Proxy.newProxyInstance(clazz.java.classLoader, arrayOf(clazz.java)
	) { proxy, method, args ->
		return@newProxyInstance onProxyMethodCall(proxy as T, method,
				args ?: emptyArray(), serializedData.getSerializedValue(method))
	} as T

	protected open fun onProxyMethodCall(obj: T, method: Method, args: Array<Any?>,
										 serializedReturnValue: SerializedReturnValue?): Any? {
		if (args.isNotEmpty())
			throw NotSerializedException()

		if (serializedReturnValue == null)
			throw IllegalStateException(
					"Couldn't get return value for method $method on deserialized interface."
			)

		return when (serializedReturnValue) {
			is SerializedReturnValue.Exception -> throw serializedReturnValue.exception
			is SerializedReturnValue.NotSerialized -> throw NotSerializedException()
			is SerializedReturnValue.Value -> serializedReturnValue.value
		}
	}
}

/**
 * A Javassist/Objenesis based serializer for immutable record classes.
 *
 * In theory this should work with all classes and interfaces alike, but
 * for some reason it crashes with classes from the javax.lang.model package.
 *
 * Classes must have a no-arg constructor (can be synthetic)!
 */
open class JavassistImmutableClassSerializer<T : Any>(
		clazz: KClass<T>, serializeExceptions: Boolean = true
) : ImmutableClassSerializer<T>(clazz, serializeExceptions) {

	init {
		require(clazz.isOpen) { "Class '$clazz' can't be serialized because it is final" }
		methods.forEach {
			require(!Modifier.isFinal(it.modifiers)) {"Method '$it' can't be serialized because it is final"}
		}
	}

	@Suppress("UNCHECKED_CAST")
	override fun createProxyInstance(serializedData: SerializedData): T {
		val proxyClass = ProxyFactory().apply {
			if(clazz.java.isInterface)
				interfaces = arrayOf(clazz.java)
			else
				superclass = clazz.java
		}.createClass()

		val proxyInstance: T = try {
			// if the class has a no-arg ctor, create instance by calling it
			proxyClass.getDeclaredConstructor().newInstance() as T
		}
		catch(e: NoSuchMethodException) {
			// otherwise create instance without calling any constructors
			ObjenesisStd().newInstance(proxyClass) as T
		}

		(proxyInstance as javassist.util.proxy.Proxy).setHandler { self, thisMethod, proceed, args ->
			onProxyMethodCall(self as T, thisMethod, proceed, args ?: emptyArray(),
					serializedData.getSerializedValue(thisMethod))
		}

		return proxyInstance
	}

	protected open fun onProxyMethodCall(obj: T, method: Method, proceed: Method?, args: Array<Any?>,
										 serializedReturnValue: SerializedReturnValue): Any? {
		if (args.isNotEmpty())
			throw NotSerializedException()

		return when (serializedReturnValue) {
			is SerializedReturnValue.Exception -> throw serializedReturnValue.exception
			is SerializedReturnValue.NotSerialized -> throw NotSerializedException()
			is SerializedReturnValue.Value -> serializedReturnValue.value
		}
	}
}

/**
 * A Mockito based serializer for immutable record classes.
 *
 * Should work with all classes but can't override [hashCode] or [equals].
 */
open class MockitoImmutableClassSerializer<T : Any>(
		clazz: KClass<T>, serializeExceptions: Boolean = true
) : ImmutableClassSerializer<T>(clazz, serializeExceptions) {

	@Suppress("UNCHECKED_CAST")
	override fun createProxyInstance(serializedData: SerializedData): T {
		return Mockito.mock(clazz.java) { invocation ->
			val obj = invocation.mock as T
			val method = invocation.method
			val args = invocation.arguments ?: emptyArray()

			return@mock onProxyMethodCall(obj, method, invocation::callRealMethod,
					args, serializedData.getSerializedValue(method))
		}
	}

	protected open fun onProxyMethodCall(obj: T, method: Method, callRealMethod: () -> Any?, args: Array<Any?>,
										 serializedReturnValue: SerializedReturnValue): Any? {
		if (args.isNotEmpty())
			throw NotSerializedException()

		return when (serializedReturnValue) {
			is SerializedReturnValue.Exception -> throw serializedReturnValue.exception
			is SerializedReturnValue.NotSerialized -> throw NotSerializedException()
			is SerializedReturnValue.Value -> serializedReturnValue.value
		}
	}
}