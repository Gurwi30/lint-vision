package dev.gurwi.lintvision.render

import dev.gurwi.lintvision.model.LineEntry
import dev.gurwi.lintvision.settings.LintVisionSettings

object LintVisionFormatter {

    private val variable = Regex("\\$(message|count|severity|source|code)")

    fun inline(entry: LineEntry): String {
        val settings = LintVisionSettings.getInstance()
        val state = settings.state
        val primary = entry.primary
        val message = truncate(primary.message.lineSequence().firstOrNull().orEmpty().trim(), state.maxLength)
        val text = variable.replace(state.messageTemplate) { match ->
            when (match.groupValues[1]) {
                "message" -> message
                "count" -> entry.diagnostics.size.toString()
                "severity" -> settings.textOf(primary.level)
                "source" -> primary.source
                else -> primary.code
            }
        }
        return text + extraCount(entry, state)
    }

    private fun extraCount(entry: LineEntry, state: LintVisionSettings.State): String {
        val total = entry.diagnostics.size
        if (!state.showExtraCount || total < state.extraCountMinMessages.coerceAtLeast(2)) return ""
        return "  (+${total - 1})"
    }

    private fun truncate(text: String, limit: Int): String {
        if (limit <= 0 || text.length <= limit) return text
        return text.take(limit - 1).trimEnd() + "…"
    }

}