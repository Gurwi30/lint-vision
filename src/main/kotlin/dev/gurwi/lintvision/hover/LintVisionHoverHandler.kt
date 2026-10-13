package dev.gurwi.lintvision.hover

import com.intellij.codeInsight.hint.HintUtil
import com.intellij.ide.IdeTooltip
import com.intellij.ide.IdeTooltipManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import dev.gurwi.lintvision.render.LintVisionFormatter
import dev.gurwi.lintvision.render.LintVisionInlays
import dev.gurwi.lintvision.render.LintVisionRenderer
import java.awt.Point

class LintVisionHoverHandler(private val project: Project) {

    private var current: Inlay<*>? = null
    private var tooltip: IdeTooltip? = null

    fun install(parent: Disposable) {
        val multicaster = EditorFactory.getInstance().eventMulticaster

        multicaster.addEditorMouseMotionListener(object : EditorMouseMotionListener {
            override fun mouseMoved(e: EditorMouseEvent) {
                onMouseMoved(e)
            }
        }, parent)

        multicaster.addEditorMouseListener(object : EditorMouseListener {
            override fun mouseExited(e: EditorMouseEvent) {
                hide()
            }
        }, parent)
    }

    private fun onMouseMoved(e: EditorMouseEvent) {
        val editor = e.editor

        if (editor.project != project) return

        val point = e.mouseEvent.point
        val hovered = LintVisionInlays.of(editor).firstOrNull { inlay ->
            (inlay.renderer as? LintVisionRenderer)?.pillBounds(inlay)?.contains(point) == true
        }

        if (hovered === current) return

        hide()
        current = hovered

        if (hovered != null) show(editor, hovered, point)
    }

    private fun show(editor: Editor, inlay: Inlay<*>, point: Point) {
        val renderer = inlay.renderer as? LintVisionRenderer ?: return
        val content = HintUtil.createInformationLabel(LintVisionFormatter.tooltip(renderer.entry))
        val next = IdeTooltip(editor.contentComponent, point, content).setPreferredPosition(Balloon.Position.below)

        tooltip = next
        IdeTooltipManager.getInstance().show(next, true)
    }

    private fun hide() {
        tooltip?.let { IdeTooltipManager.getInstance().hide(it) }
        tooltip = null
        current = null
    }

}
