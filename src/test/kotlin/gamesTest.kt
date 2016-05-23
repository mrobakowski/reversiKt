import org.junit.Test

class Tests {
     @Test fun testGames() {
        val heuristics = listOf(
                "Zależna od ilości dysków" to ::diskAmountDependantHeuristic,
                "Kombinacja liniowa" to ::linearCombination,
                "Mobilność" to ::actualMobility,
                "Stosunek dysków" to ::diskParity
        )
        for (whitePlayerDepth in 4..5) {
            for (blackPlayerDepth in 4..5) {
                val otherHeuristics = heuristics.toMutableList()
                for ((whiteName, whitePlayerHeuristic) in heuristics) {
                    for ((blackName, blackPlayerHeuristic) in otherHeuristics) {
                        val whitePlayer = AlphaBetaBot(Player.White, whitePlayerDepth, whitePlayerHeuristic)
                        val blackPlayer = AlphaBetaBot(Player.Black, blackPlayerDepth, blackPlayerHeuristic)
                        print("$whiteName\t$whitePlayerDepth\t$blackName\t$blackPlayerDepth\t")
                        play(whitePlayer, blackPlayer)
                    }
                    otherHeuristics.remove(whiteName to whitePlayerHeuristic)
                }
            }
        }
    }

    fun test() {
        val white = AlphaBetaBot(Player.White, 5, ::actualMobility)
        val black = AlphaBetaBot(Player.Black, 5, ::diskParity)
        play(white, black)
    }

    fun play(whitePlayer: AlphaBetaBot, blackPlayer: AlphaBetaBot) {
        var passed = mutableMapOf(whitePlayer to false, blackPlayer to false)
        var board = Board(8)
        var currentPlayer = when (board.currentPlayer) {
            is Player.Black -> blackPlayer
            is Player.White -> whitePlayer
        }

        while (true) {
            val (_ignored, move) = currentPlayer.getMove(board)
            if (move != null) {
                passed[currentPlayer] = false
                board = board.play(move)!!
            } else {
                board = board.copy(currentPlayer = board.currentPlayer.opposite)
                passed[currentPlayer] = true
                if (passed.values.all { it }) {
                    gameOver(board)
                    return
                }
            }
            currentPlayer = if (currentPlayer === whitePlayer) blackPlayer else whitePlayer
        }
    }

    fun gameOver(board: Board) {
        println("${board.whitePlayerDisks}\t${board.blackPlayerDisks}")
    }
}
