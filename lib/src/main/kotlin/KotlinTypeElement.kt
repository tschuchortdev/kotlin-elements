/*
 * Copyright (C) 2018 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tschuchort.kotlinelements

import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import javax.lang.model.element.TypeElement
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

open class KotlinTypeElement internal constructor(private val element: TypeElement, metadata: KotlinClassMetadata)
	: KotlinElement(element, metadata.data.nameResolver), TypeElement {

	protected val protoClass: ProtoBuf.Class = metadata.data.proto

	val fqName: String = protoNameResolver.getString(protoClass.fqName).replace('/', '.')
	val packageName: String = protoNameResolver.getString(protoClass.fqName).substringBeforeLast('/').replace('/', '.')

	val classKind = protoClass.classKind
	val visibility = protoClass.visibility
	val isInnerClass = protoClass.isInnerClass
	val isExpectClass = protoClass.isExpectClass
	val isExternalClass = protoClass.isExternalClass
	val isDataClass = protoClass.isDataClass
	val modality = protoClass.modality

	val companionObjectName: String? =
			if (protoClass.hasCompanionObjectName())
				metadata.data.nameResolver.getQualifiedClassName(protoClass.companionObjectName)
			else
				null

	companion object {
		fun get(element: TypeElement): KotlinTypeElement? {
			return (element.kotlinMetadata as? KotlinClassMetadata)?.let { metadata ->
				KotlinTypeElement(element, metadata)
			}
		}
	}

	override fun getSuperclass(): TypeMirror = element.superclass

	override fun getTypeParameters(): MutableList<out KotlinTypeParameterElement> = element.typeParameters.zip(protoClass.typeParameterList)
			.map { (typeParamElem, protoTypeParam) ->
				if (typeParamElem.simpleName.toString() == protoNameResolver.getString(protoTypeParam.name)) {
					KotlinTypeParameterElement(typeParamElem, protoTypeParam, protoNameResolver)
				}
				else {
					throw IllegalStateException("type parameter names for TypeElement $element don't match up with " +
												"the ProtoBuf TypeParameters in it's associated metadata ProtoBuf.Class:\n" +
												"	Java TypeElement type parameter: ${typeParamElem.simpleName}\n" +
												"	ProtoBuf.TypeParameter: ${protoTypeParam.name}")
				}
			}
			.toMutableList()


	override fun getQualifiedName(): Name = element.qualifiedName

	override fun getInterfaces(): MutableList<out TypeMirror> = element.interfaces

	override fun getNestingKind(): NestingKind = element.nestingKind

}

fun TypeElement.isKotlinClass() = kotlinMetadata is KotlinClassMetadata