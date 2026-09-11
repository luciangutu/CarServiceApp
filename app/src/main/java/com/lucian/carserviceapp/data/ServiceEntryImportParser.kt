package com.lucian.carserviceapp.data

import com.lucian.carserviceapp.data.entity.ServiceEntry
import java.text.Normalizer
import java.util.Calendar
import java.util.Locale

data class ServiceEntryImportResult(
    val entries: List<ServiceEntry>,
    val skippedRows: Int
)

fun parseServiceEntryImport(carId: Long, content: String): ServiceEntryImportResult {
    val lines = content
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toList()

    if (lines.isEmpty()) return ServiceEntryImportResult(emptyList(), 0)

    val delimiter = detectDelimiter(lines.first())
    val firstRow = parseDelimitedLine(lines.first(), delimiter)
    val hasHeader = firstRow.any { normalizeHeader(it) in KnownHeaders }
    val headers = if (hasHeader) firstRow.map(::normalizeHeader) else emptyList()
    val rows = if (hasHeader) lines.drop(1) else lines

    var skippedRows = 0
    val entries = rows.mapNotNull { line ->
        val columns = parseDelimitedLine(line, delimiter)
        val dateText = valueFor(columns, headers, "data", 1)
        val operations = valueFor(columns, headers, "operatiuni efectuate", 3)

        if (dateText.isBlank() || operations.isBlank() || operations == "-") {
            skippedRows += 1
            return@mapNotNull null
        }

        val date = parseImportedDate(dateText)
        if (date == null) {
            skippedRows += 1
            return@mapNotNull null
        }

        ServiceEntry(
            carId = carId,
            date = date,
            kilometers = parseKilometers(valueFor(columns, headers, "kilometraj", 0)),
            serviceName = parseServiceName(valueFor(columns, headers, "unitate service", 2)),
            operations = formatServiceOperations(listOf(operations)),
            cost = null
        )
    }

    return ServiceEntryImportResult(entries, skippedRows)
}

private val KnownHeaders = setOf(
    "kilometraj",
    "data",
    "unitate service",
    "operatiuni efectuate"
)

private val MonthNumbers = mapOf(
    "ian" to Calendar.JANUARY,
    "ianuarie" to Calendar.JANUARY,
    "jan" to Calendar.JANUARY,
    "january" to Calendar.JANUARY,
    "feb" to Calendar.FEBRUARY,
    "februarie" to Calendar.FEBRUARY,
    "february" to Calendar.FEBRUARY,
    "mar" to Calendar.MARCH,
    "martie" to Calendar.MARCH,
    "march" to Calendar.MARCH,
    "apr" to Calendar.APRIL,
    "aprilie" to Calendar.APRIL,
    "april" to Calendar.APRIL,
    "mai" to Calendar.MAY,
    "may" to Calendar.MAY,
    "iun" to Calendar.JUNE,
    "iunie" to Calendar.JUNE,
    "jun" to Calendar.JUNE,
    "june" to Calendar.JUNE,
    "iul" to Calendar.JULY,
    "iulie" to Calendar.JULY,
    "jul" to Calendar.JULY,
    "july" to Calendar.JULY,
    "aug" to Calendar.AUGUST,
    "august" to Calendar.AUGUST,
    "sep" to Calendar.SEPTEMBER,
    "sept" to Calendar.SEPTEMBER,
    "septembrie" to Calendar.SEPTEMBER,
    "september" to Calendar.SEPTEMBER,
    "oct" to Calendar.OCTOBER,
    "octombrie" to Calendar.OCTOBER,
    "october" to Calendar.OCTOBER,
    "noi" to Calendar.NOVEMBER,
    "noiembrie" to Calendar.NOVEMBER,
    "nov" to Calendar.NOVEMBER,
    "november" to Calendar.NOVEMBER,
    "dec" to Calendar.DECEMBER,
    "decembrie" to Calendar.DECEMBER,
    "december" to Calendar.DECEMBER
)

private fun detectDelimiter(header: String): Char {
    return listOf('\t', ';', ',').maxBy { delimiter ->
        parseDelimitedLine(header, delimiter).size
    }
}

private fun parseDelimitedLine(line: String, delimiter: Char): List<String> {
    val values = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var index = 0

    while (index < line.length) {
        val char = line[index]
        when {
            char == '"' && inQuotes && index + 1 < line.length && line[index + 1] == '"' -> {
                current.append('"')
                index += 1
            }
            char == '"' -> inQuotes = !inQuotes
            char == delimiter && !inQuotes -> {
                values += current.toString().trim()
                current.clear()
            }
            else -> current.append(char)
        }
        index += 1
    }

    values += current.toString().trim()
    return values
}

private fun valueFor(
    columns: List<String>,
    headers: List<String>,
    header: String,
    fallbackIndex: Int
): String {
    val index = headers.indexOf(header).takeIf { it >= 0 } ?: fallbackIndex
    return columns.getOrNull(index)?.trim().orEmpty()
}

private fun parseKilometers(value: String): Int {
    return value
        .takeUnless { it == "-" }
        ?.filter { it.isDigit() }
        ?.toIntOrNull()
        ?: 0
}

private fun parseServiceName(value: String): String {
    return value.takeUnless { it.isBlank() || it == "-" } ?: "Imported"
}

private fun parseImportedDate(value: String): Long? {
    val parts = value.trim().split("\\s+".toRegex())
    if (parts.size < 3) return null

    val day = parts[0].filter { it.isDigit() }.toIntOrNull() ?: return null
    val month = MonthNumbers[normalizeHeader(parts[1])] ?: return null
    val year = parts[2].filter { it.isDigit() }.toIntOrNull() ?: return null

    return Calendar.getInstance(Locale.getDefault()).apply {
        clear()
        set(year, month, day)
    }.timeInMillis
}

private fun normalizeHeader(value: String): String {
    val withoutDiacritics = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")

    return withoutDiacritics
        .lowercase(Locale.ROOT)
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
}
