package com.dingin.sudoku.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import com.dingin.sudoku.*
import com.dingin.sudoku.core.*

fun timeText(millis: Long): String {
    val seconds = millis / 1000
    return "%02d:%02d".format(seconds / 60, seconds % 60)
}

@Composable fun DinginApp(model: GameViewModel) {
    val state by model.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) model.foreground(true)
            if (event == Lifecycle.Event.ON_PAUSE) model.foreground(false)
        }
        lifecycle.addObserver(observer)
        model.foreground(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        onDispose { lifecycle.removeObserver(observer); model.foreground(false) }
    }
    DinginTheme(state.profile.theme) {
        val colors = MaterialTheme.colorScheme
        val view = LocalView.current
        val lightBars = colors.background.luminance() > .5f
        SideEffect {
            (view.context as? Activity)?.let { activity ->
                WindowCompat.getInsetsController(activity.window, view).apply {
                    isAppearanceLightStatusBars = lightBars
                    isAppearanceLightNavigationBars = lightBars
                }
            }
        }
        var settings by remember { mutableStateOf(false) }
        var confirmNew by remember { mutableStateOf(false) }
        BackHandler(state.screen != Screen.HOME && !state.generating) { model.open(Screen.HOME) }
        Scaffold(containerColor = colors.background, bottomBar = {
            if (state.screen != Screen.GAME && state.ready && !state.fatalError) {
                NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
                    NavigationBarItem(selected = state.screen == Screen.HOME, onClick = { model.open(Screen.HOME) },
                        icon = { Icon(Icons.Outlined.GridView, null) }, label = { Text("Oyna") }, modifier = Modifier.testTag("homeTab"))
                    NavigationBarItem(selected = state.screen == Screen.STATS, onClick = { model.open(Screen.STATS) },
                        icon = { Icon(Icons.Outlined.BarChart, null) }, label = { Text("İstatistik") }, modifier = Modifier.testTag("statsTab"))
                }
            }
        }) { padding ->
            BoxWithConstraints(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                val viewportHeight = maxHeight
                val horizontalPadding = if (maxWidth < 400.dp) 16.dp else 24.dp
                val contentWidth = minOf(maxWidth, 540.dp) - horizontalPadding * 2
                val fontScale = LocalDensity.current.fontScale
                if (!state.ready) CircularProgressIndicator(Modifier.align(Alignment.Center))
                else if (state.fatalError) {
                    Column(Modifier.padding(32.dp).align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.WarningAmber, null, Modifier.size(48.dp), tint = colors.error)
                        Text("Kayıt açılamadı", style = MaterialTheme.typography.headlineMedium)
                        Text(state.message.orEmpty(), Modifier.padding(vertical = 16.dp), textAlign = TextAlign.Center)
                        Button(onClick = model::retryLoad) { Text("Tekrar dene") }
                    }
                } else {
                    Column(Modifier.widthIn(max = 540.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = horizontalPadding)) {
                        AppHeader(state.screen, onHome = { model.open(Screen.HOME) }, onSettings = { settings = true })
                        when (state.screen) {
                            Screen.HOME -> Home(state, model, contentWidth.value, fontScale) {
                                if (state.profile.game?.finished == false) confirmNew = true else model.startNew()
                            }
                            Screen.GAME -> state.profile.game?.let { GameScreen(state, it, model, contentWidth.value, (viewportHeight - 76.dp).value, fontScale) }
                            Screen.STATS -> Statistics(state.profile)
                        }
                        if (state.message != null && !state.generating) {
                            Surface(color = colors.secondaryContainer, shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(top = 12.dp)) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(state.message!!, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                    TextButton(onClick = model::dismissMessage) { Text("Tamam") }
                                }
                            }
                        }
                        if (state.saveError) TextButton(onClick = model::retrySave, modifier = Modifier.testTag("retrySave")) { Text("Tekrar kaydet") }
                        Spacer(Modifier.height(if (state.screen == Screen.GAME) 8.dp else 24.dp))
                    }
                }
            }
        }
        if (state.generating) AlertDialog(onDismissRequest = {}, confirmButton = {},
            title = { Text("Bulmacanız hazırlanıyor") },
            text = { Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(28.dp)); Spacer(Modifier.width(16.dp)); Text("Tek çözümü kontrol ediyoruz.") } })
        if (confirmNew) AlertDialog(onDismissRequest = { confirmNew = false }, title = { Text("Yeni bir başlangıç?") },
            text = { Text("Devam eden oyununuzun yerine yeni bir bulmaca kaydedilecek.") },
            confirmButton = { TextButton(onClick = { confirmNew = false; model.startNew() }) { Text("Yeni oyun başlat") } },
            dismissButton = { TextButton(onClick = { confirmNew = false }) { Text("Vazgeç") } })
        if (settings) SettingsDialog(state, model) { settings = false }
    }
}

