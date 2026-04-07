package io.github.hugo1120.koreaderremote.platform.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ScreenshotSaver {
    suspend fun save(stream: InputStream): String
}

class MediaStoreScreenshotSaver(
    private val context: Context,
) : ScreenshotSaver {
    override suspend fun save(stream: InputStream): String = withContext(Dispatchers.IO) {
        val fileName = "koreader_${System.currentTimeMillis()}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToMediaStore(stream, fileName)
        } else {
            saveToExternalFiles(stream, fileName)
        }
    }

    private fun saveToMediaStore(stream: InputStream, fileName: String): String {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/KOReader Remote")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("创建截图记录失败")
        return try {
            resolver.openOutputStream(uri)?.use { output ->
                stream.copyTo(output)
            } ?: error("打开截图输出流失败")
            markMediaStoreRecordReady(uri)
            uri.toString()
        } catch (throwable: Throwable) {
            runCatching {
                resolver.delete(uri, null, null)
            }
            throw throwable
        }
    }

    private fun saveToExternalFiles(stream: InputStream, fileName: String): String {
        val externalPictures = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: error("访问图片目录失败")
        val targetDir = File(externalPictures, "KOReader Remote")
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            error("创建截图目录失败")
        }
        val targetFile = File(targetDir, fileName)
        return try {
            targetFile.outputStream().use { output ->
                stream.copyTo(output)
            }
            targetFile.absolutePath
        } catch (throwable: Throwable) {
            targetFile.delete()
            throw throwable
        }
    }

    private fun markMediaStoreRecordReady(uri: Uri) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }
        val updatedRows = resolver.update(uri, values, null, null)
        check(updatedRows > 0) { "更新截图状态失败" }
    }
}
