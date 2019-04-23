package com.tschuchort.kotlinelements.kotlin

import kotlinx.metadata.jvm.KotlinClassHeader
import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.load.java.JvmAbi
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.jvm.JvmProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.name.NameUtils
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

internal fun TypeElement.getKotlinMetadata(): kotlinx.metadata.jvm.KotlinClassMetadata? {
	return this.getAnnotation(Metadata::class.java)?.let { annotation ->
		val classHeader = with(annotation) {
			KotlinClassHeader(kind, metadataVersion, bytecodeVersion, data1, data2, extraString, packageName, extraInt)
		}

		return@let kotlinx.metadata.jvm.KotlinClassMetadata.read(classHeader)
			?: throw IllegalStateException("Could not read KotlinClassMetadata from following KotlinClassHeader: $classHeader")
	}
}

/**
 * Checks whether this element originates from Kotlin code
 */
internal fun Element.originatesFromKotlin(): Boolean {
	return when(this.kind) {
		ElementKind.CLASS,
		ElementKind.ENUM,
		ElementKind.INTERFACE,
		ElementKind.ANNOTATION_TYPE -> {
			annotationMirrors.any {
				(it.annotationType.asElement() as TypeElement).qualifiedName.toString() == Metadata::class.java.canonicalName
			}
		}

		ElementKind.PACKAGE,
		ElementKind.MODULE          -> enclosedElements.any { it.originatesFromKotlin() }  //TODO("check module metadata instead")

		else                        -> enclosingElement?.originatesFromKotlin() ?: false
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
		is KotlinClassMetadata   -> metadata.data.nameResolver
		else                     -> elem.enclosingElement?.let(::getNameResolver)
	}
}

/** Returns the [KotlinMetadata] of the closest parent javaElement (or this javaElement) that has one */
internal fun getMetadata(elem: Element): KotlinMetadata?
		= elem.kotlinMetadata ?: elem.enclosingElement?.let(::getMetadata)

/**
 * true if this java element may be a synthetic annotation holder for
 * property annotations with [AnnotationTarget.PROPERTY]
 */
internal fun ExecutableElement.maybeSyntheticPropertyAnnotHolder()
		//TODO("replace string with constant from JvmAbi but it is private")
		= simpleName.toString().endsWith(ANNOTATIONS_SUFFIX)
		&& returnType.kind == TypeKind.VOID
		&& parameters.isEmpty()

/** true if this javaElement may be a kotlin generated getter of a property */
internal fun ExecutableElement.maybeKotlinGetter()
		= JvmAbi.isGetterName(simpleName.toString())
		&& returnType.kind != TypeKind.VOID
		&& parameters.isEmpty()

/** true if this javaElement may be a kotlin generated setter of a property */
internal fun ExecutableElement.maybeKotlinSetter()
		= JvmAbi.isSetterName(simpleName.toString())
		&& returnType.kind == TypeKind.VOID
		&& parameters.isNotEmpty()

internal fun TypeMirror.isPrimitive() = when(kind) {
	TypeKind.BOOLEAN, TypeKind.BYTE, TypeKind.SHORT, TypeKind.INT,
	TypeKind.LONG, TypeKind.CHAR, TypeKind.FLOAT, TypeKind.DOUBLE,
	TypeKind.VOID -> true
	else          -> false
}

//TODO("make internal for release")
val ProcessingEnvironment.kotlinMetadataUtils: KotlinMetadataUtils
	get() {
		return object : KotlinMetadataUtils {
			override val processingEnv = this@kotlinMetadataUtils
		}
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

internal typealias JvmSignature = String