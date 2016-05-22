import GamePlayer.Computer
import GamePlayer.Human
import Player.Black
import Player.White
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.control.Control
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import tornadofx.*

class MainWindow : View() {
    override val root = BorderPane()
    var board: Board

    var blackPlayer: GamePlayer
    var whitePlayer: GamePlayer
    var aiBusy = false
    var uiList: List<Pane>
    val size = 8

    val currentPlayer: GamePlayer
        get() = when (board.currentPlayer) {
            is Black -> blackPlayer
            is White -> whitePlayer
        }

    val canHumanPlay: Boolean
        get() = !aiBusy && currentPlayer is Human

    fun humanBoardPlay(row: Int, column: Int) {
        if (canHumanPlay) {
            board.play(row to column)?.let {
                board = it
                board.applyToUi(uiList)
                println("${board.currentPlayer}'s turn!")
//                aiBoardPlay()
            }
        }
    }

    fun aiBoardPlay() {
        aiBusy = true
        val bot = (currentPlayer as? Computer)?.bot ?: throw IllegalStateException("current player isn't a bot")
        runAsync {
            bot.getMove(board)
        } ui {
            println(it)
            val pos = it.second
            if (pos != null) {
                board.play(pos)?.let {
                    board = it
                    board.applyToUi(uiList)
                }
            } else {
                println("AI passes");
                board = board.copy(currentPlayer = board.currentPlayer.opposite)
            }
            aiBusy = false
            println("Player's turn!")
        }
    }

    init {
        Thread.setDefaultUncaughtExceptionHandler { th, ex ->
            ex.printStackTrace()
            System.exit(1)
        }
        importStylesheet(Styles::class)

        val cellSize = 50.0

        primaryStage.minHeight = size * cellSize + 39
        primaryStage.minWidth = size * cellSize + 16

        val cellUis = Array<Pane?>(size * size) { null }

        board = Board(size)

//        val playerBoardPlay = { row: Int, column: Int ->
//            { ev: MouseEvent ->
//                if (canHumanPlay) {
//                    board.play(row to column)?.let {
//                        board = it
//                        board.applyToUi(uiList)
//                        println("AI's turn!")
//                        aiBoardPlay()
//                    }
//                }
//            }
//        }

        root.center {
            stackpane {
                val grid = gridpane {
                    for (row in 0..size - 1) {
                        for (column in 0..size - 1) {
                            val cellUi = StackPane().apply {
                                addClass(Styles.cell)
                                setOnMouseClicked {
                                    humanBoardPlay(row, column)
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
                    //region sizes
                    prefHeight = size * cellSize
                    prefWidth = size * cellSize
                    maxWidth = Control.USE_PREF_SIZE
                    maxHeight = Control.USE_PREF_SIZE
                    minWidth = Control.USE_PREF_SIZE
                    minHeight = Control.USE_PREF_SIZE
                    //endregion
                }
                StackPane.setAlignment(grid, Pos.CENTER)
            }
            root.bottom {
                hbox {
                    addClass(Styles.btnContainer)
                    button("Pass") {
                        addClass(Styles.btn)
                        setOnAction {
                            if (!aiBusy) {
                                board = board.copy(currentPlayer = board.currentPlayer.opposite)
                                aiBoardPlay()
                            }
                        }
                    }
                    button("Reset") {
                        addClass(Styles.btn)
                        setOnAction {
                            reset()
                        }
                    }
                }
            }
        }
        uiList = cellUis.filterNotNull().toList()
        board.applyToUi(uiList)
    }

    private fun reset() {
        if (!aiBusy) {
            board = Board(size)
            board.applyToUi(uiList)
        }
    }
}

sealed class GamePlayer {
    object Human : GamePlayer()
    class Computer(val bot: Bot) : GamePlayer()
}
