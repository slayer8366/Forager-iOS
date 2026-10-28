plugins { kotlin("multiplatform") version "2.3.10" }
kotlin {
    jvmToolchain(21)
    jvm()
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            implementation("com.squareup.okio:okio:3.18.2")
            implementation("org.jetbrains.kotlinx:atomicfu:0.33.0")
        }
        jvmTest.dependencies { implementation(kotlin("test")) }
    }
}
tasks.withType<Test> { systemProperty("tile", "/home/user/forager/app/src/test/resources/routing/segments/W75_N40.rd5") }
