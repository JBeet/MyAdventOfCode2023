package utils

fun <T, K> Sequence<T>.chunkedBy(selector: (T) -> K): Sequence<List<T>> = ChunkedSequence(this, selector)

fun <T, K> Iterable<T>.chunkedBy(selector: (T) -> K): List<List<T>> {
    val result = mutableListOf<List<T>>()
    val activeItems = mutableListOf<T>()
    var activeKey: K? = null
    for (e in this) {
        val key = selector(e)
        if (key != activeKey) {
            activeKey = key
            if (activeItems.isNotEmpty()) {
                result.add(activeItems.toList())
                activeItems.clear()
            }
        }
        activeItems.add(e)
    }
    if (activeItems.isNotEmpty())
        result.add(activeItems.toList())
    return result
}

private class ChunkedSequence<T, K>(private val source: Sequence<T>, private val keySelector: (T) -> K) :
    Sequence<List<T>> {
    override fun iterator(): Iterator<List<T>> = ChunkedIterator(source.iterator(), keySelector)
}

private class ChunkedIterator<T, K>(private val source: Iterator<T>, private val keySelector: (T) -> K) :
    AbstractIterator<List<T>>() {
    private var activeKey: K? = null
    private var activeItems = mutableListOf<T>()

    override fun computeNext() {
        while (source.hasNext()) {
            val next = source.next()
            val key = keySelector(next)
            if (key != activeKey) {
                activeKey = key
                if (activeItems.isNotEmpty()) {
                    setNext(activeItems.toList())
                    activeItems.clear()
                    activeItems.add(next)
                    return
                }
            }
            activeItems.add(next)
        }
        if (activeItems.isNotEmpty()) {
            setNext(activeItems.toList())
            activeItems.clear()
        } else
            done()
    }
}
