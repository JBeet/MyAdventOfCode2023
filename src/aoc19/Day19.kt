package aoc19

import utils.assertEquals
import utils.println
import utils.readInput
import utils.split

private sealed class Condition(val varName: String, val ifTrue: String) {
    abstract fun appliesTo(part: Part): Boolean
    abstract fun partitionVar(range: IntRange): Pair<IntRange, IntRange>
    fun partition(range: PartRange) = partitionVar(range[varName]).map { range.with(varName, it) }
}

fun <T, R> Pair<T, T>.map(mapper: (T) -> R): Pair<R, R> = mapper(first) to mapper(second)

private val emptyRange = IntRange(0, -1)

private class ConditionGT(varName: String, val value: Int, ifTrue: String) : Condition(varName, ifTrue) {
    override fun appliesTo(part: Part): Boolean = part[varName] > value
    override fun partitionVar(range: IntRange): Pair<IntRange, IntRange> = when {
        value <= range.first -> range to emptyRange
        range.last < value -> emptyRange to range
        else -> IntRange(value + 1, range.last) to IntRange(range.first, value)
    }
}

private class ConditionLT(varName: String, val value: Int, ifTrue: String) : Condition(varName, ifTrue) {
    override fun appliesTo(part: Part): Boolean = part[varName] < value
    override fun partitionVar(range: IntRange): Pair<IntRange, IntRange> = when {
        value <= range.first -> emptyRange to range
        range.last < value -> range to emptyRange
        else -> IntRange(range.first, value - 1) to IntRange(value, range.last)
    }
}

private class Workflow(val name: String, val ifAllFalse: String, val conditions: List<Condition>) {
    fun execute(part: Part): String = conditions.firstOrNull { it.appliesTo(part) }?.ifTrue ?: ifAllFalse
    fun split(parts: PartRange) = buildList {
        var range = parts
        for (condition in conditions) {
            val (rangeTrue, rangeFalse) = condition.partition(range)
            add(condition.ifTrue to rangeTrue)
            range = rangeFalse
        }
        add(ifAllFalse to range)
    }
}

val IntRange.length get() = endInclusive - start + 1

private data class PartRange(val map: Map<String, IntRange>) {
    val count: Long get() = map.values.fold(1L) { acc, r -> acc * r.length }
    operator fun get(varName: String) = map.getValue(varName)
    fun with(varName: String, range: IntRange) = PartRange(map + (varName to range))
}

private data class Part(val map: Map<String, Int>) {
    val score: Int get() = map.values.sum()
    operator fun get(varName: String): Int = map.getValue(varName)
}

private class WorkflowPuzzle(val flows: Map<String, Workflow>) {
    private val fullRange = PartRange(mapOf("x" to 1..4000, "m" to 1..4000, "a" to 1..4000, "s" to 1..4000))

    fun accepts(part: Part): Boolean = execute("in", part)

    private fun execute(name: String, part: Part): Boolean =
        when (name) {
            "A" -> true
            "R" -> false
            else -> execute(flow(name).execute(part), part)
        }

    private fun flow(name: String) = flows.getValue(name)

    fun countCombinations(): Long = calculate("in", fullRange)

    private fun calculate(name: String, parts: PartRange): Long =
        when (name) {
            "A" -> parts.count
            "R" -> 0L
            else -> flow(name).split(parts).sumOf { (tgtName, range) -> calculate(tgtName, range) }
        }
}

private fun parsePart(s: String): Part =
    Part(s.drop(1).dropLast(1).split(',').map { it.split('=') }.associate { (name, value) -> name to value.toInt() })

private fun parseWorkflow(s: String): Workflow = s.split('{', '}', ',').filter { it.isNotBlank() }.let { parts ->
    val name = parts[0]
    val result = parts.last()
    Workflow(name, result, parts.drop(1).dropLast(1).map { parseCondition(it) })
}

private fun parseCondition(s: String): Condition = s.split(':').let { (test, result) ->
    val varName = test.takeWhile { it != '<' && it != '>' }
    val check = test[varName.length]
    val value = test.drop(varName.length + 1).toInt()
    when (check) {
        '<' -> ConditionLT(varName, value, result)
        '>' -> ConditionGT(varName, value, result)
        else -> error("Bad operator: $check")
    }
}

fun main() {
    fun parseFlows(flows: List<String>) =
        WorkflowPuzzle(flows.map { parseWorkflow(it) }.associateBy { it.name })

    fun part1(input: List<String>) = input.split { it.isEmpty() }.let { (flows, items) ->
        parseFlows(flows).run {
            items.map { parsePart(it) }.filter { accepts(it) }.sumOf { it.score }
        }
    }

    fun part2(input: List<String>) = parseFlows(input.takeWhile { it.isNotBlank() }).countCombinations()

    val testInput = readInput("aoc19/Day19_test")
    assertEquals(19114, part1(testInput))
    val input = readInput("aoc19/Day19")
    part1(input).println()
    assertEquals(256000000000000L, part2(listOf("in{x<2:A,x>1234:A,A}", "")))
    assertEquals(16000000000L, part2(listOf("in{x>3000:md,R}", "md{m<2:A,R}", "")))
    assertEquals(64000000000000L, part2(listOf("in{x>3000:A,R}", "")))
    assertEquals(192000000000000L, part2(listOf("in{m>1000:A,R}", "")))
    assertEquals(167409079868000L, part2(testInput))
    part2(input).println()
}
