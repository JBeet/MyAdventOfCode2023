package aoc07

import utils.println
import utils.readInput

private enum class Card {
    CA, CK, CQ, CJ, CT, C9, C8, C7, C6, C5, C4, C3, C2, JOKER;

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

private data class Hand(val cards: List<Card>, val bid: Int, val handType: HandType = handTypeFrom(cards)) :
    Comparable<Hand> {
    constructor(s: String) : this(s.take(5).map { Card.valueOf("C$it") }, s.drop(5).trim().toInt())

    fun bestWithJokers(): Hand {
        val nonJokers = cards.filter { it != Card.CJ }
        return when (val jokerCount = cards.size - nonJokers.size) {
            0 -> this
            5 -> Hand(List(5) { Card.JOKER }, bid, HandType.FIVE)
            else -> {
                val bestHand =
                    nonJokers.distinct().minOf { card -> handTypeFrom(nonJokers + List(jokerCount) { card }) }
                Hand(cards.map { if (it == Card.CJ) Card.JOKER else it }, bid, bestHand)
            }
        }
    }

    override fun compareTo(other: Hand): Int {
        val rt = handType.compareTo(other.handType)
        if (rt != 0) return -rt
        return cards.zip(other.cards).firstNotNullOfOrNull { (t, o) -> if (t == o) null else -t.compareTo(o) } ?: 0
    }

    override fun toString() =
        cards.joinToString("") + " " + cards.groupBy { it } + " " + handType + " " + bid
}

fun main() {
    fun part1(input: List<String>) =
        input.map { Hand(it) }.sorted().mapIndexed { index, hand -> hand.bid * (index + 1) }.sum()

    fun part2(input: List<String>) =
        input.map { Hand(it).bestWithJokers() }.sorted()
            .mapIndexed { index, hand -> hand.bid * (index + 1) }.sum()

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("aoc07/Day07_test")
    check(part1(testInput) == 6440)

    val input = readInput("aoc07/Day07")
    part1(input).println()

    check(part2(testInput) == 5905)
    part2(input).println()
}
