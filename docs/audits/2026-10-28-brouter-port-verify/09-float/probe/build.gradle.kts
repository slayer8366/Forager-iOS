import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
plugins { kotlin("multiplatform") version "2.3.10" }
kotlin {
    jvmToolchain(21)
    jvm()
    linuxX64 { binaries { executable(listOf(NativeBuildType.DEBUG, NativeBuildType.RELEASE)) { entryPoint = "probe.main" } } }
    linuxArm64 { binaries { executable(listOf(NativeBuildType.DEBUG, NativeBuildType.RELEASE)) { entryPoint = "probe.main" } } }
    sourceSets {
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
