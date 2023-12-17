package aoc18

import utils.assertEquals
import utils.println
import utils.readInput

fun main() {
    fun part1(input: List<String>) = input.size

    fun part2(input: List<String>) = input.size

    val testInput = readInput("aoc18/Day18_test")
    assertEquals(1, part1(testInput))
    val input = readInput("aoc18/Day18")
    part1(input).println()
    assertEquals(1, part2(testInput))
    part2(input).println()
}
