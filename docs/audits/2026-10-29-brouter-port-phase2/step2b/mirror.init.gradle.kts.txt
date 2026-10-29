// Local only (not committed): Maven Central answered 429 to this container; Google's mirror of Central serves the same artifacts.
val mirror = "https://maven-central.storage-download.googleapis.com/maven2/"
beforeSettings {
    pluginManagement.repositories { maven(mirror); gradlePluginPortal() }
    dependencyResolutionManagement.repositories { maven(mirror) }
}
allprojects { repositories { maven(mirror) } }
