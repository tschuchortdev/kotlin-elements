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


annotation class SerializeElem

class SerializeAnnotatedElementProcessor : KotlinAbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(SerializeElem::class.java.canonicalName)

    override fun init(processingEnv: ProcessingEnvironment) {
        processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "${this::class.simpleName} init")
        super.init(processingEnv)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val kryo = getKryo()

        for(annotatedElem in roundEnv.getElementsAnnotatedWith(SerializeElem::class.java)) {
            val buffer = Buffer()
            val out = Output(buffer.outputStream())
            kryo.writeClassAndObject(out, annotatedElem)
            out.close()

            val base64Encoded = Base64.getEncoder().encodeToString(buffer.readByteArray())
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, SerializedMsgOverStdout(base64Encoded).print())
        }

        return false
    }
}
