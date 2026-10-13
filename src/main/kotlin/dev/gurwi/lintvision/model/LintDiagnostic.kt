package dev.gurwi.lintvision.model

data class LintDiagnostic(
    val level: Level,
    val message: String,
    val source: String,
    val code: String,
    val offset: Int
)
