package dev.gurwi.lintvision.render

import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.ColorUtil
import dev.gurwi.lintvision.model.LineEntry
import dev.gurwi.lintvision.settings.LintVisionSettings
import java.awt.Color

object LintVisionFormatter {

    private val variable = Regex("\\\$(message|count|severity|source|code)")

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

    fun tooltip(entry: LineEntry): String {
        val settings = LintVisionSettings.getInstance()
        val rows = entry.diagnostics.joinToString("<br>") { diagnostic ->
            val color = ColorUtil.toHex(Color(settings.colorOf(diagnostic.level)))
            val message = StringUtil.escapeXmlEntities(StringUtil.shortenTextWithEllipsis(diagnostic.message, 500, 0))
                .replace("\n", "<br>")
            val details = listOf(diagnostic.source, diagnostic.code)
                .filter { it.isNotEmpty() }
                .joinToString(" ")
            val suffix = if (details.isEmpty()) "" else " <font color='gray'>${StringUtil.escapeXmlEntities(details)}</font>"
            "<font color='#$color'>■</font> $message$suffix"
        }
        return "<html>$rows</html>"
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
