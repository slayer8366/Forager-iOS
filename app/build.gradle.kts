// The app (A1-1; pre-build sections 3 and 7, RECORD -201). iOS only for now: iosArm64 for devices, iosSimulatorArm64 for
// the runner's simulator; no iosX64, as :brouter. No dependency on :brouter until Phase C (T1).
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        // Static (T7): it links into the app executable, so nothing is embedded or signed at run time. A technical
        // name, not the app's display name.
        target.binaries.framework {
            baseName = "ForagerApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
        }
    }
}
