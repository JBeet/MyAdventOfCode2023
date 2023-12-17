package aoc17

import utils.assertEquals
import utils.grid.Direction
import utils.grid.FilledGrid
import utils.grid.Position
import utils.println
import utils.readInput

data class PathOption(val dir: Direction, val pathRemaining: Int, val cost: Int, val isNewPath: Boolean) {
    fun nextOptions(newCost: Int, newPathRemaining: Int) =
        if (pathRemaining <= 0)
            listOf(
                PathOption(dir.rotateCW, newPathRemaining, newCost, true),
                PathOption(dir.rotateCCW, newPathRemaining, newCost, true)
            )
        else
            listOf(
                PathOption(dir, pathRemaining - 1, newCost, false),
                PathOption(dir.rotateCW, newPathRemaining, newCost, true),
                PathOption(dir.rotateCCW, newPathRemaining, newCost, true)
            )

    fun improves(o: PathOption): Boolean {
        if (dir != o.dir || isNewPath != o.isNewPath) return false
        if (cost > o.cost) return false
        return (pathRemaining > o.pathRemaining) || (pathRemaining == o.pathRemaining && cost < o.cost)
    }
}

fun main() {
    fun parse(input: List<String>) = FilledGrid(input.map { l -> l.map { it.digitToInt() } }, 999)
    fun addOptions(a: List<PathOption>, b: List<PathOption>?) = if (b == null) a else
        a.filterNot { pa -> b.any { it.improves(pa) } } +
                b.filterNot { pb -> pb in a || a.any { it.improves(pb) } }

    fun FilledGrid<Int>.findPath(newPathMin: Int, newPathMax: Int): Int {
        val newPathRemaining = newPathMax - newPathMin
        val src = Position(0, 0)
        var maxCost = 0
        val shortest =
            mutableMapOf(
                src to listOf(
                    PathOption(Direction.E, newPathRemaining, 0, true),
                    PathOption(Direction.S, newPathRemaining, 0, true)
                )
            )
        val target = Position(height - 1, width - 1)
        while (target !in shortest) {
            maxCost++
            val processed = mutableListOf<Pair<Position, PathOption>>()
            shortest.toList().forEach { (pos, options) ->
                options.forEach { pathOption ->
                    var newPos = pos
                    var newCost = pathOption.cost
                    repeat(if (pathOption.isNewPath) newPathMin else 1) {
                        newPos += pathOption.dir
                        newCost += cell(newPos)
                    }
                    if (newCost <= maxCost) {
                        shortest.compute(newPos) { _, oldOptions ->
                            addOptions(pathOption.nextOptions(newCost, newPathRemaining), oldOptions)
                        }
                        processed.add(pos to pathOption)
                    }
                }
            }
            processed.forEach { (node, option) ->
                shortest[node] = shortest.getValue(node) - option
            }
            if (maxCost % 100 == 0) println("$maxCost..")
        }
        return maxCost
    }

    fun FilledGrid<Int>.part1(): Int = findPath(1, 3)
    fun FilledGrid<Int>.part2(): Int = findPath(4, 10)

    fun part1(input: List<String>) = parse(input).part1()
    fun part2(input: List<String>) = parse(input).part2()

    val testInput = readInput("aoc17/Day17_test")
    assertEquals(102, part1(testInput))
    assertEquals(94, part2(testInput))
    val testInput2 = readInput("aoc17/Day17_test2")
    assertEquals(71, part2(testInput2))

    val input = readInput("aoc17/Day17")
    val sol1 = part1(input)
    sol1.println()
    assertEquals(851, sol1)
    part2(input).println()
}
