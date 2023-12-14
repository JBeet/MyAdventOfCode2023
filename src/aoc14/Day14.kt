package aoc14

import utils.Direction
import utils.Position
import utils.println
import utils.readInput

data class Rock(val position: Position, val isRound: Boolean)
data class Rocks(
    private val rocks: List<Rock>,
    private val height: Int,
    private val width: Int
) {
    private val rowRange = 0..<height
    private val colRange = 0..<width
    fun part1(): Long = moveAll(Direction.N).load()

    private fun moveAll(direction: Direction): Rocks {
        val fixedRocks = rocks.filter { !it.isRound }.associateByTo(mutableMapOf()) { it.position }
        var flexRocks = rocks.filterTo(mutableSetOf()) { it.isRound }
        do {
            do {
                val moreFixed = flexRocks.filterTo(mutableSetOf()) { rock ->
                    rock.move(direction).let { pos -> pos in fixedRocks.keys || pos !in this }
                }
                flexRocks.removeAll(moreFixed)
                moreFixed.forEach { fixedRocks[it.position] = it }
            } while (moreFixed.isNotEmpty())
            flexRocks = flexRocks.mapTo(mutableSetOf()) { it.copy(position = it.move(direction)) }
        } while (flexRocks.isNotEmpty())
        return Rocks(fixedRocks.values.toList(), height, width)
    }

    operator fun contains(p: Position) = p.row in (rowRange) && p.column in (colRange)

    fun load(): Long = rocks.sumOf { if (it.isRound) height - it.position.row else 0 }

    fun Rock.move(direction: Direction): Position = position + direction.delta
    fun part2(): Long = repeatCycle(this, 1, 1_000_000_000, mutableMapOf())

    fun cycle(): Rocks = moveAll(Direction.N).moveAll(Direction.W).moveAll(Direction.S).moveAll(Direction.E)
}

private tailrec fun repeatCycle(
    target: Rocks,
    cur: Int,
    max: Int,
    knownItems: MutableMap<Rocks, Pair<Int, Rocks>>
): Long {
    val (prevIdx, next) = knownItems.getOrPut(target) { cur to target.cycle() }
    val cycleLength = (cur - prevIdx)
    if (cycleLength != 0) {
        val fullCycles = ((max - cur) / cycleLength - 1)
        if (fullCycles > 0)
            return repeatCycle(next, cur + fullCycles * cycleLength + 1, max, knownItems)
    }
    if (cur >= max) return next.load()
    return repeatCycle(next, cur + 1, max, knownItems)
}

fun main() {
    fun parse(input: List<String>) = Rocks(input.flatMapIndexed { rowIndex, rowData ->
        rowData.indices.mapNotNull { colIndex ->
            when (rowData[colIndex]) {
                'O' -> Rock(Position(rowIndex, colIndex), true)
                '#' -> Rock(Position(rowIndex, colIndex), false)
                else -> null
            }
        }
    }.sortedWith(compareBy({ it.position.row }, { it.position.column })), input.size, input[0].length)

    fun part1(input: List<String>): Long = parse(input).part1()
    fun part2(input: List<String>): Long = parse(input).part2()

    val testInput = readInput("aoc14/Day14_test")
    check(part1(testInput) == 136L)
    val input = readInput("aoc14/Day14")
    part1(input).println()
    check(part2(testInput) == 64L)
    part2(input).println()
}
