package dev.gurwi.lintvision.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ColorPanel
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.*
import dev.gurwi.lintvision.service.LintVisionService
import kotlin.reflect.KMutableProperty0

class LintVisionConfigurable : BoundConfigurable("LintVision") {

    private val state get() = LintVisionSettings.getInstance().state

    override fun createPanel(): DialogPanel = panel {
        val s = state
        lateinit var showBackground: Cell<com.intellij.ui.components.JBCheckBox>
        lateinit var showExtraCount: Cell<com.intellij.ui.components.JBCheckBox>

        row { checkBox("Enable LintVision").bindSelected(s::enabled) }

        group("Show Severities") {
            row { checkBox("Errors").bindSelected(s::showErrors) }
            row { checkBox("Warnings").bindSelected(s::showWarnings) }
            row { checkBox("Weak warnings").bindSelected(s::showWeakWarnings) }
            row { checkBox("Information").bindSelected(s::showInfos) }
        }

        group("Appearance") {
            row("Error color:") { cell(ColorPanel()).bindRgb(s::errorColor) }
            row("Warning color:") { cell(ColorPanel()).bindRgb(s::warningColor) }
            row("Weak warning color:") { cell(ColorPanel()).bindRgb(s::weakWarningColor) }
            row("Information color:") { cell(ColorPanel()).bindRgb(s::infoColor) }
            row { showBackground = checkBox("Show background").bindSelected(s::showBackground) }
            row("Background tint (%):") { spinner(0..100).bindIntValue(s::backgroundAlpha) }
                .enabledIf(showBackground.selected)
            row("Border radius (px):") { spinner(0..20).bindIntValue(s::borderRadius) }
                .enabledIf(showBackground.selected)
            row("Gap before message (spaces):") { spinner(0..40).bindIntValue(s::gap) }
                .rowComment("0 attaches the message directly to the end of the code")
            row {
                checkBox("Italic").bindSelected(s::italic)
                checkBox("Bold").bindSelected(s::bold)
            }
        }

        group("Message") {
            row("Template:") {
                textField().columns(30).bindText(s::messageTemplate)
                    .comment("Variables: \$message, \$count, \$severity, \$source, \$code. Whitespace is kept as typed.")
            }
            row("Error text:") { textField().columns(12).bindText(s::errorText) }
            row("Warning text:") { textField().columns(12).bindText(s::warningText) }
            row("Weak warning text:") { textField().columns(12).bindText(s::weakWarningText) }
            row("Information text:") { textField().columns(12).bindText(s::infoText) }
            row("Max message length (0 = unlimited):") { spinner(0..1000).bindIntValue(s::maxLength) }
        }

        group("Behavior") {
            row { showExtraCount = checkBox("Show \"(+N)\" when a line has more messages").bindSelected(s::showExtraCount) }
            row("Show \"(+N)\" when a line has at least this many messages:") {
                spinner(2..100).bindIntValue(s::extraCountMinMessages)
            }.enabledIf(showExtraCount.selected)
            row("Hide messages matching regex:") { textField().columns(30).bindText(s::excludePattern) }
            row("Update delay (ms):") { spinner(0..5000).bindIntValue(s::delayMs) }
        }

        group("Hover Popup") {
            row { checkBox("Show hover popup").bindSelected(s::showHoverPopup) }
            row("Show delay (ms):") { spinner(0..5000).bindIntValue(s::hoverShowDelayMs) }
            row("Hide delay (ms):") { spinner(0..5000).bindIntValue(s::hoverHideDelayMs) }
        }
    }

    override fun apply() {
        super.apply()
        LintVisionService.refreshAllProjects()
    }

    private fun Cell<ColorPanel>.bindRgb(prop: KMutableProperty0<Int>): Cell<ColorPanel> {
        val mutable = prop.toMutableProperty()
        return bind(
            componentGet = { it.selectedColor?.rgb?.and(0xFFFFFF) ?: mutable.get() },
            componentSet = { panel, value -> panel.selectedColor = JBColor(value, value) },
            prop = mutable,
        )
    }

}