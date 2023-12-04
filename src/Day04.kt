fun main() {
    data class Card(val winning: Set<Int>, val mine: Set<Int>) {
        val winCount = mine.count { it in winning }
        val points = if (winCount == 0) 0 else 1 shl (winCount - 1)
    }

    fun parseSet(s: String): Set<Int> =
        s.split(' ').filter { it.isNotEmpty() }.mapTo(mutableSetOf()) { it.toInt() }

    fun Card(s: String) = s.substringAfter(':').trim().split('|').let { (w, m) ->
        Card(parseSet(w), parseSet(m))
    }

    fun part1(input: List<String>): Int =
        input.map { Card(it) }.sumOf { it.points }

    data class Counter(val cardsSoFar: Int, val cardsWon: List<Int>) {
        operator fun plus(card: Card): Counter {
            val multiplier = 1 + (cardsWon.firstOrNull() ?: 0)
            val newCardsWon = List(card.winCount) { cardsWon.getOrElse(it + 1) { 0 } + multiplier } +
                    cardsWon.drop(card.winCount + 1)
            return Counter(cardsSoFar + multiplier, newCardsWon)
        }
    }

    fun part2(input: List<String>): Int =
        input.map { Card(it) }.fold(Counter(0, emptyList())) { counter, card ->
            counter + card
        }.cardsSoFar

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 30)

    val input = readInput("Day04")
    part1(input).println()
    part2(input).println()
}
