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
    var passedLastTurn = false
    var blackPlayerDepth = 5
        set(value) {
            field = value
            if (blackPlayer is Computer) {
                val bot = (blackPlayer as Computer).bot
                when (bot) {
                    is AlphaBetaBot -> blackPlayer = Computer(AlphaBetaBot(bot.maxPlayer, value, bot.heuristics))
                }
            }
        }
    var whitePlayerDepth = 5
        set(value) {
            field = value
            if (whitePlayer is Computer) {
                val bot = (whitePlayer as Computer).bot
                when (bot) {
                    is AlphaBetaBot -> whitePlayer = Computer(AlphaBetaBot(bot.maxPlayer, value, bot.heuristics))
                }
            }
        }

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
            bot.getMove(board)
        } ui {
            println(it)
            val pos = it.second
            if (pos != null) {
                board.play(pos)?.let {
                    board = it
                    board.applyToUi(uiList)
                }
                passedLastTurn = false

                aiBusy = false
                println("${board.currentPlayer}'s turn!")
                currentPlayer.waitFor(this)
            } else {
                if (passedLastTurn) {
                    aiBusy = false
                    gameOver()
                } else {
                    println("AI passes");
                    board = board.copy(currentPlayer = board.currentPlayer.opposite)
                    passedLastTurn = true

                    aiBusy = false
                    println("${board.currentPlayer}'s turn!")
                    currentPlayer.waitFor(this)
                }
            }

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
                                if (!passedLastTurn)
                                    currentPlayer.waitFor(this@MainWindow)
                                else
                                    gameOver()
                                passedLastTurn = true
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
            root.right {
                vbox {
                    addClass(Styles.btnContainer)
                    label("White Player")
                    vbox {
                        addClass(Styles.rightVbox)
                        togglegroup {
                            radiobutton("Human") {
                                setOnAction {
                                    whitePlayer = Human
                                }
                                isSelected = true
                            }
                            radiobutton("AlphaBeta") {
                                setOnAction {
                                    whitePlayer = Computer(AlphaBetaBot(White, whitePlayerDepth, ::heuristics))
                                }

                            }
                        }
                        hbox {
                            spacing = 5.0
                            label("AI depth")
                            textfield("5") {
                                setOnKeyTyped {
                                    tryOrNull { text.toInt() }?.let { whitePlayerDepth = it }
                                }
                            }
                        }
                    }
                    label("Black Player")
                    vbox {
                        addClass(Styles.rightVbox)
                        togglegroup {
                            radiobutton("Human") {
                                setOnAction {
                                    blackPlayer = Human
                                }
                                isSelected = true
                            }
                            radiobutton("AlphaBeta") {
                                setOnAction {
                                    blackPlayer = Computer(AlphaBetaBot(Black, blackPlayerDepth, ::heuristics))
                                }
                            }
                        }
                        hbox {
                            spacing = 5.0
                            label("AI depth")
                            textfield("5") {
                                setOnKeyTyped {
                                    tryOrNull { text.toInt() }?.let { blackPlayerDepth = it }
                                }
                            }
                        }
                    }
                }
            }
        }
        uiList = cellUis.filterNotNull().toList()
        board.applyToUi(uiList)

        whitePlayer = Human
        blackPlayer = Human
    }

    private fun gameOver() {
        println("Game Over:\n\tBlack: ${board.blackPlayerDisks}\n\tWhite: ${board.whitePlayerDisks}")
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
            is Human -> {
            }
            is Computer -> {
                Platform.runLater {
                    mainWindow.aiBoardPlay()
                }
            }
        }
    }
}

inline fun <T> tryOrNull(block: () -> T): T? {
    return try {
        block()
    } catch(e: Exception) {
        null
    }
}
