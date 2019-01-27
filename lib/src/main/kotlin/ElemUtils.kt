/**
 * Utilities for dealing with elements, kotlin metadata/protobuf classes
 * and JVM stuff
 */
package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.load.java.JvmAbi
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.jvm.JvmProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.name.NameUtils
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.util.ElementScanner9

//TODO("make internal for release")
val ProcessingEnvironment.kotlinMetadataUtils: KotlinMetadataUtils
	get() {
		return object : KotlinMetadataUtils {
			override val processingEnv = this@kotlinMetadataUtils
		}
	}

/**
 * A local javaElement is an javaElement declared within an executable javaElement.
 *
 * Note that elements declared in a local type are not local but members.
 */
internal fun Element.isLocal(): Boolean = when {
	enclosingElement == null -> false
	enclosingElement!!.asTypeElement() == null -> true
	else -> false
}


internal fun Element.asTypeElement(): TypeElement? = when(kind) {
	ElementKind.CLASS, ElementKind.ENUM,
	ElementKind.INTERFACE, ElementKind.ANNOTATION_TYPE -> this as? TypeElement
	else -> null
}

internal fun Element.asExecutableElement(): ExecutableElement? = when(kind) {
	ElementKind.METHOD, ElementKind.CONSTRUCTOR,
	ElementKind.STATIC_INIT, ElementKind.INSTANCE_INIT -> this as? ExecutableElement
	else -> null
}

/**
 * Returns the JVM signature string in the form "$Name$MethodDescriptor", for example: `equals(Ljava/lang/Object;)Z`
 * for this [JvmProtoBuf.JvmMethodSignature] if it has one.
 *
 * For reference, see the [JVM specification, section 4.3](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3).
 */
internal fun JvmProtoBuf.JvmMethodSignature.jvmSignatureString(nameResolver: NameResolver): String? {
	return if(hasName() && hasDesc())
		with(nameResolver) { getString(name) + getString(desc) }
	else
		null
}

/**
 * Returns the JVM signature string in the form "$Name$MethodDescriptor", for example: `equals(Ljava/lang/Object;)Z`
 * for this [JvmProtoBuf.JvmFieldSignature] if it has one.
 *
 * For reference, see the [JVM specification, section 4.3](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3).
 */
internal fun JvmProtoBuf.JvmFieldSignature.jvmSignatureString(nameResolver: NameResolver): String? {
	return if(hasName() && hasDesc())
		with(nameResolver) { getString(name) + getString(desc) }
	else
		null
}

/**
 * Returns the JVM signature in the form "$Name$MethodDescriptor", for example: `equals(Ljava/lang/Object;)Z`.
 *
 * For reference, see the [JVM specification, section 4.3](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3).
 */
//TODO("make internal for release")
fun ExecutableElement.getJvmMethodSignature(processingEnv: ProcessingEnvironment): String
		= with(processingEnv.kotlinMetadataUtils) {
	this@getJvmMethodSignature.jvmMethodSignature
}

/**
 * finds all elements in the tree that fulfill the predicate (including the
 * root element that is the receiver)
 */
internal fun Element.findElementsWhere(predicate: (Element) -> Boolean)
		: List<Element> = ElementFilter9(predicate).scan(this)

/**
 * ElementScanner that returns all scanned elements which
 * fulfill the predicate
 */
