package com.tschuchort.kotlinelements

import com.google.auto.service.AutoService
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
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
	private val generatedFilesSuffix by lazy { processingEnv.options[FILE_SUFFIX_OPTION] ?: "Generated"}

	private fun log(msg: String) = processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, msg)

	override fun getSupportedAnnotationTypes() = setOf(
			ClassAnnotation::class.java.name, FunctionAnnotation::class.java.name, TypeAliasAnnotation::class.java.name,
			VariableAnnotation::class.java.name, TypeAnnotation::class.java.name, ParameterAnnotation::class.java.name)

	override fun getSupportedOptions() = setOf(KAPT_KOTLIN_GENERATED_OPTION_NAME, GENERATE_KOTLIN_CODE_OPTION, GENERATE_ERRORS_OPTION)
	override fun getSupportedSourceVersion() = SourceVersion.latestSupported()!!

	override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
		log("annotation processing... $annotations")

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(ClassAnnotation::class.java)) {
			log("----------------------------------------------------------------------------------------------------")
			annotatedElem.printSummary()

			with(KotlinTypeElement.get(annotatedElem as TypeElement, processingEnv)!!) {
				log("classKind: $classKind")
				log("packageName: $packageName")
				log("companion: $companionObject")
				log("qualifiedName: $qualifiedName")
				log("isDataClass: $isDataClass")
				log("visibility: $visibility")
				log("modality: $modality")
				log("isExternalClass: $isExternalClass")
				log("isInnerClass: $isInnerClass")
				log("isExpectClass: $isExpectClass")
				log("typeParameters: ${typeParameters.joinToString(", ") { "$it variance: ${it.variance} reified: ${it.reified}" }}")



				typeParameters.forEach {
					log("typeParam ${it.simpleName} metadata kind: ${it.kotlinMetadata?.header?.kind}")
				}
			}

			val (nameResolver, classProto) = (annotatedElem.kotlinMetadata as KotlinClassMetadata).data

			classProto.typeParameterList.forEach {
				log("protoTypeParam ${nameResolver.getString(it.name)}")
			}


			log("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(FunctionAnnotation::class.java)) {
			log("----------------------------------------------------------------------------------------------------")
			annotatedElem.printSummary()
			//annotatedElem.printParents()

			with(annotatedElem as ExecutableElement) {
				log("$typeParameters")
			}

			log("----------------------------------------------------------------------------------------------------")
		}

		for (annotatedElem in roundEnv.getElementsAnnotatedWith(ParameterAnnotation::class.java)) {
			annotatedElem.printSummary()
		}

		return true
	}

	fun Element.printSummary() {
		log("$this")
		log("kind: $kind")
		log("simpleName $simpleName")
		log("metadata: $kotlinMetadata")

		log("enclosedElements: ")
		for(element in enclosedElements) {
			element.printSummary()
		}
	}

	fun Element.printParents() {
		enclosingElement?.let {
			log("enclosing element:")
			it.printSummary()
			it.printParents()
		}
	}
}

