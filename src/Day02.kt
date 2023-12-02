fun main() {
    fun parseGames(input: List<String>) = input.map { Game.parse(it) }

    fun part1(input: List<String>) =
        parseGames(input).filter { it.isPossible(mapOf("red" to 12, "green" to 13, "blue" to 14)) }.sumOf { it.id }

    fun part2(input: List<String>) = parseGames(input).sumOf { it.powerCubes() }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    check(part1(testInput) == 8)

    val input = readInput("Day02")
    part1(input).println()
    part2(input).println()
}

data class Turn(val items: Map<String, Int>) {
    private val colors = items.keys
    operator fun get(color: String) = items[color] ?: 0

    fun isPossible(available: Map<String, Int>): Boolean =
        items.all { (color, count) -> (available[color] ?: 0) >= count }

    fun minRequired(o: Turn): Turn = Turn((colors + o.colors).associateWith { maxOf(this[it], o[it]) })
    fun powerCubes() = items.values.fold(1) { acc, result -> acc * result }
}

data class Game(val id: Int, val turns: List<Turn>) {
    fun isPossible(available: Map<String, Int>) = turns.all { it.isPossible(available) }
    fun powerCubes() = minRequired().powerCubes()
    private fun minRequired(): Turn = turns.fold(Turn(emptyMap())) { acc, turn -> acc.minRequired(turn) }

    companion object {
        fun parse(s: String): Game {
            check(s.take(5) == "Game ")
            val cPos = s.indexOf(':')
            val id = s.substring(5, cPos).toInt()
            val remainder = s.drop(cPos + 1)
            val turns = remainder.split(';').map { turnString ->
                Turn(turnString.split(',').map { it.trim().split(' ') }
                    .associate { (count, color) -> color to count.toInt() })
            }
            return Game(id, turns)
        }
    }

}
