private enum class Card {
    CA, CK, CQ, CJ, CT, C9, C8, C7, C6, C5, C4, C3, C2, LOW;

    override fun toString(): String = name[1].toString()
}

private enum class HandType {
    FIVE {
        override fun matches(cards: List<Card>) = cards.all { it == cards[0] }
    },
    FOUR {
        override fun matches(cards: List<Card>) = cards.groupBy { it }.values.any { it.size == 4 }
    },
    FULL_HOUSE {
        override fun matches(cards: List<Card>) = cards.groupBy { it }.values.map { it.size }.toSet() == setOf(2, 3)
    },
    THREE {
        override fun matches(cards: List<Card>) = cards.groupBy { it }.values.any { it.size == 3 }
    },
    TWO_PAIR {
        override fun matches(cards: List<Card>) = cards.groupBy { it }.values.count { it.size == 2 } == 2
    },
    ONE_PAIR {
        override fun matches(cards: List<Card>) = cards.groupBy { it }.values.any { it.size == 2 }
    },
    HIGH {
        override fun matches(cards: List<Card>) = true
    };

    abstract fun matches(cards: List<Card>): Boolean
}

private fun handTypeFrom(cards: List<Card>) = HandType.entries.first { it.matches(cards) }

private data class Hand(val cards: List<Card>, val bid: Int) {
    constructor(s: String) : this(s.take(5).map { Card.valueOf("C$it") }, s.drop(5).trim().toInt())

    val handType: HandType = handTypeFrom(cards)
    val handTypeWithJoker: HandType
        get() {
            if (Card.CJ !in cards) return handType
            val nonJokers = cards.filter { it != Card.CJ }
            val jokerCount = cards.size - nonJokers.size
            return if (jokerCount == 5)
                HandType.FIVE
            else
                nonJokers.distinct().minOf { card -> handTypeFrom(nonJokers + List(jokerCount) { card }) }
        }

    override fun toString() =
        cards.joinToString("") + " " + cards.groupBy { it } + " " + handTypeWithJoker + " " + bid
}

private val part1compare: Comparator<Hand> = compareBy<Hand>(
    { it.handType }, { it.cards[0] }, { it.cards[1] }, { it.cards[2] }, { it.cards[3] }, { it.cards[4] }
).reversed()
private val part2compare: Comparator<Hand> = compareBy<Hand>(
    { it.handTypeWithJoker },
    { it.cards[0].nonJoker },
    { it.cards[1].nonJoker },
    { it.cards[2].nonJoker },
    { it.cards[3].nonJoker },
    { it.cards[4].nonJoker }
).reversed()

private val Card.nonJoker get() = if (this == Card.CJ) Card.LOW else this

fun main() {
    fun part1(input: List<String>) =
        input.map { Hand(it) }.sortedWith(part1compare).mapIndexed { index, hand -> hand.bid * (index + 1) }.sum()

    fun part2(input: List<String>) =
        input.map { Hand(it) }.sortedWith(part2compare).mapIndexed { index, hand -> hand.bid * (index + 1) }.sum()

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test")
    check(part1(testInput) == 6440)

    val input = readInput("Day07")
    part1(input).println()

    check(part2(testInput) == 5905)
    part2(input).println()
}
