import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
plugins { kotlin("multiplatform"); `maven-publish` }
group = "probe"
version = providers.gradleProperty("tag").get()
val pinLv = providers.gradleProperty("lv").orNull          // e.g. "2.3": languageVersion and apiVersion
val coreLibs = providers.gradleProperty("coreLibs").orNull  // e.g. "2.3.10": the stdlib version the POM names
val withIos = providers.gradleProperty("ios").orNull == "true"
kotlin {
    jvmToolchain(21)
    jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }   // Forager's app: JvmTarget.JVM_17
    if (withIos) { iosArm64(); iosSimulatorArm64() }
    if (coreLibs != null) coreLibrariesVersion = coreLibs
    if (pinLv != null) compilerOptions {
        languageVersion.set(KotlinVersion.fromVersion(pinLv))
        apiVersion.set(KotlinVersion.fromVersion(pinLv))
    }
}
publishing { repositories { maven { name = "probeRepo"; url = uri(providers.gradleProperty("repo").get()) } } }
