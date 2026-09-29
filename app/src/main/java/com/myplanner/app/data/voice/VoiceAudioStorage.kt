package com.myplanner.app.data.voice

import android.content.Context
import java.io.File
import java.util.UUID

class VoiceAudioStorage(context: Context) {

    private val root: File = File(context.applicationContext.filesDir, DIR_NAME).also {
        if (!it.exists()) it.mkdirs()
    }

    fun newRecordingFile(): File {
        val name = "vn_${UUID.randomUUID()}.m4a"
        return File(root, name)
    }

    fun deleteIfExists(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching {
            val file = File(path)
            if (file.exists() && isUnderRoot(file)) file.delete()
        }
    }

    fun cleanupOrphans(knownPaths: Collection<String>) {
        val known = knownPaths.filter { it.isNotBlank() }.map { File(it).absolutePath }.toSet()
        root.listFiles()?.forEach { file ->
            if (file.isFile && file.absolutePath !in known) runCatching { file.delete() }
        }
    }

    private fun isUnderRoot(file: File): Boolean {
        val rootPath = root.canonicalPath
        val filePath = runCatching { file.canonicalPath }.getOrElse { return false }
        return filePath.startsWith(rootPath)
    }

    fun rootDir(): File = root
    fun fileForName(name: String): File = File(root, name)
    fun totalBytes(): Long = root.listFiles()?.sumOf { if (it.isFile) it.length() else 0L } ?: 0L
    fun deleteAll() { root.listFiles()?.forEach { runCatching { it.delete() } } }

    companion object {
        private const val DIR_NAME = "voice_notes"
    }
}
