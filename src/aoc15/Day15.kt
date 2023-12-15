package aoc15

import utils.assertEquals
import utils.println
import utils.readInput

fun main() {
    fun String.hash() = this.fold(0) { acc, ch -> ((acc + ch.code) * 17) % 256 }

    fun part1(input: List<String>) = input[0].split(',').sumOf { it.hash() }

    class Boxes {
        private val items = List(256) { LinkedHashMap<String, Int>() }
        fun process(cmd: String) {
            if (cmd.endsWith('-'))
                processDash(cmd.dropLast(1))
            else
                cmd.split('=').let { processEquals(it[0], it[1].toInt()) }
        }

        private fun processDash(label: String) = box(label).remove(label)
        private fun processEquals(label: String, length: Int) = box(label).put(label, length)
        private fun box(label: String) = items[label.hash()]
        fun calculate() = items.mapIndexed { nr, box ->
            box.toList().mapIndexed { slot, (_, length) -> (nr + 1) * (slot + 1) * length }.sum()
        }.sum()
    }

    fun part2(input: List<String>) = Boxes().apply { input[0].split(',').forEach(::process) }.calculate()

    assertEquals(200, "H".hash())
    assertEquals(52, "HASH".hash())

    val testInput = readInput("aoc15/Day15_test")
    assertEquals(part1(testInput), 1320)
    val input = readInput("aoc15/Day15")
    part1(input).println()
    assertEquals(part2(testInput), 145)
    part2(input).println()
}
