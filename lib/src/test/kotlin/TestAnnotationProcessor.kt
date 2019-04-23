
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

private class TestAnnotationProcessor(private val testElement: (Element) -> Unit) : AbstractProcessor() {

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

	override fun getSupportedAnnotationTypes() = setOf(TestElement::class.java.canonicalName)

	override fun getSupportedOptions() =
		setOf(KAPT_KOTLIN_GENERATED_OPTION_NAME, GENERATE_KOTLIN_CODE_OPTION, GENERATE_ERRORS_OPTION)

	override fun getSupportedSourceVersion() = SourceVersion.latestSupported()!!

	override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
		for (annotatedElem in roundEnv.getElementsAnnotatedWith(TestElement::class.java)) {
			testElement(annotatedElem)
		}

		return true
	}
}

fun testAnnotatedElement(source: SourceFile, inheritClasspath: Boolean = true,
						 testElement: (Element) -> Unit)
		= testAnnotatedElement(listOf(source), inheritClasspath, testElement)

fun testAnnotatedElement(sources: List<SourceFile>, inheritClasspath: Boolean = true,
						 testElement: (Element) -> Unit): KotlinCompilation.Result {
	return KotlinCompilation().apply {
		this.sources = sources
		annotationProcessors = listOf(TestAnnotationProcessor(testElement))
		this.inheritClassPath = inheritClasspath
		messageOutputStream = System.out
		verbose = false
	}.compile()
}