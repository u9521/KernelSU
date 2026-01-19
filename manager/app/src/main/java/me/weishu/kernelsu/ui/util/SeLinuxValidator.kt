package me.weishu.kernelsu.ui.util

// https://elixir.bootlin.com/linux/v5.9.16/source/security/selinux/ss/services.c#L1415
// https://elixir.bootlin.com/linux/v5.10.247/source/security/selinux/ss/mls.c#L36


private val ID_REGEX = Regex("^[a-zA-Z0-9_]+$")

fun checkSelinuxContext(context: String?): String? {
    if (context.isNullOrEmpty()) {
        return "Context cannot be empty"
    }

    if (context.length > 63) {
        return "Context length too long > 63"
    }

    //extract user:role:type
    val parts = context.split(":", limit = 4)

    // Must contain at least User:Role:Type
    if (parts.size < 3) {
        return "Invalid Context: missing basic fields (expected 'user:role:type[:range]')"
    }

    val user = parts[0]
    val role = parts[1]
    val type = parts[2]

    // Validate basic field format
    if (!isValidIdentifier(user)) return "Invalid user format: '$user'"
    if (!isValidIdentifier(role)) return "Invalid role format: '$role'"
    if (!isValidIdentifier(type)) return "Invalid type format: '$type'"

    // If there is a 4th part, validate MLS/MCS format
    if (parts.size == 4) {
        val mlsRange = parts[3]
        if (mlsRange.isEmpty()) {
            return "Invalid format: empty MLS field after :"
        }
        return validateMlsRange(mlsRange)
    }

    // No MLS part is also valid (u:r:t)
    return null
}

private fun validateMlsRange(rangeStr: String): String? {
    // 1. Check for existence of '-' (High Level)
    val levels = rangeStr.split("-")

    // The range can have at most one '-', i.e., split into Low and High parts
    if (levels.size > 2) {
        return "Invalid MLS range: too many -"
    }

    for (level in levels) {
        val error = validateMlsLevel(level)
        if (error != null) {
            return error
        }
    }

    return null
}

private fun validateMlsLevel(levelStr: String): String? {
    if (levelStr.isEmpty()) {
        return "Invalid MLS level: empty string"
    }

    // 1. Separate Sensitivity and Category List
    val parts = levelStr.split(":", limit = 2)

    val sensitivity = parts[0]
    if (!isValidIdentifier(sensitivity)) {
        return "Invalid sensitivity identifier: '$sensitivity'"
    }

    // If there is a Category part
    if (parts.size == 2) {
        val catListStr = parts[1]
        if (catListStr.isEmpty()) {
            // For example, "s0:" case, if there is a colon, there must be content after it
            return "Invalid MLS category: empty category list after :"
        }
        return validateCategoryList(catListStr)
    }

    return null
}


private fun validateCategoryList(listStr: String): String? {
    val items = listStr.split(",")

    for (item in items) {
        if (item.isEmpty()) {
            return "Invalid category: empty item in list"
        }

        val rangeParts = item.split(".")

        if (rangeParts.size > 2) {
            return "Invalid category range: too many dots in '$item'"
        }

        // Check if each part is a valid identifier
        for (part in rangeParts) {
            if (part.isEmpty()) {
                return "Invalid category range: empty part in '$item'"
            }
            if (!isValidIdentifier(part)) {
                return "Invalid category identifier: '$part'"
            }
        }
    }
    return null
}

private fun isValidIdentifier(id: String): Boolean {
    return id.matches(ID_REGEX)
}