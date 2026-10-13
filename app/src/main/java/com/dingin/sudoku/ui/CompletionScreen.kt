package com.dingin.sudoku.ui

import android.animation.ValueAnimator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dingin.sudoku.core.Game
import kotlin.coroutines.coroutineContext

@Composable fun CompletionScreen(game: Game, details: CompletionDetails, height: Float, enabled: Boolean, onNew: () -> Unit, onHome: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val scale = remember { Animatable(1f) }
    var celebrated by rememberSaveable(game.id) { mutableStateOf(false) }
    LaunchedEffect(game.id) {
        val motion = coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
        if (!celebrated && celebrationAllowed(ValueAnimator.areAnimatorsEnabled(), motion)) {
            celebrated = true
            scale.animateTo(1.06f, tween(140)); scale.animateTo(1f, tween(160))
        } else { celebrated = true; scale.snapTo(1f) }
    }
    Column(Modifier.fillMaxWidth().heightIn(min = height.coerceAtLeast(0f).dp).testTag("completion"),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(color = colors.primaryContainer, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.CheckCircle, null, Modifier.size(48.dp).graphicsLayer { scaleX = scale.value; scaleY = scale.value }, tint = colors.primary)
                Text("Tebrikler, tamamladın!", Modifier.padding(top = 18.dp, bottom = 8.dp), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text("Bir bulmaca, güzel bir mola.", color = colors.onPrimaryContainer, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(18.dp))
        ResultDetail("Tamamlama süresi", timeText(details.elapsedMillis), "resultTime")
        ResultDetail("Zorluk", game.puzzle.difficulty.title, "resultDifficulty")
        ResultDetail("Bu seviyede kayıtlı en iyi", details.bestMillis?.let(::timeText) ?: "Henüz kayıt yok", "resultBest")
        Text("${game.hints} ipucu · ${game.mistakes} hata", Modifier.padding(vertical = 12.dp), color = colors.onSurfaceVariant, fontSize = 13.sp)
        Button(onClick = onNew, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("resultNewGame"), shape = RoundedCornerShape(14.dp)) { Text("Yeni oyun") }
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 48.dp).testTag("resultHome"), shape = RoundedCornerShape(14.dp)) { Text("Ana menüye dön") }
    }
}

@Composable private fun ResultDetail(label: String, value: String, tag: String) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, Modifier.padding(top = 4.dp).testTag(tag), style = MaterialTheme.typography.titleLarge)
        }
    }
}