@Composable private fun AppHeader(screen: Screen, onHome: () -> Unit, onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = if (screen == Screen.GAME) 8.dp else 12.dp, bottom = if (screen == Screen.GAME) 12.dp else 18.dp), verticalAlignment = Alignment.CenterVertically) {
        if (screen == Screen.GAME) IconButton(onClick = onHome, modifier = Modifier.testTag("backHome")) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Ana menü") }
        else BrandMark(Modifier.size(44.dp).testTag("brandMark"))
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text("DİNGİN", style = MaterialTheme.typography.titleMedium, letterSpacing = 3.sp)
            Text("S U D O K U", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onSettings, modifier = Modifier.testTag("settings")) { Icon(Icons.Outlined.Tune, "Ayarlar") }
    }
}

@Composable private fun Home(state: UiState, model: GameViewModel, width: Float, fontScale: Float, onStart: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val compact = width < 360f || fontScale > 1.2f
    Surface(color = colors.primary, shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.background(Brush.linearGradient(listOf(colors.primary, colors.primary.copy(alpha = .85f)))).padding(if (compact) 18.dp else 22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Spa, null, tint = colors.onPrimary.copy(alpha = .8f), modifier = Modifier.size(18.dp))
                Text("ZİHNİNE İYİ GELEN BİR MOLA", Modifier.weight(1f).padding(start = 8.dp), fontSize = 11.sp, letterSpacing = .5.sp, color = colors.onPrimary)
            }
            Text("Bir mola.\nBir bulmaca.", Modifier.padding(top = 14.dp, bottom = 10.dp).testTag("homeHeroTitle"), style = MaterialTheme.typography.displaySmall.copy(fontSize = if (compact) 28.sp else 34.sp, lineHeight = if (compact) 34.sp else 40.sp), color = colors.onPrimary)
            Text("Sayıların ritmini bul.\nKendi hızında, keyifle çöz.", color = colors.onPrimary, style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = colors.onPrimary.copy(alpha = .12f), shape = RoundedCornerShape(50)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.WifiOff, null, Modifier.size(16.dp), tint = colors.onPrimary)
                        Text("  Her zaman çevrimdışı", color = colors.onPrimary, fontSize = 12.sp)
                    }
                }
                if (!compact) DecorativeGrid(colors.onPrimary)
            }
        }
    }
    val game = state.profile.game
    if (game != null && !game.finished) {
        Surface(onClick = { model.open(Screen.GAME) }, color = colors.secondaryContainer, shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp).testTag("continueGame")) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.PlayCircle, null, tint = colors.onSecondaryContainer)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text("Kaldığın yerden devam", style = MaterialTheme.typography.titleMedium)
                    Text("${game.puzzle.difficulty.title} · ${timeText(game.elapsedMillis)}", style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null)
            }
        }
    }
    Text("Bugünün temposunu seç", Modifier.padding(top = 28.dp, bottom = 6.dp), style = MaterialTheme.typography.headlineMedium)
    Text("Her bulmaca özgün. Her çözüm tek.", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(16.dp))
    Difficulty.entries.chunked(if (width < 320f || fontScale > 1.3f) 1 else 2).forEach { pair ->
        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            pair.forEach { difficulty ->
                val index = difficulty.ordinal
                val selected = state.difficulty == difficulty
                Surface(onClick = { model.choose(difficulty) }, color = if (selected) colors.primaryContainer else colors.surface,
                    border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.outline.copy(alpha = .5f)),
                    shape = RoundedCornerShape(18.dp), modifier = Modifier.weight(1f).testTag("difficulty_${difficulty.name}")) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
                                repeat(4) { bar -> Box(Modifier.width(5.dp).height((8 + bar * 3).dp).clip(RoundedCornerShape(3.dp))
                                    .background(if (bar <= index) colors.primary else colors.outline.copy(alpha = .4f))) }
                            }
                            if (selected) Icon(Icons.Outlined.CheckCircle, "Seçili", Modifier.size(19.dp), tint = colors.primary) else Spacer(Modifier.size(19.dp))
                        }
                        Text(difficulty.title, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleMedium)
                        Text(listOf("Sakin bir başlangıç", "Bir adım daha derine", "Bağlantıları bul", "Sınırlarını keşfet")[index], color = colors.onSurfaceVariant, fontSize = 11.sp)
                    }
                }
            }
        }
    }
    Button(onClick = onStart, enabled = !state.generating, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 56.dp).testTag("newGame"), shape = RoundedCornerShape(18.dp)) {
        Text("Yeni oyun", fontSize = 16.sp); Spacer(Modifier.width(12.dp)); Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(20.dp))
    }
    Text("Reklamsız. Hesapsız. Sadece Sudoku.", Modifier.fillMaxWidth().padding(top = 16.dp), textAlign = TextAlign.Center, color = colors.onSurfaceVariant, fontSize = 12.sp)
}

