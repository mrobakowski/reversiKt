fun heuristics(b: Board, maxPlayer: Player): Double {
    val cp = diskParity(b, maxPlayer)
    val am = actualMobility(b, maxPlayer)
    val co = cornerOccupancy(b, maxPlayer)
    return 800.0 * co + 10 * cp + 70 * am
}

fun cornerOccupancy(b: Board, maxPlayer: Player): Int {
    var maxCorners = 0
    var minCorners = 0
    for (i in listOf(0, b.size - 1)) {
        for (j in listOf(0, b.size - 1)) {
            when (b[i, j]) {
                maxPlayer.disk -> maxCorners++
                maxPlayer.opposite.disk -> minCorners++
            }
        }
    }

    return 25 * (maxCorners - minCorners)
}

fun actualMobility(b: Board, maxPlayer: Player) = if (b.actualMobility(maxPlayer) + b.actualMobility(maxPlayer.opposite) != 0) {
    100 * (b.actualMobility(maxPlayer) - b.actualMobility(maxPlayer.opposite)) /
            (b.actualMobility(maxPlayer) + b.actualMobility(maxPlayer.opposite))
} else {
    0
}

fun diskParity(b: Board, maxPlayer: Player) = 100 * (b.numDisksOf(maxPlayer) - b.numDisksOf(maxPlayer.opposite)) /
        (b.numDisksOf(maxPlayer) + b.numDisksOf(maxPlayer.opposite))