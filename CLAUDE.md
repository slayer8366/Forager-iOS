# CLAUDE.md

Forager's engineering principles govern this repository too. Read them from source before any work:
`CLAUDE.md` in [slayer8366/Forager](https://github.com/slayer8366/Forager), branch `pre-main`. They are not
copied here, so the two cannot drift. That includes every rule in its Known pitfalls, the rule on the
Restricted Object among them.

What differs here, until the owner rules otherwise:

- **The record is this repository's own** (`RECORD.md` here), not Forager's. Nothing for the iOS app is
  written to Forager's record or to slayer8366/Forager.
- **Records and dispatches live on `main`;** code is built on its own branch and merged into `main` only on
  the owner's word.
- **iOS builds run in CI on GitHub's macOS runners.** The owner has no Mac and no iPhone, so every iOS check
  is a simulator check and is reported as "simulator-checked", never as "checked".

These three points are the planner's calls from 2026-10-28 (RECORD -1), except the last line, which
records the owner's rulings (RECORD -2).
