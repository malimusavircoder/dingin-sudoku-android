package com.dingin.sudoku.ui

import com.dingin.sudoku.core.*
import org.junit.Assert.*
import org.junit.Test
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

class PresentationTest {
    private val puzzle = PuzzleGenerator().generate(Difficulty.EASY, 123)
    private val empty = Game("current", puzzle)
    @Test fun startingDigitsNeverCountAsPlayerProgress() {
        val p = gameProgress(empty)
        assertEquals(puzzle.givens.count { it != 0 }, p.givens)
        assertEquals(81 - p.givens, p.required); assertEquals(0, p.correct); assertEquals(0f, p.fraction)
    }
    @Test fun wrongDigitsAndNotesDoNotCompleteCells() {
        val i = puzzle.givens.indexOf(0)
        assertEquals(0, gameProgress(empty.input(i, puzzle.solution[i], true)).correct)
        assertEquals(0, gameProgress(empty.input(i, puzzle.solution[i] % 9 + 1)).correct)
    }
    @Test fun correctInputAndUndoUpdateProgress() {
        val i = puzzle.givens.indexOf(0); val next = empty.input(i, puzzle.solution[i])
        assertEquals(1, gameProgress(next).correct); assertEquals(0, gameProgress(next.undo()).correct)
    }
    @Test fun hintedCellsAreIncludedAndFinishedBoardReachesOne() {
        assertEquals(1, gameProgress(empty.hint(-1)).correct)
        assertEquals(1f, gameProgress(empty.copy(values = puzzle.solution, finished = true)).fraction)
    }
    @Test fun aBoardWithoutEditableCellsDoesNotDivideByZero() {
        val p = gameProgress(Game("given", Puzzle(puzzle.solution, puzzle.solution, Difficulty.EASY), finished = true))
        assertEquals(0, p.required); assertEquals(1f, p.fraction)
    }
    private val records = listOf(Record("a", Difficulty.EASY, 60_000, 0, 0), Record("b", Difficulty.HARD, 120_000, 1, 2))
    @Test fun emptyStatisticsDoNotInventBestAverageOrSuccess() {
        val s = recordedStatistics(Profile())
        assertEquals(0, s.completed); assertNull(s.bestMillis); assertNull(s.averageMillis); assertNull(s.successRate); assertEquals(0, s.totalMillis)
    }
    @Test fun statisticsUseOnlyRetainedRecordsAndCurrentElapsedTime() {
        val s = recordedStatistics(Profile(game = empty.copy(elapsedMillis = 30_000), records = records))
        assertEquals(2, s.completed); assertEquals(60_000L, s.bestMillis); assertEquals(90_000L, s.averageMillis)
        assertEquals(210_000L, s.totalMillis); assertNull(s.successRate)
    }
    @Test fun completionIsNotDoubleCountedInTotalTime() {
        val s = recordedStatistics(Profile(game = empty.copy(id = "b", values = puzzle.solution, elapsedMillis = 120_000, finished = true), records = records))
        assertEquals(180_000L, s.totalMillis)
    }
    @Test fun layoutKeepsSquareBoardReadableAndKeyTargetsWide() {
        assertEquals(270f, boardSizeDp(328f, 450f, 1f)); assertEquals(380f, boardSizeDp(380f, 800f, 1f))
        assertEquals(5, keypadColumns(328f, 1f)); assertEquals(9, keypadColumns(508f, 1f)); assertEquals(5, keypadColumns(508f, 1.5f))
        for (width in listOf(288f, 328f, 380f, 508f)) assertTrue((width - 4 * 4) / 5 >= 48f)
    }
    private fun contrast(a: Color, b: Color): Float = (maxOf(a.luminance(), b.luminance()) + .05f) / (minOf(a.luminance(), b.luminance()) + .05f)
    @Test fun activeTextContrastMeetsFourPointFiveInBothThemes() {
        for (c in listOf(Light, Dark)) {
            for (background in listOf(c.background, c.surface, c.surfaceVariant)) {
                assertTrue(contrast(c.onSurfaceVariant, background) >= 4.5f)
                assertTrue(contrast(c.onSurface, background) >= 4.5f)
            }
            assertTrue(contrast(c.onPrimary, c.primary) >= 4.5f)
            assertTrue(contrast(c.primary, c.surface) >= 4.5f)
            assertTrue(contrast(c.onSecondaryContainer, c.secondaryContainer) >= 4.5f)
        }
    }
}
