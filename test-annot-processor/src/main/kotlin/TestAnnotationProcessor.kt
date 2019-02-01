package com.tschuchort.kotlinelements

import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.tools.Diagnostic
import PackageAnnotation
import mixins.EnclosesKotlinElements
import mixins.HasKotlinModality
import mixins.HasKotlinVisibility
import mixins.KotlinParameterizable

@Target(AnnotationTarget.CLASS)
annotation class ClassAnnotation

@Target(AnnotationTarget.FILE)
annotation class FileAnnotation

@Target(AnnotationTarget.TYPE)
annotation class TypeAnnotation

@Target(AnnotationTarget.FUNCTION)
annotation class FunctionAnnotation

@Target(AnnotationTarget.TYPEALIAS)
annotation class TypeAliasAnnotation

@Target(AnnotationTarget.LOCAL_VARIABLE)
annotation class LocalVarAnnotation

@Target(AnnotationTarget.FIELD)
annotation class FieldAnnotation

@Target(AnnotationTarget.PROPERTY)
annotation class PropertyAnnotation

@Target(AnnotationTarget.PROPERTY_SETTER)
annotation class SetterAnnotation

@Target(AnnotationTarget.PROPERTY_GETTER)
annotation class GetterAnnotation

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ParameterAnnotation

@Target(AnnotationTarget.TYPE_PARAMETER)
annotation class TypeParameterAnnotation

@Suppress("unused")
@AutoService(Processor::class)
internal class TestAnnotationProcessor : AbstractProcessor() {
	companion object {
		const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
		const val GENERATE_KOTLIN_CODE_OPTION = "generate.kotlin.code"
		const val GENERATE_ERRORS_OPTION = "generate.error"
		const val FILE_SUFFIX_OPTION = "suffix"
	}

