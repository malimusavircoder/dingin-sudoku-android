package com.dingin.sudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dingin.sudoku.core.Game

@Composable fun SudokuBoard(game: Game, selected: Int, checkErrors: Boolean, onSelect: (Int) -> Unit) {
    val errors = remember(game.values, game.puzzle.solution, checkErrors) { if (checkErrors) game.errors() else emptySet() }
    val conflicts = remember(game.values, checkErrors) { if (checkErrors) conflictingCells(game.values) else emptySet() }
    // Timer/history updates do not change these grid inputs; strong skipping can retain the grid.
    SudokuGrid(game.values, game.notes, game.puzzle.givens, errors, conflicts, game.finished, selected, onSelect)
}

@Composable private fun SudokuGrid(values: List<Int>, notes: List<Int>, givens: List<Int>, errors: Set<Int>, conflicts: Set<Int>, finished: Boolean, selected: Int, onSelect: (Int) -> Unit) {
    val palette = LocalBoardPalette.current
    BoxWithConstraints(Modifier.widthIn(max = 450.dp).fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(palette.base).testTag("board")) {
        val fontScale = LocalDensity.current.fontScale
        val cellDp = maxWidth.value / 9
        val numberSp = minOf(22f, (cellDp - 4) * .8f / fontScale)
        val noteSp = minOf(9.5f, (cellDp - 4) / 3 * .9f / fontScale)
        val visuals = remember(values, selected, errors, conflicts) { List(81) { cellVisual(values, it, selected, errors, conflicts) } }
        Column(Modifier.fillMaxSize()) {
            (0..8).forEach { r ->
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    (0..8).forEach { c ->
                        val i = r * 9 + c; val value = values[i]
                        val visual = visuals[i]
                        Box(Modifier.weight(1f).fillMaxHeight().background(palette.background(visual.fill)).drawBehind {
                            if (visual.selected && value == 0 && notes[i] != 0) {
                                // A frame leaves all nine candidate glyphs clear of the selection mark.
                                val inset = 2.dp.toPx()
                                drawRect(palette.selectionInk, Offset(inset, inset), Size(size.width - 2 * inset, size.height - 2 * inset), style = Stroke(1.2.dp.toPx()))
                            } else if (visual.selected) {
                                val inset = 3.5.dp.toPx(); val arm = 6.dp.toPx(); val stroke = 1.5.dp.toPx()
                                // Two corner marks retain selection even when the error fill has priority.
                                drawLine(palette.selectionInk, Offset(inset, inset + arm), Offset(inset, inset), stroke)
                                drawLine(palette.selectionInk, Offset(inset, inset), Offset(inset + arm, inset), stroke)
                                drawLine(palette.selectionInk, Offset(size.width - inset - arm, size.height - inset), Offset(size.width - inset, size.height - inset), stroke)
                                drawLine(palette.selectionInk, Offset(size.width - inset, size.height - inset), Offset(size.width - inset, size.height - inset - arm), stroke)
                            }
                            if (visual.matching && !visual.error && !visual.conflict) {
                                drawLine(palette.matchingInk, Offset(size.width / 2 - 4.dp.toPx(), size.height - 4.dp.toPx()),
                                    Offset(size.width / 2 + 4.dp.toPx(), size.height - 4.dp.toPx()), 1.5.dp.toPx())
                            }
                        }.clickable(enabled = !finished) { onSelect(i) }.testTag("cell_$i").semantics {
                            contentDescription = "Satır ${r + 1}, sütun ${c + 1}, " + (if (value == 0) "boş" else "sayı $value") +
                                (if (givens[i] != 0) ", sabit" else "") + (if (visual.error) ", yanlış" else "") +
                                (if (visual.conflict) ", çakışan rakam" else "") + (if (visual.selected) ", seçili" else "") + (if (visual.matching) ", aynı rakam" else "") +
                                (if (visual.row) ", aynı satır" else "") + (if (visual.column) ", aynı sütun" else "") +
                                (if (visual.box) ", aynı blok" else "") +
                                (if (value == 0 && notes[i] != 0) ", notlar " + (1..9).filter { notes[i] and (1 shl (it - 1)) != 0 }.joinToString(", ") else "")
                            this.selected = visual.selected
                            cellFill = visual.fill.name
                        }, contentAlignment = Alignment.Center) {
                            if (value > 0) Text(value.toString(), fontSize = numberSp.sp, lineHeight = (numberSp * 1.15f).sp,
                                fontWeight = if (givens[i] != 0) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (visual.error || visual.conflict) palette.errorInk else if (givens[i] != 0) palette.given else palette.entry)
                            else if (notes[i] > 0) Column(Modifier.fillMaxSize().padding(2.dp)) {
                                repeat(3) { nr -> Row(Modifier.weight(1f)) { repeat(3) { nc ->
                                    val digit = nr * 3 + nc + 1
                                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                                        Text(if (notes[i] and (1 shl (digit - 1)) != 0) digit.toString() else "",
                                            fontSize = noteSp.sp, lineHeight = (noteSp * 1.05f).sp, fontWeight = FontWeight.Medium, color = palette.note)
                                    }
                                } } }
                            }
                            if (visual.error || visual.conflict) Text(if (visual.conflict) "×" else "!", fontSize = minOf(10f, cellDp * .22f / fontScale).sp, lineHeight = minOf(11f, cellDp * .24f / fontScale).sp, fontWeight = FontWeight.Bold, color = palette.errorInk,
                                modifier = Modifier.align(Alignment.TopEnd).padding(top = 2.dp, end = 3.dp).clearAndSetSemantics { })
                        }
                    }
                }
            }
        }
        Canvas(Modifier.matchParentSize()) {
            val step = size.width / 9
            for (line in 0..9) {
                val block = line % 3 == 0
                val thick = if (block) 1.7.dp.toPx() else .5.dp.toPx()
                val ink = if (block) palette.blockLine else palette.cellLine
                drawLine(ink, Offset(line * step, 0f), Offset(line * step, size.height), thick)
                drawLine(ink, Offset(0f, line * step), Offset(size.width, line * step), thick)
            }
        }
    }
}
