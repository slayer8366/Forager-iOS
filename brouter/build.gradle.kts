// The BRouter port: one KMP library module (Phase 1 report, item C). Kotlin 2.3.10 exactly; targets jvm (JVM target 17),
// iosArm64 and iosSimulatorArm64; no androidTarget and no iosX64 (item C, "Targets"; item G, "The macOS label").
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

// Item D: the oracle is BRouter 1.7.10's read-only release jar, fetched and checked by tools/fetch-oracle.sh into
// build/oracle. Nothing is committed. Only jvmTest sees it; it never reaches commonMain or the iOS targets.
val oracleDir = layout.buildDirectory.dir("oracle")
val oracleJar = oracleDir.map { it.file("brouter-1.7.10-ro.jar") }
val fetchOracle by tasks.registering(Exec::class) {
    description = "Fetches BRouter 1.7.10's release zip and its -ro jar, refusing either unless it matches its pins."
    val scripts = rootProject.layout.projectDirectory.dir("tools")
    inputs.file(scripts.file("fetch-oracle.sh"))
    inputs.file(scripts.file("fetch-pinned.sh"))
    outputs.dir(oracleDir)
    commandLine("bash", scripts.file("fetch-oracle.sh").asFile.absolutePath, oracleDir.get().asFile.absolutePath)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // api: okio's Path is in the entry point's signatures (item C, "what the port exposes").
            api(libs.okio)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            implementation(files(oracleJar).builtBy(fetchOracle))
        }
    }
}

tasks.named<Test>("jvmTest") {
    dependsOn(fetchOracle)
    systemProperty("brouter.oracle.dir", oracleDir.get().asFile.absolutePath)
    // The planner's call (RECORD -24): the oracle runs with libm's intrinsics off, so HotSpot's Math.cos is fdlibm's.
    jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:-UseLibmIntrinsic")
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}
