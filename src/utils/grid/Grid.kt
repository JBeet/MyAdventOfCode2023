package utils.grid

interface Grid<C> {
    fun cell(p: Position): C
    fun cell(r: Int, c: Int): C
    fun cell(r: Long, c: Long): C
    fun row(r: Long): GridLine<C>
    fun column(c: Long): GridLine<C>
    fun forEachNonEmpty(action: (Position) -> Unit)
    fun countNonEmpty(predicate: (Position) -> Boolean): Int
    fun findAll(value: C): Set<Position> = findAll { cell -> cell == value }.keys
    fun findAll(predicate: (C) -> Boolean): Map<Position, C>
    fun transpose(): Grid<C>
}

interface GridCell {
    val directions: Set<Direction> get() = emptySet()
}

interface GridLine<C> {
    val index: Long
    val isEmpty: Boolean
    fun cell(idx: Long): C
    fun toList(size: Long) = (0..<size).map { cell(it) }
}

data class EmptyLine<C>(override val index: Long, private val empty: C) : GridLine<C> {
    override val isEmpty: Boolean get() = true
    override fun cell(idx: Long): C = empty
}
