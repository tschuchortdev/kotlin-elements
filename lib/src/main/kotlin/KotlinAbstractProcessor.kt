package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.shadow.load.java.JvmAnnotationNames
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.*
import javax.lang.model.util.Elements
import javax.lang.model.util.Types


/**
 * Base class for annotation processors that want to process Kotlin code
 */
abstract class KotlinAbstractProcessor protected constructor() : AbstractProcessor() {

	companion object {
		private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
		private const val GENERATE_KOTLIN_CODE_OPTION = "generate.kotlin.code"
		private const val GENERATE_ERRORS_OPTION = "generate.error"
		private const val FILE_SUFFIX_OPTION = "suffix"
	}

	/** The directory where generated files should be placed */
	protected val kaptKotlinGeneratedDir: String by lazy { processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]!! }

	/** Whether or not to generate errors */
	protected val generateErrors: Boolean by lazy { processingEnv.options[GENERATE_ERRORS_OPTION] == "true" }

	/** Whether or not to generate Kotlin code */
	protected val generateKotlinCode: Boolean by lazy { processingEnv.options[GENERATE_KOTLIN_CODE_OPTION] == "true" }

	/**
	 * The preferred file name suffix for generated files
	 *
	 * `null` if no preference was given, empty if no suffix is preferred
	 * */
	protected val generatedFilesSuffix: String? by lazy { processingEnv.options[FILE_SUFFIX_OPTION] }

	protected val javaElementUtils: Elements by lazy { processingEnv.elementUtils }

	protected val javaTypeUtils: Types by lazy { processingEnv.typeUtils }

	override fun getSupportedAnnotationTypes(): Set<String> {
		return super.getSupportedAnnotationTypes() + JvmAnnotationNames.METADATA_FQ_NAME.asString()
	}

	override fun getSupportedOptions(): Set<String> {
		return super.getSupportedOptions() + setOf(
				KAPT_KOTLIN_GENERATED_OPTION_NAME, GENERATE_KOTLIN_CODE_OPTION, GENERATE_ERRORS_OPTION
		)
	}

	protected fun RoundEnvironment.getJavaElementsAnnotatedWith(annotation: Class<out Annotation>): Set<Element>
			= getJavaElementsAnnotatedWithAny(setOf(annotation))

	protected fun RoundEnvironment.getJavaElementsAnnotatedWithAny(annotations: Set<Class<out Annotation>>): Set<Element> {
		return getElementsAnnotatedWithAny(annotations).asSequence().filter { !it.originatesFromKotlinCode() }.toSet()
	}

	protected fun RoundEnvironment.getKotlinRelatedElementsAnnotatedWith(annotation: Class<out Annotation>): Set<KotlinRelatedElement>
			= getKotlinRelatedElementsAnnotatedWithAny(setOf(annotation))

	protected fun RoundEnvironment.getKotlinRelatedElementsAnnotatedWithAny(annotations: Set<Class<out Annotation>>): Set<KotlinRelatedElement> {
		val annotationsElements = annotations.map { javaElementUtils.getTypeElement(it.canonicalName) }

		return rootElements.mapNotNull { it.asKotlin(processingEnv) }
				.flatMap { rootElem: KotlinRelatedElement ->
					rootElem
				}
				.toSet()
	}

	/**
	 * Returns iterables of all the Kotlin and Java elements that are defined within this
	 * [KotlinRelatedElement] (including parameters and type parameters), so that all the standard
	 * library convenience functions (like filtering, folding etc.) can be used on them
	 */
	fun KotlinRelatedElement.elementsAsIterable(): Pair<Iterable<KotlinRelatedElement>, Iterable<Element>> {
		return this.foldElements(Pair(mutableListOf<KotlinRelatedElement>(), mutableListOf<Element>()),
				{ ktElem, lists ->
					lists.apply { first += ktElem }
				},
				{ javaElem, lists ->
					lists.apply { second += javaElem }
				}
		)
	}

	/**
	 * Folds the tree of this element and all the elements defined within it
	 * (including parameters and type parameters)
	 */
	tailrec fun <R> KotlinRelatedElement.foldElements(defaultValue: R,
											   foldKotlin: (KotlinRelatedElement, R) -> R,
											   foldJava: (Element, R) -> R): R {
		return when(this) {
			is KotlinCompatElement ->;
			is KotlinModuleElement ->;
			is KotlinPackageElement ->;
			is KotlinAnnotationElement ->;
			is KotlinAnnotationParameterElement ->;
			is KotlinClassElement ->;
			is KotlinInterfaceElement ->;
			is KotlinObjectElement ->;
			is KotlinFunctionElement ->;
			is KotlinConstructorElement ->;
			is KotlinParameterElement ->;
			is KotlinTypeParameterElement ->;
			is KotlinTypeAliasElement ->;
			is KotlinPropertyElement ->;
			is KotlinEnumElement ->;
			is KotlinEnumConstantElement ->;
		}
	}
}

