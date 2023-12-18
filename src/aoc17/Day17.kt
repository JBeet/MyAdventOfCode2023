package aoc17

import utils.assertEquals
import utils.grid.Direction
import utils.grid.FilledGrid
import utils.grid.Position
import utils.println
import utils.readInput
import java.util.*

data class Node(val pos: Position, val dir: Direction, val dirSteps: Int) {
    val nextPos = pos + dir

    fun next() = Node(nextPos, dir, dirSteps + 1)
    fun rotateCW() = Node(nextPos, dir.rotateCW, 1)
    fun rotateCCW() = Node(nextPos, dir.rotateCCW, 1)
}

class LavaDucts(input: List<String>) : FilledGrid<Int>(input.map { l -> l.map { it.digitToInt() } }, 999) {
    private val visited: MutableMap<Node, Int>
    private val queue = PriorityQueue(compareBy(Pair<Node, Int>::second))
    private val target = Position(height - 1, width - 1)

    init {
        val src = Position(0, 0)
        val startEast = Node(src, Direction.E, 1)
        val startSouth = Node(src, Direction.S, 1)
        visited = mutableMapOf(startEast to 0, startSouth to 0)
        queue.add(startEast to 0)
        queue.add(startSouth to 0)
    }

    private fun add(nextNode: Node, cost: Int) {
        if (nextNode !in visited.keys) {
            queue.offer(nextNode to cost)
            visited[nextNode] = cost
        }
    }

    fun part1(): Int = findPath(1, 3)
    fun part2(): Int = findPath(4, 10)

    private fun findPath(minSteps: Int, maxSteps: Int): Int {
        while (queue.isNotEmpty()) {
            val (node, prevCost) = queue.poll()
            if (node.pos == target && node.dirSteps > minSteps) return prevCost
            if (node.nextPos !in this) continue
            val cost = prevCost + cell(node.nextPos)
            if (node.dirSteps < maxSteps)
                add(node.next(), cost)
            if (node.dirSteps >= minSteps) {
                add(node.rotateCW(), cost)
                add(node.rotateCCW(), cost)
            }
        }
        error("No path to $target!")
    }
}

fun main() {
    fun parse(input: List<String>) = LavaDucts(input)

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
