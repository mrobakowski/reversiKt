import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.control.Control
import javafx.scene.layout.*
import javafx.scene.paint.Paint
import tornadofx.View
import tornadofx.center
import tornadofx.gridpane
import tornadofx.stackpane

class MainWindow : View() {
    override val root = BorderPane()

    init {
        val size = 8
        val cellSize = 50.0

        primaryStage.minHeight = size * cellSize + 39
        primaryStage.minWidth = size * cellSize + 16

        root.center {
            stackpane {
                val grid = gridpane {
                    for (row in 0..size - 1) {
                        for (column in 0..size - 1) {
                            add(StackPane().apply {
                                style = """
                                -fx-background-color: green;
                                -fx-border-color: black;
                                -fx-border-insets: 0;
                                -fx-border-width: 1;
                                -fx-border-style: solid;
                            """
                            }, row, column)
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
                    background = Background(BackgroundFill(Paint.valueOf("blue"), CornerRadii.EMPTY, Insets.EMPTY))
                }
                StackPane.setAlignment(grid, Pos.CENTER)
                background = Background(BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY))
            }
        }
    }
}