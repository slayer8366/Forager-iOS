Item C evidence (part 3, 2026-10-29). Scratch lived in <scratch>/part3/kv; no jar, klib or build output is kept here.
- 00-bases-part3.txt: Forager pre-main moved e2969abe4 -> d100a4776 (32 commits); no premise path changed.
- kotlin-probe/run.sh: producers (a KMP library by Kotlin 2.3.10 with iOS targets, 2.3.21, 2.4.21, and 2.4.21 pinned
  to language/api 2.3 with coreLibrariesVersion 2.3.10) published to a file repo; Kotlin/JVM consumers (jvmTarget 17)
  at 2.3.10 and, as the negative control, 2.2.21, each run after `clean`. Gradle 9.7.0 throughout.
- kotlin-probe/run-android.sh: an Android app consumer at Forager's settings (AGP 9.3.1, kotlin-android 2.3.10,
  builtInKotlin=false, newDsl=false, jvmTarget 17, minSdk 26; compileSdk 35, the only platform in this container),
  `clean :app:assembleDebug` against three of the producers.
- kotlin-probe/logs/summary.txt, android-summary.txt: one line per run, with the predictions written before the runs.
- kotlin-probe/logs/metadata-versions.txt: the @kotlin.Metadata mv and class-file major read from each producer's jar
  with javap, the stdlib each POM names, and the negative control's error on the probe's own jar.
- okhttp/: OkHttp 4.12.0's POM and Gradle module, read for the okio version it requires (3.6.0).