@Composable private fun DecorativeGrid(color: Color) {
    Canvas(Modifier.size(58.dp)) {
        val side = size.width / 3
        for (row in 0..2) for (col in 0..2) drawRoundRect(color.copy(alpha = if ((row + col) % 3 == 0) .75f else .18f),
            topLeft = Offset(col * side + 2, row * side + 2), size = androidx.compose.ui.geometry.Size(side - 4, side - 4), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f))
    }
}

@Composable private fun GameScreen(state: UiState, game: Game, model: GameViewModel, width: Float, height: Float, fontScale: Float) {
    val colors = MaterialTheme.colorScheme
    val side = boardSizeDp(minOf(width, 450f), height, fontScale).dp
    val progress = gameProgress(game)
    var hintDialog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().heightIn(min = height.coerceAtLeast(0f).dp).testTag("gameLayout"), verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(game.puzzle.difficulty.title, Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium)
                Surface(color = colors.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Timer, null, Modifier.size(16.dp), tint = colors.onSurfaceVariant)
                        Text("  ${timeText(game.elapsedMillis)}", fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("timer"))
                    }
                }
                if (!game.finished) IconButton(onClick = model::pause, modifier = Modifier.testTag("pause")) {
                    Icon(if (state.paused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause, if (state.paused) "Devam et" else "Duraklat")
                }
            }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.size(side)) {
                    if (state.paused) Surface(color = colors.surfaceVariant, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Spa, null, Modifier.size(32.dp), tint = colors.primary)
                            Text("Küçük bir mola", Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.headlineMedium)
                            Text("Süre durdu. Bulmacan seni bekliyor.", textAlign = TextAlign.Center, color = colors.onSurfaceVariant)
                            Button(onClick = model::pause, modifier = Modifier.padding(top = 16.dp).testTag("resume")) { Text("Devam et") }
                        }
                    } else SudokuBoard(game, state.selected, state.profile.checkErrors, model::select)
                }
            }
            Column(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp)) {
                LinearProgressIndicator(progress = { progress.fraction }, color = colors.primary, trackColor = colors.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().height(3.dp).testTag("gameProgress").semantics {
                        contentDescription = "Doğru doldurulan ${progress.correct} / ${progress.required} hücre; başlangıç ${progress.givens}"
                    })
                Text("Başlangıç: ${progress.givens} · Doğru doldurulan: ${progress.correct} / ${progress.required}",
                    Modifier.padding(top = 8.dp).testTag("progressCounts"), color = colors.onSurfaceVariant, fontSize = 12.sp)
                Text(if (state.saveError) "Kayıt bekliyor" else if (state.savedRevision >= state.profile.revision) "Otomatik kaydedildi" else "Kaydediliyor…",
                    color = if (state.saveError) colors.error else colors.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.testTag("saveStatus"))
            }
        }
        Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            if (game.finished) {
                Surface(color = colors.primaryContainer, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().testTag("completion")) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.EmojiEvents, null, Modifier.size(36.dp), tint = colors.primary)
                        Text("Tebrikler, tamamladın!", Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                        Text("${timeText(game.elapsedMillis)} · ${game.hints} ipucu · ${game.mistakes} hata", fontSize = 13.sp)
                        Button(onClick = { model.open(Screen.HOME) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Yeni bir bulmaca") }
                    }
                }
            } else {
                ActionToolbar(canUndo = game.history.isNotEmpty(), canErase = !game.locked(state.selected), enabled = !state.paused,
                    notesActive = state.noteMode, undo = model::undo, erase = model::erase, notes = model::toggleNotes, hint = { hintDialog = true })
                Spacer(Modifier.height(12.dp))
                (1..9).toList().chunked(keypadColumns(width, fontScale)).forEach { digits ->
                    Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (digits.size == 4) Spacer(Modifier.weight(.5f))
                        digits.forEach { n ->
                            Surface(onClick = { model.digit(n) }, enabled = !state.paused && !game.locked(state.selected), color = colors.surface,
                                shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).heightIn(min = 56.dp).testTag("digit_$n")) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
                                    Text(n.toString(), fontSize = 23.sp, fontWeight = FontWeight.Medium, color = colors.primary)
                                }
                            }
                        }
                        if (digits.size == 4) Spacer(Modifier.weight(.5f))
                    }
                }
                Text(if (state.noteMode) "Not modu açık · Adaylarını işaretle." else "Kendi hızında, bir hücre daha.",
                    Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp).testTag("gameFooter"), textAlign = TextAlign.Center, fontSize = 12.sp, color = colors.onSurfaceVariant)
            }
        }
    }
    if (hintDialog) AlertDialog(onDismissRequest = { hintDialog = false }, title = { Text("Bir adım yardım ister misin?") },
        text = { Text("Seçili hücreye doğru sayı yerleştirilir. Seçim yoksa boş veya yanlış bir hücre tamamlanır.") },
        confirmButton = { TextButton(onClick = { hintDialog = false; model.hint() }, modifier = Modifier.testTag("confirmHint")) { Text("İpucu kullan") } },
        dismissButton = { TextButton(onClick = { hintDialog = false }) { Text("Kendim deneyeyim") } })
}

