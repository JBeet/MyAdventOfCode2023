package aoc16

import utils.assertEquals
import utils.grid.*
import utils.println
import utils.readInput

enum class LavaCell(val ch: Char) {
    EMPTY('.') {
        override fun outgoing(incoming: Direction): Directions = setOf(incoming)
    },
    MIRROR_RU('/') {
        override fun outgoing(incoming: Direction): Directions = when (incoming) {
            Direction.N -> directions("E")
            Direction.E -> directions("N")
            Direction.S -> directions("W")
            Direction.W -> directions("S")
        }
    },
    MIRROR_RD('\\') {
        override fun outgoing(incoming: Direction): Directions = when (incoming) {
            Direction.N -> directions("W")
            Direction.E -> directions("S")
            Direction.S -> directions("E")
            Direction.W -> directions("N")
        }
    },
    SPLIT_UD('|') {
        override fun outgoing(incoming: Direction): Directions = when (incoming) {
            Direction.N -> directions("N")
            Direction.E -> directions("NS")
            Direction.S -> directions("S")
            Direction.W -> directions("NS")
        }
    },
    SPLIT_LR('-') {
        override fun outgoing(incoming: Direction): Directions = when (incoming) {
            Direction.N -> directions("EW")
            Direction.E -> directions("E")
            Direction.S -> directions("EW")
            Direction.W -> directions("W")
        }
    };

    abstract fun outgoing(incoming: Direction): Directions
}

fun parse(ch: Char) = LavaCell.entries.first { it.ch == ch }

class LavaCave(input: List<String>) : FilledGrid<LavaCell>(input.map { it.map { parse(it) } }, LavaCell.EMPTY) {
    private fun calculateIncomingBeans(
        initialPosition: Position,
        initialDirection: Direction
    ): Map<Position, Directions> {
        val result = mutableMapOf<Position, Directions>()
        val traverse = DeepRecursiveFunction<Pair<Position, Direction>, Unit> { (pos, curDir) ->
            if (pos !in this@LavaCave) return@DeepRecursiveFunction
            val known = result[pos] ?: emptySet()
            if (curDir in known) return@DeepRecursiveFunction
            result[pos] = known + curDir
            cell(pos).outgoing(curDir).forEach { newDir ->
                callRecursive((pos + newDir) to newDir)
            }
        }
        traverse(initialPosition to initialDirection)
        return result
    }

    fun part1(): Int = calculateEnergized(Position(0, 0), Direction.E)
    fun part2(): Int = listOf(
        bounds.columnRange.maxOf { calculateEnergized(Position(0, it), Direction.S) },
        bounds.rowRange.maxOf { calculateEnergized(Position(it, 0), Direction.E) },
        bounds.columnRange.maxOf { calculateEnergized(Position(height - 1, it), Direction.N) },
        bounds.rowRange.maxOf { calculateEnergized(Position(it, width - 1), Direction.W) },
    ).max()

    private fun calculateEnergized(position: Position, direction: Direction) =
        calculateIncomingBeans(position, direction).values.count { it.isNotEmpty() }
}

fun main() {
    fun part1(input: List<String>) = LavaCave(input).part1()

    fun part2(input: List<String>) = LavaCave(input).part2()

    val testInput = readInput("aoc16/Day16_test")
    assertEquals(46, part1(testInput))
    val input = readInput("aoc16/Day16")
    part1(input).println()
    assertEquals(51, part2(testInput))
    part2(input).println()
}
