import java.util.*

abstract class Bot(val maxPlayer: Player) {
    val minPlayer = maxPlayer.opposite

    fun heuristics(b: Board): Double {
        val cp = diskParity(b)
        val am = actualMobility(b)
        val co = cornerOccupancy(b)
        return 800.0 * co + 10 * cp + 70 * am
    }

    fun cornerOccupancy(b: Board): Int {
        var maxCorners = 0
        var minCorners = 0
        for (i in listOf(0, b.size - 1)) {
            for (j in listOf(0, b.size - 1)) {
                when (b[i, j]) {
                    Player.Black.disk -> maxCorners++
                    Player.Black.opposite.disk -> minCorners++
                }
            }
        }

        return 25 * (maxCorners - minCorners)
    }

    fun actualMobility(b: Board) = if (b.actualMobility(maxPlayer) + b.actualMobility(minPlayer) != 0) {
        100 * (b.actualMobility(maxPlayer) - b.actualMobility(minPlayer)) /
                (b.actualMobility(maxPlayer) + b.actualMobility(minPlayer))
    } else {
        0
    }

    fun diskParity(b: Board) = 100 * (b.numDisksOf(maxPlayer) - b.numDisksOf(minPlayer)) /
            (b.numDisksOf(maxPlayer) + b.numDisksOf(minPlayer))

    abstract fun getMove(board: Board): Pair<Double, Pair<Int, Int>?>
}

class AlphaBetaBot(maxPlayer: Player, val depth: Int) : Bot(maxPlayer) {
    override fun getMove(board: Board): Pair<Double, Pair<Int, Int>?> =
            alphaBeta(board, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true)

    private val c = Comparator<Pair<Double, Pair<Int, Int>?>> { a, b -> a.first.compareTo(b.first) }

    fun alphaBeta(board: Board, depth: Int, _alpha: Double, _beta: Double, isMaxPlayer: Boolean): Pair<Double, Pair<Int, Int>?> {
        var alpha = _alpha
        var beta = _beta

        if (depth == 0 || board.legalMoves.isEmpty())
            return heuristics(board) to null
        var bestMove: Pair<Double, Pair<Int, Int>?>
        if (isMaxPlayer) {
            bestMove = Double.NEGATIVE_INFINITY to null

            for (move in board.legalMoves) {
                bestMove = c.max(bestMove, alphaBeta(board.play(move)!!, depth - 1, alpha, beta, !isMaxPlayer).first to move)
                alpha = Math.max(alpha, bestMove.first)
                if (beta <= alpha) break
            }
            return bestMove
        } else {
            bestMove = Double.POSITIVE_INFINITY to null

            for (move in board.legalMoves) {
                bestMove = c.min(bestMove, alphaBeta(board.play(move)!!, depth - 1, alpha, beta, !isMaxPlayer).first to move)
                beta = Math.min(beta, bestMove.first)
                if (beta <= alpha) break
            }
            return bestMove
        }
    }
}

fun <T> Comparator<T>.min(a: T, b: T): T = when (Integer.signum(this.compare(a, b))) { 1 -> b; -1 -> a; else -> b }
fun <T> Comparator<T>.max(a: T, b: T): T = when (Integer.signum(this.compare(a, b))) { 1 -> a; -1 -> b; else -> a }