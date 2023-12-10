package aoc10

import utils.readInput
import utils.println
import java.lang.Integer.min

enum class Pipe(
    val inpChar: Char,
    val isDown: Boolean = false, val isUp: Boolean = false,
    val isLeft: Boolean = false, val isRight: Boolean = false
) {
    `┌`('F', isDown = true, isRight = true), `┐`('7', isDown = true, isLeft = true),
    `└`('L', isUp = true, isRight = true), `┘`('J', isUp = true, isLeft = true),
    `│`('|', isDown = true, isUp = true), `─`('-', isLeft = true, isRight = true),
    `╳`('S'), `□`('.');
}

class MetalField(private val pipes: List<List<Pipe>>) {
    private val rowCount: Int = pipes.size
    private val colCount: Int = pipes[0].size.also { cc ->
        check(pipes.all { it.size == cc }) { "expected length $cc" }
    }
    private val distances: Array<Array<Int>> by lazy {
        val target = Array(rowCount) { Array(colCount) { -1 } }
        val (startR, startC) = findPosition(Pipe.`╳`)
        val fillDistances = DeepRecursiveFunction<Triple<Int, Int, Int>, Unit> { (r, c, dist) ->
            if (r < 0 || r >= rowCount) return@DeepRecursiveFunction
            if (c < 0 || c >= colCount) return@DeepRecursiveFunction
            if (target[r][c] in 0..dist) return@DeepRecursiveFunction
            val pipe = pipe(r, c)
            if (pipe != Pipe.`□`)
                target[r][c] = dist
            if (isLeft(r, c))
                callRecursive(Triple(r, c - 1, dist + 1))
            if (isRight(r, c))
                callRecursive(Triple(r, c + 1, dist + 1))
            if (isDown(r, c))
                callRecursive(Triple(r + 1, c, dist + 1))
            if (isUp(r, c))
                callRecursive(Triple(r - 1, c, dist + 1))
        }
        fillDistances(Triple(startR, startC, 0))
        target
    }

    private fun findPosition(p: Pipe) = pipes.withIndex()
        .firstNotNullOf { (rowIndex, row) -> row.indexOf(p).let { if (it < 0) null else (rowIndex to it) } }

    fun findMaxDistance(): Int = distances.maxOf { it.max() }

    fun countInside(): Int = (0..<rowCount).sumOf { row -> (0..<colCount).count { col -> isInsideLoop(row, col) } }

    private fun isInsideLoop(row: Int, col: Int) = (!isPartOfLoop(row, col)) && (countCrossings(row, col) % 2 == 1)
    private fun isPartOfLoop(row: Int, c: Int) = distance(row, c) >= 0

    private fun isSnake(row: Int, col: Int): Boolean = pipe(row, col) == Pipe.`╳`
    private fun countCrossings(row: Int, col: Int) = min(countCrossingsDown(row, col), countCrossingsUp(row, col))
    private fun countCrossingsDown(row: Int, col: Int) = (0..<col).count { c -> isPartOfLoop(row, c) && isDown(row, c) }
    private fun countCrossingsUp(row: Int, col: Int) = (0..<col).count { c -> isPartOfLoop(row, c) && isUp(row, c) }

    private fun isDown(r: Int, c: Int) = if (isSnake(r, c)) pipe(r + 1, c).isUp else pipe(r, c).isDown
    private fun isUp(r: Int, c: Int) = if (isSnake(r, c)) pipe(r - 1, c).isDown else pipe(r, c).isUp
    private fun isLeft(r: Int, c: Int) = if (isSnake(r, c)) pipe(r, c - 1).isRight else pipe(r, c).isLeft
    private fun isRight(r: Int, c: Int) = if (isSnake(r, c)) pipe(r, c + 1).isLeft else pipe(r, c).isRight

    private fun distance(r: Int, c: Int) = if (r in 0..<rowCount && c in 0..<colCount) distances[r][c] else -1
    private fun pipe(r: Int, c: Int) = if (r in 0..<rowCount && c in 0..<colCount) pipes[r][c] else Pipe.`□`

    override fun toString() = "$rowCount x $colCount\n" + pipes.joinToString("\n") { it.joinToString("") }
}

fun main() {
    fun parseChar(ch: Char): Pipe = Pipe.entries.first { it.inpChar == ch }
    fun parseLine(s: String): List<Pipe> = s.map { parseChar(it) }
    fun parse(input: List<String>): MetalField = MetalField(input.map { parseLine(it) }.filter { it.isNotEmpty() })

    fun part1(input: List<String>): Int {
        return parse(input).findMaxDistance()
    }

    fun part2(input: List<String>): Int {
        return parse(input).countInside()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("aoc10/Day10_test")
    check(part1(testInput) == 8)

    val input = readInput("aoc10/Day10")
    part1(input).println()
    val testInput2 = readInput("aoc10/Day10_test2")
    check(part2(testInput2) == 10) { "Expected 10 but found " + part2(testInput2) }
    val testInput3 = readInput("aoc10/Day10_test3")
    check(part2(testInput3) == 8) { "Expected 8 but found " + part2(testInput3) }
    part2(input).println()
}
