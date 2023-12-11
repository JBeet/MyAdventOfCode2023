package aoc10

import utils.*

sealed interface MetalCell : GridCell {
    data class MPipe(override val connections: Set<Direction>) : MetalCell
    data object MEmpty : MetalCell {
        override fun toString(): String = "."
    }

    data object MAnimal : MetalCell {
        override fun toString(): String = "╳"
    }
}

fun main() {
    class MetalGrid(lines: List<List<MetalCell>>) : FilledGrid<MetalCell>(lines, MetalCell.MEmpty) {
        private val distances: Map<Position, Int> by lazy {
            val target = mutableMapOf<Position, Int>()
            val startPos = findAll(MetalCell.MAnimal).single()
            foldConnected(startPos, 0) { position, distance ->
                val oldValue = target[position]
                if (oldValue != null && oldValue < distance) return@foldConnected null
                target[position] = distance
                return@foldConnected distance + 1
            }
            target
        }

        override fun connections(pos: Position, cell: MetalCell) = if (cell == MetalCell.MAnimal)
            Direction.entries.filter { it.inverse in connections(pos + it.delta) }
        else
            super.connections(pos, cell)

        fun findMaxDistance(): Int = distances.values.max()
        fun countInside(): Int = countWithEmpty { pos -> isInsideLoop(pos) }
        private fun isInsideLoop(pos: Position) = (!isPartOfLoop(pos)) && (countCrossings(pos) % 2 == 1)
        private fun isPartOfLoop(pos: Position) = pos in distances.keys
        private fun countCrossings(pos: Position) =
            pos.rowBefore.count { isPartOfLoop(it) && Direction.S in connections(it) }

        override fun cellAsString(pos: Position, cell: MetalCell) =
            if (cell == MetalCell.MAnimal) cell.toString() else super.cellAsString(pos, cell)

        override fun fgColor(pos: Position) = when {
            cell(pos) == MetalCell.MAnimal -> AnsiColor.RED
            isPartOfLoop(pos) -> AnsiColor.BLUE
            isInsideLoop(pos) -> AnsiColor.MAGENTA
            else -> AnsiColor.DEFAULT
        }
    }

    fun parseChar(ch: Char): MetalCell = when (ch) {
        'F' -> MetalCell.MPipe(setOf(Direction.E, Direction.S))
        '7' -> MetalCell.MPipe(setOf(Direction.W, Direction.S))
        'L' -> MetalCell.MPipe(setOf(Direction.E, Direction.N))
        'J' -> MetalCell.MPipe(setOf(Direction.W, Direction.N))
        '|' -> MetalCell.MPipe(setOf(Direction.N, Direction.S))
        '-' -> MetalCell.MPipe(setOf(Direction.E, Direction.W))
        '.' -> MetalCell.MEmpty
        'S' -> MetalCell.MAnimal
        else -> error("Unknown character: $ch")
    }

    fun parseLine(s: String): List<MetalCell> = s.map { parseChar(it) }
    fun parse(input: List<String>): MetalGrid = MetalGrid(input.filter { it.isNotEmpty() }.map { parseLine(it) })

    fun part1(input: List<String>): Int = parse(input).findMaxDistance()
    fun part2(input: List<String>): Int = parse(input).countInside()

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("aoc10/Day10_test")
    check(part1(testInput) == 8)
    val input = readInput("aoc10/Day10")
    println(parse(input))
    part1(input).println()

    val testInput2 = readInput("aoc10/Day10_test2")
    check(part2(testInput2) == 10) { "Expected 10 but found " + part2(testInput2) }
    val testInput3 = readInput("aoc10/Day10_test3")
    check(part2(testInput3) == 8) { "Expected 8 but found " + part2(testInput3) }
    part2(input).println()
}
