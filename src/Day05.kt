fun main() {
    data class Translation(val destinationStart: Long, val sourceStart: Long, val length: Long) {
        val sourceEnd = sourceStart + length - 1
        val sourceRange = sourceStart..sourceEnd
        fun appliesTo(nrs: LongRange): Boolean = nrs.start in sourceRange || nrs.endInclusive in sourceRange
        fun translate(nrs: LongRange): LongRange = LongRange(translate(nrs.start), translate(nrs.endInclusive))
        fun translate(nr: Long): Long {
            check(nr in sourceRange) { "$nr should be in $sourceRange at $this" }
            return destinationStart + (nr - sourceStart)
        }
    }

    fun LongRange.coerce(o: LongRange) = LongRange(start.coerceIn(o), endInclusive.coerceIn(o))

    data class Map(val source: String, val destination: String, val translations: List<Translation>) {
        fun translate(nr: Long): Long = translate(LongRange(nr, nr)).single().single()
        fun translate(nrs: List<LongRange>): List<LongRange> = nrs.flatMap { translate(it) }.sortedBy(LongRange::start)
        fun translate(nrs: LongRange): List<LongRange> = buildList {
            var start = nrs.start
            for (part in translations)
                if (part.appliesTo(nrs)) {
                    add(LongRange(start, part.sourceStart - 1))
                    add(part.translate(nrs.coerce(part.sourceRange)))
                    start = part.sourceEnd + 1
                }
            add(LongRange(start, nrs.endInclusive))
        }.filterNot { it.isEmpty() }.sortedBy { it.start }
    }

    data class AlmanacPart1(private val targetSeeds: List<Long>, private val maps: List<Map>) {
        fun solve(): Long = targetSeeds.minOf { resolve("location", "seed", it) }
        fun resolve(target: String, currentType: String, nr: Long): Long =
            if (currentType == target)
                nr
            else
                maps.filter { it.source == currentType }
                    .minOf { resolve(target, it.destination, it.translate(nr)) }
    }

    data class AlmanacPart2(private val targetSeeds: List<LongRange>, private val maps: List<Map>) {
        fun solve(): Long = targetSeeds.minOf { targetRange ->
            resolve("location", "seed", listOf(targetRange)).minOf(LongRange::start)
        }

        fun resolve(target: String, currentType: String, ranges: List<LongRange>): List<LongRange> =
            if (currentType == target)
                ranges
            else
                maps.filter { it.source == currentType }
                    .flatMap { resolve(target, it.destination, it.translate(ranges)) }
    }

    fun Translation(s: String): Translation =
        s.split(' ').let { (dest, src, len) -> Translation(dest.toLong(), src.toLong(), len.toLong()) }

    fun Map(lines: List<String>): Map {
        val (src, _, dest) = lines[0].split('-', ' ')
        return Map(src, dest, lines.drop(1).map { Translation(it) }.sortedBy { it.sourceStart })
    }

    fun parseMaps(input: List<String>): List<Map> = buildList {
        var start = input.indexOf("")
        while (start < input.size) {
            val mapLines = input.drop(start + 1).takeWhile { it.isNotEmpty() }
            add(Map(mapLines))
            start += mapLines.size + 1
        }
    }

    fun part1(input: List<String>): Long {
        val targetSeeds = (input[0]).removePrefix("seeds: ").splitToLongs()
        return AlmanacPart1(targetSeeds, parseMaps(input)).solve()
    }

    fun part2(input: List<String>): Long {
        val targetSeeds = (input[0]).removePrefix("seeds: ").splitToLongs()
            .chunked(2).map { (min, length) -> LongRange(min, min + length - 1) }
        return AlmanacPart2(targetSeeds, parseMaps(input)).solve()
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day05_test")
    check(part1(testInput) == 35L) { "part 1" }

    val input = readInput("Day05")
    part1(input).println()

    check(part2(testInput) == 46L) { "part 2" }
    part2(input).println()
}
