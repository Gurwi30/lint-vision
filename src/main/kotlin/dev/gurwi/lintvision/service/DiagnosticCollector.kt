package dev.gurwi.lintvision.service

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.project.Project
import dev.gurwi.lintvision.model.LineEntry
import dev.gurwi.lintvision.model.Level
import dev.gurwi.lintvision.model.LintDiagnostic
import dev.gurwi.lintvision.settings.LintVisionSettings

class DiagnosticCollector(private val project: Project) {

    fun collect(editor: Editor): List<LineEntry> {
        val document = editor.document
        val markup = DocumentMarkupModel.forDocument(document, project, false) ?: return emptyList()
        val settings = LintVisionSettings.getInstance()
        val exclude = settings.state.excludePattern
            .takeIf { it.isNotBlank() }
            ?.let { runCatching { Regex(it) }.getOrNull() }

        val byLine = HashMap<Int, MutableList<LintDiagnostic>>()
        for (highlighter in markup.allHighlighters) {
            if (!highlighter.isValid) continue

            val info = HighlightInfo.fromRangeHighlighter(highlighter) ?: continue
            val level = levelOf(info.severity) ?: continue

            if (!settings.isShown(level)) continue

            val message = info.description?.trim().orEmpty()

            if (message.isEmpty()) continue

            if (exclude != null && exclude.containsMatchIn(message)) continue
            if (highlighter.startOffset > document.textLength) continue

            val line = document.getLineNumber(highlighter.startOffset)
            byLine.getOrPut(line) { mutableListOf() }.add(
                LintDiagnostic(
                    level = level,
                    message = message,
                    source = info.inspectionToolId.orEmpty(),
                    code = info.problemGroup?.problemName.orEmpty(),
                    offset = highlighter.startOffset
                )
            )
        }

        return byLine.map { (line, diagnostics) ->
            val sorted = diagnostics
                .sortedWith(compareByDescending<LintDiagnostic> { it.level.rank }.thenBy { it.offset })
                .distinctBy { Triple(it.level, it.message, it.code) }

            LineEntry(document.getLineEndOffset(line), sorted)
        }
    }

    private fun levelOf(severity: HighlightSeverity): Level? = when {
        severity >= HighlightSeverity.ERROR -> Level.ERROR
        severity >= HighlightSeverity.WARNING -> Level.WARNING
        severity >= HighlightSeverity.WEAK_WARNING -> Level.WEAK_WARNING
        severity == HighlightSeverity.INFORMATION -> Level.INFO
        else -> null
    }

}
