package com.dingin.sudoku.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.*
import org.junit.Test

class BoardVisualTest {
    private val values = List(81) { if (it == 10 || it == 80) 4 else 0 }
    @Test fun selectedCellHasItsOwnFill() { assertEquals(CellFill.SELECTED, cellVisual(values, 10, 10, emptySet()).fill) }
    @Test fun selectedMistakeKeepsErrorAndSelectionFlags() {
        val v = cellVisual(values, 10, 10, setOf(10))
        assertEquals(CellFill.ERROR, v.fill); assertTrue(v.selected); assertTrue(v.error)
    }
    @Test fun matchingNumberHasPriorityOverRelation() {
        val v = cellVisual(values, 80, 10, emptySet())
        assertEquals(CellFill.MATCHING, v.fill); assertTrue(v.matching)
        assertEquals(CellFill.ERROR, cellVisual(values, 80, 10, setOf(80)).fill)
    }
    @Test fun rowsColumnsAndBlocksUseTheSameRelatedRole() {
        assertTrue(cellVisual(values, 12, 10, emptySet()).row)
        assertTrue(cellVisual(values, 19, 10, emptySet()).column)
        assertTrue(cellVisual(values, 0, 10, emptySet()).box)
        for (i in listOf(12, 19, 0)) assertEquals(CellFill.RELATED, cellVisual(values, i, 10, emptySet()).fill)
        assertEquals(CellFill.BASE, cellVisual(values, 40, 10, emptySet()).fill)
    }
    @Test fun blankSelectionDoesNotMarkAllBlanksAsMatching() {
        assertFalse(cellVisual(values, 40, 0, emptySet()).matching)
        assertEquals(CellFill.BASE, cellVisual(values, 40, 0, emptySet()).fill)
    }
    @Test fun absentOrOutOfRangeSelectionHasNoRelations() {
        for (selected in listOf(-1, 81, 100)) {
            val v = cellVisual(values, 10, selected, emptySet())
            assertFalse(v.selected || v.matching || v.row || v.column || v.box)
            assertEquals(CellFill.BASE, v.fill)
        }
    }
    @Test fun presentationLeavesBoardUnchanged() {
        val copy = values.toList()
        for (i in 0..80) cellVisual(values, i, 10, setOf(80))
        assertEquals(copy, values)
    }
    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance(); val lb = b.luminance()
        return (maxOf(la, lb) + .05f) / (minOf(la, lb) + .05f)
    }
    @Test fun givenEntryAndNoteInkMeetContrastInBothThemes() {
        for ((name, p) in listOf("light" to BoardPalette.Light, "dark" to BoardPalette.Dark)) {
            for (fill in CellFill.entries.filter { it != CellFill.ERROR }) {
                for ((type, ink) in listOf("given" to p.given, "entry" to p.entry, "note" to p.note)) {
                    val ratio = contrast(ink, p.background(fill))
                    assertTrue("$name $fill $type contrast=$ratio", ratio >= 4.5f)
                }
            }
            assertTrue(contrast(p.errorInk, p.errorFill) >= 4.5f)
            assertTrue(contrast(p.selectionInk, p.errorFill) >= 3f)
            assertTrue(contrast(p.blockLine, p.base) >= 3f)
            assertTrue(contrast(p.cellLine, p.base) >= 3f)
        }
    }
}
