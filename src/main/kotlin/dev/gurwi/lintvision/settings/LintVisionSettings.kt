package dev.gurwi.lintvision.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.ui.JBColor
import com.intellij.util.xmlb.XmlSerializerUtil
import dev.gurwi.lintvision.model.Level

@Service(Service.Level.APP)
@State(name = "LintVisionSettings", storages = [Storage("lintvision.xml")])
class LintVisionSettings : PersistentStateComponent<LintVisionSettings.State> {

    class State {

        var enabled = true

        var showErrors = true
        var showWarnings = true
        var showWeakWarnings = false
        var showInfos = false

        var errorColor = 0xE06C75
        var warningColor = 0xE5C07B
        var weakWarningColor = 0x98C379
        var infoColor = 0x61AFEF

        var showBackground = true
        var backgroundAlpha = 14
        var borderRadius = 4
        var gap = 4
        var italic = false
        var bold = false

        var messageTemplate = "■ \$message"
        var errorText = "Error"
        var warningText = "Warning"
        var weakWarningText = "Weak warning"
        var infoText = "Info"
        var maxLength = 120

        var showExtraCount = true
        var extraCountMinMessages = 2
        var excludePattern = ""
        var delayMs = 100

    }

    private val state = State()

    override fun getState(): State = state

    override fun loadState(loaded: State) {
        XmlSerializerUtil.copyBean(loaded, state)
    }

    fun isShown(level: Level): Boolean = when (level) {
        Level.ERROR -> state.showErrors
        Level.WARNING -> state.showWarnings
        Level.WEAK_WARNING -> state.showWeakWarnings
        Level.INFO -> state.showInfos
    }

    fun colorOf(level: Level): Int = when (level) {
        Level.ERROR -> state.errorColor
        Level.WARNING -> state.warningColor
        Level.WEAK_WARNING -> state.weakWarningColor
        Level.INFO -> state.infoColor
    }

    fun textOf(level: Level): String = when (level) {
        Level.ERROR -> state.errorText
        Level.WARNING -> state.warningText
        Level.WEAK_WARNING -> state.weakWarningText
        Level.INFO -> state.infoText
    }

    companion object {

        fun getInstance(): LintVisionSettings = service()

    }

    fun jbColorOf(level: Level): JBColor {
        val rgb = colorOf(level)
        return JBColor(rgb, rgb)
    }

}
