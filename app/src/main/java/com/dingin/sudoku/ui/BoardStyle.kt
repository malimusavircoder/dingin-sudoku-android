package com.dingin.sudoku.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

enum class CellFill { BASE, RELATED, MATCHING, SELECTED, ERROR }

@Immutable data class CellVisual(
    val selected: Boolean, val error: Boolean, val matching: Boolean,
    val row: Boolean, val column: Boolean, val box: Boolean
) {
    // A selected mistake keeps its red fill. Selection remains visible as green corner marks.
    val fill: CellFill get() = when {
        error -> CellFill.ERROR
        selected -> CellFill.SELECTED
        matching -> CellFill.MATCHING
        row || column || box -> CellFill.RELATED
        else -> CellFill.BASE
    }
}

/** Presentation only: it never changes a value, candidate, solution, or saved game. */
fun cellVisual(values: List<Int>, index: Int, selection: Int, errors: Set<Int>): CellVisual {
    require(values.size == 81 && index in 0..80)
    val hasSelection = selection in 0..80
    val sameCell = hasSelection && index == selection
    val selectedNumber = if (hasSelection) values[selection] else 0
    return CellVisual(
        selected = sameCell,
        error = index in errors,
        matching = selectedNumber != 0 && values[index] == selectedNumber,
        row = hasSelection && !sameCell && index / 9 == selection / 9,
        column = hasSelection && !sameCell && index % 9 == selection % 9,
        box = hasSelection && !sameCell && index / 27 == selection / 27 && index % 9 / 3 == selection % 9 / 3
    )
}

@Immutable data class BoardPalette(
    val base: Color, val related: Color, val matching: Color, val selected: Color, val errorFill: Color,
    val given: Color, val entry: Color, val note: Color, val errorInk: Color,
    val selectionInk: Color, val matchingInk: Color, val cellLine: Color, val blockLine: Color
) {
    fun background(fill: CellFill): Color = when (fill) {
        CellFill.BASE -> base; CellFill.RELATED -> related; CellFill.MATCHING -> matching
        CellFill.SELECTED -> selected; CellFill.ERROR -> errorFill
    }
    companion object {
        val Light = BoardPalette(
            Color(0xFFFEFDF9), Color(0xFFEEF3EE), Color(0xFFF4E5CA), Color(0xFFD7EAE3), Color(0xFFFBE2DC),
            Color(0xFF1D302C), Color(0xFF19594F), Color(0xFF496157), Color(0xFF9D2727),
            Color(0xFF19594F), Color(0xFF856239), Color(0xFF63796E), Color(0xFF3D6557)
        )
        val Dark = BoardPalette(
            Color(0xFF182823), Color(0xFF20362E), Color(0xFF4E4030), Color(0xFF234D42), Color(0xFF4D2829),
            Color(0xFFE9EEE6), Color(0xFF9CD5C0), Color(0xFFB6C8BA), Color(0xFFFFB4AB),
            Color(0xFF9CD5C0), Color(0xFFE4C395), Color(0xFF698773), Color(0xFF9CBFA9)
        )
    }
}

val LocalBoardPalette = staticCompositionLocalOf { BoardPalette.Light }
val CellFillKey = SemanticsPropertyKey<String>("DinginCellFill")
var SemanticsPropertyReceiver.cellFill by CellFillKey
