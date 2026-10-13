package dev.gurwi.lintvision.hover

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.ColorUtil
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.HTMLEditorKitBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import dev.gurwi.lintvision.model.LineEntry
import dev.gurwi.lintvision.model.LintDiagnostic
import dev.gurwi.lintvision.settings.LintVisionSettings
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JPanel
import javax.swing.text.DefaultCaret

object LintVisionPopup {

    private const val WIDTH = 460
    private const val BAR_WIDTH = 3
    private const val MAX_MESSAGE_LENGTH = 800
    private const val INTENTIONS_ACTION_ID = "ShowIntentionActions"

    fun create(project: Project, editor: Editor, entry: LineEntry, onClose: () -> Unit): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = JBUI.Borders.empty(8, 10)

        entry.diagnostics.forEachIndexed { index, diagnostic ->
            if (index > 0) panel.add(spacer())
            panel.add(row(project, editor, diagnostic, onClose))
        }

        return panel
    }

    private fun row(project: Project, editor: Editor, diagnostic: LintDiagnostic, onClose: () -> Unit): JComponent {
        val message = messagePane(diagnostic)

        val content = JPanel()
        content.layout = BoxLayout(content, BoxLayout.Y_AXIS)
        content.isOpaque = false
        content.border = JBUI.Borders.emptyLeft(8)
        content.add(message)
        content.add(actionsLine(project, editor, diagnostic, message, onClose))

        val row = JPanel(BorderLayout())
        row.isOpaque = false
        row.alignmentX = JComponent.LEFT_ALIGNMENT
        row.add(bar(diagnostic), BorderLayout.WEST)
        row.add(content, BorderLayout.CENTER)
        return row
    }

    private fun bar(diagnostic: LintDiagnostic): JComponent {
        val bar = JPanel()
        bar.background = LintVisionSettings.getInstance().jbColorOf(diagnostic.level)
        bar.preferredSize = Dimension(JBUI.scale(BAR_WIDTH), 0)
        return bar
    }

    private fun spacer(): JComponent {
        val spacer = JPanel()
        spacer.isOpaque = false
        spacer.alignmentX = JComponent.LEFT_ALIGNMENT
        spacer.preferredSize = Dimension(0, JBUI.scale(10))
        spacer.maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(10))
        return spacer
    }

    private fun messagePane(diagnostic: LintDiagnostic): JEditorPane {
        val message = StringUtil.escapeXmlEntities(
            StringUtil.shortenTextWithEllipsis(diagnostic.message, MAX_MESSAGE_LENGTH, 0)
        ).replace("\n", "<br>")
        val details = listOf(diagnostic.source, diagnostic.code)
            .filter { it.isNotEmpty() }
            .joinToString(" ")
        val detailsHtml = if (details.isEmpty()) {
            ""
        } else {
            "<br><font color='#${ColorUtil.toHex(UIUtil.getContextHelpForeground())}'>" +
                    "${StringUtil.escapeXmlEntities(details)}</font>"
        }

        val pane = JEditorPane()
        pane.editorKit = HTMLEditorKitBuilder().withWordWrapViewFactory().build()
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
        pane.font = UIUtil.getLabelFont()
        pane.foreground = UIUtil.getLabelForeground()
        pane.isEditable = false
        pane.isOpaque = false
        pane.border = null
        pane.alignmentX = JComponent.LEFT_ALIGNMENT
        pane.text = "<html><body>$message$detailsHtml</body></html>"

        (pane.caret as? DefaultCaret)?.let {
            it.updatePolicy = DefaultCaret.NEVER_UPDATE
            it.isSelectionVisible = true
        }

        pane.setSize(JBUI.scale(WIDTH), Short.MAX_VALUE.toInt())
        val size = Dimension(JBUI.scale(WIDTH), pane.preferredSize.height)
        pane.preferredSize = size
        pane.maximumSize = size
        return pane
    }

    private fun actionsLine(
        project: Project,
        editor: Editor,
        diagnostic: LintDiagnostic,
        message: JEditorPane,
        onClose: () -> Unit
    ): JComponent {
        val actions = HyperlinkLabel("More actions...")
        actions.addHyperlinkListener {
            onClose()
            ApplicationManager.getApplication().invokeLater { openQuickFixes(project, editor, diagnostic) }
        }

        val copy = HyperlinkLabel("Copy")
        copy.addHyperlinkListener {
            val text = message.selectedText?.takeIf { it.isNotEmpty() } ?: diagnostic.message
            CopyPasteManager.getInstance().setContents(StringSelection(text))
        }

        val line = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        line.isOpaque = false
        line.border = JBUI.Borders.emptyTop(4)
        line.alignmentX = JComponent.LEFT_ALIGNMENT
        line.add(actions)

        val shortcut = KeymapUtil.getFirstKeyboardShortcutText(INTENTIONS_ACTION_ID)
        if (shortcut.isNotEmpty()) {
            val hint = JBLabel("  $shortcut")
            hint.foreground = UIUtil.getContextHelpForeground()
            line.add(hint)
        }

        line.add(JBLabel("    "))
        line.add(copy)
        return line
    }

    private fun openQuickFixes(project: Project, editor: Editor, diagnostic: LintDiagnostic) {
        if (editor.isDisposed) return
        editor.caretModel.moveToOffset(diagnostic.offset.coerceIn(0, editor.document.textLength))
        IdeFocusManager.getInstance(project).requestFocus(editor.contentComponent, true)

        val action = ActionManager.getInstance().getAction(INTENTIONS_ACTION_ID) ?: return
        val dataContext = DataManager.getInstance().getDataContext(editor.contentComponent)
        val event = AnActionEvent.createEvent(dataContext, null, ActionPlaces.EDITOR_POPUP, ActionUiKind.NONE, null)
        ActionUtil.invokeAction(action, event, null)
    }

}