val maxPlayer = Player.Black
val minPlayer = maxPlayer.opposite

fun heuristics(b: Board): Double {
    val cp = coinParity(b)
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
                maxPlayer.disk -> maxCorners++
                minPlayer.disk -> minCorners++
            }
        }
    }

    return 25 * (maxCorners - minCorners)
}

fun actualMobility(b: Board) = if (b.maxPlayerActualMobility + b.minPlayerActualMobility != 0) {
    100 * (b.maxPlayerActualMobility - b.minPlayerActualMobility) / (b.maxPlayerActualMobility + b.minPlayerActualMobility)
} else {
    0
}

fun coinParity(b: Board) = 100 * (b.maxPlayerDisks - b.minPlayerDisks) / (b.maxPlayerDisks + b.minPlayerDisks)

fun alphabeta(board: Board, depth: Int, alpha: Double, beta: Double, isMaxPlayer: Boolean): Pair<Double, Pair<Int, Int>?> {
    var alpha = alpha
    var beta = beta
    if (depth == 0 || board.legalMoves.isEmpty())
        return heuristics(board) to null
    if (isMaxPlayer) {
        var bestMove = Double.NEGATIVE_INFINITY to null as Pair<Int, Int>?
        for (move in board.legalMoves) {
            bestMove = listOf(bestMove, alphabeta(board.play(move)!!, depth - 1, alpha, beta, false).first to move).maxBy { it.first }!!
            alpha = Math.max(alpha, bestMove.first)
            if (beta <= alpha) break
        }
        return bestMove
    } else {
        var bestMove = Double.POSITIVE_INFINITY to null as Pair<Int, Int>?
        for (child in board.legalMoves) {
            bestMove = listOf(bestMove, alphabeta(board.play(child)!!, depth - 1, alpha, beta, false).first to child).minBy { it.first }!!
            beta = Math.min(beta, bestMove.first)
            if (beta <= alpha) break
        }
        return bestMove
    }
}
