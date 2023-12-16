package aoc16

import utils.assertEquals
import utils.grid.Direction
import utils.grid.FilledCharGrid
import utils.grid.Position
import utils.println
import utils.readInput

const val EMPTY = '.'
const val MIRROR_RU = '/'
const val MIRROR_RD = '\\'
const val SPLIT_UD = '|'
const val SPLIT_LR = '-'

typealias Directions = Set<Direction>

class LavaCave(input: List<String>) : FilledCharGrid(input) {
    private fun calculateIncomingBeans(
        initialPosition: Position,
        initialDirection: Direction
    ): Map<Position, Directions> {
        val result = mutableMapOf<Position, Directions>()
        val traverse = DeepRecursiveFunction<Pair<Position, Direction>, Unit> { (pos, curDir) ->
            if (pos !in this@LavaCave) return@DeepRecursiveFunction
            val known = result[pos] ?: emptySet()
            if (curDir in known) return@DeepRecursiveFunction
            result[pos] = (result[pos] ?: emptySet()) + curDir
            val cell = cell(pos)
            val outgoing = outgoing(cell, curDir)
            outgoing.forEach { newDir ->
                callRecursive((pos + newDir) to newDir)
            }
        }
        traverse(initialPosition to initialDirection)
        return result
    }

    private fun outgoing(cell: Char, incoming: Direction): Set<Direction> = when (cell) {
        empty -> setOf(incoming)
        MIRROR_RU -> setOf(
            when (incoming) {
                Direction.N -> Direction.E
                Direction.E -> Direction.N
                Direction.S -> Direction.W
                Direction.W -> Direction.S
                else -> error("unknown direction: $incoming")
            }
        )

        MIRROR_RD -> setOf(
            when (incoming) {
                Direction.N -> Direction.W
                Direction.E -> Direction.S
                Direction.S -> Direction.E
                Direction.W -> Direction.N
                else -> error("unknown direction: $incoming")
            }
        )

        SPLIT_UD -> when (incoming) {
            Direction.N -> setOf(Direction.N)
            Direction.E -> setOf(Direction.N, Direction.S)
            Direction.S -> setOf(Direction.S)
            Direction.W -> setOf(Direction.N, Direction.S)
            else -> error("unknown direction: $incoming")
        }

        SPLIT_LR -> when (incoming) {
            Direction.N -> setOf(Direction.E, Direction.W)
            Direction.E -> setOf(Direction.E)
            Direction.S -> setOf(Direction.E, Direction.W)
            Direction.W -> setOf(Direction.W)
            else -> error("unknown direction: $incoming")
        }

        else -> error("unknown: $cell")
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
