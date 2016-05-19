import javafx.scene.layout.Pane
import tornadofx.CSSClass
import tornadofx.addClass
import tornadofx.removeClass
import tornadofx.stackpane
import java.util.*

private val directions = listOf(
        Pair<Int, Int>::up, Pair<Int, Int>::down,
        Pair<Int, Int>::left, Pair<Int, Int>::right,
        Pair<Int, Int>::upRight, Pair<Int, Int>::upLeft,
        Pair<Int, Int>::downRight, Pair<Int, Int>::downLeft
)

data class Board private constructor(val cells: List<Cell>, val size: Int, val currentPlayer: Player) {
    val legalMoves by lazy {
        val res = mutableSetOf<Pair<Int, Int>>()
        for (row in 0..size - 1) {
            for (col in 0..size - 1) {
                val pos = row to col
                if (this[row, col] == Cell.Empty &&
                        directions.any { checkDirection(it, pos) })
                    res += pos
            }
        }
        res
    }

    private fun checkDirection(dir: Pair<Int, Int>.() -> Pair<Int, Int>, pos: Pair<Int, Int>): Boolean {
        var next = pos.dir()
        val oppositeDiskColor = currentPlayer.opposite.disk
        if (next !in this || this[next] != oppositeDiskColor) return false
        while (next in this && this[next] == oppositeDiskColor) {
            next = next.dir()
        }
        return next in this && this[next] == currentPlayer.disk
    }

    operator fun get(row: Int, column: Int) = cells[column * size + row]
    operator fun get(pos: Pair<Int, Int>) = cells[pos.second * size + pos.first]

    fun isInBounds(x: Int, y: Int) = x >= 0 && x < size && y >= 0 && y < size
    fun isInBounds(pos: Pair<Int, Int>) = isInBounds(pos.first, pos.second)
    operator fun contains(pos: Pair<Int, Int>) = isInBounds(pos)

    constructor(size: Int) : this(
            {
                val len = size * size
                val res: MutableList<Cell> = (0..len - 1).mapTo(ArrayList(len)) { Cell.Empty }
                val center = size / 2
                res[center * size + center] = Cell.BlackDisk
                res[(center - 1) * size + center - 1] = Cell.BlackDisk
                res[center * size + center - 1] = Cell.WhiteDisk
                res[(center - 1) * size + center] = Cell.WhiteDisk
                res
            }(),
            size,
            Player.White
    )

    fun applyToUi(uis: List<Pane>) {
        cells.forEachIndexed { i, cell ->
            val row = i % size
            val col = i / size
            val ui = uis[i]
            ui.children.clear()
            ui.removeClass(Styles.legalCell)
            if (row to col in legalMoves) ui.addClass(Styles.legalCell)
            when (cell) {
                is Cell.WhiteDisk -> addDisk(Styles.whiteDisk, ui)
                is Cell.BlackDisk -> addDisk(Styles.blackDisk, ui)
            }
        }
    }

    private fun addDisk(s: CSSClass, ui: Pane) {
        ui.stackpane {
            addClass(s)
        }
    }

    fun play(at: Pair<Int, Int>): Board? {
        if (!canPlay(at)) return null
        val boardMut = cells.toMutableList().twoDim(size)
        boardMut[at] = currentPlayer.disk

        for (direction in directions) {
            var pos = direction(at)
            val toChange = ArrayList<Pair<Int, Int>>(8)
            toChange += pos
            while (pos in boardMut && boardMut[pos] == currentPlayer.opposite.disk) {
                toChange += pos
                pos = direction(pos)
            }
            if (pos in boardMut && boardMut[pos] == currentPlayer.disk) {
                toChange.forEach { boardMut[it] = currentPlayer.disk }
            }
        }

        return this.copy(cells = boardMut.oneDim(), currentPlayer = currentPlayer.opposite)
    }

    private fun canPlay(at: Pair<Int, Int>): Boolean {
        return at in legalMoves
    }

    val blackPlayerDisks by lazy {
        cells.count { it == Player.Black.disk }
    }

    val whitePlayerDisks by lazy {
        cells.count { it == Player.Black.opposite.disk }
    }

    fun numDisksOf(p: Player) = when (p) {
        is Player.White -> whitePlayerDisks
        is Player.Black -> blackPlayerDisks
    }

    val blackPlayerActualMobility by lazy {
        if (currentPlayer == Player.Black)
            legalMoves.size
        else
            copy(currentPlayer = Player.Black).legalMoves.size
    }

    val whitePlayerActualMobility by lazy {
        if (currentPlayer == Player.White)
            legalMoves.size
        else
            copy(currentPlayer = Player.White).legalMoves.size
    }

    fun actualMobility(p: Player) = when (p) {
        is Player.Black -> blackPlayerActualMobility
        is Player.White -> whitePlayerActualMobility
    }

}

sealed class Player {
    abstract val disk: Cell
    abstract val opposite: Player

    object White : Player() {
        override val disk = Cell.WhiteDisk
        override val opposite = Black
    }

    object Black : Player() {
        override val disk = Cell.BlackDisk
        override val opposite = White
    }
}

sealed class Cell {
    abstract val opposite: Cell?

    object Empty : Cell() {
        override val opposite = null
    }

    object WhiteDisk : Cell() {
        override val opposite = BlackDisk
    }

    object BlackDisk : Cell() {
        override val opposite = WhiteDisk
    }
}

class List2dMut<E>(val l: MutableList<E>, val s: Int) {
    operator fun get(row: Int, col: Int) = l[col * s + row]
    operator fun get(rowCol: Pair<Int, Int>) = l[rowCol.second * s + rowCol.first]
    operator fun set(row: Int, col: Int, v: E) {
        l[col * s + row] = v
    }

    operator fun set(rowCol: Pair<Int, Int>, v: E) {
        l[rowCol.second * s + rowCol.first] = v
    }

    fun isInBounds(x: Int, y: Int) = x >= 0 && x < s && y >= 0 && y < s
    fun isInBounds(pos: Pair<Int, Int>) = isInBounds(pos.first, pos.second)

    operator fun contains(pos: Pair<Int, Int>) = isInBounds(pos)

    fun oneDim() = l
}

fun <E> MutableList<E>.twoDim(size: Int) = List2dMut(this, size)

fun Pair<Int, Int>.up() = this.copy(second = second + 1)
fun Pair<Int, Int>.down() = this.copy(second = second - 1)
fun Pair<Int, Int>.right() = this.copy(first = first + 1)
fun Pair<Int, Int>.left() = this.copy(first = first - 1)

fun Pair<Int, Int>.upRight() = up().right()
fun Pair<Int, Int>.upLeft() = up().left()
fun Pair<Int, Int>.downRight() = down().right()
fun Pair<Int, Int>.downLeft() = down().left()

