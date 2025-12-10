package me.weishu.kernelsu.ui.util.module

data class LatestVersionInfo(
    val versionCode: Int = 0,
    val downloadUrl: String = "",
    val changelog: String = ""
)

fun sanitizeVersionString(version: String): String {
    return version.replace(Regex("[^a-zA-Z0-9.\\-_]"), "_")
}