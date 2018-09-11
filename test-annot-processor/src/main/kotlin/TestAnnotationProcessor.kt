package com.tschuchort.kotlinelements

import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.tools.Diagnostic

@Target(AnnotationTarget.CLASS)
annotation class ClassAnnotation

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
			ClassAnnotation::class.java.name, FunctionAnnotation::class.java.name, TypeAliasAnnotation::class.java.name,
			LocalVarAnnotation::class.java.name, TypeAnnotation::class.java.name, ParameterAnnotation::class.java.name,
			FieldAnnotation::class.java.name, PropertyAnnotation::class.java.name, SetterAnnotation::class.java.name,
			GetterAnnotation::class.java.name)

	override fun getSupportedOptions() = setOf(KAPT_KOTLIN_GENERATED_OPTION_NAME, GENERATE_KOTLIN_CODE_OPTION, GENERATE_ERRORS_OPTION)
	override fun getSupportedSourceVersion() = SourceVersion.latestSupported()!!

	override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
		log("annotation processing... $annotations")

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(ClassAnnotation::class.java)) {
			/*val classData = (annotatedElem.kotlinMetadata as KotlinClassMetadata).data
			val (nameResolver, classProto) = classData
			val method = classProto.functionList.first()

			log("method")

			log(method.valueParameterList.map {
				"""
					name: ${nameResolver.getString(it.name)}
					getType: ${it.type.extractFullName(classData)}
					type from class typeTable: ${it.type(TypeTable(classProto.typeTable)).extractFullName(classData)}
				"""
			}.joinToString("\n"))*/

			log(annotatedElem.correspondingKotlinElement(processingEnv)!!.printKotlinSummary())
			log("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(FunctionAnnotation::class.java)) {

			log(annotatedElem.correspondingKotlinElement(processingEnv)!!.printKotlinSummary())
			log("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(FieldAnnotation::class.java)) {
			log(annotatedElem.correspondingKotlinElement(processingEnv)!!.printKotlinSummary())
			log("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(PropertyAnnotation::class.java)) {
			log(annotatedElem.correspondingKotlinElement(processingEnv)!!.printKotlinSummary())
			log("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(SetterAnnotation::class.java)) {
			log(annotatedElem.correspondingKotlinElement(processingEnv)!!.printKotlinSummary())
			log("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(GetterAnnotation::class.java)) {
			log(annotatedElem.correspondingKotlinElement(processingEnv)!!.printKotlinSummary())
			log("----------------------------------------------------------------------------------------------------")
		}

		return true
	}

	fun KotlinElement.printKotlinSummary(): String {
		return """
				   name: $this
				   simpleName: $simpleName
				   isTopLevel: ${isTopLevel()}
				   kind: $kind
				   modifiers: $modifiers
			   """.trimIndent() +
			   if (this is KotlinExecutableElement) {
				    """
					   isDefault: $isDefault
					   isVarArgs: $isVarArgs
					   receiverType: $receiverType
					   returnType: $returnType
					   thrownTypes: $thrownTypes
					   typeParameters:
					""".trimIndent() +
				   		typeParameters.printKotlinSummary().prependIndent("\t") +
					"javaElement:"
				        javaElement.printSummary().prependIndent("\t") +
					"jvmOverloads:"
				   		jvmOverloadElements.printSummary().prependIndent("\t")
			   }
			   else "" +
			   when (this) {
				   is KotlinPropertyElement -> """
					   visibility:$visibility
					   modality: $modality
					   hasConstant: $hasConstant
					   isConst: $isConst
					   isExternal: $isExternal
					   isReadOnly: $isReadOnly
					   isDelegated:	$isDelegated
					   isExpect: $isExpect
					   isLateInit: $isLateInit

					   hasGetter: $hasGetter
					   getterHasAnnotations: $getterHasAnnotations
					   getterModality: $getterModality
					   getterVisibility: $getterVisibility
					   isGetterDefault:	$isGetterDefault
					   isGetterNotDefault: $isGetterNotDefault
					   isGetterExternal: $isGetterExternal
					   isGetterInline: $isGetterInline

					   hasSetter: $hasSetter
					   setterHasAnnotations: $setterHasAnnotations
					   setterModality: $setterModality
					   setterVisibility: $setterVisibility
					   isSetterInline: $isSetterInline
					   isSetterExternal: $isSetterExternal
					   isSetterDefault: $isSetterDefault
					   isSetterNotDefault: $isSetterNotDefault
				   """.trimIndent() +
											   "\njavaField:" +
											   javaFieldElement?.printSummary()?.prependIndent("\t") +
											   "\njavaSetter:" +
											   javaSetterElement?.printSummary()?.prependIndent("\t") +
											   "\njavaGetter:" +
											   javaGetterElement?.printSummary()?.prependIndent("\t")
				   is KotlinTypeElement -> """
					   packageName: $packageName
					   nestingKind: $nestingKind
					   isExternalClass: $isExternalClass
					   isDataClass: $isDataClass
					   isExpectClass: $isExpectClass
					   isInnerClass: $isInnerClass
					   isObject: $isObject
					   modality: $modality
					   visibility: $visibility
					   constructors:
				   """.trimIndent() +
							constructors.printKotlinSummary().prependIndent("\t") +
						"\ndeclaredMethods:" +
							declaredMethods.printKotlinSummary().prependIndent("\t") +
						"\ntypeParams:" +
							typeParameters.printKotlinSummary().prependIndent("\t") +
						"\ncompanionObject:" +
							companionObject?.printKotlinSummary()?.prependIndent("\t")

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

				   else -> ""
			   } //+
			   //"\nenclosed Elements:" +
			       //enclosedElements.printKotlinSummary().prependIndent("\t")
	}

	fun List<KotlinElement>.printKotlinSummary() =
			joinToString("\n--------------------------------\n") { it.printKotlinSummary() }

	fun Element.printSummary(): String {
		return """
			$this
			simpleName: $simpleName
			kind: $kind
			modifiers: $modifiers
			origin: ${processingEnv.elementUtils.getOrigin(this)}
			originatesFromKotlin: ${originatesFromKotlinCode()}
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
			   "enclosedElements:\n" +
			   enclosedElements.printSummary().prependIndent("\t")
	}

	fun List<Element>.printSummary() =
			joinToString("\n--------------------------------\n") { it.printSummary() }



}

