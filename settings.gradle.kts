// Forager-iOS's Gradle build: the BRouter port (dispatch 2026-10-28-01; Phase 1 report, item C) and, beside it, the app (A1-1).
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        // Google's Maven, for the androidx artifacts Compose Multiplatform's iOS variants pull (A1-1; pre-build section 2,
        // RECORD -201 T3). Filtered to androidx groups, so every other group resolves from Maven Central only, as before.
        google {
            content { includeGroupByRegex("androidx\\..*") }
        }
        mavenCentral()
    }
}

rootProject.name = "forager-ios"
include(":brouter")
include(":app")
