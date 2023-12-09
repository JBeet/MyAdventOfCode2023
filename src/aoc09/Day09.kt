package aoc09

import utils.println
import utils.readInput
import utils.splitToInts


fun main() {
    fun calculateNext(list: List<Int>): Int = if (list.all { it == 0 }) 0 else
        list.last() + calculateNext(list.zipWithNext { a, b -> b - a })

    fun part1(input: List<String>): Int = input.map { it.splitToInts() }.sumOf { calculateNext(it) }

    fun part2(input: List<String>): Int =
        input.map { it.splitToInts() }.map { it.reversed() }.sumOf { calculateNext(it) }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("aoc09/Day09_test")
    check(part1(testInput) == 114)

    val input = readInput("aoc09/Day09")
    part1(input).println()
    check(part2(testInput) == 2)
    part2(input).println()
}
