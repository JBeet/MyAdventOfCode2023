package aoc08

import utils.greatestCommonDivisor
import utils.leastCommonMultiple
import utils.notBlank
import utils.println
import utils.readInput

private enum class Direction {
    L, R
}

private data class Path(val l: String, val r: String) {
    fun select(d: Direction) = if (d == Direction.L) l else r
}

private data class NodeCycle(val offset: Long, val cycleLength: Long) {
    fun joinWith(cyclesForThisNode: List<NodeCycle>): List<NodeCycle> =
        cyclesForThisNode.mapNotNull { this + it }

    operator fun plus(o: NodeCycle): NodeCycle? {
        val gcd = greatestCommonDivisor(cycleLength, o.cycleLength)
        return if ((offset - o.offset) % gcd == 0L) {
            var newOffset = o.offset
            while ((newOffset - offset) % cycleLength != 0L)
                newOffset += o.cycleLength
            NodeCycle(newOffset, leastCommonMultiple(cycleLength, o.cycleLength))
        } else
            null
    }
}

private data class GhostMap(val steps: List<Direction>, val paths: Map<String, Path>) {
    fun findPart1(): Int = findPart1("AAA", 0)

    private tailrec fun findPart1(current: String, idx: Int): Int =
        if (current == "ZZZ") idx else findPart1(next(current, idx), idx + 1)

    private fun next(current: String, idx: Int): String =
        paths.getValue(current).select(steps[idx % steps.size])

    fun findPart2naive(): Int = findPart2naive(paths.keys.filter { it.endsWith("A") }, 0)

    private tailrec fun findPart2naive(current: Collection<String>, idx: Int): Int =
        if (current.all { it.endsWith('Z') }) idx else findPart2naive(current.map { next(it, idx) }, idx + 1)

    fun findPart2() = paths.keys.filter { it.endsWith("A") }.map { startNode ->
        buildCycles(startNode, 0, emptyMap())
    }.fold(listOf(NodeCycle(0, 1))) { results, cyclesForThisNode ->
        results.flatMap { prevResult -> prevResult.joinWith(cyclesForThisNode) }
    }.minOf { it.offset }

    private tailrec fun buildCycles(current: String, idx: Int, nodes: Map<String, List<Int>>): List<NodeCycle> {
        val knownNodes = nodes[current] ?: emptyList()
        val prevIndex = knownNodes.firstOrNull { (idx - it) % steps.size == 0 }
        if (prevIndex != null)
            return nodes.values.toNodeCycles((idx - prevIndex).toLong())
        val nextNodes = if (current.endsWith('Z')) nodes + (current to (knownNodes + idx)) else nodes
        return buildCycles(next(current, idx), idx + 1, nextNodes)
    }

    private fun Collection<List<Int>>.toNodeCycles(cycleLength: Long) =
        flatMap { list -> list.toNodeCycles(cycleLength) }

    private fun List<Int>.toNodeCycles(cycleLength: Long) = map { NodeCycle(it.toLong(), cycleLength) }
}

fun main() {
    fun parseMap(input: List<String>): GhostMap {
        val steps = input[0].map { Direction.valueOf(it.toString()) }
        val paths = input.drop(1).notBlank().map { it.split(*" =(),".toCharArray()) }.map { it.notBlank() }
            .associate { (start, l, r) -> start to Path(l, r) }
        return GhostMap(steps, paths)
    }

    fun part1(input: List<String>) = parseMap(input).findPart1()
    fun part2(input: List<String>) = parseMap(input).findPart2()
    fun part2naive(input: List<String>) = parseMap(input).findPart2naive()

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("aoc08/Day08_test")
    check(part1(testInput) == 2)
    val testInput2 = readInput("aoc08/Day08_test2")
    check(part1(testInput2) == 6)

    val input = readInput("aoc08/Day08")
    part1(input).println()


    val testInput3 = readInput("aoc08/Day08_test3")
    check(part2(testInput3) == 6L)
    val testInput4 = readInput("aoc08/Day08_test4")
    val expected = part2naive(testInput4).toLong()
    check(part2(testInput4) == expected)
    part2(input).println()
}