@Composable private fun Statistics(profile: Profile) {
    val colors = MaterialTheme.colorScheme
    val stats = recordedStatistics(profile)
    Text("Her adım bir ilerleme.", style = MaterialTheme.typography.headlineMedium)
    Text("Cihazında kayıtlı oyunların özeti.", Modifier.padding(top = 8.dp, bottom = 18.dp), color = colors.onSurfaceVariant)
    Surface(color = colors.primaryContainer, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(stats.completed.toString(), fontSize = 40.sp, fontFamily = FontFamily.Serif, color = colors.primary, modifier = Modifier.testTag("completedCount"))
            Text("Tamamlanan bulmaca", style = MaterialTheme.typography.titleMedium)
        }
    }
    Spacer(Modifier.height(14.dp))
    StatMetric("En iyi süre", stats.bestMillis?.let(::timeText) ?: "—", "statBest")
    StatMetric("Ortalama süre", stats.averageMillis?.let(::timeText) ?: "—", "statAverage")
    StatMetric("Kayıtlı toplam oyun süresi", timeText(stats.totalMillis), "statTotal")
    StatMetric("Başarı oranı", "Hesaplanamıyor", "statSuccess")
    Text("Başlatılan ve bırakılan oyun sayısı kaydedilmediği için başarı oranı hesaplanamaz.",
        Modifier.padding(top = 2.dp, bottom = 18.dp), fontSize = 12.sp, color = colors.onSurfaceVariant)
    Difficulty.entries.forEach { d ->
        val records = profile.records.filter { it.difficulty == d }
        Surface(color = colors.surface, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("${d.title} · ${records.size} oyun", style = MaterialTheme.typography.titleMedium)
                Text(if (records.isEmpty()) "Henüz tamamlanan oyun yok." else "En iyi ${timeText(records.minOf { it.elapsedMillis })} · Ortalama ${timeText(records.map { it.elapsedMillis }.average().toLong())}",
                    Modifier.padding(top = 6.dp), fontSize = 12.sp, color = colors.onSurfaceVariant)
            }
        }
    }
    Text("Süreler son 1.000 tamamlanan oyuna ve mevcut oyun kaydına dayanır. Toplam süreye mevcut kayıt bir kez eklenir; önceki bırakılmış oyunların süreleri tutulmaz. Doğru doldurulan hücrelere ipucuyla doldurulanlar da dahildir.",
        fontSize = 12.sp, color = colors.onSurfaceVariant)
}

@Composable private fun StatMetric(label: String, value: String, tag: String) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, Modifier.padding(top = 4.dp).testTag(tag), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable private fun SettingsDialog(state: UiState, model: GameViewModel, dismiss: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text("Sana göre Sudoku") }, text = {
        Column {
            Text("Görünüm", style = MaterialTheme.typography.titleMedium)
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ThemeMode.entries.forEach { mode -> FilterChip(selected = state.profile.theme == mode, onClick = { model.theme(mode) }, label = { Text(mode.title) }, modifier = Modifier.testTag("theme_${mode.name}")) }
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Hata kontrolü", style = MaterialTheme.typography.titleMedium)
                    Text("Çözüme uymayan sayıları vurgula.", fontSize = 12.sp)
                }
                Switch(checked = state.profile.checkErrors, onCheckedChange = { model.toggleErrors() }, modifier = Modifier.testTag("checkErrors"))
            }
            Spacer(Modifier.height(18.dp))
            Text("İpucu süre cezası içermez. Arka planda süre durur. Verilerin cihazından ayrılmaz.", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton(onClick = dismiss, modifier = Modifier.testTag("closeSettings")) { Text("Tamam") } })
}
