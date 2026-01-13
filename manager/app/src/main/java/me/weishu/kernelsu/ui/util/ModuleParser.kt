package me.weishu.kernelsu.ui.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel
import java.io.ByteArrayOutputStream
import java.util.Properties
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

data class ParsedModuleInfo(
    val id: String, val name: String?, val version: String?, val versionCode: Int?, val author: String?, val description: String?
)

object ModuleParser {

    class ModuleParseException(@param:StringRes val messageRes: Int, vararg val formatArgs: Any) : Exception() {
        fun getMessage(context: Context): String {
            return context.getString(messageRes, *formatArgs)
        }
    }

    private const val MAX_PROP_SIZE = 1 * 1024 * 1024 // 1 MiB

    fun parse(context: Context, uri: Uri): Result<ParsedModuleInfo> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val zipInputStream = ZipInputStream(inputStream)
                val entry = zipInputStream.nextEntry ?: return@use Result.failure(
                    ModuleParseException(R.string.module_error_invalid_zip)
                )
                val entriesSequence = sequence {
                    yield(entry)
                    yieldAll(generateSequence { zipInputStream.nextEntry })
                }

                val modulePropEntry = entriesSequence.firstOrNull { it.name == "module.prop" }

                if (modulePropEntry != null) {
                    val propertiesContentBytes = readEntry(zipInputStream)
                    parseProperties(propertiesContentBytes)
                } else {
                    Result.failure(ModuleParseException(R.string.module_error_no_prop))
                }
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

    private fun parseProperties(propBytes: ByteArray): Result<ParsedModuleInfo> {
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
            ParsedModuleInfo(
                id = id, name = name, version = version, versionCode = versionCode, author = author, description = description
            )
        )
    }

    @SuppressLint("StringFormatInvalid")
    fun getModuleInstallDesc(
        context: Context, uri: Uri, moduleList: List<ModuleViewModel.ModuleInfo>?
    ): String {
        val fileName = uri.getFileName(context) ?: uri.lastPathSegment ?: "Unknown"

        return parse(context, uri).fold(onSuccess = { newModuleInfo ->
            val oldModuleInfo = moduleList?.find { it.id == newModuleInfo.id }
            if (oldModuleInfo == null) {
                // ====================
                // Fresh Install (全新安装)
                // ====================
                buildString {
                    // 标题：### 安装模块：**文件名**
                    appendLine(
                        "### " + context.getString(
                            R.string.module_install_prompt_with_name, "**$fileName**"
                        )
                    )
                    appendLine()

                    val details = listOfNotNull(
                        // ID 使用代码块显示：`id`
                        context.getString(R.string.module_info_id, "`${newModuleInfo.id}`"),

                        // 名称加粗
                        newModuleInfo.name?.let {
                            context.getString(R.string.module_info_name, "**$it**")
                        },

                        // 版本信息
                        newModuleInfo.version?.let {
                            context.getString(R.string.module_info_version, "`$it`")
                        }, newModuleInfo.versionCode?.let {
                            context.getString(R.string.module_info_version_code, "`$it`")
                        }, newModuleInfo.author?.let {
                            context.getString(R.string.module_info_author, it)
                        })

                    // 使用列表形式展示详情
                    details.forEach { appendLine("- $it") }

                    // 描述信息使用引用块 >
                    newModuleInfo.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        appendLine()
                        // 处理多行描述，确保每一行都有引用符号
                        appendLine(desc.lines().joinToString("\n") { "> $it" })
                    }
                }
            } else {
                // ====================
                // Update (更新模块)
                // ====================
                buildString {
                    // 标题
                    appendLine(
                        "### " + context.getString(
                            R.string.module_update_prompt_with_name, "**$fileName**"
                        )
                    )
                    appendLine()

                    fun compare(old: String, new: String?): String? {
                        val newTrimmed = new?.trim()
                        if ((newTrimmed.isNullOrEmpty() && old.isEmpty())) return null
                        if (old == newTrimmed) return newTrimmed

                        return when {
                            old.isEmpty() -> "**$newTrimmed**" // 新增：加粗
                            newTrimmed.isNullOrEmpty() -> "_${old}_" // 删除：斜体
                            else -> "$old -> **$newTrimmed**" // 修改：旧 -> **新**
                        }
                    }

                    fun compareInt(old: Int, new: Int?): String {
                        if (new == null) return "_${old}_"
                        if (old == new) return "**${new}**"
                        return "$old -> **${new}**"
                    }

                    val changes = listOfNotNull(compare(oldModuleInfo.name, newModuleInfo.name)?.let {
                        context.getString(R.string.module_info_name, it)
                    }, compare(oldModuleInfo.version, newModuleInfo.version)?.let {
                        context.getString(R.string.module_info_version, it)
                    }, compareInt(oldModuleInfo.versionCode, newModuleInfo.versionCode).let {
                        context.getString(R.string.module_info_version_code, it)
                    }, compare(oldModuleInfo.author, newModuleInfo.author)?.let {
                        context.getString(R.string.module_info_author, it)
                    })

                    if (changes.isNotEmpty()) {
                        changes.forEach { appendLine("- $it") }
                    }

                    newModuleInfo.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        appendLine()
                        appendLine(desc.lines().joinToString("\n") { "> $it" })
                    }
                }
            }
        }, onFailure = { exception ->
            val reason = if (exception is ModuleParseException) {
                exception.getMessage(context)
            } else {
                exception.message ?: "unknown error"
            }
            buildString {
                appendLine("### ❌ " + context.getString(R.string.module_parse_failed, "**$fileName**"))
                appendLine()

                appendLine("> ${context.getString(R.string.module_parse_reason, reason)}")
                appendLine()

                appendLine("_${context.getString(R.string.module_install_confirm_extra)}_")
            }
        })
    }
}
