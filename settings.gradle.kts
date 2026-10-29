// Forager-iOS's Gradle build. Its one module is the BRouter port (dispatch 2026-10-28-01; Phase 1 report, item C).
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "forager-ios"
include(":brouter")
