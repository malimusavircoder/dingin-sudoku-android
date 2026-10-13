package com.dingin.sudoku

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.dingin.sudoku.core.*
import com.dingin.sudoku.ui.CellFillKey
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.graphics.toPixelMap
import org.junit.*
import org.junit.Assert.*
import java.io.File

class AppTest {
    @get:Rule val compose = createEmptyComposeRule()
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var file: File
    private lateinit var puzzle: Puzzle
    @Before fun setup() {
        file = File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "profile.bin")
        file.delete(); File(file.path + ".bak").delete(); File(file.path + ".tmp").delete()
        puzzle = PuzzleGenerator().generate(Difficulty.EASY, 123)
        ProfileStore(file).save(Profile(1, Game("ui-fixture", puzzle)))
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("continueGame").fetchSemanticsNodes().isNotEmpty() }
    }
    @After fun teardown() { if (::scenario.isInitialized) scenario.close() }
    private fun continueGame() { compose.onNodeWithTag("continueGame").performScrollTo().performClick(); compose.onNodeWithTag("board").assertExists() }
    private fun cell(): Int = puzzle.givens.indexOfFirst { it == 0 }
    private fun saved(): Profile = ProfileCodec.decode(file.readBytes())
    private fun waitSave(check: (Profile) -> Boolean) { compose.waitUntil(10_000) { runCatching { check(saved()) }.getOrDefault(false) } }

    @Test fun notesUndoEraseHintsAndSave() {
        continueGame(); val i = cell(); val digit = puzzle.solution[i]
        compose.onNodeWithTag("cell_$i").performClick()
        compose.onNodeWithTag("notes").performScrollTo().performClick()
        compose.onNodeWithTag("digit_$digit").performScrollTo().performClick()
        waitSave { it.game!!.notes[i] == 1 shl (digit - 1) }
        compose.onNodeWithTag("notes").performClick()
        compose.onNodeWithTag("digit_$digit").performClick()
        waitSave { it.game!!.values[i] == digit && it.game!!.notes[i] == 0 }
        compose.onNodeWithTag("undo").performClick()
        waitSave { it.game!!.values[i] == 0 && it.game!!.notes[i] != 0 }
        compose.onNodeWithTag("erase").performClick()
        waitSave { it.game!!.notes[i] == 0 }
        compose.onNodeWithTag("hint").performClick()
        compose.onNodeWithTag("confirmHint").performClick()
        waitSave { it.game!!.values[i] == digit && it.game!!.hints == 1 }
        scenario.recreate()
        // Configuration recreation retains the ViewModel and the current game screen.
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("board").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(digit, saved().game!!.values[i]); assertEquals(1, saved().game!!.hints)
    }
    @Test fun errorsThemePauseAndBackground() {
        continueGame(); val i = cell(); val wrong = puzzle.solution[i] % 9 + 1
        compose.onNodeWithTag("cell_$i").performClick()
        compose.onNodeWithTag("digit_$wrong").performScrollTo().performClick()
        waitSave { it.game!!.mistakes == 1 }
        compose.onNodeWithTag("cell_$i").assert(SemanticsMatcher("Hata açıklaması") { node -> node.config.getOrNull(SemanticsProperties.ContentDescription)?.any { it.contains("Satır ${i / 9 + 1}, sütun ${i % 9 + 1}, sayı $wrong, yanlış") } == true })
        compose.onNodeWithTag("settings").performScrollTo().performClick()
        compose.onNodeWithTag("theme_DARK").performClick()
        compose.onNodeWithTag("checkErrors").performClick()
        compose.onNodeWithTag("closeSettings").performClick()
        waitSave { it.theme == ThemeMode.DARK && !it.checkErrors }
        compose.onNodeWithTag("pause").performClick()
        compose.onNodeWithTag("resume").assertExists()
        compose.onNodeWithTag("board").assertDoesNotExist()
        waitSave { it.game!!.elapsedMillis > 0 }
        val pausedAt = saved().game!!.elapsedMillis
        Thread.sleep(1300); assertEquals(pausedAt, saved().game!!.elapsedMillis)
        compose.onNodeWithTag("resume").performClick()
        scenario.moveToState(Lifecycle.State.CREATED)
        val backgroundAt = saved().game!!.elapsedMillis
        Thread.sleep(1300); assertEquals(backgroundAt, saved().game!!.elapsedMillis)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.recreate()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("board").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(ThemeMode.DARK, saved().theme)
    }
    @Test fun newPuzzleAndDifficultySelection() {
        compose.onNodeWithTag("difficulty_EXPERT").performScrollTo().performClick()
        compose.onNodeWithTag("newGame").performScrollTo().performClick()
        compose.onNodeWithText("Yeni oyun başlat").performClick()
        compose.waitUntil(25_000) { compose.onAllNodesWithTag("board").fetchSemanticsNodes().isNotEmpty() }
        waitSave { it.game!!.id != "ui-fixture" && it.game!!.puzzle.difficulty == Difficulty.EXPERT }
        assertEquals(1, Sudoku.countSolutions(saved().game!!.puzzle.givens))
        compose.onNodeWithTag("backHome").performClick()
        compose.onNodeWithTag("continueGame").assertExists()
    }
    @Test fun completionRecordsStatisticsOnce() {
        scenario.close()
        val givens = puzzle.solution.toMutableList(); givens[0] = 0
        val almost = Puzzle(givens, puzzle.solution, Difficulty.EASY)
        ProfileStore(file).save(Profile(100, Game("finish-fixture", almost)))
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("continueGame").fetchSemanticsNodes().isNotEmpty() }
        continueGame(); compose.onNodeWithTag("cell_0").performClick()
        compose.onNodeWithTag("digit_${puzzle.solution[0]}").performScrollTo().performClick()
        compose.onNodeWithTag("completion").assertExists()
        waitSave { it.game!!.finished && it.records.size == 1 }
        compose.onNodeWithTag("backHome").performScrollTo().performClick()
        compose.onNodeWithTag("statsTab").performClick()
        compose.onNodeWithTag("completedCount").assertTextEquals("1")
        scenario.recreate(); assertEquals(1, saved().records.size)
    }
    @Test fun boardStatesAndToolbarRemainConsistentInBothThemes() {
        continueGame()
        val i = cell(); val wrong = puzzle.solution[i] % 9 + 1
        compose.onNodeWithTag("cell_$i").performClick()
        compose.onNodeWithTag("digit_$wrong").performScrollTo().performClick()
        waitSave { it.game!!.values[i] == wrong }
        for (theme in listOf(ThemeMode.LIGHT, ThemeMode.DARK)) {
            compose.onNodeWithTag("settings").performScrollTo().performClick()
            compose.onNodeWithTag("theme_${theme.name}").performClick()
            compose.onNodeWithTag("closeSettings").performClick()
            compose.onNodeWithTag("cell_$i").assertIsSelected().assert(SemanticsMatcher.expectValue(CellFillKey, "ERROR"))
            compose.onNodeWithTag("cell_$i").assert(SemanticsMatcher("Hata açıklaması") { node -> node.config.getOrNull(SemanticsProperties.ContentDescription)?.any { it.contains("Satır ${i / 9 + 1}, sütun ${i % 9 + 1}, sayı $wrong, yanlış") } == true })
            compose.onNodeWithTag("actionToolbar").assertExists()
        }
        compose.onNodeWithTag("notes").performScrollTo().performClick()
        compose.onNodeWithTag("notes").assertIsSelected().assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Açık"))
        compose.onNodeWithTag("notes").performClick()
        compose.onNodeWithTag("notes").assertIsNotSelected()
        compose.onNodeWithTag("undo").assertIsEnabled()
        compose.onNodeWithTag("erase").assertIsEnabled()
        compose.onNodeWithTag("hint").assertIsEnabled()
    }
    @Test fun progressStartsAtZeroAndCountsOnlyCorrectEditableCells() {
        continueGame()
        val initial = "Başlangıç: ${puzzle.givens.count { it != 0 }} · Doğru doldurulan: 0 / ${puzzle.givens.count { it == 0 }}"
        compose.onNodeWithTag("progressCounts").assertTextEquals(initial)
        val i = cell(); val wrong = puzzle.solution[i] % 9 + 1
        compose.onNodeWithTag("cell_$i").performClick()
        compose.onNodeWithTag("digit_$wrong").performScrollTo().performClick()
        waitSave { it.game!!.values[i] == wrong }
        compose.onNodeWithTag("progressCounts").assertTextEquals(initial)
        compose.onNodeWithTag("hint").performScrollTo().performClick()
        compose.onNodeWithTag("confirmHint").performClick()
        waitSave { it.game!!.values[i] == puzzle.solution[i] }
        compose.onNodeWithTag("progressCounts").assertTextEquals(initial.replace("doldurulan: 0", "doldurulan: 1"))
    }
    @Test fun statisticsShowActualTimesWithoutInventingSuccessRate() {
        scenario.close()
        ProfileStore(file).save(Profile(200, Game("ui-fixture", puzzle, elapsedMillis = 30_000),
            listOf(Record("a", Difficulty.EASY, 60_000, 0, 0), Record("b", Difficulty.HARD, 120_000, 1, 2))))
        scenario = ActivityScenario.launch(MainActivity::class.java)
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("statsTab").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("statsTab").performClick()
        compose.onNodeWithTag("statBest").performScrollTo().assertTextEquals("01:00")
        compose.onNodeWithTag("statAverage").performScrollTo().assertTextEquals("01:30")
        compose.onNodeWithTag("statTotal").performScrollTo().assertTextEquals("03:30")
        compose.onNodeWithTag("statSuccess").performScrollTo().assertTextEquals("Hesaplanamıyor")
    }

    @Test fun brandInkRemainsVisibleAfterThemeChanges() {
        for (theme in listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.LIGHT)) {
            compose.onNodeWithTag("settings").performClick()
            compose.onNodeWithTag("theme_${theme.name}").performClick()
            compose.onNodeWithTag("closeSettings").performClick()
            compose.waitForIdle()
            val pixels = compose.onNodeWithTag("brandMark").captureToImage().toPixelMap()
            var cream = 0
            for (x in pixels.width / 5 until pixels.width * 4 / 5) for (y in pixels.height / 5 until pixels.height * 4 / 5) {
                val c = pixels[x, y]
                if (c.red > .8f && c.green > .8f && c.blue > .6f) cream++
            }
            assertTrue("Monogram missing in $theme", cream > 10)
        }
    }

}
