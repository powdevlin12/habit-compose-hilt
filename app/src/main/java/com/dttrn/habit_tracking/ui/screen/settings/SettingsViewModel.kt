package com.dttrn.habit_tracking.ui.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dttrn.habit_tracking.data.db.entity.HabitEntity
import com.dttrn.habit_tracking.data.db.entity.HabitLogEntity
import com.dttrn.habit_tracking.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ExportState(
    val isExporting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val exportedFileName: String? = null
)

data class ImportState(
    val isImporting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val habitsImported: Int = 0,
    val logsImported: Int = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _exportState = MutableStateFlow(ExportState())
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val _importState = MutableStateFlow(ImportState())
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun exportCsvToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _exportState.update { it.copy(isExporting = true, isSuccess = false, errorMessage = null) }
            try {
                val habits = repository.getAllHabitsForExport()
                val logs = repository.getAllLogsForExport()

                // Build a map of habitId -> habitName for quick lookup
                val habitNameMap = habits.associate { it.id to it.name }

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)
                    // Write BOM for Excel compatibility
                    writer.write("\uFEFF")

                    // CSV Header
                    writer.write("Habit ID,Habit Name,Check-in Date,Note\n")

                    // CSV Data rows
                    logs.forEach { log ->
                        val habitName = escapeCsv(habitNameMap[log.habitId] ?: "Unknown")
                        val note = escapeCsv(log.note ?: "")
                        writer.write("${log.habitId},$habitName,${log.loggedDate},$note\n")
                    }

                    // Add a separator and habits summary
                    writer.write("\n")
                    writer.write("--- Habits Summary ---\n")
                    writer.write("ID,Name,Description,Emoji,Color,Frequency,Archived,Created At\n")
                    habits.forEach { habit ->
                        val name = escapeCsv(habit.name)
                        val desc = escapeCsv(habit.description ?: "")
                        val emoji = escapeCsv(habit.iconEmoji)
                        val createdDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            .format(Date(habit.createdAt))
                        writer.write(
                            "${habit.id},$name,$desc,$emoji,${habit.colorHex}," +
                                    "${habit.frequency.name},${habit.isArchived},$createdDate\n"
                        )
                    }

                    writer.flush()
                }

                _exportState.update {
                    it.copy(
                        isExporting = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _exportState.update {
                    it.copy(
                        isExporting = false,
                        isSuccess = false,
                        errorMessage = e.message ?: "Lỗi không xác định"
                    )
                }
            }
        }
    }

    fun importCsvFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _importState.update { it.copy(isImporting = true, isSuccess = false, errorMessage = null) }
            try {
                val lines = mutableListOf<String>()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                        reader.forEachLine { line ->
                            // Strip BOM if present
                            val cleaned = line.removePrefix("\uFEFF")
                            lines.add(cleaned)
                        }
                    }
                } ?: throw IllegalStateException("Không thể mở file")

                // Parse the CSV: find sections
                val habits = mutableListOf<HabitEntity>()
                val logEntries = mutableListOf<HabitLogEntity>()

                var section = "LOGS" // Start with logs section
                var headerSkipped = false

                for (line in lines) {
                    val trimmed = line.trim()

                    // Skip empty lines
                    if (trimmed.isEmpty()) continue

                    // Detect section change
                    if (trimmed == "--- Habits Summary ---") {
                        section = "HABITS"
                        headerSkipped = false
                        continue
                    }

                    when (section) {
                        "LOGS" -> {
                            // Skip header line
                            if (!headerSkipped) {
                                if (trimmed.startsWith("Habit ID,") || trimmed.startsWith("Habit ID")) {
                                    headerSkipped = true
                                    continue
                                }
                            }
                            // Parse: Habit ID,Habit Name,Check-in Date,Note
                            val fields = parseCsvLine(trimmed)
                            if (fields.size >= 3) {
                                val habitId = fields[0].toIntOrNull() ?: continue
                                val loggedDate = fields[2].trim()
                                val note = if (fields.size >= 4 && fields[3].isNotBlank()) fields[3].trim() else null

                                // Validate date format (yyyy-MM-dd)
                                if (loggedDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                                    logEntries.add(
                                        HabitLogEntity(
                                            habitId = habitId,
                                            loggedDate = loggedDate,
                                            note = note
                                        )
                                    )
                                }
                            }
                        }
                        "HABITS" -> {
                            // Skip header line
                            if (!headerSkipped) {
                                if (trimmed.startsWith("ID,")) {
                                    headerSkipped = true
                                    continue
                                }
                            }
                            // Parse: ID,Name,Description,Emoji,Color,Frequency,Archived,Created At
                            val fields = parseCsvLine(trimmed)
                            if (fields.size >= 7) {
                                val id = fields[0].toIntOrNull() ?: continue
                                val name = fields[1].trim()
                                val description = fields[2].trim().ifBlank { null }
                                val emoji = fields[3].trim().ifBlank { "✅" }
                                val color = fields[4].trim().ifBlank { "#4CAF50" }
                                val frequency = fields[5].trim().ifBlank { "DAILY" }
                                val archived = fields[6].trim().equals("true", ignoreCase = true)
                                val createdAt = if (fields.size >= 8) {
                                    parseCreatedAt(fields[7].trim())
                                } else {
                                    System.currentTimeMillis()
                                }

                                habits.add(
                                    HabitEntity(
                                        id = id,
                                        name = name,
                                        description = description,
                                        iconEmoji = emoji,
                                        colorHex = color,
                                        frequency = frequency,
                                        isArchived = archived,
                                        createdAt = createdAt
                                    )
                                )
                            }
                        }
                    }
                }

                if (habits.isEmpty()) {
                    throw IllegalStateException("File CSV không hợp lệ hoặc không chứa dữ liệu thói quen")
                }

                // Restore to database
                repository.restoreFromBackup(habits, logEntries)

                _importState.update {
                    it.copy(
                        isImporting = false,
                        isSuccess = true,
                        errorMessage = null,
                        habitsImported = habits.size,
                        logsImported = logEntries.size
                    )
                }
            } catch (e: Exception) {
                _importState.update {
                    it.copy(
                        isImporting = false,
                        isSuccess = false,
                        errorMessage = e.message ?: "Lỗi không xác định"
                    )
                }
            }
        }
    }

    fun clearExportState() {
        _exportState.update { ExportState() }
    }

    fun clearImportState() {
        _importState.update { ImportState() }
    }

    /**
     * Parses a CSV line respecting quoted fields (handles commas and quotes inside quoted values).
     */
    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val char = line[i]
            when {
                char == '"' && inQuotes -> {
                    // Check for escaped quote ""
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++ // skip next quote
                    } else {
                        inQuotes = false
                    }
                }
                char == '"' && !inQuotes -> {
                    inQuotes = true
                }
                char == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> {
                    current.append(char)
                }
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }

    /**
     * Parses "yyyy-MM-dd HH:mm" back to epoch millis.
     */
    private fun parseCreatedAt(dateStr: String): Long {
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(dateStr)?.time
                ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    companion object {
        fun generateFileName(): String {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            return "habit_backup_${dateFormat.format(Date())}.csv"
        }
    }
}
