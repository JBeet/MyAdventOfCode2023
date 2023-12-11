package utils

import kotlin.math.abs

enum class Direction(val delta: Position) {
    N(Position(-1, 0)), E(Position(0, +1)), S(Position(+1, 0)), W(Position(0, -1)),
    NE(Position(-1, +1)), SE(Position(+1, +1)), NW(Position(-1, -1)), SW(Position(+1, -1));

    val inverse: Direction
        get() = when (this) {
            N -> S
            E -> W
            S -> N
            W -> E
            NE -> SW
            SE -> NW
            NW -> SE
            SW -> NE
        }
}

interface Grid<C : GridCell> {
    fun cell(p: Position): C
    fun cell(r: Int, c: Int): C
    fun cell(r: Long, c: Long): C
    fun forEachNonEmpty(action: (Position) -> Unit)
    fun countNonEmpty(predicate: (Position) -> Boolean): Int
    fun findAll(value: C): Set<Position> = findAll { cell -> cell == value }.keys
    fun findAll(predicate: (C) -> Boolean): Map<Position, C>
}

interface GridCell {
    val connections: Set<Direction> get() = emptySet()
}

interface GridLine<C : GridCell> {
    val index: Long
    val isEmpty: Boolean
    fun cell(idx: Long): C
}

data class Position(val row: Long, val column: Long) {
    constructor(row: Int, column: Int) : this(row.toLong(), column.toLong())

    val rowBefore: Sequence<Position> = (0..<column).asSequence().map { Position(row, it) }
    val columnBefore: Sequence<Position> = (0..<row).asSequence().map { Position(it, column) }

    operator fun plus(delta: Position) = Position(row + delta.row, column + delta.column)
    operator fun minus(term: Position) = Position(row - term.row, column - term.column)
    operator fun unaryMinus() = Position(-row, -column)
    operator fun times(f: Int) = times(f.toLong())
    operator fun times(f: Long) = Position(row * f, column * f)
    fun manhattanDistanceTo(o: Position) = abs(row - o.row) + abs(column - o.column)
    override fun toString(): String = "($row,$column)"

    companion object {
        val zero: Position = Position(0L, 0L)
    }
}

operator fun Int.times(f: Position) = f * toLong()
operator fun Long.times(f: Position) = f * this

abstract class AbstractGrid<C : GridCell> : Grid<C> {
    fun connections(pos: Position) = connections(pos, cell(pos))
    open fun connections(pos: Position, cell: C): Iterable<Direction> = cell.connections

    fun <R : Any> foldConnected(pos: Position, value: R, cellHandler: (Position, R) -> R?) {
        val traverse = DeepRecursiveFunction<Pair<Position, R>, Unit> { (pos, acc) ->
            val result = cellHandler(pos, acc)
            if (result != null)
                connections(pos).forEach { dir -> callRecursive((pos + dir.delta) to result) }
        }
        traverse(pos to value)
    }

    open fun cellAsString(pos: Position, cell: C): String = when (connections(pos, cell).toSet()) {
        emptySet<Direction>() -> cell.toString()
        else -> charFor(connections(pos, cell).toSet())?.toString() ?: cell.toString()
    }

    private fun charFor(directions: Set<Direction>): Char? = when (directions) {
        emptySet<Direction>() -> ' '
        setOf(Direction.N) -> '╵'
        setOf(Direction.E) -> '╶'
        setOf(Direction.S) -> '╷'
        setOf(Direction.W) -> '╴'
        setOf(Direction.N, Direction.E) -> '└'
        setOf(Direction.N, Direction.S) -> '│'
        setOf(Direction.N, Direction.W) -> '┘'
        setOf(Direction.E, Direction.S) -> '┌'
        setOf(Direction.E, Direction.W) -> '─'
        setOf(Direction.S, Direction.W) -> '┐'
        setOf(Direction.N, Direction.E, Direction.S) -> '├'
        setOf(Direction.N, Direction.E, Direction.W) -> '┴'
        setOf(Direction.N, Direction.S, Direction.W) -> '┤'
        setOf(Direction.E, Direction.S, Direction.W) -> '┬'
        setOf(Direction.N, Direction.E, Direction.S, Direction.W) -> '┼'
        else -> null
    }
}

