val maxPlayer = Player.White
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

fun alphabeta(node: Board, depth: Int, alpha: Double, beta: Double, isMaxPlayer: Boolean): Double {
    var alpha = alpha
    var beta = beta
    if (depth == 0 || node.legalMoves.isEmpty())
        return heuristics(node)
    if (isMaxPlayer) {
        var v = Double.NEGATIVE_INFINITY
        for (child in node.legalMoves) {
            v = Math.max(v, alphabeta(node.play(child)!!, depth - 1, alpha, beta, false))
            alpha = Math.max(alpha, v)
            if (beta <= alpha) break
        }
        return v
    } else {
        var v = Double.POSITIVE_INFINITY
        for (child in node.legalMoves) {
            v = Math.min(v, alphabeta(node.play(child)!!, depth - 1, alpha, beta, true))
            beta = Math.min(beta, v)
            if (beta < alpha) break
        }
        return v
    }
}
