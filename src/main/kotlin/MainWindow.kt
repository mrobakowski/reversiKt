import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.control.Control
import javafx.scene.layout.*
import tornadofx.*

class MainWindow : View() {
    override val root = BorderPane()

    init {
        Thread.setDefaultUncaughtExceptionHandler { th, ex ->
            ex.printStackTrace()
        }
        importStylesheet(Styles::class)

        val size = 8
        val cellSize = 50.0

        primaryStage.minHeight = size * cellSize + 39
        primaryStage.minWidth = size * cellSize + 16

        val cellUis = Array<Pane?>(size * size) { null }
        var uiList: List<Pane> = listOf()

        var board = Board(size)

        root.center {
            stackpane {
                val grid = gridpane {
                    for (row in 0..size - 1) {
                        for (column in 0..size - 1) {
                            val cellUi = StackPane().apply {
                                addClass(Styles.cell)
                                setOnMouseClicked {
                                    board.play(row to column)?.let {
                                        board = it
                                        board.applyToUi(uiList)
                                    }
                                }
                            }
                            cellUis[column * size + row] = cellUi
                            add(cellUi, row, column)
                        }
                    }

                    //region constraints
                    for (i in 0..size - 1) {
                        columnConstraints += ColumnConstraints(
                                cellSize,
                                cellSize,
                                cellSize,
                                Priority.ALWAYS,
                                HPos.CENTER,
                                true
                        )
                        rowConstraints += RowConstraints(
                                cellSize,
                                cellSize,
                                cellSize,
                                Priority.ALWAYS,
                                VPos.CENTER,
                                true
                        )
                    }
                    //endregion

                    prefHeight = size * cellSize
                    prefWidth = size * cellSize
                    maxWidth = Control.USE_PREF_SIZE
                    maxHeight = Control.USE_PREF_SIZE
                    minWidth = Control.USE_PREF_SIZE
                    minHeight = Control.USE_PREF_SIZE
                }
                StackPane.setAlignment(grid, Pos.CENTER)
            }
        }
        uiList = cellUis.filterNotNull().toList()
        board.applyToUi(uiList)
    }
}