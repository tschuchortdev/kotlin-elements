package mixins

import com.tschuchort.kotlinelements.KotlinElement
import com.tschuchort.kotlinelements.KotlinTypeParameterElement
import com.tschuchort.kotlinelements.zipWith
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.lang.model.element.TypeParameterElement


/**
 * A Kotlin element that can have type parameters
 */
interface KotlinParameterizable {
	 val typeParameters: List<KotlinTypeParameterElement>
}

/**
 * Delegate for implementing [KotlinParameterizable] in multiple classes
 */
internal class KotlinParameterizableDelegate(
	private val enclosingKtElement: KotlinElement,
	private val protoTypeParams: List<ProtoBuf.TypeParameter>,
	private val javaTypeParams: List<TypeParameterElement>,
	private val protoNameResolver: NameResolver
) : KotlinParameterizable {

	override val typeParameters: List<KotlinTypeParameterElement> by lazy {
		protoTypeParams.zipWith(javaTypeParams) { protoTypeParam, javaTypeParam ->
			if (doTypeParamsMatch(javaTypeParam, protoTypeParam))
				KotlinTypeParameterElement(
					javaTypeParam,
					protoTypeParam,
					enclosingKtElement
				)
			else
				throw AssertionError("Kotlin ProtoBuf.TypeParameters should always " +
									 "match up with Java TypeParameterElements")
		}
	}

	private fun doTypeParamsMatch(typeParamElem: TypeParameterElement, protoTypeParam: ProtoBuf.TypeParameter): Boolean
			= (typeParamElem.simpleName.toString() == protoNameResolver.getString(protoTypeParam.name))

}