package com.dingin.sudoku

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dingin.sudoku.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.UUID

enum class Screen { HOME, GAME, STATS }
data class UiState(
    val ready: Boolean = false,
    val profile: Profile = Profile(),
    val screen: Screen = Screen.HOME,
    val difficulty: Difficulty = Difficulty.EASY,
    val selected: Int = -1,
    val noteMode: Boolean = false,
    val paused: Boolean = false,
    val generating: Boolean = false,
    val fatalError: Boolean = false,
    val message: String? = null,
    val saveError: Boolean = false,
    val savedRevision: Long = -1
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val store = ProfileStore(File(application.filesDir, "profile.bin"))
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private val saves = Channel<Profile>(Channel.UNLIMITED)
    private var clock = PlayClock(SystemClock::elapsedRealtime)
    private var foreground = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            for (snapshot in saves) {
                try {
                    store.save(snapshot)
                    _state.update { it.copy(savedRevision = maxOf(it.savedRevision, snapshot.revision), saveError = false) }
                } catch (_: Exception) {
                    _state.update { it.copy(saveError = true, message = "Oyun kaydedilemedi. Tekrar kaydet düğmesine dokunun.") }
                }
            }
        }
        load()
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (running()) checkpoint()
            }
        }
    }
    private fun load() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { store.load() }
                clock = PlayClock(SystemClock::elapsedRealtime, result.profile.game?.elapsedMillis ?: 0)
                _state.value = UiState(ready = true, profile = result.profile, savedRevision = result.profile.revision,
                    message = if (result.recovered) "Son kayıt okunamadı; önceki güvenli kayıt geri yüklendi." else null)
            } catch (_: Exception) {
                _state.update { it.copy(ready = true, fatalError = true, message = "Kayıt dosyası okunamadı. Verilerinizi korumak için yeni oyun başlatılmadı.") }
            }
        }
    }
    fun retryLoad() { if (_state.value.fatalError) load() }
    private fun running() = _state.value.ready && foreground && _state.value.screen == Screen.GAME &&
        !_state.value.paused && _state.value.profile.game?.finished == false && !_state.value.generating
    private fun syncClock() { if (running()) clock.resume() else clock.pause() }
    fun foreground(active: Boolean) { foreground = active; syncClock(); if (!active) checkpoint() }
    private fun persist(profile: Profile) { saves.trySend(profile) }
    fun retrySave() { persist(_state.value.profile) }
    private fun checkpoint() {
        val g = _state.value.profile.game ?: return
        if (g.finished || _state.value.fatalError) return
        val elapsed = clock.elapsed()
        if (elapsed != g.elapsedMillis) {
            val p = _state.value.profile.withGame(g.copy(elapsedMillis = elapsed))
            _state.update { it.copy(profile = p) }; persist(p)
        }
    }
    fun choose(difficulty: Difficulty) { _state.update { it.copy(difficulty = difficulty) } }
    fun startNew() {
        if (!_state.value.ready || _state.value.generating || _state.value.fatalError) return
        checkpoint(); clock.pause()
        val difficulty = _state.value.difficulty
        _state.update { it.copy(generating = true, message = null) }
        viewModelScope.launch {
            try {
                val puzzle = runInterruptible(Dispatchers.Default) { PuzzleGenerator().generate(difficulty) }
                val game = Game(UUID.randomUUID().toString(), puzzle)
                clock = PlayClock(SystemClock::elapsedRealtime)
                val p = _state.value.profile.withGame(game)
                _state.update { it.copy(profile = p, screen = Screen.GAME, generating = false, selected = -1, noteMode = false, paused = false) }
                persist(p); syncClock()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { _state.update { it.copy(generating = false, message = "Bulmaca hazırlanamadı. Lütfen tekrar deneyin.") }; syncClock() }
        }
    }
    fun open(screen: Screen) {
        if (!_state.value.ready || _state.value.generating) return
        checkpoint(); _state.update { it.copy(screen = screen, paused = false) }; syncClock()
    }
    fun pause() { checkpoint(); _state.update { it.copy(paused = !it.paused) }; syncClock() }
    fun select(index: Int) { if (index in 0..80 && !_state.value.paused) _state.update { it.copy(selected = index) } }
    fun toggleNotes() { _state.update { it.copy(noteMode = !it.noteMode) } }
    private fun change(action: (Game) -> Game) {
        if (!running()) return
        val before = _state.value.profile.game ?: return
        val timed = before.copy(elapsedMillis = clock.elapsed())
        val next = action(timed)
        if (next == timed) return
        val p = _state.value.profile.withGame(next)
        _state.update { it.copy(profile = p) }; persist(p); syncClock()
    }
    fun digit(number: Int) = change { it.input(_state.value.selected, number, _state.value.noteMode) }
    fun erase() = change { it.erase(_state.value.selected) }
    fun undo() = change { it.undo() }
    fun hint() = change { it.hint(_state.value.selected) }
    fun theme(mode: ThemeMode) {
        if (!_state.value.ready || _state.value.fatalError) return
        val p = _state.value.profile.copy(revision = _state.value.profile.revision + 1, theme = mode)
        _state.update { it.copy(profile = p) }; persist(p)
    }
    fun toggleErrors() {
        val p = _state.value.profile.copy(revision = _state.value.profile.revision + 1, checkErrors = !_state.value.profile.checkErrors)
        _state.update { it.copy(profile = p) }; persist(p)
    }
    fun dismissMessage() { _state.update { it.copy(message = null) } }
}
