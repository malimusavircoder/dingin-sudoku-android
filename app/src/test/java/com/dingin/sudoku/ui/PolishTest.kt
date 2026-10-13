package com.dingin.sudoku.ui

import com.dingin.sudoku.core.*
import org.junit.Test
import org.junit.Assert.*
import androidx.compose.ui.graphics.luminance

class PolishTest {
    private fun board(vararg cells: Pair<Int, Int>): List<Int> = MutableList(81) { 0 }.also { b -> cells.forEach { b[it.first] = it.second } }
    @Test fun rowClashesMarkBothEnds() { assertEquals(setOf(0, 8), conflictingCells(board(0 to 4, 8 to 4))) }
    @Test fun columnClashesMarkBothEnds() { assertEquals(setOf(0, 72), conflictingCells(board(0 to 4, 72 to 4))) }
    @Test fun boxClashesMarkBothEnds() { assertEquals(setOf(0, 20), conflictingCells(board(0 to 4, 20 to 4))) }
    @Test fun unrelatedEqualNumbersAndBlankCellsNeverClash() { assertTrue(conflictingCells(board(0 to 4, 40 to 4)).isEmpty()); assertTrue(conflictingCells(List(81) { 0 }).isEmpty()) }
    @Test fun selectedConflictKeepsSelectionAndErrorFill() {
        val v = cellVisual(board(0 to 4, 8 to 4), 0, 0, emptySet(), setOf(0, 8))
        assertEquals(CellFill.ERROR, v.fill); assertTrue(v.selected && v.conflict && v.matching); assertFalse(v.error)
    }
    private val puzzle = PuzzleGenerator().generate(Difficulty.EASY, 123)
    @Test fun selectedNumberAndMultipleNotesAreMarkedWithoutChangingGame() {
        val i = puzzle.givens.indexOf(0); val g = Game("a", puzzle).input(i, 3, true).input(i, 7, true)
        assertTrue(digitMarked(g, i, 3, true)); assertTrue(digitMarked(g, i, 7, true)); assertFalse(digitMarked(g, i, 2, true)); assertFalse(digitMarked(g, -1, 3, true))
        val entered = g.input(i, puzzle.solution[i]); assertTrue(digitMarked(entered, i, puzzle.solution[i], false)); assertEquals(0, entered.notes[i]); assertFalse(digitMarked(entered, i, puzzle.solution[i], true))
    }
    @Test fun falseFinishedFlagNeverProducesAResult() {
        val g = Game("a", puzzle, finished = true)
        assertNull(completionDetails(g, Profile()))
        assertNull(completionDetails(g.copy(values = puzzle.solution, finished = false), Profile()))
    }
    @Test fun bestResultUsesOnlyRealRecordsOfTheSameDifficulty() {
        val g = Game("a", puzzle, values = puzzle.solution, elapsedMillis = 90_000, finished = true)
        assertNull(completionDetails(g, Profile())!!.bestMillis)
        val p = Profile(records = listOf(Record("old", Difficulty.EASY, 60_000, 0, 0), Record("other", Difficulty.HARD, 10_000, 0, 0)))
        assertEquals(60_000L, completionDetails(g, p)!!.bestMillis); assertEquals(90_000L, completionDetails(g, p)!!.elapsedMillis)
    }
    @Test fun reducedMotionAndPortraitLayoutsAreExplicit() {
        assertFalse(celebrationAllowed(false, 1f)); assertFalse(celebrationAllowed(true, 0f)); assertTrue(celebrationAllowed(true, 1f))
        assertTrue(useSplitGameLayout(866f, 360f, 1f)); assertFalse(useSplitGameLayout(360f, 720f, 1f)); assertFalse(useSplitGameLayout(866f, 360f, 1.5f))
    }
    @Test fun completionTextContrastPassesBothThemes() {
        for (c in listOf(Light, Dark)) {
            val a = c.onPrimaryContainer.luminance(); val b = c.primaryContainer.luminance()
            assertTrue((maxOf(a, b) + .05f) / (minOf(a, b) + .05f) >= 4.5f)
        }
    }
}
