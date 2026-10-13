package com.dingin.sudoku.ui

import com.dingin.sudoku.core.Game
import com.dingin.sudoku.core.Profile

/** Derived views of existing schema 1 data; no history is invented or persisted. */
data class GameProgress(val givens: Int, val required: Int, val correct: Int) {
    val fraction: Float get() = if (required == 0) 1f else correct.toFloat() / required
}
fun gameProgress(game: Game): GameProgress {
    val givens = game.puzzle.givens.count { it != 0 }
    return GameProgress(givens, 81 - givens, (0..80).count {
        game.puzzle.givens[it] == 0 && game.values[it] != 0 && game.values[it] == game.puzzle.solution[it]
    })
}
data class RecordedStatistics(val completed: Int, val bestMillis: Long?, val averageMillis: Long?, val totalMillis: Long) {
    // There is no persisted started/abandoned-game denominator in schema 1.
    val successRate: Double? get() = null
}
fun recordedStatistics(profile: Profile): RecordedStatistics {
    val records = profile.records
    val currentMillis = profile.game?.takeIf { game -> records.none { it.gameId == game.id } }?.elapsedMillis ?: 0L
    return RecordedStatistics(records.size, records.minOfOrNull { it.elapsedMillis },
        records.takeIf { it.isNotEmpty() }?.map { it.elapsedMillis }?.average()?.toLong(),
        records.sumOf { it.elapsedMillis } + currentMillis)
}

/** A short viewport scrolls rather than squeezing notes into unreadable cells. */
fun boardSizeDp(width: Float, height: Float, fontScale: Float): Float =
    minOf(width, maxOf(270f, height - 320f * fontScale.coerceAtLeast(1f)))
fun keypadColumns(width: Float, fontScale: Float): Int =
    if (width >= 9 * 48 + 8 * 6 && fontScale <= 1.2f) 9 else 5


data class CompletionDetails(val elapsedMillis: Long, val bestMillis: Long?)
fun completionDetails(game: Game, profile: Profile): CompletionDetails? {
    if (!game.finished || game.values != game.puzzle.solution || !com.dingin.sudoku.core.Sudoku.complete(game.values)) return null
    return CompletionDetails(game.elapsedMillis, profile.records.filter { it.difficulty == game.puzzle.difficulty }.minOfOrNull { it.elapsedMillis })
}
fun digitMarked(game: Game, selected: Int, digit: Int, noteMode: Boolean): Boolean {
    if (selected !in 0..80 || digit !in 1..9) return false
    return if (noteMode) game.values[selected] == 0 && game.notes[selected] and (1 shl (digit - 1)) != 0 else game.values[selected] == digit
}
fun useSplitGameLayout(width: Float, height: Float, fontScale: Float): Boolean =
    width >= 680f && width > height && fontScale <= 1.3f
fun celebrationAllowed(systemAnimationsEnabled: Boolean, motionScale: Float): Boolean = systemAnimationsEnabled && motionScale > 0f