	private val kaptKotlinGeneratedDir by lazy { processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] }
	private val generateErrors by lazy { processingEnv.options[GENERATE_ERRORS_OPTION] == "true" }
	private val generateKotlinCode by lazy { processingEnv.options[GENERATE_KOTLIN_CODE_OPTION] == "true" }
	private val generatedFilesSuffix by lazy { processingEnv.options[FILE_SUFFIX_OPTION] ?: "Generated" }

	private fun log(msg: String) = processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, msg)

	override fun getSupportedAnnotationTypes() = setOf(
			ClassAnnotation::class.java.canonicalName, FunctionAnnotation::class.java.canonicalName,
			TypeAliasAnnotation::class.java.canonicalName, LocalVarAnnotation::class.java.canonicalName,
			TypeAnnotation::class.java.canonicalName, ParameterAnnotation::class.java.canonicalName,
			FieldAnnotation::class.java.canonicalName, PropertyAnnotation::class.java.canonicalName,
			SetterAnnotation::class.java.canonicalName, GetterAnnotation::class.java.canonicalName,
			PackageAnnotation::class.java.canonicalName, TypeParameterAnnotation::class.java.canonicalName)

	override fun getSupportedOptions() = setOf(KAPT_KOTLIN_GENERATED_OPTION_NAME, GENERATE_KOTLIN_CODE_OPTION, GENERATE_ERRORS_OPTION)
	override fun getSupportedSourceVersion() = SourceVersion.latestSupported()!!

	fun getPackage(elem: Element): PackageElement {
		if(elem is PackageElement)
			return elem
		else if(elem.enclosingElement == null)
			throw IllegalArgumentException("javaElement $elem is not enclosed by a package")
		else
			return getPackage(elem.enclosingElement)
	}

	override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
		log("annotation processing... $annotations")

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(ClassAnnotation::class.java)) {
			log(annotatedElem.asKotlin(processingEnv)!!.printKotlinSummary())
			//log(annotatedElem.printSummary())
		}


		/*for (annotatedElem in roundEnv.getElementsAnnotatedWith(ClassAnnotation::class.java)) {
			/*val classData = (annotatedElem.kotlinMetadata as KotlinClassMetadata).data
			val (nameResolver, classProto) = classData
			val method = classProto.functionList.first()

			logW("method")

			logW(method.valueParameterList.map {
				"""
					name: ${nameResolver.getString(it.name)}
					getType: ${it.type.extractFullName(classData)}
					type from class typeTable: ${it.type(TypeTable(classProto.typeTable)).extractFullName(classData)}
				"""
			}.joinToString("\n"))*/

			logW(annotatedElem.printSummary())
			logW("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(FunctionAnnotation::class.java)) {

			logW(annotatedElem.asKotlin(processingEnv)!!.printKotlinSummary())
			logW("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(FieldAnnotation::class.java)) {
			logW(annotatedElem.asKotlin(processingEnv)!!.printKotlinSummary())
			logW("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(PropertyAnnotation::class.java)) {
			logW(annotatedElem.asKotlin(processingEnv)!!.printKotlinSummary())
			logW("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(SetterAnnotation::class.java)) {
			logW(annotatedElem.asKotlin(processingEnv)!!.printKotlinSummary())
			logW("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(GetterAnnotation::class.java)) {
			logW(annotatedElem.asKotlin(processingEnv)!!.printKotlinSummary())
			logW("----------------------------------------------------------------------------------------------------")
		}*/

		return true
	}

	fun KotlinRelatedElement.printKotlinSummary(): String {
		return "name: $this\n" +
			   (if (this is KotlinElement)
				   """
					   simpleName: $simpleName
					   directly present annotations: $annotationMirrors
				   """.trimIndent()
			   else "") +
			   (if (this is HasKotlinModality)
				   "\nkotlin modality: $modality"
			   else "") +
			   (if (this is HasKotlinVisibility)
				   "\nkotlin visibility: $visibility"
			   else "") +
			   (if (this is KotlinParameterizable) {
				   "\ntypeParameters:\n" + typeParameters.printKotlinSummary().prependIndent("\t")
			   }
			   else "") +
			   (if (this is KotlinExecutableElement) {
				   """
					    isVarArgs: $isVarArgs
					    receiverType: $receiverType
					    returnType: $returnType
					    thrownTypes: $thrownTypes
					""".trimIndent() +
				   "\njavaOverloads:\n" + javaOverloads.printKotlinSummary().prependIndent("\t")
			   }
			   else "") +
			   when (this) {
				   is KotlinPropertyElement -> """
					   isExternal: $isExternal
					   isReadOnly: $isReadOnly
					   isDelegated:	$isDelegated
					   isExpect: $isExpect
					   isLateInit: $isLateInit
				   """.trimIndent() +
					"\nsetter:" + setter?.printKotlinSummary()?.prependIndent("\t") +
					"\ngetter:" + getter?.printKotlinSummary()?.prependIndent("\t")	+
					"\nbacking field:" + backingField?.printKotlinSummary()?.prependIndent("\t")

				   is KotlinGetterElement -> """
				   """.trimIndent()

				   is KotlinSetterElement ->
					   "\nsetter parameter:" + parameters.first().printKotlinSummary().prependIndent("\t")

				   is KotlinTypeElement -> """
					   isExternal: $isExternal
					   isExpect: $isExpect
					   isInner: $isInner
					   superclass: $superclass
					   interfaces: $interfaces
				   """.trimIndent()

				   is KotlinClassElement -> """
					   isDataClass: $isDataClass
				   """.trimIndent()

				   /*is KotlinInterfaceElement -> """
					   $interfaceDefaultImpls
				   """.trimIndent()*/

				   is KotlinEnumElement ->
					   "\nenumConstants:\n" + enumConstants.printKotlinSummary().prependIndent("\t")

				   is KotlinEnumConstantElement -> """
				   """.trimIndent()

				   is KotlinAnnotationElement ->
					   "\nannotation parameters:\n" + parameters.printKotlinSummary().prependIndent("\t")

				   is KotlinAnnotationParameterElement -> """
					   defaultValue: $defaultValue
				   """.trimIndent()

				   is KotlinFunctionElement -> """
					   isExpectFunc: $isExpect
					   isExternalFunc: $isExternal
					   isInfix: $isInfix
					   isInline: $isInline
					   isOperator: $isOperator
					   isSuspend: $isSuspend
					   isTailRec: $isTailRec
				   """.trimIndent()

				   is KotlinConstructorElement -> """
					   isPrimary: $isPrimary
				   """.trimIndent()

				   is KotlinTypeParameterElement -> """
					   reified: $reified
					   variance: $variance
					   bounds: $bounds
				   """.trimIndent()

				   is KotlinTypeAliasElement -> """
					   expanded type: $expandedType
					   underlying type: $underlyingType
				   """.trimIndent()

				   is KotlinPackageElement -> """
					   isUnnamed: $isUnnamed
				   """.trimIndent() +
						"\njavaPackages:\n" + javaPackages.printSummary().prependIndent("\t") +
						"\nkotlinPackages:\n" + kotlinPackages.printKotlinSummary().prependIndent("\t")
				   else -> ""
			   } +
			   (if(this is EnclosesKotlinElements)
				   "\nenclosed Elements:\n" + enclosedKotlinElements.printKotlinSummary().prependIndent("\t")
			   	else
				   ""
			   )

	}

	fun List<KotlinRelatedElement>.printKotlinSummary() =
			joinToString("\n--------------------------------\n") { it.printKotlinSummary() }

	fun Set<KotlinRelatedElement>.printKotlinSummary() = toList().printKotlinSummary()

	fun Element.printSummary(): String {
		return """
			$this
			simpleName: $simpleName
			kind: $kind
			modifiers: $modifiers
			origin: ${processingEnv.elementUtils.getOrigin(this)}
			asType: ${asType()}
			annotations: $annotationMirrors
			""".trimIndent() + "\n" +
			   when (this) {
				   is ExecutableElement -> """
						receiverType: $receiverType
						returnType: $returnType
						thrownTypes: $thrownTypes
						isDefault: $isDefault
						defaultValue: $defaultValue
						isVarArgs: $isVarArgs
						jvmMethodSignature: ${getJvmMethodSignature(processingEnv)}
						parameters:
				   """.trimIndent() + "\n" +
										   parameters.printSummary().prependIndent("\t") +
										   "typeParameters:" +
										   typeParameters.printSummary().prependIndent("\t")
				   is TypeElement -> """
						nestingKind: $nestingKind
						superclass: $superclass
						interfaces: $interfaces
						qualifiedName: $qualifiedName
						asType: ${asType()}
				   """.trimIndent() + "\n" +
									 "typeParameters:\n" +
									 typeParameters.printSummary().prependIndent("\t")
				   is VariableElement -> """
						constantValue: $constantValue
				   """.trimIndent()
				   is PackageElement -> """
					   isUnnamed: $isUnnamed
				   """.trimIndent()
				   else -> ""
			   } + "\n" +
			   //"enclosingElement:" +
			   //enclosingElement?.printSummary()?.prependIndent("\t") +
			   "enclosed elements:\n" +
			   enclosedElements.printSummary().prependIndent("\t")
	}

	fun List<Element>.printSummary() =
			joinToString("\n--------------------------------\n") { it.printSummary() }

	fun Set<Element>.printSummary() = toList().printSummary()
}


