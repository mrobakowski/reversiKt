import java.util.*

abstract class Bot(val maxPlayer: Player, val heuristics: (Board, Player) -> Double) {
    val minPlayer = maxPlayer.opposite

    var openedNodes = 0

    abstract fun getMove(board: Board): Pair<Double, Pair<Int, Int>?>
}

class AlphaBetaBot(maxPlayer: Player, val depth: Int, heuristics: (Board, Player) -> Double) :
        Bot(maxPlayer, heuristics) {
    override fun getMove(board: Board): Pair<Double, Pair<Int, Int>?> =
            alphaBeta(board, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, board.currentPlayer == maxPlayer)

    private val c = Comparator<Pair<Double, Pair<Int, Int>?>> { a, b -> a.first.compareTo(b.first) }

    fun alphaBeta(board: Board, depth: Int, _alpha: Double, _beta: Double, isMaxPlayer: Boolean): Pair<Double, Pair<Int, Int>?> {
        var alpha = _alpha
        var beta = _beta

        if (depth == 0 || board.legalMoves.isEmpty())
            return heuristics(board, maxPlayer) to null

        var bestMove: Pair<Double, Pair<Int, Int>?>
        if (isMaxPlayer) {
            bestMove = Double.NEGATIVE_INFINITY to null

            val sortedMoves = board.legalMoves//.sortedBy { staticBoardWeights(board.play(it)!!, maxPlayer) }
            for (move in sortedMoves) {
                openedNodes++
                bestMove = c.max(bestMove, alphaBeta(board.play(move)!!, depth - 1, alpha, beta, !isMaxPlayer).first to move)
                alpha = Math.max(alpha, bestMove.first)
                if (beta <= alpha) break
            }
            return bestMove
        } else {
            bestMove = Double.POSITIVE_INFINITY to null

            val sortedMoves = board.legalMoves//.sortedBy { staticBoardWeights(board.play(it)!!, minPlayer) }
            for (move in sortedMoves) {
                openedNodes++
                bestMove = c.min(bestMove, alphaBeta(board.play(move)!!, depth - 1, alpha, beta, !isMaxPlayer).first to move)
                beta = Math.min(beta, bestMove.first)
                if (beta <= alpha) break
            }
            return bestMove
        }
    }
}

class MiniMaxBot(maxPlayer: Player, val depth: Int, heuristics: (Board, Player) -> Double) :
        Bot(maxPlayer, heuristics) {
    override fun getMove(board: Board): Pair<Double, Pair<Int, Int>?> =
            miniMax(board, depth, board.currentPlayer == maxPlayer)

    private val c = Comparator<Pair<Double, Pair<Int, Int>?>> { a, b -> a.first.compareTo(b.first) }

    fun miniMax(board: Board, depth: Int, isMaxPlayer: Boolean): Pair<Double, Pair<Int, Int>?> {

        if (depth == 0 || board.legalMoves.isEmpty())
            return heuristics(board, maxPlayer) to null

        var bestMove: Pair<Double, Pair<Int, Int>?>
        if (isMaxPlayer) {
            bestMove = Double.NEGATIVE_INFINITY to null

            val sortedMoves = board.legalMoves//.sortedBy { staticBoardWeights(board.play(it)!!, maxPlayer) }
            for (move in sortedMoves) {
                openedNodes++
                bestMove = c.max(bestMove, miniMax(board.play(move)!!, depth - 1, !isMaxPlayer).first to move)
            }
            return bestMove
        } else {
            bestMove = Double.POSITIVE_INFINITY to null

            val sortedMoves = board.legalMoves//.sortedBy { staticBoardWeights(board.play(it)!!, minPlayer) }
            for (move in sortedMoves) {
                openedNodes++
                bestMove = c.min(bestMove, miniMax(board.play(move)!!, depth - 1, !isMaxPlayer).first to move)
            }
            return bestMove
        }
    }
}

fun staticBoardWeights(b: Board, player: Player): Int {
    var sum = 0
    for (i in 0..63) {
        if (b.cells[i] == player.disk)
            sum += boardWeights[i]
        else if (b.cells[i] == player.opposite.disk) {
            sum -= boardWeights[i]
        }
    }
    return -sum
}

private val boardWeights = intArrayOf(
        4, -3, 2, 2, 2, 2, -3, 4,
        -3, -4, -1, -1, -1, -1, -4, -3,
        2, -1, 1, 0, 0, 1, -1, 2,
        2, -1, 0, 1, 1, 0, -1, 2,
        2, -1, 0, 1, 1, 0, -1, 2,
        2, -1, 1, 0, 0, 1, -1, 2,
        -3, -4, -1, -1, -1, -1, -4, -3,
        4, -3, 2, 2, 2, 2, -3, 4
)

fun <T> Comparator<T>.min(a: T, b: T): T = when (Integer.signum(this.compare(a, b))) {
    1 -> b
    -1 -> a
    else -> b
}

fun <T> Comparator<T>.max(a: T, b: T): T = when (Integer.signum(this.compare(a, b))) {
    1 -> a
    -1 -> b
    else -> a
}