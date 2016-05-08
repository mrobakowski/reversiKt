import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Pane
import javafx.scene.paint.Paint
import tornadofx.stackpane
import java.util.*

data class Board private constructor(val cells: List<Cell>, val size: Int) {
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
            size
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


}

sealed class Player {
    abstract val disk: Cell

    object White : Player() {
        override val disk = Cell.WhiteDisk
    }

    object Black : Player() {
        override val disk = Cell.BlackDisk
    }
}

sealed class Cell {
    object Empty : Cell()

    object WhiteDisk : Cell()

    object BlackDisk : Cell()
}
