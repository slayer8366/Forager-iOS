import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins { kotlin("jvm"); application }
val vendored = providers.gradleProperty("vendored").orNull   // a path: compile the port's common source here instead
kotlin {
    jvmToolchain(21)
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }       // as Forager's app
}
java { targetCompatibility = JavaVersion.VERSION_17; sourceCompatibility = JavaVersion.VERSION_17 }
dependencies {
    if (vendored == null) implementation("probe:portprobe:" + providers.gradleProperty("tag").get())
}
if (vendored != null) sourceSets["main"].kotlin.srcDir(vendored)
application { mainClass.set("MainKt") }
