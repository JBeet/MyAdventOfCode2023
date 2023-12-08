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

private data class CycleInfo(val cycleLength: Int, val nodesByIndex: Map<Int, String>) {
    constructor(indicesByNode: Map<String, List<Int>>, cycleLength: Int) : this(
        cycleLength, indicesByNode.flatMap { (node, indices) ->
            indices.map { it to node }
        }.toMap()
    )

    fun filter(predicate: (String) -> Boolean) = copy(nodesByIndex = nodesByIndex.filterValues(predicate))
}

private data class Result(val offset: Long, val cycleLength: Long)

private data class GhostMap(val steps: List<Direction>, val paths: Map<String, Path>) {
    fun findPart1(): Int = findPart1("AAA", 0)

    private tailrec fun findPart1(current: String, idx: Int): Int {
        if (current == "ZZZ") return idx
        return findPart1(next(current, idx), idx + 1)
    }

    private fun next(current: String, idx: Int): String {
        val step = steps[idx % steps.size]
        return paths.getValue(current).select(step)
    }

    fun findPart2naive(): Int = findPart2naive(paths.keys.filter { it.endsWith("A") }, 0)

    private tailrec fun findPart2naive(current: Collection<String>, idx: Int): Int {
        if (current.all { it.endsWith('Z') }) return idx
        return findPart2naive(current.map { next(it, idx) }, idx + 1)
    }

    fun findPart2(): Long {
        val startingNodes = paths.keys.filter { it.endsWith("A") }
        val pathsToEndNodes = startingNodes.map { buildCycle(it, 0, emptyMap()) }.map { ci ->
            ci.filter { it.endsWith('Z') }
        }
        return pathsToEndNodes.fold(listOf(Result(0, 1))) { results, ci ->
            results.flatMap { prevResult ->
                val gcd = greatestCommonDivisor(prevResult.cycleLength, ci.cycleLength.toLong())
                ci.nodesByIndex.keys.mapNotNull { offset ->
                    if ((prevResult.offset - offset) % gcd == 0L) {
                        var newOffset = offset.toLong()
                        while ((newOffset - prevResult.offset) % prevResult.cycleLength != 0L)
                            newOffset += ci.cycleLength
                        Result(newOffset, leastCommonMultiple(prevResult.cycleLength, ci.cycleLength.toLong()))
                    } else
                        null
                }
            }
        }.minOf { it.offset }
    }

    private tailrec fun buildCycle(current: String, idx: Int, nodes: Map<String, List<Int>>): CycleInfo {
        val knownNodes = nodes[current] ?: emptyList()
        val prevIndex = knownNodes.firstOrNull { (idx - it) % steps.size == 0 }
        return if (prevIndex == null)
            buildCycle(next(current, idx), idx + 1, nodes + (current to (knownNodes + idx)))
        else
            CycleInfo(nodes, idx - prevIndex)
    }
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
