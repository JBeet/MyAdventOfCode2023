private enum class Direction {
    L, R
}

private data class Path(val l: String, val r: String) {
    fun select(d: Direction) = if (d == Direction.L) l else r
}

private data class GhostMap(val steps: List<Direction>, val paths: Map<String, Path>) {
    fun findPart1(): Int = findPart1("AAA", 0)

    private tailrec fun findPart1(current: String, idx: Int): Int {
        if (current == "ZZZ") return idx
        return findPart1(next(current, idx), idx + 1)
    }

    private fun next(current: String, idx: Int): String {
        val step = steps[idx % steps.size]
        return paths.getValue(current).select(step)
    }

    fun findPart2(): Long {
        val startingNodes = paths.keys.filter { it.endsWith("A") }
        val cycleSize = startingNodes.map { findPart2cycle(it, 0) }
        return cycleSize.fold(1L) { acc, cs -> lcm(acc, cs.toLong()) }
    }

    private fun lcm(a: Long, b: Long): Long = (a * b) / gcd(a, b)
    private tailrec fun gcd(a: Long, b: Long): Long = if (a == b) a else if (a < b) gcd(b, a) else gcd(a - b, b)

    private tailrec fun findPart2cycle(current: String, idx: Int): Int {
        if (current.last() == 'Z') return idx else
            return findPart2cycle(next(current, idx), idx + 1)
    }
}

private fun List<String>.notBlank() = filter { it.isNotBlank() }

fun main() {
    fun parseMap(input: List<String>): GhostMap {
        val steps = input[0].map { Direction.valueOf(it.toString()) }
        val paths = input.drop(1).notBlank().map { it.split(*" =(),".toCharArray()) }.map { it.notBlank() }
            .associate { (start, l, r) -> start to Path(l, r) }
        return GhostMap(steps, paths)
    }

    fun part1(input: List<String>) = parseMap(input).findPart1()

    fun part2(input: List<String>) = parseMap(input).findPart2()

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
    check(part1(testInput) == 2)
    val testInput2 = readInput("Day08_test2")
    check(part1(testInput2) == 6)

    val input = readInput("Day08")
    part1(input).println()


    val testInput3 = readInput("Day08_test3")
    check(part2(testInput3) == 6L)
    part2(input).println()
}
