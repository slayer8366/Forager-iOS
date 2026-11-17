# Session 78: review 5's four fixes (S1, S2, S3, N4)

Coder Plumb Ledger, session 78. Branch `brouter-port-5b`, base `e7032774aae2626faeeee8ee2075baf34722a93c` (checked:
`git fetch origin brouter-port-5b`, then `git rev-parse HEAD origin/brouter-port-5b` both gave it; `git status --porcelain`
printed nothing). The review is `docs/reviews/2026-11-17-review-5-cosine-rows.md` in the review clone (read there at
`d89108f`, not copied here). Marks: [C] confirmed by a run or a read named here, [I] inferred, [U] unverified. No coordinate
and no node id is written here.

## 1. Prediction, written before any code commit

No test is added or removed, so the counts of run 44020606098 (head `31939ed`) should not change on the last code commit:

| job | `TOTAL:` lines (tests) | `^e: ` | ` e: ` | gate lines |
|---|---|---:|---:|---|
| `jvm` | 262 | 0 | 0 | none (no gate step) |
| `jvm-production` | 3 | 0 | 0 | new: `OK: all 2 classes ran` |
| `ios-smoke` | 61 (simulator) and 7 (arm64 JVM step) | 0 | 0 | `OK on the iOS simulator: all 25 classes ran`; `OK: all 2 classes ran` (arm64) |
| `ios-memory` | 3, 1, 1 | 0 | 0 | the memory groups and R ran, unchanged |

All four jobs green; the `cosrows production record: COS...` lines' last column 0 in all 8 rows, now asserted.

## 2. Results

Filled in after the code commits (section 3 onward).
