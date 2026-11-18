// The plugins are declared once here and applied in the module that uses it.
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    // :app only (A1-1, RECORD -201 T2).
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}
