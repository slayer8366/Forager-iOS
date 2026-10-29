import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "probe.consumer"
    compileSdk = 35            // the SDK in this container; Forager uses 37
    buildToolsVersion = "35.0.0"
    defaultConfig { applicationId = "probe.consumer"; minSdk = 26; targetSdk = 35 }   // Forager minSdk 26
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
dependencies { implementation("probe:portprobe:" + providers.gradleProperty("tag").get()) }
