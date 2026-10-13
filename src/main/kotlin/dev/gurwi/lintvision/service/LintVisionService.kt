package dev.gurwi.lintvision.service

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.Alarm
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.UIUtil
import dev.gurwi.lintvision.hover.LintVisionHoverHandler
import dev.gurwi.lintvision.model.LineEntry
import dev.gurwi.lintvision.render.LintVisionInlays
import dev.gurwi.lintvision.settings.LintVisionSettings
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class LintVisionService(private val project: Project) : Disposable {

    private val collector = DiagnosticCollector(project)
    private val pending = ConcurrentHashMap.newKeySet<TextEditor>()
    private val alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)

    @Volatile
    private var started = false

    fun start() {
        if (started) return
        started = true

        project.messageBus.connect(this).subscribe(
            DaemonCodeAnalyzer.DAEMON_EVENT_TOPIC,
            object : DaemonCodeAnalyzer.DaemonListener {
                override fun daemonFinished(fileEditors: Collection<FileEditor>) {
                    schedule(fileEditors.filterIsInstance<TextEditor>())
                }
            }
        )

        LintVisionHoverHandler(project).install(this)
    }

    fun refreshAll() {
        val editors = FileEditorManager.getInstance(project).allEditors.filterIsInstance<TextEditor>()
        if (LintVisionSettings.getInstance().state.enabled) {
            schedule(editors, immediate = true)
            return
        }
        UIUtil.invokeLaterIfNeeded {
            editors.forEach { LintVisionInlays.clear(it.editor) }
        }
    }

    private fun schedule(editors: List<TextEditor>, immediate: Boolean = false) {
        if (editors.isEmpty()) return
        pending.addAll(editors)
        alarm.cancelAllRequests()
        val delay = if (immediate) 0 else LintVisionSettings.getInstance().state.delayMs
        alarm.addRequest({ flush() }, delay)
    }

    private fun flush() {
        val batch = pending.toList()
        pending.removeAll(batch.toSet())
        for (textEditor in batch) {
            val editor = textEditor.editor
            if (editor.isDisposed) continue
            ReadAction.nonBlocking(Callable { collector.collect(editor) })
                .expireWith(this)
                .coalesceBy(this, editor)
                .finishOnUiThread(ModalityState.any()) { entries -> render(editor, entries) }
                .submit(AppExecutorUtil.getAppExecutorService())
        }
    }

    private fun render(editor: Editor, entries: List<LineEntry>) {
        if (editor.isDisposed) return
        if (LintVisionSettings.getInstance().state.enabled) {
            LintVisionInlays.show(editor, entries)
        } else {
            LintVisionInlays.clear(editor)
        }
    }

    override fun dispose() {
        pending.clear()
    }

    companion object {

        fun refreshAllProjects() {
            for (project in ProjectManager.getInstance().openProjects) {
                if (project.isDisposed) continue
                project.service<LintVisionService>().refreshAll()
            }
        }

    }

}