open class OpenGrid<C : GridCell>(private val cells: Map<Position, C>, private val empty: C) : AbstractGrid<C>() {
    constructor(cells: List<List<C>>, empty: C) : this(cells.flatMapIndexed { rowIndex: Int, rowData: List<C> ->
        rowData.mapIndexedNotNull { colIndex, c -> if (c == empty) null else (Position(rowIndex, colIndex) to c) }
    }.toMap(), empty)

    private val rowIndices = cells.keys.map { it.row }.toSortedSet()
    private val colIndices = cells.keys.map { it.column }.toSortedSet()
    val minRow = rowIndices.first()
    val maxRow = rowIndices.last()
    val minColumn = colIndices.first()
    val maxColumn = colIndices.last()

    val rows get() = (minRow..maxRow).asSequence().map { row(it) }
    val columns get() = (minColumn..maxColumn).asSequence().map { column(it) }
    fun row(r: Long): GridLine<C> = if (r in rowIndices)
        OpenGridLine(r, cells.filterKeys { it.row == r }.mapKeys { it.key.column }, empty)
    else
        EmptyLine(r, empty)

    fun column(c: Long) = if (c in colIndices)
        OpenGridLine(c, cells.filterKeys { it.column == c }.mapKeys { it.key.row }, empty)
    else
        EmptyLine(c, empty)

    override fun cell(p: Position) = cells[p] ?: empty
    override fun cell(r: Int, c: Int) = cell(Position(r.toLong(), c.toLong()))
    override fun cell(r: Long, c: Long) = cell(Position(r, c))
    override fun findAll(predicate: (C) -> Boolean): Map<Position, C> = cells.filterValues(predicate)

    override fun forEachNonEmpty(action: (Position) -> Unit) = cells.forEach { (pos, _) -> action(pos) }
    override fun countNonEmpty(predicate: (Position) -> Boolean): Int = cells.count { (pos, _) -> predicate(pos) }

    fun forEachWithEmpty(action: (Position) -> Unit) = rowIndices.forEach { rowIndex ->
        colIndices.forEach { colIndex -> action(Position(rowIndex, colIndex)) }
    }

    fun countWithEmpty(predicate: (Position) -> Boolean) = rowIndices.sumOf { rowIndex ->
        colIndices.count { colIndex -> predicate(Position(rowIndex, colIndex)) }
    }

    override fun toString() = buildString {
        (minRow..maxRow).forEach { rowIndex ->
            (minColumn..maxColumn).forEach { colIndex ->
                val pos = Position(rowIndex, colIndex)
                append(cellAsString(pos, cell(pos)))
            }
            append('\n')
        }
    }
}

private data class OpenGridLine<C : GridCell>(
    override val index: Long, private val cells: Map<Long, C>, private val empty: C
) : GridLine<C> {
    override val isEmpty: Boolean get() = cells.all { it == empty }
    override fun cell(idx: Long): C = cells[idx] ?: empty
}

open class FilledGrid<C : GridCell>(private val cells: List<List<C>>, private val empty: C) : AbstractGrid<C>() {
    val rowCount: Int = cells.size
    val colCount: Int = cells[0].size.also { cc ->
        check(cells.all { it.size == cc }) { "expected length $cc" }
    }
    private val rowIndices = 0..<rowCount
    private val colIndices = 0..<colCount

    val rows get() = rowIndices.asSequence().map { row(it) }
    val columns get() = colIndices.asSequence().map { column(it) }
    val nonEmptyCells
        get() = cells.asSequence().flatMapIndexed { rowIndex, row ->
            row.asSequence().mapIndexedNotNull { colIndex, c ->
                if (c == empty) null else (Position(rowIndex, colIndex) to c)
            }
        }

