package utils

import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * The cleaner shorthand for printing output.
 */
fun <T> T.show(context: String? = null): T = also {
    if (context != null) print("$context: ")
    println(this)
}

fun Int.println() = show()
fun Long.println() = show()

fun String.splitOnSpaces() = split(' ').filter { it.isNotBlank() }
fun String.splitToInts() = splitOnSpaces().map { it.toInt() }
fun String.splitToLongs() = splitOnSpaces().map { it.toLong() }
fun List<String>.notBlank() = filter { it.isNotBlank() }

fun leastCommonMultiple(a: Long, b: Long): Long = (a * b) / greatestCommonDivisor(a, b)
tailrec fun greatestCommonDivisor(a: Long, b: Long): Long {
    check(b > 0L) { "No GCD for $a, $b" }
    val mod = a % b
    return if (mod == 0L) b else greatestCommonDivisor(b, mod)
}

fun <T> List<T>.allPairs(): Sequence<Pair<T, T>> = allPermutations(2, 2).map { it[0] to it[1] }

fun <T> List<T>.allPermutations(minLength: Int = 0, maxLength: Int = size): Sequence<List<T>> = sequence {
    check(minLength <= maxLength) { "minLength ($minLength) > maxLength ($maxLength)" }
    check(minLength >= 0) { "minLength ($minLength) < 0" }
    check(minLength <= size) { "minLength ($minLength) > size ($size)" }
    this.yieldAllPermutations(minLength, maxLength, this@allPermutations, mutableListOf<T>())
}

private suspend fun <T> SequenceScope<List<T>>.yieldAllPermutations(
    minLength: Int, maxLength: Int, remainder: List<T>, currentPermutation: MutableList<T>
) {
    if (maxLength == 0 || remainder.isEmpty())
        yield(currentPermutation.toList())
    else {
        val tail = remainder.subList(1, remainder.size)
        currentPermutation += remainder[0]
        yieldAllPermutations(minLength - 1, maxLength - 1, tail, currentPermutation)
        currentPermutation.removeLast()
        if (minLength < remainder.size)
            yieldAllPermutations(minLength, maxLength, tail, currentPermutation)
    }
}

@JvmName("transposeStrings")
fun List<String>.transpose(): List<String> = this[0].indices.map { this.column(it) }
fun List<String>.column(c: Int): String = buildString { this@column.forEach { s -> append(s[c]) } }

@JvmName("transposeLists")
fun <T> List<List<T>>.transpose(): List<List<T>> = this[0].indices.map { this.column(it) }
fun <T> List<List<T>>.column(c: Int): List<T> = map { it[c] }

@JvmName("splitThis")
fun <T> List<T>.split(predicate: (T) -> Boolean): List<List<T>> = split(this, predicate)

@JvmName("split")
private fun <T> split(src: List<T>, predicate: (T) -> Boolean): List<List<T>> = buildList {
    var start = 0
    src.indices.filter { predicate(src[it]) }.forEach { cur ->
        add(src.subList(start, cur))
        start = cur + 1
    }
    add(src.subList(start, src.size))
}

enum class AnsiColor(private val fg: Int, private val bg: Int) {
    BLACK(30, 40), RED(31, 41), GREEN(32, 42), YELLOW(33, 43), BLUE(34, 44),
    MAGENTA(35, 45), CYAN(36, 46), WHITE(37, 47), DEFAULT(39, 49), RESET(0, 0);

    fun fgCode(): String = "\u001b[${fg}m"
    fun bgCode(): String = "\u001b[${bg}m"
}
