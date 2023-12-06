data class Race(val time: Long, val maxDistance: Long) {
    val winCount: Int
        get() = (1 until time).count { distance(it) > maxDistance }

    private fun distance(buttonPressTime: Long): Long = (time - buttonPressTime) * buttonPressTime
}

fun main() {
    fun part1(input: List<String>): Int {
        fun numbers(time: String) = time.split(' ').filter { it.isNotBlank() }.map { it.toLong() }
        fun races(time: String, distance: String) = numbers(time).zip(numbers(distance)).map { (t, d) -> Race(t, d) }
        fun races(input: List<String>) = races(input[0].removePrefix("Time:"), input[1].removePrefix("Distance:"))
        return races(input).map { it.winCount }.reduce { a, b -> a * b }
    }

    fun part2(input: List<String>): Int {
        fun number(time: String) = time.replace(" ", "").toLong()
        fun race(time: String, distance: String) = Race(number(time), number(distance))
        fun race(input: List<String>) = race(input[0].removePrefix("Time:"), input[1].removePrefix("Distance:"))
        return race(input).winCount
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day06_test")
    check(part1(testInput) == 288) { "part 1" }

    val input = readInput("Day06")
    part1(input).println()

    check(part2(testInput) == 71503) { "part 2" }
    part2(input).println()
}
