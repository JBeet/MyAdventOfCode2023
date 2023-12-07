fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 4361)
    check(part2(testInput) == 467835)

    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()
}

fun part1(input: List<String>): Int = Engine(input).run {
    numbers.filter { nr -> symbols.any { nr.isAdjacentTo(it) } }.sumOf { it.value }
}

fun part2(input: List<String>): Int = Engine(input).run {
    symbols.filter { it.isGearSymbol() }.sumOf { symbol ->
        numbers.filter { it.isAdjacentTo(symbol) }.let { if (it.size == 2) it[0].value * it[1].value else 0 }
    }
}

private class Engine(field: List<String>) {
    data class EngineNumber(val value: Int, val row: Int, val colStart: Int, val colEnd: Int) {
        private val adjacentColumns = (colStart - 1)..(colEnd + 1)
        private val adjacentRows = (row - 1)..(row + 1)
        fun isAdjacentTo(symbol: EngineSymbol) = symbol.col in adjacentColumns && symbol.row in adjacentRows
        fun add(next: EngineNumber) = if (next.row != row || next.colStart != colEnd + 1) null else
            EngineNumber(value * 10 + next.value, row, colStart, next.colEnd)
    }

    data class EngineSymbol(val row: Int, val col: Int, val ch: Char) {
        fun isGearSymbol(): Boolean = ch == '*'
    }

    val numbers: List<EngineNumber> = field.flatMapIndexed { row: Int, s: String ->
        s.mapIndexedNotNull { col, ch ->
            if (ch.isDigit()) EngineNumber(ch.digitToInt(), row, col, col) else null
        }.fold(emptyList<EngineNumber>()) { list, cur ->
            val combined = list.lastOrNull()?.add(cur)
            if (combined == null) list + cur else (list.dropLast(1) + combined)
        }
    }
    val symbols: List<EngineSymbol> = field.flatMapIndexed { row, s ->
        s.mapIndexedNotNull { col, ch -> if (ch.isSymbol()) EngineSymbol(row, col, ch) else null }
    }

    private fun Char.isSymbol() = !isDigit() && this != '.'
}
