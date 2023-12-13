package aoc13

import utils.chunkedBy
import utils.println
import utils.readInput
import utils.transpose

class MirrorPuzzle(private val parts: List<String>) {
    fun part1(): Int = findReflections().single()

    private fun findReflections(): List<Int> = findByRow().map { rowPoints(it) } + findByColumn()
    private fun rowPoints(it: Int) = it * 100

    fun part2() = parts.indices.flatMapTo(mutableSetOf()) { rowIdx ->
        parts[0].indices.flatMap { colIdx -> MirrorPuzzle(swap(rowIdx, colIdx)).findReflections() }
    }.let { it - part1() }.single()

    private fun swap(rowIdx: Int, colIdx: Int) = parts.toMutableList().apply {
        this[rowIdx] = this[rowIdx].swap(colIdx)
    }

    private fun String.swap(pos: Int) = take(pos) + swap(this[pos]) + drop(pos + 1)
    private fun swap(c: Char) = if (c == '.') '#' else '.'

    private fun findByRow(): List<Int> = (1..<parts.size).filter { reflectsByRow(it) }
    private fun reflectsByRow(row: Int): Boolean =
        if (row + row < parts.size)
            parts.take(row) == parts.drop(row).take(row).reversed()
        else
            parts.take(row).drop(row + row - parts.size) == parts.drop(row).reversed()

    private fun findByColumn(): List<Int> = MirrorPuzzle(parts.transpose()).findByRow()
}

fun main() {
    fun parse(input: List<String>) =
        input.chunkedBy { it.isEmpty() }.filter { it != listOf("") }.map { MirrorPuzzle(it) }

    fun part1(input: List<String>): Int = parse(input).sumOf { it.part1() }
    fun part2(input: List<String>): Int = parse(input).sumOf { it.part2() }


    val testInput = readInput("aoc13/Day13_test")
    check(part1(testInput) == 405)
    val input = readInput("aoc13/Day13")
    part1(input).println()
    check(part2(testInput) == 400)
    part2(input).println()
}
