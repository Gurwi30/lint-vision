package dev.gurwi.lintvision.hover

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseEventArea
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.openapi.util.Disposer
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.Alarm
import dev.gurwi.lintvision.render.LintVisionInlays
import dev.gurwi.lintvision.render.LintVisionRenderer
import dev.gurwi.lintvision.settings.LintVisionSettings
import java.awt.Component
import java.awt.Container
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class LintVisionHoverHandler(private val project: Project) {

    private lateinit var showAlarm: Alarm
    private lateinit var hideAlarm: Alarm

    private var target: Inlay<*>? = null
    private var popup: JBPopup? = null
    private var popupInlay: Inlay<*>? = null

    private var state = LintVisionSettings.getInstance().state

    fun install(parent: Disposable) {
        showAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, parent)
        hideAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, parent)

        val multicaster = EditorFactory.getInstance().eventMulticaster
        multicaster.addEditorMouseMotionListener(object : EditorMouseMotionListener {
            override fun mouseMoved(e: EditorMouseEvent) {
                onMouseMoved(e)
            }
        }, parent)

        multicaster.addEditorMouseListener(object : EditorMouseListener {
            override fun mouseExited(e: EditorMouseEvent) {
                target = null
                showAlarm.cancelAllRequests()
                scheduleHide()
            }
        }, parent)

        multicaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                target = null
                close()
            }
        }, parent)

        Disposer.register(parent) { close() }
    }

    private fun onMouseMoved(e: EditorMouseEvent) {
        if (!state.showHoverPopup) return

        val editor = e.editor
        if (editor.project != project || e.area != EditorMouseEventArea.EDITING_AREA) return

        val hovered = inlayAt(editor, e.mouseEvent.point)
        if (hovered == null) {
            target = null
            showAlarm.cancelAllRequests()
            scheduleHide()
            return
        }

        hideAlarm.cancelAllRequests()
        if (hovered === target) return

        target = hovered
        showAlarm.cancelAllRequests()
        if (popupInlay === hovered && popup?.isVisible == true) return

        close()
        showAlarm.addRequest({ show(editor, hovered) }, state.hoverShowDelayMs)
    }

    private fun inlayAt(editor: Editor, point: Point): Inlay<*>? {
        if (editor.isDisposed) return null
        val limit = editor.document.textLength
        return LintVisionInlays.of(editor).firstOrNull { inlay ->
            if (!inlay.isValid || inlay.offset > limit) return@firstOrNull false
            val renderer = inlay.renderer as? LintVisionRenderer ?: return@firstOrNull false
            runCatching { renderer.pillBounds(inlay)?.contains(point) == true }.getOrDefault(false)
        }
    }

    private fun show(editor: Editor, inlay: Inlay<*>) {
        if (editor.isDisposed || !inlay.isValid || target !== inlay) return
        val renderer = inlay.renderer as? LintVisionRenderer ?: return
        val bounds = runCatching { renderer.pillBounds(inlay) }.getOrNull() ?: return

        close()

        val content = LintVisionPopup.create(project, editor, renderer.entry) { close() }
        track(content)

        val created = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(content, null)
            .setRequestFocus(false)
            .setFocusable(false)
            .setMovable(false)
            .setResizable(false)
            .setCancelOnClickOutside(true)
            .setCancelOnWindowDeactivation(true)
            .createPopup()

        created.addListener(object : JBPopupListener {
            override fun onClosed(event: LightweightWindowEvent) {
                if (popup !== created) return
                popup = null
                popupInlay = null
            }
        })

        popup = created
        popupInlay = inlay
        created.show(RelativePoint(editor.contentComponent, Point(bounds.x, bounds.y + bounds.height)))
    }

    private fun track(component: Component) {
        component.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                hideAlarm.cancelAllRequests()
            }

            override fun mouseExited(e: MouseEvent) {
                scheduleHide()
            }
        })
        (component as? Container)?.components?.forEach(::track)
    }

    private fun scheduleHide() {
        if (popup == null) return
        hideAlarm.cancelAllRequests()
        hideAlarm.addRequest({ close() }, state.hoverHideDelayMs)
    }

    private fun close() {
        showAlarm.cancelAllRequests()
        hideAlarm.cancelAllRequests()
        val current = popup
        popup = null
        popupInlay = null
        current?.cancel()
    }

}