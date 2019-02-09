package com.tschuchort.kotlinelements

import com.esotericsoftware.kryo.io.Output
import okio.Buffer
import serialization.SerializedMsgOverStdout
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.tools.Diagnostic
import java.util.*
import serialization.getKryo
import javax.annotation.processing.AbstractProcessor


/**
 * Mark element to be serialized for testing by [SerializeAnnotatedElementProcessor].
 */
@Target(AnnotationTarget.TYPEALIAS, AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS,
        AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FIELD, AnnotationTarget.FILE, AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.FUNCTION, AnnotationTarget.LOCAL_VARIABLE, AnnotationTarget.PROPERTY, AnnotationTarget.FILE,
        AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SerializeElemForTesting

/**
 * An annotation processor for testing purposes that serializes the annotated element
 * and then sends it base64-encoded via stdout so it can be deserialized and inspected
 * in a JUnit test.
 */
class SerializeAnnotatedElementProcessor : KotlinAbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedAnnotationTypes(): Set<String>
            = setOf(SerializeElemForTesting::class.java.canonicalName)

    override fun init(processingEnv: ProcessingEnvironment) {
        processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "${this::class.simpleName} init")
        super.init(processingEnv)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val kryo = getKryo()

        for(jElem in roundEnv.getElementsAnnotatedWith(SerializeElemForTesting::class.java)) {
            val kElem = jElem.asKotlin(processingEnv)

            val buffer = Buffer()
            val out = Output(buffer.outputStream())
            kryo.writeClassAndObject(out, kElem ?: jElem)
            out.close()

            val base64Encoded = Base64.getEncoder().encodeToString(buffer.readByteArray())
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, SerializedMsgOverStdout(base64Encoded).print())
        }

        return false
    }
}
