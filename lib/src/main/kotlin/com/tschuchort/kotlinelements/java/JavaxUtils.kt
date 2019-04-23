/**
 * Utilities for dealing with elements, kotlin metadata/protobuf classes
 * and JVM stuff
 */
package com.tschuchort.kotlinelements.java


import javax.lang.model.element.*
import javax.lang.model.util.ElementScanner9


/**
 * A local element is an element declared within an executable element.
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



