package com.tschuchort.kotlinelements

/*import com.tschuchort.kotlinelements.from_javax.*
import com.tschuchort.kotlinelements.from_javax.JxConstructorElement
import com.tschuchort.kotlinelements.from_javax.JxInitializerElement
import com.tschuchort.kotlinelements.from_javax.JxMethodElement
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import kotlin.reflect.jvm.internal.impl.util.capitalizeDecapitalize.CapitalizeDecapitalizeKt

data class ConvertedElems(
		val set: Set<KJElement>,
		private val memberLookupTable: Map<Element, KJElement>
) {
	fun lookupJavaxElement(javaxElement: Element): KJElement?
			= memberLookupTable[javaxElement]
}

fun Set<KJElement>.inferPropertiesByName(processingEnv: ProcessingEnvironment) : ConvertedElems {
	val elems = this
	val convertedElems = ArrayList<KJElement>(elems.size)
	val propertyCandidates = HashMap<PropertySignature, PropertyCandidates>(elems.size)

	fun modifyPropertyCandidates(propSig: PropertySignature, f: PropertyCandidates.() -> Unit) {
		propertyCandidates[propSig] = (propertyCandidates[propSig] ?: PropertyCandidates()).apply(f)
	}

	fun addGetterCandidate(propSig: PropertySignature, getter: ExecutableElement)
			= modifyPropertyCandidates(propSig) { getters += getter }

	fun addSetterCandidate(propSig: PropertySignature, setter: ExecutableElement)
			= modifyPropertyCandidates(propSig) { setters += setter }

	fun addBackingFieldCandidate(propSig: PropertySignature, field: VariableElement)
			= modifyPropertyCandidates(propSig) { fields += field }

	for (elem in elems) {
		when (elem) {
			is KJFunctionElement -> {
				val methodElem = elem as ExecutableElement

				getterPropertyBaseNameOrNull(methodElem)?.let { baseName ->
					addGetterCandidate(PropertySignature(baseName, methodElem.returnType), methodElem)
				} ?:
				setterPropertyBaseNameOrNull(methodElem)?.let { baseName ->
					addSetterCandidate(
							PropertySignature(baseName, methodElem.parameters.first().asType()),
							methodElem
					)
				} ?:
				run {
					convertedElems += JxMethodElement(
							methodElem, enclosingElem, inheritedOrigin, processingEnv
					)
				}
			}
			is KJFieldElement  -> {
				val baseName = PropertyBaseName.fromField(elem as VariableElement)
				addBackingFieldCandidate(PropertySignature(baseName, elem.asType()), elem)
			}
			else               -> convertedElems += elem
		}
	}

	for (propertyCandidate in propertyCandidates) {
		val baseName = propertyCandidate.key.baseName
		val propType = propertyCandidate.key.type
		val getters = propertyCandidate.value.getters
		val setters = propertyCandidate.value.setters
		val fields = propertyCandidate.value.fields

		check(setters.size <= 1) {
			"There are multiple Java setter elements with the same type and " +
					"base name ($baseName): $setters. This should never happen."
		}

		check(getters.size <= 2) {
			"There are more than two Java getter elements with the same type and " +
					"base name ($baseName): $getters. This should never happen."
		}

		val setter = setters.singleOrNull()

		val getter = if (getters.size > 1) {



			processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "Ambiguous property accessor mapping: " +
					"There are multiple getters that the setter $setter could belong to: $getters. " +
					"It is unclear which getter should be assigned to the property of this setter. Choosing the ")

			TODO()
		}
		else {
			getters.singleOrNull()
		}
	}

	return TODO("infer properties by name")
}

private data class PropertyCandidates(
		var getters: List<ExecutableElement> = emptyList(),
		var setters: List<ExecutableElement> = emptyList(),
		var fields: List<VariableElement> = emptyList()
)

private data class PropertySignature(
		val baseName: String,
		val type: TypeMirror
)

private object PropertyBaseName {
	fun fromField(fieldElem: VariableElement): String {
		val simpleName = fieldElem.simpleName.toString()
		return when {
					!fieldElem.modifiers.containsAny(Modifier.PRIVATE, Modifier.PROTECTED)
						 -> simpleName

					simpleName.startsWith('m')
							&& simpleName[1].isUpperCase()
							&& simpleName[1].isJavaIdentifierPart()
						 -> CapitalizeDecapitalizeKt.decapitalizeAsciiOnly(simpleName.removePrefix("m"))

					simpleName.startsWith("m_")
							&& simpleName[2].isJavaIdentifierPart()
						 -> CapitalizeDecapitalizeKt.decapitalizeAsciiOnly(simpleName.removePrefix("m_"))

					else -> simpleName
		}
	}

	/** Returns the property base name if this elem could be a getter or null */
	fun fromGetterOrNull(elem: ExecutableElement, processingEnv: ProcessingEnvironment): String? {
		val simpleName = elem.simpleName.toString()

		val boxedBooleanType = with(processingEnv.typeUtils) {
			boxedClass(getPrimitiveType(TypeKind.BOOLEAN))
		}

		return if (elem.parameters.isEmpty()) {
			null
		}
		else if (simpleName.startsWith("get") && simpleName.length > 3
				&& simpleName[3].isUpperCase() && simpleName[3].isJavaIdentifierPart()
				&& elem.returnType.kind != TypeKind.VOID) {

			CapitalizeDecapitalizeKt.decapitalizeAsciiOnly(simpleName.removePrefix("get"))
		}
		else if (simpleName.startsWith("is") && simpleName.length > 2
				&& simpleName[2].isUpperCase() && simpleName[2].isJavaIdentifierPart()
				&& (elem.returnType.kind == TypeKind.BOOLEAN || elem.returnType == boxedBooleanType)) {
			CapitalizeDecapitalizeKt.decapitalizeAsciiOnly(simpleName.removePrefix("is"))
		}
		else null
	}

	fun fromSetterOrNull(elem: ExecutableElement): String? {
		val simpleName = elem.simpleName.toString()

		return if (simpleName.startsWith("set") && simpleName.length > 3
				&& simpleName[3].isUpperCase() && simpleName[3].isJavaIdentifierPart()
				&& elem.parameters.size == 1
				&& elem.parameters.first().asType().kind != TypeKind.VOID
				&& elem.returnType.kind == TypeKind.VOID) {

			CapitalizeDecapitalizeKt.decapitalizeAsciiOnly(simpleName.removePrefix("set"))
		}
		else null
	}
}
*/
