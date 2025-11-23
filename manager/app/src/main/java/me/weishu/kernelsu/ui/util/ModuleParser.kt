package me.weishu.kernelsu.ui.util

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import me.weishu.kernelsu.R
import java.io.ByteArrayOutputStream
import java.util.Properties
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

data class ModuleInfo(
    val id: String,
    val name: String?,
    val version: String?,
    val versionCode: Int?,
    val author: String?,
    val description: String?
)

object ModuleParser {

    class ModuleParseException(@StringRes val messageRes: Int, vararg val formatArgs: Any) :
        Exception() {
        fun getMessage(context: Context): String {
            return context.getString(messageRes, *formatArgs)
        }
    }

    private const val MAX_PROP_SIZE = 1 * 1024 * 1024 // 1 MiB

    fun parse(context: Context, uri: Uri): Result<ModuleInfo> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val zipInputStream = ZipInputStream(inputStream)
                var entry = zipInputStream.nextEntry
                if (entry == null) {
                   return Result.failure(ModuleParseException(R.string.module_error_invalid_zip))
                }
                while (entry != null) {
                    if (entry.name == "module.prop") {
                        // Found module.prop, now read and parse it
                        val propBytes = readEntry(zipInputStream)
                        return parseProperties(propBytes)
                    }
                    entry = zipInputStream.nextEntry
                }
                // Loop finished without finding module.prop
                Result.failure(ModuleParseException(R.string.module_error_no_prop))
            } ?: Result.failure(ModuleParseException(R.string.module_error_open_zip))
        } catch (e: ZipException) {
            Result.failure(ModuleParseException(R.string.module_error_invalid_zip))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun readEntry(zipInputStream: ZipInputStream): ByteArray {
        val baos = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var len: Int
        var totalRead = 0L
        while (zipInputStream.read(buffer).also { len = it } > -1) {
            totalRead += len
            if (totalRead > MAX_PROP_SIZE) {
                throw ModuleParseException(R.string.module_error_prop_too_large)
            }
            baos.write(buffer, 0, len)
        }
        return baos.toByteArray()
    }

    private fun parseProperties(propBytes: ByteArray): Result<ModuleInfo> {
        val properties = Properties()
        properties.load(propBytes.inputStream().reader(Charsets.UTF_8))

        val id = properties.getProperty("id")?.trim()

        if (id.isNullOrEmpty()) {
            return Result.failure(ModuleParseException(R.string.module_error_missing_id))
        }

        if (!id.matches("^[a-zA-Z][a-zA-Z0-9._-]+$".toRegex())) {
            return Result.failure(ModuleParseException(R.string.module_error_invalid_id, id))
        }

        val name = properties.getProperty("name")?.trim()
        val version = properties.getProperty("version")?.trim()
        val versionCodeStr = properties.getProperty("versionCode")?.trim()
        val author = properties.getProperty("author")?.trim()
        val description = properties.getProperty("description")?.trim()

        val versionCode = versionCodeStr?.toIntOrNull()

        return Result.success(
            ModuleInfo(
                id = id,
                name = name,
                version = version,
                versionCode = versionCode,
                author = author,
                description = description
            )
        )
    }
}
