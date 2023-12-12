package aoc12

import utils.println
import utils.readInput

private const val SPR_UNKNOWN = '?'
private const val SPR_BROKEN = '#'
private const val SPR_WORKING = '.'

data class SpringConfig(val s: String, val groups: List<Int>) {
    private val knownValues = mutableMapOf<Triple<Int, Int, Int>, Long>()
    private fun possibilities(idxS: Int, idxL: Int, groupRemainder: Int): Long =
        knownValues[Triple(idxS, idxL, groupRemainder)] ?: calculatePossibilities(idxS, idxL, groupRemainder).also {
            knownValues[Triple(idxS, idxL, groupRemainder)] = it
        }

    private fun calculatePossibilities(idxS: Int, idxGroup: Int, groupRemainder: Int): Long {
        if (idxS >= s.length) return if (groupRemainder <= 0 && idxGroup == groups.size) 1 else 0
        return when (val next = s[idxS]) {
            SPR_WORKING -> if (groupRemainder <= 0) noActiveGroup(idxS, idxGroup) else 0
            SPR_BROKEN -> when {
                groupRemainder < 0 -> startNewGroup(idxS, idxGroup)
                groupRemainder == 0 -> 0
                else -> continueCurrentGroup(idxS, idxGroup, groupRemainder)
            }

            SPR_UNKNOWN -> when {
                groupRemainder < 0 -> startNewGroup(idxS, idxGroup) + noActiveGroup(idxS, idxGroup)
                groupRemainder == 0 -> noActiveGroup(idxS, idxGroup)
                else -> continueCurrentGroup(idxS, idxGroup, groupRemainder)
            }

            else -> {
                error("Unexpected character: $next")
            }
        }
    }

    private fun noActiveGroup(idxS: Int, idxGroup: Int) =
        possibilities(idxS + 1, idxGroup, -1)

    private fun startNewGroup(idxS: Int, idxGroup: Int) =
        if (idxGroup >= groups.size) 0 else possibilities(idxS + 1, idxGroup + 1, groups[idxGroup] - 1)

    private fun continueCurrentGroup(idxS: Int, idxGroup: Int, groupRemainder: Int) =
        possibilities(idxS + 1, idxGroup, groupRemainder - 1)

    fun part1() = possibilities(0, 0, -1)
    fun unfold() = SpringConfig("$s?".repeat(5).dropLast(1), List(5) { groups }.flatten())
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

    fun part2(input: List<String>): Long =
        input.map { s -> SpringConfig(s) }.map { it.unfold() }.sumOf { it.part1() }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("aoc12/Day12_test")
    check(part1(testInput) == 21L)

    val input = readInput("aoc12/Day12")
    part1(input).println()

    check(part2(testInput) == 525152L)
    part2(input).println()
}
