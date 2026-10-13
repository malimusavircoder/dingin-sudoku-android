package com.dingin.sudoku.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** One quiet rail, with transparent action areas rather than four filled cards. */
@Composable fun ActionToolbar(
    canUndo: Boolean, canErase: Boolean, enabled: Boolean, notesActive: Boolean,
    undo: () -> Unit, erase: () -> Unit, notes: () -> Unit, hint: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface, shape = RoundedCornerShape(12.dp),
        border = BorderStroke(.6.dp, colors.outline.copy(alpha = .7f)), modifier = Modifier.fillMaxWidth().testTag("actionToolbar")) {
        Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Action("Geri al", Icons.AutoMirrored.Outlined.Undo, enabled && canUndo, Modifier.weight(1f).testTag("undo"), onClick = undo)
            VerticalDivider(Modifier.height(22.dp), color = colors.outline.copy(alpha = .45f))
            Action("Sil", Icons.AutoMirrored.Outlined.Backspace, enabled && canErase, Modifier.weight(1f).testTag("erase"), onClick = erase)
            VerticalDivider(Modifier.height(22.dp), color = colors.outline.copy(alpha = .45f))
            Action(if (notesActive) "Not açık" else "Notlar", Icons.Outlined.EditNote, enabled,
                Modifier.weight(1f).testTag("notes"), toggle = true, active = notesActive, onClick = notes)
            VerticalDivider(Modifier.height(22.dp), color = colors.outline.copy(alpha = .45f))
            Action("İpucu", Icons.Outlined.Lightbulb, enabled, Modifier.weight(1f).testTag("hint"), onClick = hint)
        }
    }
}

@Composable private fun Action(
    label: String, icon: ImageVector, enabled: Boolean, modifier: Modifier,
    toggle: Boolean = false, active: Boolean = false, onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val interaction = if (toggle) Modifier.selectable(active, enabled = enabled, role = Role.Switch, onClick = onClick)
        .semantics { stateDescription = if (active) "Açık" else "Kapalı" }
    else Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    val ink = when { !enabled -> colors.onSurfaceVariant.copy(alpha = .55f); active -> colors.primary; else -> colors.onSurface }
    Column(modifier.then(interaction).heightIn(min = 64.dp).padding(horizontal = 3.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(icon, null, Modifier.size(20.dp), tint = ink)
        Text(label, Modifier.padding(top = 4.dp), fontSize = 12.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal, color = ink)
        Spacer(Modifier.height(3.dp))
        if (active) HorizontalDivider(Modifier.width(18.dp), thickness = 2.dp, color = colors.primary)
        else Spacer(Modifier.height(2.dp))
    }
}
