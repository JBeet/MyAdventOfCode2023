package aoc13

import utils.println
import utils.readInput
import utils.split
import utils.transpose
import kotlin.math.min

class MirrorPuzzle(private val parts: List<String>, private val smudgeCount: Int = 0) {
    fun part1(): Int = allReflections().single()
    fun part2() = MirrorPuzzle(parts, 1).allReflections().let { it - part1() }.single()

    private fun allReflections(): List<Int> = findByRow().map { it * 100 } + findByColumn()

    private fun findByRow(): List<Int> =
        (1..<parts.size).filter { reflectsByRow(it, min(it, parts.size - it)) }

    private fun reflectsByRow(row: Int, size: Int): Boolean =
        (0..<size).sumOf { diffCount(parts[row - 1 - it], parts[row + it]) } <= smudgeCount

    private fun diffCount(a: String, b: String) = a.indices.count { a[it] != b[it] }

    private fun findByColumn() = MirrorPuzzle(parts.transpose(), smudgeCount).findByRow()
}

fun main() {
    fun parse(input: List<String>) = input.split { it.isEmpty() }.map { MirrorPuzzle(it) }
    fun part1(input: List<String>): Int = parse(input).sumOf { it.part1() }
    fun part2(input: List<String>): Int = parse(input).sumOf { it.part2() }


    val testInput = readInput("aoc13/Day13_test")
    check(part1(testInput) == 405)
    val input = readInput("aoc13/Day13")
    part1(input).println()
    check(part2(testInput) == 400)
    part2(input).println()
}
