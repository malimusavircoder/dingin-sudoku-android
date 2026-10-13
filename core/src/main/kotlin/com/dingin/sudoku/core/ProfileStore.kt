package com.dingin.sudoku.core

import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.CRC32

/** One versioned document commits game, statistics and settings in a single atomic replace. */
object ProfileCodec {
    private const val MAGIC = 0x44494E47
    fun encode(p: Profile): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            out.writeInt(MAGIC); out.writeInt(1); out.writeLong(p.revision)
            out.writeInt(p.theme.ordinal); out.writeBoolean(p.checkErrors); out.writeBoolean(p.game != null)
            p.game?.let { g ->
                out.writeUTF(g.id); out.writeInt(g.puzzle.difficulty.ordinal)
                listOf(g.puzzle.givens, g.puzzle.solution, g.values, g.notes).forEach { a -> a.forEach(out::writeInt) }
                out.writeLong(g.elapsedMillis); out.writeInt(g.mistakes); out.writeInt(g.hints); out.writeBoolean(g.finished)
                out.writeInt(g.history.size); g.history.forEach { h -> h.values.forEach(out::writeInt); h.notes.forEach(out::writeInt) }
            }
            out.writeInt(p.records.size); p.records.forEach { rec ->
                out.writeUTF(rec.gameId); out.writeInt(rec.difficulty.ordinal); out.writeLong(rec.elapsedMillis); out.writeInt(rec.mistakes); out.writeInt(rec.hints)
            }
        }
        val payload = bytes.toByteArray()
        return ByteArrayOutputStream().also { b -> DataOutputStream(b).use { it.writeInt(payload.size); it.write(payload); it.writeLong(CRC32().apply { update(payload) }.value) } }.toByteArray()
    }
    fun decode(bytes: ByteArray): Profile = DataInputStream(ByteArrayInputStream(bytes)).use { frame ->
        val size = frame.readInt(); require(size in 0..1_000_000 && size == bytes.size - 12) { "Invalid document size" }
        val payload = ByteArray(size); frame.readFully(payload)
        require(frame.readLong() == CRC32().apply { update(payload) }.value) { "Checksum mismatch" }
        DataInputStream(ByteArrayInputStream(payload)).use { i ->
            require(i.readInt() == MAGIC && i.readInt() == 1) { "Unsupported save schema" }
            val revision = i.readLong(); require(revision >= 0)
            val theme = ThemeMode.entries[i.readInt()]; val errors = i.readBoolean()
            val game = if (i.readBoolean()) {
                val id = i.readUTF(); val difficulty = Difficulty.entries[i.readInt()]
                fun array() = List(81) { i.readInt() }
                val givens = array(); val solution = array(); val values = array(); val notes = array()
                require(Sudoku.valid(givens) && Sudoku.complete(solution) && givens.indices.all { givens[it] == 0 || givens[it] == solution[it] })
                val elapsed = i.readLong(); val mistakes = i.readInt(); val hints = i.readInt(); val done = i.readBoolean()
                val count = i.readInt(); require(count in 0..100)
                val history = List(count) {
                    val v = array(); val n = array()
                    require(v.all { it in 0..9 } && n.all { it in 0..511 } && givens.indices.all { givens[it] == 0 || givens[it] == v[it] })
                    MoveSnapshot(v, n)
                }
                require(done == Sudoku.complete(values))
                Game(id, Puzzle(givens, solution, difficulty), values, notes, history, elapsed, mistakes, hints, done)
            } else null
            val count = i.readInt(); require(count in 0..1000)
            val records = List(count) {
                Record(i.readUTF(), Difficulty.entries[i.readInt()], i.readLong(), i.readInt(), i.readInt()).also {
                    require(it.elapsedMillis >= 0 && it.mistakes >= 0 && it.hints >= 0)
                }
            }
            require(i.available() == 0 && records.map { it.gameId }.distinct().size == records.size)
            Profile(revision, game, records, theme, errors)
        }
    }
}

data class LoadResult(val profile: Profile, val recovered: Boolean = false)
class ProfileStore(private val file: File) {
    private var lastRevision = -1L
    private val backup = File(file.path + ".bak")
    @Synchronized fun load(): LoadResult {
        if (!file.exists() && !backup.exists()) return LoadResult(Profile()).also { lastRevision = 0 }
        try { return LoadResult(ProfileCodec.decode(file.readBytes())).also { lastRevision = it.profile.revision } }
        catch (first: Exception) {
            try { return LoadResult(ProfileCodec.decode(backup.readBytes()), recovered = true).also { lastRevision = it.profile.revision } }
            catch (second: Exception) { throw IOException("Kayıt okunamadı. Dosyalar korunuyor.", first) }
        }
    }
    @Synchronized fun save(profile: Profile) {
        if (profile.revision <= lastRevision) return
        file.parentFile?.mkdirs()
        val next = File(file.path + ".tmp")
        val bytes = ProfileCodec.encode(profile)
        FileOutputStream(next).use { it.write(bytes); it.fd.sync() }
        // Only back up a verified document. A corrupt primary never overwrites recovery data.
        if (file.exists() && runCatching { ProfileCodec.decode(file.readBytes()) }.isSuccess) {
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        try { Files.move(next.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING) }
        catch (_: java.nio.file.AtomicMoveNotSupportedException) { Files.move(next.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING) }
        lastRevision = profile.revision
    }
}
