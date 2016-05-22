
import GamePlayer.Computer
import GamePlayer.Human
import Player.Black
import Player.White
import javafx.application.Platform
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.control.Control
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
                currentPlayer.waitFor(this)
            }
        }
    }

    fun aiBoardPlay() {
        aiBusy = true
        val bot = (currentPlayer as? Computer)?.bot ?: throw IllegalStateException("current player isn't a bot")
        runAsync {
            Thread.sleep(200)
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
            println("${board.currentPlayer}'s turn!")
            currentPlayer.waitFor(this)
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
                            if (canHumanPlay) {
                                board = board.copy(currentPlayer = board.currentPlayer.opposite)
                                currentPlayer.waitFor(this@MainWindow)
                            }
                        }
                    }
                    button("Reset") {
                        addClass(Styles.btn)
                        setOnAction {
                            reset()
                        }
                    }
                    button("Start AI") {
                        addClass(Styles.btn)
                        setOnAction {
                            currentPlayer.waitFor(this@MainWindow)
                        }
                    }
                }
            }
        }
        uiList = cellUis.filterNotNull().toList()
        board.applyToUi(uiList)

        whitePlayer = Computer(AlphaBetaBot(White, 5))
        blackPlayer = Computer(AlphaBetaBot(Black, 3))
//        blackPlayer = Human
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

    fun waitFor(mainWindow: MainWindow) {
        when (this) {
            is Human -> {}
            is Computer -> {
                Platform.runLater {
                    mainWindow.aiBoardPlay()
                }
            }
        }
    }
}
