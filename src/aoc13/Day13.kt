package aoc13

import utils.println
import utils.readInput
import utils.split
import utils.transpose
import kotlin.math.min

class MirrorPuzzle(private val parts: List<String>, private val smudgeCount: Int) {
    fun findReflection(): Int = findRow() * 100 + findColumn()

    private fun findRow(): Int =
        (1..<parts.size).singleOrNull { reflectsAtRow(it, min(it, parts.size - it)) } ?: 0

    private fun reflectsAtRow(row: Int, size: Int): Boolean =
        (0..<size).sumOf { diffCount(parts[row - 1 - it], parts[row + it]) } == smudgeCount

    private fun diffCount(a: String, b: String) = a.indices.count { a[it] != b[it] }

    private fun findColumn() = MirrorPuzzle(parts.transpose(), smudgeCount).findRow()
}

fun main() {
    fun split(input: List<String>) = input.split { it.isEmpty() }
    fun part1(input: List<String>): Int = split(input).map { MirrorPuzzle(it, 0) }.sumOf { it.findReflection() }
    fun part2(input: List<String>): Int = split(input).map { MirrorPuzzle(it, 1) }.sumOf { it.findReflection() }

    val testInput = readInput("aoc13/Day13_test")
    check(part1(testInput) == 405)
    val input = readInput("aoc13/Day13")
    part1(input).println()
    check(part2(testInput) == 400)
    part2(input).println()
}
