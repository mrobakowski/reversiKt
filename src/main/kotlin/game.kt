import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Pane
import javafx.scene.paint.Paint
import tornadofx.stackpane
import java.util.*

data class Board private constructor(val cells: List<Cell>, val size: Int, val currentPlayer: Player) {
    val playableCells: List<Pair<Int, Int>> by lazy { throw NotImplementedError() } // TODO

    operator fun get(row: Int, column: Int) = cells[column * size + row]

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
            val ui = uis[i]
            ui.children.clear()
            when (cell) {
                is Cell.WhiteDisk -> addDisk("white", ui)
                is Cell.BlackDisk -> addDisk("black", ui)
            }
        }
    }

    private fun addDisk(s: String, ui: Pane) {
        ui.stackpane {
            background = Background(BackgroundFill(Paint.valueOf(s), CornerRadii(100.0, true), Insets(5.0)))
        }
    }

    fun play(at: Pair<Int, Int>): Board? {
        if (!canPlay(at)) return null
        val boardMut = cells.toMutableList().twoDim(size)
        boardMut[at] = currentPlayer.disk

        for (direction in listOf(
                Pair<Int, Int>::up, Pair<Int, Int>::down,
                Pair<Int, Int>::left, Pair<Int, Int>::right,
                Pair<Int, Int>::upRight, Pair<Int, Int>::upLeft,
                Pair<Int, Int>::downRight, Pair<Int, Int>::downLeft
        )) {
            var pos = direction(at)
            while (pos in boardMut && boardMut[pos] == currentPlayer.opposite.disk) {
                boardMut[pos] = currentPlayer.disk
                pos = direction(pos)
            }
        }

        return this.copy(cells = boardMut.oneDim(), currentPlayer = currentPlayer.opposite)
    }

    private fun canPlay(at: Pair<Int, Int>): Boolean {
        return at in playableCells
    }
}

sealed class Player(val disk: Cell, val opposite: Player) {
    object White : Player(Cell.WhiteDisk, Black)

    object Black : Player(Cell.BlackDisk, White)
}

sealed class Cell(val opposite: Cell) {
    object Empty : Cell(Empty)

    object WhiteDisk : Cell(BlackDisk)

    object BlackDisk : Cell(WhiteDisk)
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

