package aoc12

import utils.println
import utils.readInput

private const val SPR_UNKNOWN = '?'
private const val SPR_BROKEN = '#'
private const val SPR_WORKING = '.'

data class SpringConfig(val s: String, val l: List<Int>) {
    private val knownValues = mutableMapOf<Pair<Int, Int>, Long>()
    private fun possibilities(idxS: Int, idxL: Int): Long =
        knownValues[idxS to idxL] ?: calculatePossibilities(idxS, idxL).also {
            knownValues[idxS to idxL] = it
        }

    private fun calculatePossibilities(idxS: Int, idxL: Int): Long {
        if (idxS >= s.length) return if (idxL == l.size) 1 else 0
        if (idxL == l.size) return if (s.lastIndexOf(SPR_BROKEN) < idxS) 1 else 0
        return when (val next = s[idxS]) {
            SPR_WORKING -> possibilities(idxS + 1, idxL)
            SPR_BROKEN -> calculateWithGroup(idxL, idxS)
            SPR_UNKNOWN -> calculateWithGroup(idxL, idxS) + possibilities(idxS + 1, idxL)
            else -> error("Unexpected character: $next")
        }
    }

    private fun calculateWithGroup(idxL: Int, idxS: Int): Long {
        val groupSize = l[idxL]
        val groupOption = s.drop(idxS).take(groupSize)
        if (groupOption.length < groupSize || groupOption.any { it == SPR_WORKING }) return 0
        val nextIdxS = idxS + groupSize
        return when {
            nextIdxS == s.length -> if (idxL + 1 == l.size) 1 else 0
            s[nextIdxS] == SPR_BROKEN -> 0
            else -> possibilities(nextIdxS + 1, idxL + 1)
        }
    }

    fun part1() = possibilities(0, 0)
    fun unfold() = SpringConfig("$s?".repeat(5).dropLast(1), List(5) { l }.flatten())
}

fun main() {
    fun SpringConfig(s: String) = s.split(' ').let { (base, groups) ->
        SpringConfig(base, groups.split(',').map { nr -> nr.toInt() })
    }

    check(SpringConfig(".?", listOf(1)).part1() == 1L)
    check(SpringConfig(".", listOf()).part1() == 1L)
    check(SpringConfig("#", listOf()).part1() == 0L)
    check(SpringConfig("#", listOf(1)).part1() == 1L)
    check(SpringConfig("?", listOf(1)).part1() == 1L)
    check(SpringConfig("?.", listOf(1)).part1() == 1L)
    check(SpringConfig("#?", listOf(1)).part1() == 1L)
    check(SpringConfig("?#", listOf(1)).part1() == 1L)
    check(SpringConfig("??", listOf(1)).part1() == 2L)
    check(SpringConfig("??", listOf(1, 1)).part1() == 0L)
    check(SpringConfig("???", listOf(1, 1)).part1() == 1L)
    check(SpringConfig(".??.??.", listOf(1, 1)).part1() == 4L)
    check(SpringConfig(".??.", listOf(1)).part1() == 2L)
    check(SpringConfig(".?##.", listOf(3)).part1() == 1L)

    fun part1(input: List<String>): Long =
        input.map { s -> SpringConfig(s) }.sumOf { it.part1() }

    fun part2(input: List<String>): Long = input.map { s -> SpringConfig(s) }.map { it.unfold() }.sumOf {
        it.part1()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("aoc12/Day12_test")
    check(part1(testInput) == 21L)


    val input = readInput("aoc12/Day12")
    part1(input).println()

    check(part2(testInput) == 525152L)
    part2(input).println()
}
