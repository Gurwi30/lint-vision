package dev.gurwi.lintvision.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.ColorPanel
import com.intellij.ui.JBColor
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import dev.gurwi.lintvision.service.LintVisionService
import java.awt.Color
import javax.swing.JComponent

class LintVisionConfigurable : Configurable {

    private val enabled = JBCheckBox("Enable LintVision")

    private val showErrors = JBCheckBox("Errors")
    private val showWarnings = JBCheckBox("Warnings")
    private val showWeak = JBCheckBox("Weak warnings")
    private val showInfos = JBCheckBox("Information")

    private val errorColor = ColorPanel()
    private val warningColor = ColorPanel()
    private val weakColor = ColorPanel()
    private val infoColor = ColorPanel()

    private val showBackground = JBCheckBox("Show background")
    private val backgroundAlpha = JBIntSpinner(14, 0, 100)
    private val borderRadius = JBIntSpinner(4, 0, 20)
    private val gap = JBIntSpinner(4, 0, 40)
    private val italic = JBCheckBox("Italic")
    private val bold = JBCheckBox("Bold")

    private val template = JBTextField()
    private val errorText = JBTextField()
    private val warningText = JBTextField()
    private val weakText = JBTextField()
    private val infoText = JBTextField()
    private val maxLength = JBIntSpinner(120, 0, 1000)

    private val showExtraCount = JBCheckBox("Show \"(+N)\" when a line has more messages")
    private val extraCountMinMessages = JBIntSpinner(2, 2, 100)
    private val exclude = JBTextField()
    private val delay = JBIntSpinner(100, 0, 5000)

    private val showHoverPopup = JBCheckBox("Show hover popup")
    private val hoverShowDelay = JBIntSpinner(300, 0, 5000)
    private val hoverHideDelay = JBIntSpinner(0, 0, 5000)

    private val state get() = LintVisionSettings.getInstance().state

    init {
        showBackground.addItemListener { updateEnabledState() }
        showExtraCount.addItemListener { updateEnabledState() }
    }

    override fun getDisplayName() = "LintVision"

    override fun createComponent(): JComponent {
        val component = panel {
            row { cell(enabled) }
            group("Show Severities") {
                row { cell(showErrors) }
                row { cell(showWarnings) }
                row { cell(showWeak) }
                row { cell(showInfos) }
            }
            group("Appearance") {
                row("Error color:") { cell(errorColor) }
                row("Warning color:") { cell(warningColor) }
                row("Weak warning color:") { cell(weakColor) }
                row("Information color:") { cell(infoColor) }
                row { cell(showBackground) }
                row("Background tint (%):") { cell(backgroundAlpha) }
                row("Border radius (px):") { cell(borderRadius) }
                row("Gap before message (spaces):") { cell(gap) }
                    .rowComment("0 attaches the message directly to the end of the code")
                row { cell(italic); cell(bold) }
            }
            group("Message") {
                row("Template:") {
                    cell(template).columns(30)
                        .comment("Variables: \$message, \$count, \$severity, \$source, \$code. Whitespace is kept as typed.")
                }
                row("Error text:") { cell(errorText).columns(12) }
                row("Warning text:") { cell(warningText).columns(12) }
                row("Weak warning text:") { cell(weakText).columns(12) }
                row("Information text:") { cell(infoText).columns(12) }
                row("Max message length (0 = unlimited):") { cell(maxLength) }
            }
            group("Behavior") {
                row { cell(showExtraCount) }
                row("Show \"(+N)\" when a line has at least this many messages:") { cell(extraCountMinMessages) }
                row("Hide messages matching regex:") { cell(exclude).columns(30) }
                row("Update delay (ms):") { cell(delay) }
            }
            group("Hover Popup") {
                row { cell(showHoverPopup) }
                row("Show delay (ms):") { cell(hoverShowDelay) }
                row("Hide delay (ms):") { cell(hoverHideDelay) }
            }
        }

        reset()
        return component
    }

