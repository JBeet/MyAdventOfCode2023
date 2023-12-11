package aoc01

import utils.println
import utils.readInput
import kotlin.math.abs

fun main() {
    class BaseGalaxies(private val input: List<String>) {
        private val colCount: Int = input[0].length.also { cc ->
            check(input.all { it.length == cc }) { "expected length $cc" }
        }
        private val rowsWithoutGalaxy =
            input.mapIndexedNotNullTo(mutableSetOf()) { i, s -> if (s.indexOf('#') >= 0) null else i }
        private val columnsWithoutGalaxy = (0..<colCount).filterTo(mutableSetOf()) { c ->
            input.none { it[c] == '#' }
        }

        fun expanded(factor: Long = 1L): List<Pair<Long, Long>> = buildList {
            var rowShift = 0
            for (orgRowIndex in input.indices)
                if (orgRowIndex in rowsWithoutGalaxy)
                    rowShift++
                else {
                    val row = input[orgRowIndex]
                    var colShift = 0
                    for (orgColIndex in row.indices) {
                        if (orgColIndex in columnsWithoutGalaxy)
                            colShift++
                        else if (row[orgColIndex] == '#')
                            add((orgRowIndex + rowShift * factor) to (orgColIndex + colShift * factor))
                    }
                }
        }
    }

    fun manhattan(a: Pair<Long, Long>, b: Pair<Long, Long>): Long = abs(a.first - b.first) + abs(a.second - b.second)

    fun List<Pair<Long, Long>>.sumOfShortestPathLengths() = foldIndexed(0L) { idx, sum, cur ->
        sum + subList(0, idx).sumOf { manhattan(it, cur) }
    }

    fun part1(input: List<String>): Long = BaseGalaxies(input).expanded().sumOfShortestPathLengths()
    fun part2(input: List<String>, factor: Long): Long = BaseGalaxies(input).expanded(factor).sumOfShortestPathLengths()

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("aoc11/Day11_test")
    check(part1(testInput) == 374L)

    val input = readInput("aoc11/Day11")
    part1(input).println()
    check(part2(testInput, 9) == 1030L)
    check(part2(testInput, 99) == 8410L)

    part2(input, 999_999).println()
}
