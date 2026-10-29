pluginManagement {
    repositories { google(); maven("https://maven-central.storage-download.googleapis.com/maven2/"); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { maven { url = uri(providers.gradleProperty("repo").get()) }; google(); maven("https://maven-central.storage-download.googleapis.com/maven2/") }
}
rootProject.name = "android-consumer"
include(":app")
