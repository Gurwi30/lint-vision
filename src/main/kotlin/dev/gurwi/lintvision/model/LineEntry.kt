package dev.gurwi.lintvision.model

data class LineEntry(
    val offset: Int,
    val diagnostics: List<LintDiagnostic>
) {

    val primary: LintDiagnostic get() = diagnostics.first()

}
