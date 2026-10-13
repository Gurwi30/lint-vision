package dev.gurwi.lintvision.render

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import dev.gurwi.lintvision.model.LineEntry
import dev.gurwi.lintvision.settings.LintVisionSettings
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D

class LintVisionRenderer(val entry: LineEntry) : EditorCustomElementRenderer {

    private val state = LintVisionSettings.getInstance().state
    private val text = LintVisionFormatter.inline(entry)
    private val foreground = LintVisionSettings.getInstance().jbColorOf(entry.primary.level)
    private val showBackground = state.showBackground && state.backgroundAlpha > 0
    private val alpha = state.backgroundAlpha.coerceIn(0, 100) / 100.0
    private val radius = state.borderRadius.coerceAtLeast(0)
    private val gap = state.gap.coerceAtLeast(0)
    private val fontType = when {
        state.italic && state.bold -> EditorFontType.BOLD_ITALIC
        state.italic -> EditorFontType.ITALIC
        state.bold -> EditorFontType.BOLD
        else -> EditorFontType.PLAIN
    }

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        val editor = inlay.editor
        val metrics = editor.contentComponent.getFontMetrics(font(editor))

        return gapWidth(editor) + metrics.stringWidth(text) + 2 * horizontalPadding()
    }

    override fun paint(inlay: Inlay<*>, g: Graphics2D, targetRegion: Rectangle2D, textAttributes: TextAttributes) {
        val editor = inlay.editor
        val font = font(editor)
        val metrics = g.getFontMetrics(font)
        val gapWidth = gapWidth(editor)
        val x = targetRegion.x + gapWidth
        val width = targetRegion.width - gapWidth
        val inset = JBUI.scale(1).toDouble()
        val y = targetRegion.y + inset
        val height = targetRegion.height - 2 * inset

        if (showBackground) {
            val previousHint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING)
            val arc = JBUI.scale(radius) * 2.0
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.color = ColorUtil.mix(editor.colorsScheme.defaultBackground, foreground, alpha)
            g.fill(RoundRectangle2D.Double(x, y, width, height, arc, arc))
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, previousHint)
        }

        val baseline = targetRegion.y + (targetRegion.height - metrics.height) / 2 + metrics.ascent
        g.color = foreground
        g.font = font
        g.drawString(text, (x + horizontalPadding()).toFloat(), baseline.toFloat())
    }

    fun pillBounds(inlay: Inlay<*>): Rectangle? {
        val bounds = inlay.bounds ?: return null
        val offset = gapWidth(inlay.editor)

        return Rectangle(bounds.x + offset, bounds.y, bounds.width - offset, bounds.height)
    }

    private fun font(editor: Editor): Font = editor.colorsScheme.getFont(fontType)

    private fun gapWidth(editor: Editor): Int = gap * EditorUtil.getPlainSpaceWidth(editor)

    private fun horizontalPadding(): Int = if (showBackground) JBUI.scale(6) else 0

}
