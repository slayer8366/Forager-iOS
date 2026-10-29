pluginManagement {
    repositories {
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) useVersion(providers.gradleProperty("kv").get())
        }
    }
}
dependencyResolutionManagement {
    repositories { maven("https://maven-central.storage-download.googleapis.com/maven2/"); mavenCentral() }
}
rootProject.name = "portprobe"
