package com.github.luciangutu.carserviceapp.data

import com.github.luciangutu.carserviceapp.data.entity.ServiceEntry

private val OperationSeparators = Regex("[,;|\\n\\r\\t]+")

fun parseServiceOperations(operations: String): List<String> {
    return operations
        .removeSurrounding("[", "]")
        .split(OperationSeparators)
        .map { operation ->
            operation.trim()
                .trim('"', '\'', '[', ']', '{', '}')
                .trim()
        }
        .filter { it.isNotBlank() }
}

fun formatServiceOperations(operations: List<String>): String {
    return operations
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase() }
        .joinToString(", ")
}

fun ServiceEntry.matchesServiceSearch(query: String): Boolean {
    val queryParts = query.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (queryParts.isEmpty()) return true

    val searchableText = buildString {
        append(serviceName)
        append(' ')
        append(parseServiceOperations(operations).joinToString(" "))
        append(' ')
        append(operations)
    }

    return queryParts.all { part ->
        searchableText.contains(part, ignoreCase = true)
    }
}

fun ServiceEntry.hasOperation(component: String): Boolean {
    val componentText = component.trim()
    if (componentText.isBlank()) return false

    return parseServiceOperations(operations).any { operation ->
        operation.contains(componentText, ignoreCase = true) ||
                componentText.contains(operation, ignoreCase = true)
    } || operations.contains(componentText, ignoreCase = true)
}
