package dev.gurwi.lintvision.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.ColorPanel
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

    private val state get() = LintVisionSettings.getInstance().state

    init {
        showBackground.addItemListener { updateEnabledState() }
        showExtraCount.addItemListener { updateEnabledState() }
    }

    override fun getDisplayName() = "LintVision"

    override fun createComponent(): JComponent {
        val component = panel {
            row { cell(enabled) }
            group("Show severities") {
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
        }
        reset()
        return component
    }

    override fun isModified(): Boolean {
        val s = state
        return enabled.isSelected != s.enabled ||
                showErrors.isSelected != s.showErrors ||
                showWarnings.isSelected != s.showWarnings ||
                showWeak.isSelected != s.showWeakWarnings ||
                showInfos.isSelected != s.showInfos ||
                rgb(errorColor.selectedColor, s.errorColor) != s.errorColor ||
                rgb(warningColor.selectedColor, s.warningColor) != s.warningColor ||
                rgb(weakColor.selectedColor, s.weakWarningColor) != s.weakWarningColor ||
                rgb(infoColor.selectedColor, s.infoColor) != s.infoColor ||
                showBackground.isSelected != s.showBackground ||
                backgroundAlpha.number != s.backgroundAlpha ||
                borderRadius.number != s.borderRadius ||
                gap.number != s.gap ||
                italic.isSelected != s.italic ||
                bold.isSelected != s.bold ||
                template.text != s.messageTemplate ||
                errorText.text != s.errorText ||
                warningText.text != s.warningText ||
                weakText.text != s.weakWarningText ||
                infoText.text != s.infoText ||
                maxLength.number != s.maxLength ||
                showExtraCount.isSelected != s.showExtraCount ||
                extraCountMinMessages.number != s.extraCountMinMessages ||
                exclude.text != s.excludePattern ||
                delay.number != s.delayMs
    }

    override fun apply() {
        val s = state
        s.enabled = enabled.isSelected
        s.showErrors = showErrors.isSelected
        s.showWarnings = showWarnings.isSelected
        s.showWeakWarnings = showWeak.isSelected
        s.showInfos = showInfos.isSelected
        s.errorColor = rgb(errorColor.selectedColor, s.errorColor)
        s.warningColor = rgb(warningColor.selectedColor, s.warningColor)
        s.weakWarningColor = rgb(weakColor.selectedColor, s.weakWarningColor)
        s.infoColor = rgb(infoColor.selectedColor, s.infoColor)
        s.showBackground = showBackground.isSelected
        s.backgroundAlpha = backgroundAlpha.number
        s.borderRadius = borderRadius.number
        s.gap = gap.number
        s.italic = italic.isSelected
        s.bold = bold.isSelected
        s.messageTemplate = template.text
        s.errorText = errorText.text
        s.warningText = warningText.text
        s.weakWarningText = weakText.text
        s.infoText = infoText.text
        s.maxLength = maxLength.number
        s.showExtraCount = showExtraCount.isSelected
        s.extraCountMinMessages = extraCountMinMessages.number
        s.excludePattern = exclude.text
        s.delayMs = delay.number

        LintVisionService.refreshAllProjects()
    }

    override fun reset() {
        val s = state
        enabled.isSelected = s.enabled
        showErrors.isSelected = s.showErrors
        showWarnings.isSelected = s.showWarnings
        showWeak.isSelected = s.showWeakWarnings
        showInfos.isSelected = s.showInfos
        errorColor.selectedColor = Color(s.errorColor)
        warningColor.selectedColor = Color(s.warningColor)
        weakColor.selectedColor = Color(s.weakWarningColor)
        infoColor.selectedColor = Color(s.infoColor)
        showBackground.isSelected = s.showBackground
        backgroundAlpha.number = s.backgroundAlpha
        borderRadius.number = s.borderRadius
        gap.number = s.gap
        italic.isSelected = s.italic
        bold.isSelected = s.bold
        template.text = s.messageTemplate
        errorText.text = s.errorText
        warningText.text = s.warningText
        weakText.text = s.weakWarningText
        infoText.text = s.infoText
        maxLength.number = s.maxLength
        showExtraCount.isSelected = s.showExtraCount
        extraCountMinMessages.number = s.extraCountMinMessages
        exclude.text = s.excludePattern
        delay.number = s.delayMs
        updateEnabledState()
    }

    private fun updateEnabledState() {
        backgroundAlpha.isEnabled = showBackground.isSelected
        borderRadius.isEnabled = showBackground.isSelected
        extraCountMinMessages.isEnabled = showExtraCount.isSelected
    }

    private fun rgb(color: Color?, fallback: Int) = color?.rgb?.and(0xFFFFFF) ?: fallback

}