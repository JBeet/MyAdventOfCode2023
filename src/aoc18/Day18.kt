package aoc18

import utils.assertEquals
import utils.grid.Direction
import utils.grid.Position
import utils.println
import utils.readInput
import utils.splitOnSpaces
import kotlin.math.absoluteValue

private class Lagoon {
    val nodes = mutableListOf(Position(0, 0))
    fun process1(cmd: String) {
        cmd.splitOnSpaces().let { (dir, len, color) ->
            process(toDirection(dir), len.toInt())
        }
    }

    fun process2(cmd: String) {
        cmd.splitOnSpaces().let { (dir, len, color) ->
            process(toDirection(color.takeLast(2).take(1)), color.take(7).drop(2).toInt(16))
        }
    }

    private fun process(dir: Direction, len: Int) {
        nodes.add(nodes.last() + (dir.delta * len))
    }

    private fun toDirection(dir: String): Direction = when (dir) {
        "R" -> Direction.E
        "D" -> Direction.S
        "L" -> Direction.W
        "U" -> Direction.N
        "0" -> Direction.E
        "1" -> Direction.S
        "2" -> Direction.W
        "3" -> Direction.N
        else -> error("Unknown direction $dir")
    }

    fun size(): Long {
        check(nodes.last() == nodes.first())
        return area() + (length() / 2) + 1
    }

    private fun length() = nodes.zipWithNext { a, b ->
        a.manhattanDistanceTo(b)
    }.sum()

    private fun area() = nodes.zipWithNext { a, b ->
        a.row * b.column - b.row * a.column
    }.sum().absoluteValue / 2
}

fun main() {
    fun part1(input: List<String>) = Lagoon().apply { input.forEach { process1(it) } }.size()
    fun part2(input: List<String>) = Lagoon().apply { input.forEach { process2(it) } }.size()
    val testInput = readInput("aoc18/Day18_test")
    assertEquals(4, part1(listOf("R 1 x", "D 1 x", "L 1 x", "U 1 x")))
    assertEquals(62, part1(testInput))
    val input = readInput("aoc18/Day18")
    part1(input).println()
    assertEquals(952408144115, part2(testInput))
    part2(input).println()
}
