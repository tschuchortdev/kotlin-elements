package com.tschuchort.kotlinelements
import java.util.*

internal inline fun <reified R : Any> List<*>.castList(): List<R> = map { it as R }

internal fun <T> List<T>.allEqual(): Boolean = distinct().size == 1

/**
 * checks if all elements in the list are equal with respect to the comparator
 */
internal inline fun <T> List<T>.allEqualBy(crossinline areEqual: (T, T) -> Boolean): Boolean {
	if(size < 2)
		return true

	val first = first()

	for(other in this) {
		if(!areEqual(first, other))
			return false
	}

	return true
}

/**
 * checks if all elements in the list are equal with respect to the metric (a property for example)
 */
internal inline fun <T,R> List<T>.allEqualBy(crossinline metric: T.() -> R): Boolean {
	if(size < 2)
		return true

	val firstMetric = first().metric()

	for(other in this) {
		if(firstMetric != other.metric())
			return false
	}

	return true
}

internal inline fun <T,S,R> List<T>.zipWith(other: List<S>, crossinline zipper: (T,S) -> R): List<R>
		= zip(other).map { (fst, snd) -> zipper(fst, snd) }

internal inline fun <T> Collection<T>.atMostOne(crossinline predicate: (T) -> Boolean): T? {
	val elems = filter(predicate)

	return when(elems.size) {
		0 -> null
		1 -> elems.single()
		else -> throw IllegalStateException("more than one javaElement fit the predicate")
	}
}

/**
 * checks whether [this] is equal to a subset of [superset]
 * i.e. every javaElement of [this] is equal to some (not necessarily
 * distinct) javaElement in [superset]
 */
internal fun <T> List<T>.equalsSubset(superset: List<T>)
		= superset.toHashSet().containsAll(this)

internal fun String.removeFirstOccurance(literal: String) = replaceFirst(Regex(Regex.escape(literal)), "")

internal fun <T : Any> setOfNotNull(vararg elements: T?): Set<T> = listOfNotNull(*elements).toSet()

internal data class MutablePair<A, B>(var first: A, var second: B) {
	/** Returns string representation of the [MutablePair] including its [first] and [second] values. */
	override fun toString(): String = "($first, $second)"

	fun toPair() = Pair(first, second)
}

internal fun <T> MutableCollection<T>.addAll(vararg args: T): Boolean = addAll(args.toList())


internal fun removeTypeArgs(s: String): String {
	val openBrackets = Stack<Int>()

	for(pos in 0 .. s.lastIndex) {
		if(s[pos] == '<')
			openBrackets.push(pos)
		else if(s[pos] == '>') {
			if(openBrackets.size == 1)
				return removeTypeArgs(
						s.removeRange(openBrackets.peek(), pos)
				)

			openBrackets.pop()
		}
	}

	return s
}

internal fun <T> Set<T>.containsAny(vararg elems: T): Boolean {
	for (elem in elems) {
		if (contains(elem))
			return true
	}

	return false
}

internal class LruCache<K, V>(val maxEntries: Int) : LinkedHashMap<K, V>(maxEntries + 1) {
	override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>): Boolean
			= super.size > maxEntries
}

