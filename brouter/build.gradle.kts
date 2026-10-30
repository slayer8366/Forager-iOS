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

// Step 2b (Amendment 3, Part B): bionic's msun cos and atan2 built from their pinned C sources with Android's flags, the
// reference the port is compared against. tools/msun-ref/build-ref.sh fails unless the sources match their pins, clang
// and gcc agree, and the outputs carry the research's SHA-256. Only jvmTest reads it; commonTest reads the committed
// copy (MsunReference.kt), which jvmTest checks against this build line for line.
val msunRefDir = layout.buildDirectory.dir("msun-ref")
val msunReference by tasks.registering(Exec::class) {
    description = "Builds bionic's msun cos and atan2 from pinned sources and runs them over the step 2b inputs."
    val refTools = rootProject.layout.projectDirectory.dir("tools/msun-ref")
    inputs.dir(refTools)
    inputs.file(rootProject.layout.projectDirectory.file("tools/fetch-pinned.sh"))
    outputs.dir(msunRefDir)
    commandLine("bash", refTools.file("build-ref.sh").asFile.absolutePath, msunRefDir.get().asFile.absolutePath)
}

// Item I, step 1 (Amendment 3 Part B; RECORD -45): the four brouter.de tiles and F0's fixture, fetched from the
// Forager-iOS release by tools/fetch-tiles.sh into build/tiles, each refused unless its size and SHA-256 match
// tools/tile-pins.tsv (a byte-for-byte copy of the release's own pin file). Nothing is committed. jvmTest is the first
// task whose tests read a tile (tiles/TilePinsTest), so the fetch hangs off it; only jvmTest sees the tiles.
val tilesRelease = "https://github.com/slayer8366/Forager-iOS/releases/download/brouter-tiles-2026-10-30"
val tilesDir = layout.buildDirectory.dir("tiles")
val tilePins = rootProject.layout.projectDirectory.file("tools/tile-pins.tsv")
val fetchTiles by tasks.registering(Exec::class) {
    description = "Fetches the pinned tiles from the brouter-tiles-2026-10-30 release, refusing any that differs from its pin."
    val scripts = rootProject.layout.projectDirectory.dir("tools")
    inputs.file(scripts.file("fetch-tiles.sh"))
    inputs.file(scripts.file("fetch-pinned.sh"))
    inputs.file(tilePins)
    inputs.property("release", tilesRelease)
    outputs.dir(tilesDir)
    commandLine(
        "bash",
        scripts.file("fetch-tiles.sh").asFile.absolutePath,
        tilesDir.get().asFile.absolutePath,
        tilesRelease,
        tilePins.asFile.absolutePath,
    )
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
    dependsOn(fetchOracle, msunReference, fetchTiles)
    systemProperty("brouter.oracle.dir", oracleDir.get().asFile.absolutePath)
    systemProperty("brouter.tiles.dir", tilesDir.get().asFile.absolutePath)
    systemProperty("brouter.tiles.pins", tilePins.asFile.absolutePath)
    systemProperty("brouter.msunref.dir", msunRefDir.get().asFile.absolutePath)
    // The planner's call (RECORD -24): the oracle runs with libm's intrinsics off, so HotSpot's Math.cos is fdlibm's.
    // -XX:-UseLibmIntrinsic exists only on x86 (RECORD -21). An aarch64 HotSpot refuses it and does not start: seen on
    // macos-26 (RECORD -37; tools/jvm-probe/probe.sh): "Unrecognized VM option 'UseLibmIntrinsic'", exit 1. aarch64's own
    // switch is -XX:DisableIntrinsic=_dcos (RECORD -21); on that runner it takes Math.cos from 11,628 of 200,000 inputs
    // unequal to StrictMath.cos (fdlibm) to 0, interpreted and JIT-compiled alike. The test JVM is the JVM running Gradle
    // (no toolchain is set), so that JVM's os.arch picks. Any other arch is refused rather than run with a guessed flag.
    when (val arch = providers.systemProperty("os.arch").get()) {
        "amd64", "x86_64" -> jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:-UseLibmIntrinsic")
        "aarch64" -> jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:DisableIntrinsic=_dcos")
        else -> throw GradleException("brouter jvmTest: no libm intrinsic setting for os.arch '$arch' (RECORD -37)")
    }
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}
