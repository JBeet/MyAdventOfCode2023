package utils

fun main() {
    assertEquals(setOf(emptyList()), emptyList<Int>().allPermutations())
    assertEquals(setOf(emptyList(), listOf(1)), listOf(1).allPermutations())
    assertEquals(setOf(emptyList()), listOf(1).allPermutations(0, 0))
    assertEquals(setOf(listOf(1)), listOf(1).allPermutations(1, 1))
    assertEquals(setOf(listOf(1), listOf(2), listOf(3)), listOf(1, 2, 3).allPermutations(1, 1))
    assertEquals(setOf(emptyList()), listOf(1, 2, 3).allPermutations(0, 0))
    assertEquals(
        setOf(listOf(1), listOf(2), listOf(3), listOf(1, 2), listOf(2, 3), listOf(1, 3)),
        listOf(1, 2, 3).allPermutations(1, 2)
    )
    assertEquals(setOf(listOf(1, 2, 3)), listOf(1, 2, 3).allPermutations(3, 3))
    assertEquals(
        setOf(emptyList(), listOf(1), listOf(2), listOf(3), listOf(1, 2), listOf(2, 3), listOf(1, 3), listOf(1, 2, 3)),
        listOf(1, 2, 3).allPermutations()
    )
    assertEquals(
        setOf(emptyList(), listOf(1), listOf(2), listOf(3), listOf(1, 2), listOf(2, 3), listOf(1, 3), listOf(1, 2, 3)),
        listOf(1, 2, 3).allPermutations(0, 3)
    )
    check(listOf(1, 2, 3, 4, 5).allPermutations(2, 4).count() == 25)

    assertEquals(listOf("147", "258", "369"), listOf("123", "456", "789").transpose())
    assertEquals(
        listOf("147", "258", "369").map { it.toList() },
        listOf("123", "456", "789").map { it.toList() }.transpose()
    )

    assertEquals(listOf(emptyList()), emptyList<Int>().split { it == 0 })
    assertEquals(listOf("123", "456", "789").map { it.toList() }, "123 456 789".toList().split { it == ' ' })
    assertEquals(listOf("123").map { it.toList() }, "123".toList().split { it == ' ' })
}

fun <T> assertEquals(expected: List<T>, actual: List<T>) {
    check(expected == actual) { "Expected $expected but got $actual" }
}

fun <T> assertEquals(expected: Set<T>, actual: Sequence<T>) {
    val list = actual.toList()
    check(expected.size == list.size) { "Expected $expected but got $list" }
    check(expected == list.toSet()) { "Expected $expected but got $list" }
}
