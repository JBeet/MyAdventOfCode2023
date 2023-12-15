package aoc15

import utils.assertEquals
import utils.println
import utils.readInput

fun main() {
    fun part1(input: List<String>) = input[0].split(',').sumOf { it.hash() }

    class Boxes() {
        val items = LinkedHashMap<Long, LinkedHashMap<String, Int>>()
        fun process(cmd: String) {
            if (cmd.endsWith('-'))
                processDash(cmd.dropLast(1))
            else
                cmd.split('=').let { processEquals(it[0], it[1].toInt()) }
        }

        private fun processEquals(label: String, length: Int) = box(label).put(label, length)
        private fun processDash(label: String) = box(label).remove(label)
        private fun box(label: String): LinkedHashMap<String, Int> =
            items.getOrPut(label.hash()) { LinkedHashMap() }

        fun calculate() = items.map { (nr, box) ->
            box.asIterable().mapIndexed { slot, (nm, length) -> (nr + 1) * (slot + 1) * length }.sum()
        }.sum()
    }


    fun part2(input: List<String>) = Boxes().apply { input[0].split(',').forEach { cmd -> process(cmd) } }.calculate()

    assertEquals(200, "H".hash())
    assertEquals(52, "HASH".hash())

    val testInput = readInput("aoc15/Day15_test")
    check(part1(testInput) == 1320L)
    val input = readInput("aoc15/Day15")
    part1(input).println()
    check(part2(testInput) == 145L)
    part2(input).println()
}

private fun String.hash(): Long = this.fold(0) { acc, ch -> ((acc + ch.code) * 17) % 256 }
