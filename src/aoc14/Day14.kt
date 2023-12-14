package aoc14

import utils.*
import utils.grid.*

private const val CUBE = '#'
private const val ROUND = 'O'

class Rocks(rocks: Map<Position, Char>, bounds: ZeroBasedBounds) : OpenCharGrid(rocks, bounds) {
    private val height = bounds.height

    fun part1() = moveAll(Direction.N).load()

    private fun moveAll(direction: Direction): Rocks {
        val cubes = findAll(CUBE)
        val fixedRocks = cubes.toMutableSet()
        var flexRocks = findAll(ROUND).toMutableSet()
        while (flexRocks.isNotEmpty()) {
            do {
                val moreFixed = flexRocks.filterTo(mutableSetOf()) { pos ->
                    (pos + direction).let { it in fixedRocks || it !in this }
                }
                flexRocks -= moreFixed
                fixedRocks += moreFixed
            } while (moreFixed.isNotEmpty())
            flexRocks = flexRocks.mapTo(mutableSetOf()) { it + direction }
        }
        return Rocks(
            rocks = cubes.associateWith { CUBE } + (fixedRocks - cubes).associateWith { ROUND },
            zeroBasedBounds
        )
    }

    private fun load(): Long = findAll(ROUND).sumOf { height - it.row }

    fun part2(): Long = repeatCycle(1_000_000_000, mutableMapOf())

    private fun repeatCycle(repeat: Int, cache: MutableMap<Any, Pair<Int, Rocks>>): Long {
        if (repeat == 0) return load()
        val (prevRep, next) = cache.getOrPut(cells) { repeat to cycle() }
        val nextRepeat = if (prevRep == repeat) repeat - 1 else (repeat - 1) % (prevRep - repeat)
        return next.repeatCycle(nextRepeat, cache)
    }

    private fun cycle(): Rocks = moveAll(Direction.N).moveAll(Direction.W).moveAll(Direction.S).moveAll(Direction.E)
}

fun main() {
    fun parse(input: List<String>) = Rocks(openCharCells(input), boundsFrom(input))

    fun part1(input: List<String>): Long = parse(input).part1()
    fun part2(input: List<String>): Long = parse(input).part2()

    val testInput = readInput("aoc14/Day14_test")
    check(part1(testInput) == 136L)
    val input = readInput("aoc14/Day14")
    part1(input).println()
    check(part2(testInput) == 64L)
    part2(input).println()
}
