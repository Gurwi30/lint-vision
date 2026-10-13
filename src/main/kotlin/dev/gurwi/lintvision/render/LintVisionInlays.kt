package dev.gurwi.lintvision.render

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import dev.gurwi.lintvision.model.LineEntry

object LintVisionInlays {

    private val key = Key.create<List<Inlay<*>>>("dev.gurwi.lintvision.inlays")

    fun of(editor: Editor): List<Inlay<*>> = editor.getUserData(key).orEmpty()

    fun clear(editor: Editor) {
        if (editor.isDisposed) return
        val inlays = editor.getUserData(key) ?: return
        editor.putUserData(key, null)

        editor.inlayModel.execute(true) {
            inlays.filter { it.isValid }.forEach { Disposer.dispose(it) }
        }
    }

    fun show(editor: Editor, entries: List<LineEntry>) {
        clear(editor)
        val limit = editor.document.textLength
        val inlays = ArrayList<Inlay<*>>()

        editor.inlayModel.execute(true) {
            for (entry in entries) {
                val inlay = editor.inlayModel.addAfterLineEndElement(
                    entry.offset.coerceAtMost(limit),
                    true,
                    LintVisionRenderer(entry)
                )
                if (inlay != null) inlays.add(inlay)
            }
        }

        editor.putUserData(key, inlays)
    }

}
