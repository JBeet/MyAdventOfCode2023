package aoc11

import utils.*

fun main() {
    data class GxCell(val ch: Char) : GridCell {
        override fun toString(): String = ch.toString()
    }

    class GxGrid(input: List<String>) : FilledGrid<GxCell>(input.map { r -> r.map { GxCell(it) } }, GxCell('.')) {
        private val emptyRows = rows.filter { it.isEmpty }.map { it.index }.toSortedSet()
        private val emptyColumns = columns.filter { it.isEmpty }.map { it.index }.toSortedSet()
        private fun galaxies() = findAll(GxCell('#'))
        fun expanded(factor: Long = 1L) = galaxies().map { expand(it, factor) }

        private fun expand(position: Position, factor: Long) =
            Position(position.row + emptyRows.count { it < position.row } * factor,
                position.column + emptyColumns.count { it < position.column } * factor)

        override fun fgColor(pos: Position): AnsiColor = when {
            pos.column in emptyColumns || pos.row in emptyRows -> AnsiColor.WHITE
            else -> AnsiColor.DEFAULT
        }

        override fun bgColor(pos: Position): AnsiColor = when {
            pos.column in emptyColumns && pos.row in emptyRows -> AnsiColor.MAGENTA
            pos.column in emptyColumns -> AnsiColor.RED
            pos.row in emptyRows -> AnsiColor.BLUE
            else -> AnsiColor.DEFAULT
        }
    }

    fun List<Position>.sumOfShortestPathLengths() =
        allPairs().sumOf { (a, b) -> a.manhattanDistanceTo(b) }

    fun parse(input: List<String>) = GxGrid(input)

    fun part1(input: List<String>): Long = parse(input).expanded().sumOfShortestPathLengths()
    fun part2(input: List<String>, factor: Long): Long = parse(input).expanded(factor).sumOfShortestPathLengths()

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("aoc11/Day11_test")
    check(part1(testInput) == 374L)

    val input = readInput("aoc11/Day11")
    println(parse(input))
    part1(input).println()
    check(part2(testInput, 9) == 1030L)
    check(part2(testInput, 99) == 8410L)

    part2(input, 999_999).println()
}
