package dev.gurwi.lintvision

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import dev.gurwi.lintvision.service.LintVisionService

class LintVision : ProjectActivity {

    override suspend fun execute(project: Project) {
        project.service<LintVisionService>().start()
    }

}
