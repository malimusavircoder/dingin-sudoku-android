package com.dingin.sudoku.core

import kotlin.random.Random

enum class Difficulty(val title: String) { EASY("Kolay"), MEDIUM("Orta"), HARD("Zor"), EXPERT("Uzman") }
data class Puzzle(val givens: List<Int>, val solution: List<Int>, val difficulty: Difficulty)

/** Independent 9x9 solver. All search state belongs to the call, never to global mutable data. */
object Sudoku {
    val units: List<List<Int>> = (0..8).map { r -> (0..8).map { r * 9 + it } } +
        (0..8).map { c -> (0..8).map { it * 9 + c } } +
        (0..8).map { b -> (0..8).map { (b / 3 * 3 + it / 3) * 9 + b % 3 * 3 + it % 3 } }
    val peers: List<Set<Int>> = (0..80).map { i -> units.filter { i in it }.flatten().toSet() - i }
    fun valid(board: List<Int>): Boolean = board.size == 81 && board.all { it in 0..9 } &&
        units.all { u -> val v = u.map { board[it] }.filter { it != 0 }; v.size == v.toSet().size }
    fun candidates(board: List<Int>, index: Int): List<Int> {
        require(board.size == 81 && index in 0..80)
        val used = peers[index].map { board[it] }.toSet()
        return (1..9).filter { it !in used }
    }
    fun isMoveValid(board: List<Int>, index: Int, digit: Int): Boolean =
        board.size == 81 && index in 0..80 && digit in 1..9 && peers[index].none { board[it] == digit }
    fun complete(board: List<Int>): Boolean = valid(board) && board.none { it == 0 }
    fun countSolutions(board: List<Int>, limit: Int = 2): Int {
        require(limit > 0)
        return search(board, limit, null).first
    }
    fun solve(board: List<Int>): List<Int>? = search(board, 1, null).second
    fun randomSolution(random: Random): List<Int> = checkNotNull(search(List(81) { 0 }, 1, random).second)
    private fun search(input: List<Int>, limit: Int, random: Random?): Pair<Int, List<Int>?> {
        if (!valid(input)) return 0 to null
        val a = input.toIntArray()
        val row = IntArray(9); val col = IntArray(9); val box = IntArray(9)
        a.forEachIndexed { i, v -> if (v > 0) { val bit = 1 shl v; row[i / 9] = row[i / 9] or bit; col[i % 9] = col[i % 9] or bit; val b = i / 27 * 3 + i % 9 / 3; box[b] = box[b] or bit } }
        var count = 0; var solution: List<Int>? = null
        fun visit() {
            if (Thread.currentThread().isInterrupted) throw InterruptedException("Sudoku search cancelled")
            var best = -1; var mask = 0; var size = 10
            for (i in 0..80) if (a[i] == 0) {
                val m = 1022 and (row[i / 9] or col[i % 9] or box[i / 27 * 3 + i % 9 / 3]).inv()
                val n = Integer.bitCount(m)
                if (n == 0) return
                if (n < size) { best = i; mask = m; size = n; if (n == 1) break }
            }
            if (best < 0) { count++; if (solution == null) solution = a.toList(); return }
            val options = (1..9).filter { mask and (1 shl it) != 0 }.let { if (random != null) it.shuffled(random) else it }
            val r = best / 9; val c = best % 9; val b = best / 27 * 3 + best % 9 / 3
            for (digit in options) {
                val bit = 1 shl digit; a[best] = digit
                row[r] = row[r] or bit; col[c] = col[c] or bit; box[b] = box[b] or bit
                visit()
                a[best] = 0; row[r] = row[r] xor bit; col[c] = col[c] xor bit; box[b] = box[b] xor bit
                if (count >= limit) return
            }
        }
        visit(); return count to solution
    }
}

/** Technique-based rating: naked singles, then hidden singles, then locked/pair reductions. */
object DifficultyRater {
    fun rate(input: List<Int>): Difficulty {
        require(Sudoku.valid(input))
        val board = input.toMutableList()
        val masks = IntArray(81) { i -> if (board[i] == 0) Sudoku.candidates(board, i).fold(0) { m, v -> m or (1 shl v) } else 0 }
        var level = Difficulty.EASY
        fun assign(i: Int, digit: Int) { board[i] = digit; masks[i] = 0; Sudoku.peers[i].forEach { p -> masks[p] = masks[p] and (1 shl digit).inv() } }
        while (board.any { it == 0 }) {
            if ((0..80).any { board[it] == 0 && masks[it] == 0 }) return Difficulty.EXPERT
            val single = (0..80).firstOrNull { board[it] == 0 && Integer.bitCount(masks[it]) == 1 }
            if (single != null) { assign(single, Integer.numberOfTrailingZeros(masks[single])); continue }
            var assigned = false
            loop@ for (unit in Sudoku.units) for (digit in 1..9) {
                val places = unit.filter { board[it] == 0 && masks[it] and (1 shl digit) != 0 }
                if (places.size == 1) { assign(places[0], digit); if (level == Difficulty.EASY) level = Difficulty.MEDIUM; assigned = true; break@loop }
            }
            if (assigned) continue
            var changed = false
            fun remove(indices: Iterable<Int>, bits: Int) { indices.forEach { i -> if (board[i] == 0) { val m = masks[i] and bits.inv(); if (m != masks[i]) { masks[i] = m; changed = true } } } }
            // A pair confined to two cells eliminates those two digits elsewhere in its unit.
            for (unit in Sudoku.units) {
                val pairs = unit.filter { board[it] == 0 && Integer.bitCount(masks[it]) == 2 }.groupBy { masks[it] }
                pairs.forEach { (m, cells) -> if (cells.size == 2) remove(unit - cells.toSet(), m) }
            }
            // A digit confined to an intersection can be removed from the rest of the other unit.
            for (a in Sudoku.units) for (digit in 1..9) {
                val places = a.filter { board[it] == 0 && masks[it] and (1 shl digit) != 0 }
                if (places.size >= 2) for (other in Sudoku.units) if (other !== a && other.containsAll(places)) remove(other - a.toSet(), 1 shl digit)
            }
            if (!changed) return Difficulty.EXPERT
            level = Difficulty.HARD
        }
        return level
    }
}

class PuzzleGenerator {
    fun generate(difficulty: Difficulty, seed: Long = System.nanoTime(), timeoutMillis: Long = 15_000): Puzzle {
        require(timeoutMillis > 0)
        val random = Random(seed)
        val deadline = System.nanoTime() + timeoutMillis * 1_000_000
        val target = when (difficulty) { Difficulty.EASY -> 40; Difficulty.MEDIUM -> 34; Difficulty.HARD -> 28; Difficulty.EXPERT -> 25 }
        repeat(160) {
            if (System.nanoTime() >= deadline || Thread.currentThread().isInterrupted) throw IllegalStateException("Bu seviyede bulmaca üretilemedi. Tekrar deneyin.")
            val solution = Sudoku.randomSolution(random)
            val board = solution.toMutableList()
            for (index in (0..80).shuffled(random)) {
                if (System.nanoTime() >= deadline || Thread.currentThread().isInterrupted) throw IllegalStateException("Bu seviyede bulmaca üretilemedi. Tekrar deneyin.")
                val prev = board[index]; board[index] = 0
                if (Sudoku.countSolutions(board) != 1) board[index] = prev
                if (board.count { it != 0 } <= target && DifficultyRater.rate(board) == difficulty) return Puzzle(board.toList(), solution, difficulty)
            }
        }
        throw IllegalStateException("Bu seviyede bulmaca üretilemedi. Tekrar deneyin.")
    }
}
