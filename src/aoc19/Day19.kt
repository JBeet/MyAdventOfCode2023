package aoc19

import utils.assertEquals
import utils.println
import utils.readInput
import utils.split

private sealed class Condition(val varName: String, val ifTrue: String) {
    abstract fun appliesTo(part: Part): Boolean
    abstract fun partitionVar(range: IntRange): Pair<IntRange, IntRange>
    fun partition(range: PartRange): Pair<PartRange, PartRange> = when (varName) {
        "x" -> partitionVar(range.x).map { PartRange(it, range.m, range.a, range.s) }
        "m" -> partitionVar(range.m).map { PartRange(range.x, it, range.a, range.s) }
        "a" -> partitionVar(range.a).map { PartRange(range.x, range.m, it, range.s) }
        "s" -> partitionVar(range.s).map { PartRange(range.x, range.m, range.a, it) }
        else -> error("unknown var $varName")
    }
}

private val emptyRange = IntRange(0, -1)

private class ConditionGT(varName: String, val value: Int, ifTrue: String) : Condition(varName, ifTrue) {
    override fun appliesTo(part: Part): Boolean = part[varName] > value
    override fun partitionVar(range: IntRange): Pair<IntRange, IntRange> {
        if (value <= range.first) return range to emptyRange
        if (value > range.last) return emptyRange to range
        return IntRange(value + 1, range.last) to IntRange(range.first, value)
    }
}

fun <T, R> Pair<T, T>.map(mapper: (T) -> R): Pair<R, R> = mapper(first) to mapper(second)

private class ConditionLT(varName: String, val value: Int, ifTrue: String) : Condition(varName, ifTrue) {
    override fun appliesTo(part: Part): Boolean = part[varName] < value
    override fun partitionVar(range: IntRange): Pair<IntRange, IntRange> {
        if (value <= range.first) return emptyRange to range
        if (value > range.last) return range to emptyRange
        return IntRange(range.first, value - 1) to IntRange(value, range.last)
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

private data class PartRange(val x: IntRange, val m: IntRange, val a: IntRange, val s: IntRange) {
    val count: Long get() = 1L * x.length * m.length * a.length * s.length
}

private data class Part(val map: Map<String, Int>) {
    val score: Int get() = map.getValue("x") + map.getValue("m") + map.getValue("a") + map.getValue("s")
    operator fun get(varName: String): Int = map.getValue(varName)
}

private class WorkflowPuzzle(val flows: Map<String, Workflow>) {
    fun accepts(part: Part): Boolean = execute(flows.getValue("in"), part)

    private tailrec fun execute(workflow: Workflow, part: Part): Boolean {
        val result = workflow.execute(part)
        if (result == "A") return true
        if (result == "R") return false
        return execute(flows.getValue(result), part)
    }

    fun countCombinations(): Long = calculate("in", PartRange(1..4000, 1..4000, 1..4000, 1..4000))

    private fun calculate(name: String, parts: PartRange): Long = when (name) {
        "A" -> parts.count
        "R" -> 0L
        else -> flows.getValue(name).split(parts).fold(0L) { acc, (tgtName, range) ->
            acc + calculate(tgtName, range)
        }
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
