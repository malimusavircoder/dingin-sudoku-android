package com.dingin.sudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dingin.sudoku.core.Game

@Composable fun SudokuBoard(game: Game, selected: Int, checkErrors: Boolean, onSelect: (Int) -> Unit) {
    val palette = LocalBoardPalette.current
    val errors = if (checkErrors) game.errors() else emptySet()
    Box(Modifier.widthIn(max = 450.dp).fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(palette.base).testTag("board")) {
        Column(Modifier.fillMaxSize()) {
            (0..8).forEach { r ->
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    (0..8).forEach { c ->
                        val i = r * 9 + c; val value = game.values[i]
                        val visual = cellVisual(game.values, i, selected, errors)
                        Box(Modifier.weight(1f).fillMaxHeight().background(palette.background(visual.fill)).drawBehind {
                            if (visual.selected) {
                                val inset = 3.5.dp.toPx(); val arm = 6.dp.toPx(); val stroke = 1.5.dp.toPx()
                                // Two corner marks retain selection even when the error fill has priority.
                                drawLine(palette.selectionInk, Offset(inset, inset + arm), Offset(inset, inset), stroke)
                                drawLine(palette.selectionInk, Offset(inset, inset), Offset(inset + arm, inset), stroke)
                                drawLine(palette.selectionInk, Offset(size.width - inset - arm, size.height - inset), Offset(size.width - inset, size.height - inset), stroke)
                                drawLine(palette.selectionInk, Offset(size.width - inset, size.height - inset), Offset(size.width - inset, size.height - inset - arm), stroke)
                            }
                            if (visual.matching && !visual.error) {
                                drawLine(palette.matchingInk, Offset(size.width / 2 - 4.dp.toPx(), size.height - 4.dp.toPx()),
                                    Offset(size.width / 2 + 4.dp.toPx(), size.height - 4.dp.toPx()), 1.5.dp.toPx())
                            }
                        }.clickable(enabled = !game.finished) { onSelect(i) }.testTag("cell_$i").semantics {
                            contentDescription = "Satır ${r + 1}, sütun ${c + 1}, " + (if (value == 0) "boş" else "sayı $value") +
                                (if (game.locked(i)) ", sabit" else "") + (if (visual.error) ", yanlış" else "") +
                                (if (visual.selected) ", seçili" else "") + (if (visual.matching) ", aynı rakam" else "") +
                                (if (visual.row) ", aynı satır" else "") + (if (visual.column) ", aynı sütun" else "") +
                                (if (visual.box) ", aynı blok" else "") +
                                (if (value == 0 && game.notes[i] != 0) ", notlar " + (1..9).filter { game.notes[i] and (1 shl (it - 1)) != 0 }.joinToString(", ") else "")
                            this.selected = visual.selected
                            cellFill = visual.fill.name
                        }, contentAlignment = Alignment.Center) {
                            if (value > 0) Text(value.toString(), fontSize = 22.sp,
                                fontWeight = if (game.locked(i)) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (visual.error) palette.errorInk else if (game.locked(i)) palette.given else palette.entry)
                            else if (game.notes[i] > 0) Column(Modifier.fillMaxSize().padding(2.dp)) {
                                repeat(3) { nr -> Row(Modifier.weight(1f)) { repeat(3) { nc ->
                                    val digit = nr * 3 + nc + 1
                                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                                        Text(if (game.notes[i] and (1 shl (digit - 1)) != 0) digit.toString() else "",
                                            fontSize = 9.5.sp, lineHeight = 10.5.sp, fontWeight = FontWeight.Medium, color = palette.note)
                                    }
                                } } }
                            }
                            if (visual.error) Text("!", fontSize = 10.sp, lineHeight = 11.sp, fontWeight = FontWeight.Bold, color = palette.errorInk,
                                modifier = Modifier.align(Alignment.TopEnd).padding(top = 3.dp, end = 5.dp).clearAndSetSemantics { })
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