    fun row(r: Int): GridLine<C> =
        if (r in rowIndices) GridList(r.toLong(), cells[r], empty) else EmptyLine(r.toLong(), empty)

    fun column(c: Int) =
        if (c in colIndices) ColumnGridLine(c) else EmptyLine(c.toLong(), empty)

    override fun cell(p: Position) = cell(p.row, p.column)
    override fun cell(r: Int, c: Int) = if (r in rowIndices && c in colIndices) cells[r][c] else empty
    override fun cell(r: Long, c: Long) = cell(r.toInt(), c.toInt())

    private inner class ColumnGridLine(private val columnIndex: Int) : GridLine<C> {
        private val size: Int = rowCount
        override val index: Long = columnIndex.toLong()
        override val isEmpty: Boolean by lazy { (0..size).all { cell(it) == empty } }
        override fun cell(idx: Long): C = cell(idx.toInt())
        private fun cell(idx: Int): C = if (idx in 0..<size) cells[idx][columnIndex] else empty
    }

    override fun findAll(predicate: (C) -> Boolean): Map<Position, C> = buildMap {
        cells.forEachIndexed { rowIndex, rowData ->
            rowData.forEachIndexed { colIndex, cell ->
                if (predicate(cell)) put(Position(rowIndex, colIndex), cell)
            }
        }
    }

    override fun forEachNonEmpty(action: (Position) -> Unit) = rowIndices.forEach { rowIndex ->
        colIndices.forEach { colIndex ->
            if (cell(rowIndex, colIndex) != empty) action(Position(rowIndex, colIndex))
        }
    }

    override fun countNonEmpty(predicate: (Position) -> Boolean) = rowIndices.sumOf { rowIndex ->
        colIndices.count { colIndex ->
            (cell(rowIndex, colIndex) != empty) && predicate(Position(rowIndex, colIndex))
        }
    }

    fun forEachWithEmpty(action: (Position) -> Unit) = rowIndices.forEach { rowIndex ->
        colIndices.forEach { colIndex -> action(Position(rowIndex, colIndex)) }
    }

    fun countWithEmpty(predicate: (Position) -> Boolean) = rowIndices.sumOf { rowIndex ->
        colIndices.count { colIndex -> predicate(Position(rowIndex, colIndex)) }
    }


    open fun bgColor(pos: Position): AnsiColor = AnsiColor.DEFAULT
    open fun fgColor(pos: Position): AnsiColor = AnsiColor.DEFAULT
    override fun toString() = buildString {
        rowIndices.forEach { rowIndex ->
            colIndices.forEach { colIndex ->
                val pos = Position(rowIndex, colIndex)
                val fgColor = fgColor(pos)
                val bgColor = bgColor(pos)
                if (fgColor != AnsiColor.DEFAULT)
                    append(fgColor.fgCode())
                if (bgColor != AnsiColor.DEFAULT)
                    append(bgColor.bgCode())
                append(cellAsString(pos, cell(pos)))
                if (fgColor != AnsiColor.DEFAULT)
                    append(AnsiColor.DEFAULT.fgCode())
                if (bgColor != AnsiColor.DEFAULT)
                    append(AnsiColor.DEFAULT.bgCode())
            }
            append('\n')
        }
    }
}

private data class GridList<C : GridCell>(override val index: Long, private val cells: List<C>, private val empty: C) :
    GridLine<C> {
    private val size: Int = cells.size
    override val isEmpty: Boolean get() = cells.all { it == empty }
    override fun cell(idx: Long): C = cell(idx.toInt())
    fun cell(idx: Int): C = if (idx in 0..<size) cells[idx] else empty

    override fun toString(): String = cells.joinToString("")
}

private data class EmptyLine<C : GridCell>(override val index: Long, private val empty: C) : GridLine<C> {
    override val isEmpty: Boolean get() = true
    override fun cell(idx: Long): C = empty
}
