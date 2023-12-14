package utils.grid

import utils.AnsiColor
import utils.transpose

fun filledCharCells(input: List<String>) = input.map { it.toList() }

open class FilledCharGrid(cells: List<List<Char>>, empty: Char = '.') : FilledGrid<Char>(cells, empty) {
    constructor(input: List<String>) : this(filledCharCells(input))
}

open class FilledGrid<C>(
    private val cells: List<List<C>>,
    private val empty: C,
    private val zeroBasedBounds: ZeroBasedBounds = ZeroBasedBounds(cells.size, cells[0].size)
) : AbstractGrid<C>(zeroBasedBounds) {
    val height = zeroBasedBounds.height
    val width = zeroBasedBounds.width

    init {
        check(cells.all { it.size.toLong() == width }) { "expected length ${zeroBasedBounds.width}" }
    }

    operator fun contains(p: Position) = p in bounds

    val rows get() = bounds.rowRange.asSequence().map { row(it) }
    val columns get() = bounds.columnRange.asSequence().map { column(it) }
    val nonEmptyCells
        get() = cells.asSequence().flatMapIndexed { rowIndex, row ->
            row.asSequence().mapIndexedNotNull { colIndex, c ->
                if (c == empty) null else (Position(rowIndex, colIndex) to c)
            }
        }

    override fun row(r: Long): GridLine<C> =
        if (bounds.hasRow(r)) GridList(r, cells[r.toInt()], empty) else EmptyLine(r, empty)

    override fun column(c: Long) = if (bounds.hasColumn(c)) ColumnGridLine(c.toInt()) else EmptyLine(c, empty)

    override fun cell(p: Position) = cell(p.row, p.column)
    override fun cell(r: Int, c: Int) = if (r in bounds.rowRange && c in bounds.columnRange) cells[r][c] else empty
    override fun cell(r: Long, c: Long) = cell(r.toInt(), c.toInt())

    private inner class ColumnGridLine(private val columnIndex: Int) : GridLine<C> {
        private val size: Int = zeroBasedBounds.height.toInt()
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

    override fun forEachNonEmpty(action: (Position) -> Unit) = forEachWithEmpty { if (cell(it) != empty) action(it) }
    override fun countNonEmpty(predicate: (Position) -> Boolean) = countWithEmpty { cell(it) != empty && predicate(it) }

    override fun transpose() = FilledGrid(cells.transpose(), empty)

    open fun bgColor(pos: Position): AnsiColor = AnsiColor.DEFAULT
    open fun fgColor(pos: Position): AnsiColor = AnsiColor.DEFAULT
    override fun toString() = buildString {
        bounds.rowRange.forEach { rowIndex ->
            bounds.columnRange.forEach { colIndex ->
                val pos = Position(rowIndex, colIndex)
                val fgColor = fgColor(pos)
                val bgColor = bgColor(pos)
                if (fgColor != AnsiColor.DEFAULT) append(fgColor.fgCode())
                if (bgColor != AnsiColor.DEFAULT) append(bgColor.bgCode())
                append(cellAsString(pos, cell(pos)))
                if (fgColor != AnsiColor.DEFAULT) append(AnsiColor.DEFAULT.fgCode())
                if (bgColor != AnsiColor.DEFAULT) append(AnsiColor.DEFAULT.bgCode())
            }
            append('\n')
        }
    }
}

private data class GridList<C>(override val index: Long, private val cells: List<C>, private val empty: C) :
    GridLine<C> {
    private val size: Int = cells.size
    override val isEmpty: Boolean get() = cells.all { it == empty }
    override fun cell(idx: Long): C = cell(idx.toInt())
    fun cell(idx: Int): C = if (idx in 0..<size) cells[idx] else empty

    override fun toString(): String = cells.joinToString("")
}