internal class ElementFilter9(
		private val predicate: (Element) -> Boolean
) : ElementScanner9IncludingTypeParams<List<Element>, Void>(/*defaultValue = */mutableListOf()) {

	override fun scan(e: Element?, p: Void?): List<Element> {
		return if (predicate(e!!))
			e.accept(this, p) + e
		else
			e.accept(this, p)
	}

	/*override fun visitExecutable(e: ExecutableElement?, p: Void?): List<Element> {
		processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "visiting: $e")//, origin: ${javaElementUtils.getOrigin(e)}")

		return if(predicate(e!!))
			super.visitExecutable(e, p) + e
		else
			super.visitExecutable(e, p)
	}


	override fun visitModule(e: ModuleElement?, p: Void?): List<Element> {
		processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "visiting: $e, origin: ${javaElementUtils.getOrigin(e)}")

		return if(predicate(e!!))
			super.visitModule(e, p) + e
		else
			super.visitModule(e, p)
	}

	override fun visitPackage(e: PackageElement?, p: Void?): List<Element> {
		processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "visiting: $e")//, origin: ${javaElementUtils.getOrigin(e)}")

		return if(predicate(e!!))
			super.visitPackage(e, p) + e
		else
			super.visitPackage(e, p)
	}

	override fun visitType(e: TypeElement?, p: Void?): List<Element> {
		processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "visiting: $e")//, origin: ${javaElementUtils.getOrigin(e)}")
		return if(predicate(e!!))
			super.visitType(e, p) + e
		else
			super.visitType(e, p)
	}

	override fun visitTypeParameter(e: TypeParameterElement?, p: Void?): List<Element> {
		processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "visiting: $e")//, origin: ${javaElementUtils.getOrigin(e)}")
		return if(predicate(e!!))
			super.visitTypeParameter(e, p) + e
		else
			super.visitTypeParameter(e, p)
	}

	override fun visitVariable(e: VariableElement?, p: Void?): List<Element> {
		processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "visiting: $e")//, origin: ${javaElementUtils.getOrigin(e)}")
		return if(predicate(e!!))
			super.visitVariable(e, p) + e
		else
			super.visitVariable(e, p)
	}

	override fun visitUnknown(e: Element?, p: Void?): List<Element> {
		processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "visiting: $e")//, origin: ${javaElementUtils.getOrigin(e)}")
		return if(predicate(e!!))
			super.visitUnknown(e, p) + e
		else
			super.visitUnknown(e, p)
	}*/
}

/**
 * [ElementVisitor] that also scans type parameters (in contrast to [ElementScanner9])
 */
internal abstract class ElementScanner9IncludingTypeParams<R, P>
protected constructor(defaultValue: R) : ElementScanner9<R, P>(defaultValue) {

	override fun visitType(e: TypeElement?, p: P?): R {
		// Type parameters are not considered to be enclosed by a type
		scan(e!!.typeParameters, p)
		return super.visitType(e, p)
	}

	override fun visitExecutable(e: ExecutableElement?, p: P?): R {
		// Type parameters are not considered to be enclosed by an executable
		scan(e!!.typeParameters, p)
		return super.visitExecutable(e, p)
	}
}

//TODO("replace with constant in [JvmAbi] but it's private")
internal const val ANNOTATIONS_SUFFIX = "\$annotations"

/**
 * Returns the mangling suffix added to members with [KotlinVisibility.INTERNAL]
 */
internal fun getManglingSuffix(jvmModuleName: String?): String {
	return NameUtils.sanitizeAsJavaIdentifier(jvmModuleName ?: JvmAbi.DEFAULT_MODULE_NAME)
}

/**
 * Whether this is a (possibly anonymous) singleton class of the kind denoted by the `object` keyword
 */
internal fun ProtoBuf.Class.isObject(): Boolean = (classKind == ProtoBuf.Class.Kind.OBJECT)

/** Returns the [NameResolver] of the closest parent javaElement (or this javaElement) that has one */
internal fun getNameResolver(elem: Element): NameResolver? {
	val metadata = elem.kotlinMetadata
	return when(metadata) {
		is KotlinPackageMetadata -> metadata.data.nameResolver
		is KotlinClassMetadata -> metadata.data.nameResolver
		else -> elem.enclosingElement?.let(::getNameResolver)
	}
}

/** Returns the [KotlinMetadata] of the closest parent javaElement (or this javaElement) that has one */
internal fun getMetadata(elem: Element): KotlinMetadata?
		= elem.kotlinMetadata ?: elem.enclosingElement?.let(::getMetadata)