    override fun isModified(): Boolean {
        val state = this@LintVisionConfigurable.state
        return enabled.isSelected != state.enabled ||
                showErrors.isSelected != state.showErrors ||
                showWarnings.isSelected != state.showWarnings ||
                showWeak.isSelected != state.showWeakWarnings ||
                showInfos.isSelected != state.showInfos ||
                rgb(errorColor.selectedColor, state.errorColor) != state.errorColor ||
                rgb(warningColor.selectedColor, state.warningColor) != state.warningColor ||
                rgb(weakColor.selectedColor, state.weakWarningColor) != state.weakWarningColor ||
                rgb(infoColor.selectedColor, state.infoColor) != state.infoColor ||
                showBackground.isSelected != state.showBackground ||
                backgroundAlpha.number != state.backgroundAlpha ||
                borderRadius.number != state.borderRadius ||
                gap.number != state.gap ||
                italic.isSelected != state.italic ||
                bold.isSelected != state.bold ||
                template.text != state.messageTemplate ||
                errorText.text != state.errorText ||
                warningText.text != state.warningText ||
                weakText.text != state.weakWarningText ||
                infoText.text != state.infoText ||
                maxLength.number != state.maxLength ||
                showExtraCount.isSelected != state.showExtraCount ||
                extraCountMinMessages.number != state.extraCountMinMessages ||
                exclude.text != state.excludePattern ||
                delay.number != state.delayMs ||
                showHoverPopup.isSelected != state.showHoverPopup ||
                hoverShowDelay.number != state.hoverShowDelayMs ||
                hoverHideDelay.number != state.hoverHideDelayMs
    }

    override fun apply() {
        val state = this@LintVisionConfigurable.state
        state.enabled = enabled.isSelected
        state.showErrors = showErrors.isSelected
        state.showWarnings = showWarnings.isSelected
        state.showWeakWarnings = showWeak.isSelected
        state.showInfos = showInfos.isSelected
        state.errorColor = rgb(errorColor.selectedColor, state.errorColor)
        state.warningColor = rgb(warningColor.selectedColor, state.warningColor)
        state.weakWarningColor = rgb(weakColor.selectedColor, state.weakWarningColor)
        state.infoColor = rgb(infoColor.selectedColor, state.infoColor)
        state.showBackground = showBackground.isSelected
        state.backgroundAlpha = backgroundAlpha.number
        state.borderRadius = borderRadius.number
        state.gap = gap.number
        state.italic = italic.isSelected
        state.bold = bold.isSelected
        state.messageTemplate = template.text
        state.errorText = errorText.text
        state.warningText = warningText.text
        state.weakWarningText = weakText.text
        state.infoText = infoText.text
        state.maxLength = maxLength.number
        state.showExtraCount = showExtraCount.isSelected
        state.extraCountMinMessages = extraCountMinMessages.number
        state.excludePattern = exclude.text
        state.delayMs = delay.number
        state.showHoverPopup = showHoverPopup.isSelected
        state.hoverShowDelayMs = hoverShowDelay.number
        state.hoverHideDelayMs = hoverHideDelay.number

        LintVisionService.refreshAllProjects()
    }

    override fun reset() {
        val defaultState = state

        enabled.isSelected = defaultState.enabled
        showErrors.isSelected = defaultState.showErrors
        showWarnings.isSelected = defaultState.showWarnings
        showWeak.isSelected = defaultState.showWeakWarnings
        showInfos.isSelected = defaultState.showInfos
        errorColor.selectedColor = JBColor(defaultState.errorColor, defaultState.errorColor)
        warningColor.selectedColor = JBColor(defaultState.warningColor, defaultState.warningColor)
        weakColor.selectedColor = JBColor(defaultState.weakWarningColor, defaultState.weakWarningColor)
        infoColor.selectedColor = JBColor(defaultState.infoColor, defaultState.infoColor)
        showBackground.isSelected = defaultState.showBackground
        backgroundAlpha.number = defaultState.backgroundAlpha
        borderRadius.number = defaultState.borderRadius
        gap.number = defaultState.gap
        italic.isSelected = defaultState.italic
        bold.isSelected = defaultState.bold
        template.text = defaultState.messageTemplate
        errorText.text = defaultState.errorText
        warningText.text = defaultState.warningText
        weakText.text = defaultState.weakWarningText
        infoText.text = defaultState.infoText
        maxLength.number = defaultState.maxLength
        showExtraCount.isSelected = defaultState.showExtraCount
        extraCountMinMessages.number = defaultState.extraCountMinMessages
        exclude.text = defaultState.excludePattern
        delay.number = defaultState.delayMs
        showHoverPopup.isSelected = defaultState.showHoverPopup
        hoverShowDelay.number = defaultState.hoverShowDelayMs
        hoverHideDelay.number = defaultState.hoverHideDelayMs

        updateEnabledState()
    }

    private fun updateEnabledState() {
        backgroundAlpha.isEnabled = showBackground.isSelected
        borderRadius.isEnabled = showBackground.isSelected
        extraCountMinMessages.isEnabled = showExtraCount.isSelected
    }

    private fun rgb(color: Color?, fallback: Int) = color?.rgb?.and(0xFFFFFF) ?: fallback

}