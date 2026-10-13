package dev.gurwi.lintvision.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import dev.gurwi.lintvision.service.LintVisionService
import dev.gurwi.lintvision.settings.LintVisionSettings

class ToggleLintVisionAction : AnAction(), DumbAware {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val state = LintVisionSettings.getInstance().state
        state.enabled = !state.enabled
        LintVisionService.refreshAllProjects()
    }

}
