package aoc14

import utils.Direction
import utils.Position
import utils.println
import utils.readInput

data class Rock(val position: Position, val isRound: Boolean) : Comparable<Rock> {
    override fun compareTo(other: Rock): Int = compareValuesBy(this, other, { it.position.row }, { it.position.column })
}

data class Rocks(private val rocks: List<Rock>, private val height: Int, private val width: Int) {
    private val rowRange = 0..<height
    private val colRange = 0..<width
    fun part1(): Long = moveAll(Direction.N).load()

    private fun moveAll(direction: Direction): Rocks {
        val fixedRocks = rocks.filter { !it.isRound }.associateByTo(mutableMapOf()) { it.position }
        var flexRocks = rocks.filterTo(mutableSetOf()) { it.isRound }
        while (flexRocks.isNotEmpty()) {
            do {
                val moreFixed = flexRocks.filterTo(mutableSetOf()) { rock ->
                    rock.move(direction).let { pos -> pos in fixedRocks.keys || pos !in this }
                }
                flexRocks.removeAll(moreFixed)
                moreFixed.forEach { fixedRocks[it.position] = it }
            } while (moreFixed.isNotEmpty())
            flexRocks = flexRocks.mapTo(mutableSetOf()) { it.copy(position = it.move(direction)) }
        }
        return copy(rocks = fixedRocks.values.sorted())
    }

    operator fun contains(p: Position) = p.row in (rowRange) && p.column in (colRange)

    fun load(): Long = rocks.sumOf { if (it.isRound) height - it.position.row else 0 }

    private fun Rock.move(direction: Direction): Position = position + direction.delta
    fun part2(): Long = repeatCycle(1_000_000_000, mutableMapOf())

    fun cycle(): Rocks = moveAll(Direction.N).moveAll(Direction.W).moveAll(Direction.S).moveAll(Direction.E)
}

private tailrec fun Rocks.repeatCycle(repeat: Int, cache: MutableMap<Rocks, Pair<Int, Rocks>>): Long {
    if (repeat == 0) return load()
    val (prevRep, next) = cache.getOrPut(this) { repeat to cycle() }
    val nextRepeat = if (prevRep == repeat) repeat - 1 else (repeat - 1) % (prevRep - repeat)
    return next.repeatCycle(nextRepeat, cache)
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
    }.sorted(), input.size, input[0].length)

    fun part1(input: List<String>): Long = parse(input).part1()
    fun part2(input: List<String>): Long = parse(input).part2()

    val testInput = readInput("aoc14/Day14_test")
    check(part1(testInput) == 136L)
    val input = readInput("aoc14/Day14")
    part1(input).println()
    check(part2(testInput) == 64L)
    part2(input).println()
}
