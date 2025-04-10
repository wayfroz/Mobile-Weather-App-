plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    kotlin(libs.plugins.kotlin.serialization.get().pluginId).version(libs.versions.kotlin.serialization).apply(false)
}