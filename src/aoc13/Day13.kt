package aoc13

import utils.chunkedBy
import utils.println
import utils.readInput

class MirrorPuzzle(private val parts: List<String>, private val rejected: Int = -1) {
    fun part1(): Int = checkNotNull(findReflection()) { "No reflection found in $parts" }

    private fun findReflection(): Int? = findByColumn() ?: findByRow()?.let { rowPoints(it) }
    private fun rowPoints(it: Int) = it * 100

    fun part2(): Int {
        val oldSolution = part1()
        return parts.mapIndexedNotNull { rowIdx, part ->
            part.indices.firstNotNullOfOrNull { colIdx ->
                val newParts = parts.toMutableList()
                newParts[rowIdx] = part.swap(colIdx)
                MirrorPuzzle(newParts, oldSolution).findReflection()
            }
        }.first()
    }

    private fun String.swap(pos: Int) = take(pos) + swap(this[pos]) + drop(pos + 1)
    private fun swap(c: Char) = if (c == '.') '#' else '.'

    private fun findByColumn(): Int? =
        (1..<parts[0].length).filter { it != rejected }.firstOrNull { reflectsByColumn(it) }

    private fun reflectsByColumn(col: Int): Boolean =
        parts.all { lineReflectsByColumn(it, col) }

    private fun lineReflectsByColumn(s: String, col: Int): Boolean =
        if (col + col < s.length)
            s.take(col) == s.drop(col).take(col).reversed()
        else
            s.take(col).drop(col + col - s.length) == s.drop(col).reversed()

    private fun findByRow(): Int? =
        (1..<parts.size).filter { rowPoints(it) != rejected }.firstOrNull { reflectsByRow(it) }

    private fun reflectsByRow(row: Int): Boolean =
        if (row + row < parts.size)
            parts.take(row) == parts.drop(row).take(row).reversed()
        else
            parts.take(row).drop(row + row - parts.size) == parts.drop(row).reversed()
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
