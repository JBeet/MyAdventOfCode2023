fun main() {
    fun part1(input: List<String>): Int {
        return input.sumOf { s ->
            val firstDigit = s.first { it.isDigit() }
            val lastDigit = s.last { it.isDigit() }
            "$firstDigit$lastDigit".toInt()
        }
    }

    fun part2(input: List<String>): Int {
        val possibleDigits = mapOf(
            "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4,
            "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9
        ) + (0..9).associateBy { it.toString() }
        return input.sumOf { s ->
            val digits = s.indices.mapNotNull { idx ->
                possibleDigits.firstNotNullOfOrNull { (txt, nr) ->
                    if (s.substring(idx).take(txt.length) == txt) nr else null
                }
            }
            digits.first() * 10 + digits.last()
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 142)

    val testInput2 = readInput("Day01_test2")
    check(part2(testInput2) == 281) { "Expected 281 but found ${part2(testInput2)}" }

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}
