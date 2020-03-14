package com.tschuchort.kotlinelements.mixins

import kotlinx.metadata.KmVariance
import javax.lang.model.type.WildcardType

/** Variance of a type parameter */
enum class KJVariance  {
	IN {
		override val keyword = "in"
	},
	OUT {
		override val keyword = "out"
	},
	INVARIANT {
		override val keyword = ""
	};

	abstract val keyword: String

	companion object {
		/*internal fun fromProtoBuf(variance: ProtoBuf.TypeParameter.Variance): KJVariance
				= when(variance) {
			ProtoBuf.TypeParameter.Variance.IN -> IN
			ProtoBuf.TypeParameter.Variance.OUT -> OUT
			ProtoBuf.TypeParameter.Variance.INV -> INVARIANT
		}*/

		internal fun fromKm(variance: KmVariance): KJVariance = when (variance) {
			KmVariance.IN        -> IN
			KmVariance.OUT       -> OUT
			KmVariance.INVARIANT -> INVARIANT
		}
	}
}

/** Mixin interface for an element or type mirror that has a variance */
interface HasVariance {
	val variance: KJVariance
}