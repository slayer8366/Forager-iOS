# Forager-iOS

The iOS app of Forager, built with Kotlin Multiplatform and Compose Multiplatform. The Android app lives
in [slayer8366/Forager](https://github.com/slayer8366/Forager) and is not changed by work here.

This repository keeps its own record, separate from Forager's. The owner, 2026-10-28, verbatim: "Option 2.
But keep the record separate. Start the record for our iOS app with this port, the Android version stays
separate. Save work on Forager-iOS repo".

- **`RECORD.md`**: every intent, ruling and outcome for the iOS app, append-only.
- **`prompts/preserved/`**: each dispatch, as the coder reads it.
- **`docs/audits/`**: reports, filed as written.
- **Why iOS is built this way:** Forager's `docs/audits/2026-10-26-ios-multiplatform-feasibility-report.md`
  (on Forager's `pre-main`) holds the evidence and every ruling this record starts from.

The first work is a Kotlin port of the BRouter routing engine, which Forager's Android app runs as Java.
