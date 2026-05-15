package com.dersium.feature.export

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BackupManager {
    private const val DB_NAME = "dersium.db"

    fun exportBackup(context: Context): Result<File> = runCatching {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) error("Veritabani bulunamadi")
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Dersium"
        ).also { it.mkdirs() }
        val ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
        val backup = File(dir, "dersium_yedek_$ts.db")
        FileInputStream(dbFile).use { i -> FileOutputStream(backup).use { o -> i.copyTo(o) } }
        backup
    }

    fun importBackup(context: Context, uri: Uri): Result<Unit> = runCatching {
        val dbFile = context.getDatabasePath(DB_NAME)
        context.getDatabasePath("$DB_NAME-wal").delete()
        context.getDatabasePath("$DB_NAME-shm").delete()
        context.contentResolver.openInputStream(uri)?.use { i ->
            FileOutputStream(dbFile).use { o -> i.copyTo(o) }
        } ?: error("Dosya acilamadi")
    }

    fun listBackups(context: Context): List<File> {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Dersium"
        )
        return dir.listFiles { f -> f.extension == "db" }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
