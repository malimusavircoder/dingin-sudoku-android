package com.dingin.sudoku.core

data class MoveSnapshot(val values: List<Int>, val notes: List<Int>)
data class Game(
    val id: String,
    val puzzle: Puzzle,
    val values: List<Int> = puzzle.givens,
    val notes: List<Int> = List(81) { 0 },
    val history: List<MoveSnapshot> = emptyList(),
    val elapsedMillis: Long = 0,
    val mistakes: Int = 0,
    val hints: Int = 0,
    val finished: Boolean = false
) {
    init {
        require(puzzle.givens.size == 81 && puzzle.solution.size == 81)
        require(values.size == 81 && values.all { it in 0..9 })
        require(notes.size == 81 && notes.all { it in 0..511 })
        require(elapsedMillis >= 0 && mistakes >= 0 && hints >= 0)
        require((0..80).all { puzzle.givens[it] == 0 || puzzle.givens[it] == values[it] })
    }
    fun locked(i: Int): Boolean = i !in 0..80 || puzzle.givens[i] != 0
    private fun push(v: List<Int>, n: List<Int>): Game = copy(values = v, notes = n,
        history = (history + MoveSnapshot(values, notes)).takeLast(100), finished = Sudoku.complete(v))
    fun input(index: Int, digit: Int, noteMode: Boolean = false): Game {
        if (finished || locked(index) || digit !in 1..9) return this
        if (noteMode) {
            if (values[index] != 0) return this
            val n = notes.toMutableList(); n[index] = n[index] xor (1 shl (digit - 1))
            return push(values, n.toList())
        }
        if (values[index] == digit) return this
        val v = values.toMutableList(); v[index] = digit
        val n = notes.toMutableList(); n[index] = 0
        // Wrong entries must not erase otherwise valid candidate notes.
        if (digit == puzzle.solution[index]) Sudoku.peers[index].forEach { n[it] = n[it] and (1 shl (digit - 1)).inv() }
        return push(v.toList(), n.toList()).copy(mistakes = mistakes + if (digit != puzzle.solution[index]) 1 else 0)
    }
    fun erase(index: Int): Game {
        if (finished || locked(index) || values[index] == 0 && notes[index] == 0) return this
        val v = values.toMutableList(); v[index] = 0
        val n = notes.toMutableList(); n[index] = 0
        return push(v.toList(), n.toList())
    }
    fun undo(): Game {
        if (finished || history.isEmpty()) return this
        val last = history.last()
        return copy(values = last.values, notes = last.notes, history = history.dropLast(1))
    }
    fun hint(selected: Int): Game {
        if (finished) return this
        val i = if (!locked(selected) && values[selected] != puzzle.solution[selected]) selected else
            (0..80).firstOrNull { !locked(it) && values[it] != puzzle.solution[it] } ?: return this
        return input(i, puzzle.solution[i]).copy(hints = hints + 1)
    }
    fun errors(): Set<Int> = (0..80).filter { values[it] != 0 && values[it] != puzzle.solution[it] }.toSet()
}

enum class ThemeMode(val title: String) { SYSTEM("Sistem"), LIGHT("Açık"), DARK("Koyu") }
data class Record(val gameId: String, val difficulty: Difficulty, val elapsedMillis: Long, val mistakes: Int, val hints: Int)
data class Profile(val revision: Long = 0, val game: Game? = null, val records: List<Record> = emptyList(),
    val theme: ThemeMode = ThemeMode.SYSTEM, val checkErrors: Boolean = true) {
    fun withGame(next: Game): Profile {
        val newRecords = if (next.finished && records.none { it.gameId == next.id })
            (records + Record(next.id, next.puzzle.difficulty, next.elapsedMillis, next.mistakes, next.hints)).takeLast(1000) else records
        return copy(revision = revision + 1, game = next, records = newRecords)
    }
}

/** Monotonic clock deltas, independent of UI ticks. The clock origin is never persisted. */
class PlayClock(private val now: () -> Long, initialMillis: Long = 0) {
    private var accumulated = initialMillis
    private var started: Long? = null
    fun resume() { if (started == null) started = now() }
    fun elapsed(): Long = accumulated + (started?.let { (now() - it).coerceAtLeast(0) } ?: 0)
    fun pause() { accumulated = elapsed(); started = null }
}
