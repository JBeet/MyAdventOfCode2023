package utils.grid

@JvmName("parseStringCellMap")
fun <R : Any> parseCellMap(input: List<String>, cellConstruction: (Char) -> R?): Map<Position, R> =
    input.flatMapIndexed { rowIndex, rowData ->
        rowData.mapIndexedNotNull { colIndex, cellData ->
            val cell = cellConstruction(cellData)
            if (cell == null) null else Position(rowIndex, colIndex) to cell
        }
    }.toMap()

@JvmName("parseListCellMap")
fun <T, R : Any> parseCellMap(input: List<List<T>>, cellConstruction: (T) -> R?): Map<Position, R> =
    input.flatMapIndexed { rowIndex, rowData ->
        rowData.mapIndexedNotNull { colIndex, cellData ->
            val cell = cellConstruction(cellData)
            if (cell == null) null else Position(rowIndex, colIndex) to cell
        }
    }.toMap()

fun openCharCells(input: List<String>, empty: Char = '.') = parseCellMap(input) { it.takeUnless { it == empty } }
fun boundsFrom(input: List<String>) = ZeroBasedBounds(input.size, input[0].length)

open class OpenCharGrid(cells: Map<Position, Char>, protected val zeroBasedBounds: ZeroBasedBounds, empty: Char = '.') :
    OpenGrid<Char>(cells, empty, zeroBasedBounds)

open class OpenGrid<C>(
    val cells: Map<Position, C>, private val empty: C, bounds: Bounds = DetectBounds(cells.keys)
) : AbstractGrid<C>(bounds) {
    constructor(cells: List<List<C>>, empty: C) : this(cells.flatMapIndexed { rowIndex: Int, rowData: List<C> ->
        rowData.mapIndexedNotNull { colIndex, c -> if (c == empty) null else (Position(rowIndex, colIndex) to c) }
    }.toMap(), empty, ZeroBasedBounds(cells.size, cells.maxOf { it.size }))

    constructor(cells: Map<Position, C>, empty: C, height: Int, width: Int) :
            this(cells, empty, ZeroBasedBounds(height, width))

    val rows get() = bounds.rowRange.asSequence().map { row(it) }
    val columns get() = bounds.columnRange.asSequence().map { column(it) }

    override fun row(r: Long): GridLine<C> =
        if (bounds.hasRow(r)) OpenGridLine(r, cells.filterKeys { it.row == r }.mapKeys { it.key.column }, empty)
        else EmptyLine(r, empty)

    override fun column(c: Long) =
        if (bounds.hasColumn(c)) OpenGridLine(c, cells.filterKeys { it.column == c }.mapKeys { it.key.row }, empty)
        else EmptyLine(c, empty)

    override fun cell(p: Position) = cells[p] ?: empty
    override fun cell(r: Int, c: Int) = cell(Position(r.toLong(), c.toLong()))
    override fun cell(r: Long, c: Long) = cell(Position(r, c))
    override fun findAll(predicate: (C) -> Boolean): Map<Position, C> = cells.filterValues(predicate)
    operator fun contains(p: Position) = p in bounds

    override fun forEachNonEmpty(action: (Position) -> Unit) = cells.forEach { (pos, _) -> action(pos) }
    override fun countNonEmpty(predicate: (Position) -> Boolean): Int = cells.count { (pos, _) -> predicate(pos) }

    override fun transpose() = OpenGrid(transposeCells(), empty)
    fun transposeCells(): Map<Position, C> = cells.mapKeys { (pos, _) -> pos.transpose() }

    override fun toString() = buildString {
        bounds.rowRange.forEach { rowIndex ->
            bounds.columnRange.forEach { colIndex ->
                val pos = Position(rowIndex, colIndex)
                append(cellAsString(pos, cell(pos)))
            }
            append('\n')
        }
    }
}

private data class OpenGridLine<C>(override val index: Long, private val cells: Map<Long, C>, private val empty: C) :
    GridLine<C> {
    override val isEmpty: Boolean get() = cells.all { it == empty }
    override fun cell(idx: Long): C = cells[idx] ?: empty
}
