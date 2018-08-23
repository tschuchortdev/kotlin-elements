package com.tschuchort.kotlinelements

import com.google.auto.service.AutoService
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.extractFullName
import me.eugeniomarletti.kotlin.metadata.jvm.jvmMethodSignature
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.TypeTable
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.type
import java.lang.annotation.RetentionPolicy
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
annotation class VariableAnnotation

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
			VariableAnnotation::class.java.name, TypeAnnotation::class.java.name, ParameterAnnotation::class.java.name)

	override fun getSupportedOptions() = setOf(KAPT_KOTLIN_GENERATED_OPTION_NAME, GENERATE_KOTLIN_CODE_OPTION, GENERATE_ERRORS_OPTION)
	override fun getSupportedSourceVersion() = SourceVersion.latestSupported()!!

	override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
		log("annotation processing... $annotations")

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(ClassAnnotation::class.java)) {
			val classData = (annotatedElem.kotlinMetadata as KotlinClassMetadata).data
			val (nameResolver, classProto) = classData
			val method = classProto.functionList.first()

			log("method")

			log(method.valueParameterList.map {
				"""
					name: ${nameResolver.getString(it.name)}
					getType: ${it.type.    extractFullName(classData)}
					type from class typeTable: ${it.type(TypeTable(classProto.typeTable)).extractFullName(classData)}
				"""
			}.joinToString("\n"))

			//log(annotatedElem.printSummary())
			log("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(FunctionAnnotation::class.java)) {

			//log(annotatedElem.printSummary())
			log("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(ParameterAnnotation::class.java)) {

		}

		return true
	}

	/*fun KotlinElement.printSummary(): StringWriter {
		return StringWriter() +
			   """
				   name: $this
				   simpleName: $simpleName
				   isTopLevel: $isTopLevel
				   kind: $kind
				   modifiers: $modifiers
				   isKotlinElement: ${isKotlinElement()}
			   """.trimIndent() +
			   if (this is KotlinExecutableElement) {
				   StringWriter() + """
					   isDefault: $isDefault
					   isVarArgs: $isVarArgs
					   receiverType: $receiverType
					   returnType: $returnType
					   thrownTypes: $thrownTypes
					   typeParameters:
				   """.trimIndent() +
				   typeParameters.map { it.printSummary() }.combine().indent()
			   }
			   else {
				   StringWriter()
			   } +
			   when (this) {
				   is KotlinTypeElement -> StringWriter() + """
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
										   constructors.map { it.printSummary() }.combine().indent() +
										   "declaredMethods:" +
										   declaredMethods.map { it.printSummary() }.combine().indent() +
										   "typeParams:" +
										   typeParameters.map { it.printSummary() }.combine().indent() +
										   "companionObject:" +
										   companionObject?.printSummary()?.indent()

				   is KotlinFunctionElement -> StringWriter() + """
					   isExpectFunc: $isExpectFunction
					   isExternalFunc: $isExternalFunction
					   isInfix: $isInfix
					   isInline: $isInline
					   isOperator: $isOperator
					   isSuspend: $isSuspend
					   isTailRec: $isTailRec
				   """.trimIndent()

				   is KotlinConstructorElement -> StringWriter() + """
					   isPrimary: $isPrimary
				   """.trimIndent()

				   is KotlinTypeParameterElement -> StringWriter() + """
					   reified: $reified
					   variance: $variance
					   bounds: $bounds
				   """.trimIndent()

				   is KotlinPackageElement -> StringWriter() + """
					   jvmPackageModuleName: $jvmPackageModuleName
				   """.trimIndent()

				   else -> StringWriter()
			   } +
			   "enclosed Elements:" +
			   enclosedElements.map { it.printSummary() }.combine().indent()
	}*/

	fun Element.printSummary(): String {
		return """
			$this
			simpleName: $simpleName
			kind: $kind
			modifiers: $modifiers
			isKotlinElement: ${isKotlinElement()}
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
						isKotlinClass: ${isKotlinClass()}
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

