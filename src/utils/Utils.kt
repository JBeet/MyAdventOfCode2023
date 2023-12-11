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
    this.yieldAllPermutations(minLength, maxLength, this@allPermutations, mutableListOf<T>())
}

private suspend fun <T> SequenceScope<List<T>>.yieldAllPermutations(
    minLength: Int, maxLength: Int,
    remainder: List<T>, currentPermutation: MutableList<T>
) {
    if (maxLength >= 0 && minLength <= remainder.size)
        if (remainder.isEmpty() || maxLength == 0)
            yield(currentPermutation.toList())
        else {
            val head = remainder[0]
            val tail = remainder.subList(1, remainder.size)
            currentPermutation += head
            yieldAllPermutations(minLength - 1, maxLength - 1, tail, currentPermutation)
            currentPermutation.removeLast()
            yieldAllPermutations(minLength, maxLength, tail, currentPermutation)
        }
}

enum class AnsiColor(private val fg: Int, private val bg: Int) {
    BLACK(30, 40), RED(31, 41), GREEN(32, 42), YELLOW(33, 43), BLUE(34, 44),
    MAGENTA(35, 45), CYAN(36, 46), WHITE(37, 47), DEFAULT(39, 49), RESET(0, 0);

    fun fgCode(): String = "\u001b[${fg}m"
    fun bgCode(): String = "\u001b[${bg}m"
}
