package com.dingin.sudoku.core

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.zip.CRC32
import java.nio.file.Files
import java.util.concurrent.Executors

class SudokuTest {
    private val board = "530070000600195000098000060800060003400803001700020006060000280000419005000080079".map { it.digitToInt() }
    private val solution = "534678912672195348198342567859761423426853791713924856961537284287419635345286179".map { it.digitToInt() }
    private fun game() = Game("test-game", Puzzle(board, solution, Difficulty.EASY))

    @Test fun solvesKnownPuzzleAndPreservesGivens() {
        assertEquals(solution, Sudoku.solve(board)); assertEquals(1, Sudoku.countSolutions(board))
        assertTrue(Sudoku.complete(solution)); assertFalse(Sudoku.complete(board))
    }
    @Test fun rejectsDuplicateAndOutOfRangeBoards() {
        val invalid = board.toMutableList(); invalid[2] = 5
        assertFalse(Sudoku.valid(invalid)); assertNull(Sudoku.solve(invalid)); assertEquals(0, Sudoku.countSolutions(invalid))
        assertFalse(Sudoku.valid(List(81) { 10 })); assertFalse(Sudoku.valid(List(80) { 0 }))
    }
    @Test fun detectsNoSolutionWithoutImmediateDuplicates() {
        val impossible = board.toMutableList(); impossible[2] = 1
        assertTrue(Sudoku.valid(impossible)); assertEquals(0, Sudoku.countSolutions(impossible))
    }
    @Test fun stopsCountingAtTwoForAmbiguousPuzzle() { assertEquals(2, Sudoku.countSolutions(List(81) { 0 })) }
    @Test fun checksRowsColumnsAndBoxes() {
        assertFalse(Sudoku.isMoveValid(board, 2, 5)); assertFalse(Sudoku.isMoveValid(board, 2, 8))
        assertTrue(Sudoku.isMoveValid(board, 2, 4)); assertFalse(Sudoku.isMoveValid(board, 2, 0))
        assertEquals(listOf(1, 2, 4), Sudoku.candidates(board, 2))
    }
    @Test fun generatesUniqueAccuratelyRatedPuzzlesAcrossLevelsAndSeeds() {
        for (level in Difficulty.entries) for (seed in 1L..4L) {
            val start = System.nanoTime()
            val p = PuzzleGenerator().generate(level, seed)
            assertEquals(level, DifficultyRater.rate(p.givens)); assertEquals(1, Sudoku.countSolutions(p.givens))
            assertEquals(p.solution, Sudoku.solve(p.givens)); assertTrue(Sudoku.complete(p.solution))
            assertTrue(p.givens.indices.all { p.givens[it] == 0 || p.givens[it] == p.solution[it] })
            println("Generated $level seed=$seed clues=${p.givens.count { it > 0 }} timeMs=${(System.nanoTime() - start) / 1_000_000}")
        }
    }
    @Test fun generatorIsDeterministicAndHonorsDeadline() {
        assertEquals(PuzzleGenerator().generate(Difficulty.MEDIUM, 42), PuzzleGenerator().generate(Difficulty.MEDIUM, 42))
        assertThrows(IllegalArgumentException::class.java) { PuzzleGenerator().generate(Difficulty.EASY, timeoutMillis = 0) }
        assertThrows(IllegalStateException::class.java) { PuzzleGenerator().generate(Difficulty.HARD, 1, timeoutMillis = 1) }
    }
    @Test fun lockedCellsCannotChangeAndNotesToggle() {
        val g = game(); assertEquals(g, g.input(0, 9)); assertEquals(g, g.erase(0)); assertEquals(g, g.input(-1, 2))
        val noted = g.input(2, 4, true); assertEquals(8, noted.notes[2]); assertEquals(0, noted.values[2])
        assertEquals(0, noted.input(2, 4, true).notes[2]); assertEquals(0, noted.input(2, 4).notes[2])
    }
    @Test fun undoRestoresBoardAndNotesWithoutRefundingMistakesOrHints() {
        val g = game().input(2, 4, true); val wrong = g.input(2, 1)
        assertEquals(1, wrong.mistakes); assertEquals(setOf(2), wrong.errors())
        val undone = wrong.undo(); assertEquals(g.values, undone.values); assertEquals(g.notes, undone.notes); assertEquals(1, undone.mistakes)
        val hint = undone.hint(2); assertEquals(4, hint.values[2]); assertEquals(1, hint.hints)
        assertEquals(1, hint.undo().hints)
    }
    @Test fun eraseAndCompletionAreValidated() {
        val g = game().input(2, 4); assertEquals(0, g.erase(2).values[2])
        val filled = board.indices.filter { board[it] == 0 }.fold(game()) { a, i -> a.input(i, solution[i]) }
        assertTrue(filled.finished); assertEquals(filled, filled.erase(2)); assertEquals(filled, filled.undo())
        val profile = Profile().withGame(filled).withGame(filled); assertEquals(1, profile.records.size)
    }
    @Test fun wrongValueDoesNotErasePeerNotes() {
        val g = game().input(3, 1, true).input(2, 1)
        assertEquals(1, g.notes[3]); assertEquals(1, g.mistakes)
    }
    @Test fun monotonicTimerPausesAndRestoresWithoutCountingBackground() {
        var now = 100L; val clock = PlayClock({ now }, 500)
        clock.resume(); now += 1000; assertEquals(1500L, clock.elapsed())
        clock.resume(); clock.pause(); now += 9000; assertEquals(1500L, clock.elapsed())
        clock.resume(); now += 300; assertEquals(1800L, clock.elapsed())
        val restored = PlayClock({ now }, clock.elapsed()); assertEquals(1800L, restored.elapsed())
    }
    @Test fun codecRoundTripsAllProgressSettingsAndHistory() {
        val g = game().input(2, 4, true).hint(2).copy(elapsedMillis = 8271)
        val p = Profile(4, g, listOf(Record("older", Difficulty.HARD, 1500, 2, 1)), ThemeMode.DARK, false)
        assertEquals(p, ProfileCodec.decode(ProfileCodec.encode(p)))
    }
    @Test fun codecRejectsTruncationCorruptionAndUnknownSchema() {
        val bytes = ProfileCodec.encode(Profile(1, game()))
        assertThrows(Exception::class.java) { ProfileCodec.decode(bytes.copyOf(30)) }
        bytes[20] = (bytes[20].toInt() xor 1).toByte()
        assertThrows(Exception::class.java) { ProfileCodec.decode(bytes) }
        val unknown = ProfileCodec.encode(Profile(1, game()))
        ByteBuffer.wrap(unknown).putInt(8, 99)
        val crc = CRC32().apply { update(unknown, 4, unknown.size - 12) }.value
        ByteBuffer.wrap(unknown).putLong(unknown.size - 8, crc)
        assertThrows(IllegalArgumentException::class.java) { ProfileCodec.decode(unknown) }
    }
    @Test fun savesRestoreAndIgnoreStaleRevisions() {
        val dir = Files.createTempDirectory("dingin-test").toFile()
        try {
            val file = File(dir, "profile.bin"); val store = ProfileStore(file); assertEquals(Profile(), store.load().profile)
            val p = Profile(1, game().input(2, 4, true)); store.save(p)
            val next = p.copy(revision = 2, game = p.game!!.hint(2)); store.save(next); store.save(p)
            assertEquals(next, ProfileStore(file).load().profile)
            file.writeBytes(byteArrayOf(1, 2, 3))
            val recovered = ProfileStore(file).load(); assertTrue(recovered.recovered); assertEquals(p, recovered.profile)
        } finally { dir.deleteRecursively() }
    }
    @Test fun concurrentSavesCannotOverwriteLatestRevision() {
        val dir = Files.createTempDirectory("dingin-concurrency").toFile(); val pool = Executors.newFixedThreadPool(4)
        try {
            val file = File(dir, "profile.bin"); val store = ProfileStore(file); store.load()
            val futures = (1L..50L).shuffled().map { rev -> pool.submit { store.save(Profile(rev, game().copy(elapsedMillis = rev))) } }
            futures.forEach { it.get() }; assertEquals(50L, ProfileStore(file).load().profile.revision)
        } finally { pool.shutdown(); dir.deleteRecursively() }
    }
    @Test fun ioFailureIsReportedAndDoesNotPretendToSave() {
        val dir = Files.createTempDirectory("dingin-failure").toFile()
        try {
            val blocked = File(dir, "blocked"); blocked.writeText("file, not directory")
            assertThrows(IOException::class.java) { ProfileStore(File(blocked, "save")).save(Profile(1, game())) }
        } finally { dir.deleteRecursively() }
    }
}
