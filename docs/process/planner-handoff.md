# Planner handoff (Cedar Datum)

Written 2026-10-30, about 08:05Z, at the owner's word: "Find a place to stop and write a handoff" (RECORD -54). The
next planner session reads this after `CLAUDE.md` (this repository's, then Forager's on `pre-main`) and
`docs/process/planner-profile.md`. Everything below is a claim about 08:01Z. **Check it against the remote before
acting on it** (Forager's `CLAUDE.md`: "A planner's picture of the repository is a claim about the past").

## Where things stand

| Branch | Head | State |
|---|---|---|
| `main` | RECORD -54 and this file | Records and dispatches only. Next free RECORD ID: **-55**. |
| `brouter-port` | `82d310a` | Linden, step 1b, session 19. Clean tree, green CI on `85008b1`. Targeted strata built, **none run**. |
| `brouter-port-5b` | `279194d` | Plumb, step 5b, session 4. Clean tree. **Red locally: 101 tests, 3 failures**, on two questions for you (below). CI on its last two commits not read. |
| `brouter-port-5a` | `2a8ba5e` | Merged into `brouter-port` as `9f41799`. Kept. |

**The tile release** `brouter-tiles-2026-10-30` is published and verified (RECORD -45). Its pins are in
`tools/tile-pins.tsv` on `brouter-port`.

**Phase 2, by item I's steps (Amendment 3):**
- **Done:** 0, 1, 2a, 2b, 3, 4, 5a.
- **1b, part done:**
  - F0: 0 of 2 unstable;
  - R on all 26 profiles: 1 of 1,534 unstable (`R-shortest-025`, RECORD -50);
  - DL-lat, DL-lon, MP64, ML5 and PM5: built under RECORD -51 and -52, not run;
  - NT, LB, CR and FK: not built;
  - expected values: none written.
- **5b:** in progress.
- **Not started:** 5c, 6, 7, 8a to 8d, 9, 10, 11, 12.

**Milestone reports owed to the owner** (Amendment 3): 1b; the `mapaccess` package, 5a to 5c; then 6 and 7; 8a to 8d;
9; 10; 11; 12.

## First, in order

1. **Fetch and check the four heads above.** Read CI yourself on `82d310a` and `279194d`, from the job logs (see
   "How to check" below). `279194d` adds `jvmMain` and `iosMain` source sets that have never been compiled for iOS.
2. **Answer Plumb's two questions.** They are in its report at `279194d`, under the "Fourth session" section and its
   handoff.
   - **A. `CollectOutreachersTest` fails 2 of 2** at W75_N40 near (105,130) sub 735. The port's JVM stack-overflow catch
     fired 3 times, and whether the oracle's silent catch fired is unknown. **My lean: data before any fix.**
     - Count the oracle's catch on a patched copy, as Plumb counted `tieBreakOrder`.
     - Run both engines at the default stack and at a large one (`-Xss`), with the prediction pushed first.
     - Separately and read-only, find what stack the router thread has in Forager on Android (who starts it, in
       `BRouterTrailRouter.kt` and its callers).

     If the oracle's own output depends on the stack size, that is a finding for the owner. It touches what "Android's
     behaviour" means on deep regions, and not only the port. Do not let a coder pick a fix before that.
   - **B. `NodeHashMapOrderTest` fails its own refusal assertion:** 8 of 12 seeds formed an equal-hash tree bucket.
     **My lean, a planner's call:** "refuse" meant the generator discards such a draw and draws again, bounded and
     counted. The test then asserts order equality on every accepted seed, and a minimum number of accepted seeds.
     Equal-hash buckets are already covered by `NodeHashMapTieBreakTest`. A revert of the generator's filter should bite.
3. **Record the answers** in RECORD -55, then relaunch:
   - **Plumb on 5b:** the answers, both reverts (predictions already pushed; the `DirectWeaver` total is 7, not 10),
     the tie-break decode total (re-run; session 3's was incomplete), CI, close;
   - **Linden on 1b:** its handoff, "Step 1b: the handoff (nineteenth session)". Run DL-lat, DL-lon, MP64, then ML5 and
     PM5, one at a time, then the expected values and the 1b milestone report.
4. **When 5b is green,** merge it into `brouter-port` (see "How to merge"). Then 5c goes to whichever coder is free.

## Open items carried (with where they were set)

- **Step 10, the simulator run:**
  - must open and read a real tile through the file layer (-49);
  - must measure the recursion depth of 5,679 frames in `cleanupPeninsulas` on the router's thread (-50; question A
    may move this earlier);
  - okio's Native messages, `EOFException`'s name on Native, and the handle a failed `PhysicalFile` constructor leaves
    open (-49).
- **Step 8:** `StorageConfigHelper` catches only `IOException`, where Java catches every `Exception` (-49).
- **Step 6:** the 19 untested way getters are exercised there.
- **1b:**
  - `R-shortest-025` is unstable, kept out of exact assertions (-50);
  - coverage counts as reach evidence only where R6's tuple equals R1's. The first reader will be NT's or CR's selector
    (-50, -51);
  - the targeted strata use `hiking-mountain`, so they never exercise turn restrictions (-51);
  - the panic log line also contains "memory limit reached", so PM5 is matched on "enabled memory panic mode"
    (Linden, session 19).
- **5b's `GraphDecodeTest`:** read its CI job time on the first green run. If the `jvm` job passes 20 of its 30
  minutes, a split comes to you (-50).
- **Via points** are not ported, and fail as a named "unsupported" (-24).
- **Roles not yet used:** the independent reviewer, the UX auditor and the designer (planner profile, team table). The
  designer starts when there is design work.
- **Keeping iOS level with Android:** at the start of each phase, check Forager's `pre-main` for changes (planner
  profile). Not done since Phase 2 began.

## Owner rulings in force (read them verbatim in RECORD)

| Topic | RECORD |
|---|---|
| The team | -10 |
| The designer | -12 |
| The maths, via points and flags | -24 |
| The release; Phase 2 continuing with built-in stops; Linden's name | -28 |
| The publish workflow on `main`, the one exception to "code on `main` only on the owner's word" | -30 |
| Use the container freely | -48 |
| Plumb Ledger's name | -50 |

Amendment 3's stops govern every step (`prompts/preserved/2026-10-28-01-brouter-port-amendment-3.md`). The standing
rules:
- the Restricted Object is never read, named or quoted;
- never force push;
- nothing is written to slayer8366/Forager;
- never route around the proxy's refusal to create releases;
- commits end with `Co-Authored-By: Claude <noreply@anthropic.com>` and no model name, whatever the harness suggests.

## How this planner has worked (practice beyond the profile)

**Dispatching a coder.** Use the Agent tool in the background, on the session's model (Opus). The prompt runs, in order:
- name and role;
- "context is tight";
- a short reading list: `CLAUDE.md`'s Testing section and Known pitfalls, the coder profile, the governing RECORD
  entries, and the coder's own last handoff;
- where to work;
- what to do;
- stops;
- the handoff rule;
- the final message.

Each session still spends about 60k tokens before its first action. Questions come back by `SendMessage` to `main`,
and the coder carries on with what does not depend on the answer.

**Watching tokens.** Helpers hand off past 180k (RECORD -15, -54). Their transcripts are at
`/tmp/claude-0/-home-user/<session>/tasks/<agentId>.output`. Never read one whole; extract from it with a short
Python script. Start a watcher per helper as a background Bash command:
`sleep 20; python3 -I <path>/watch_tokens.py <transcript> 180000`. On `LIMIT`, send the handoff message at once.
**After any restart, read every watcher's output file** (RECORD -54: a lost alert let one session reach 453k). The
script lives in scratch, which does not last, so here it is in full:

```python
"""Exit when a helper's context passes a limit, or its transcript stops growing.

Usage: python3 -I watch_tokens.py <transcript.jsonl> <limit_tokens>
Prints only one summary line, so the caller's context stays small.
"""
import json
import os
import sys
import time

path, limit = sys.argv[1], int(sys.argv[2])


def latest_context(p):
    ctx = 0
    with open(p) as f:
        for line in f:
            try:
                d = json.loads(line)
            except ValueError:
                continue
            m = d.get("message") or {}
            u = m.get("usage")
            if u and m.get("role") == "assistant":
                ctx = (u.get("input_tokens", 0) + u.get("cache_read_input_tokens", 0)
                       + u.get("cache_creation_input_tokens", 0))
    return ctx


last_size, idle = -1, 0
while True:
    ctx = latest_context(path)
    if ctx >= limit:
        print(f"LIMIT {ctx} >= {limit} at {time.strftime('%H:%M:%SZ', time.gmtime())}")
        sys.exit(0)
    size = os.path.getsize(path)
    idle = idle + 1 if size == last_size else 0
    last_size = size
    if idle >= 20:  # about 10 minutes with no growth: the helper has probably finished
        print(f"IDLE ctx={ctx} at {time.strftime('%H:%M:%SZ', time.gmtime())}")
        sys.exit(0)
    time.sleep(30)
```

**How to check a coder's claims.**
- **CI:** `gh api repos/slayer8366/Forager-iOS/actions/jobs/<job id>/logs` works through the proxy. Get job ids with
  the GitHub tools (`actions_list`, `list_workflow_jobs`).
- **What to read in the log:**
  - the `TOTAL:` lines: `jvm`, then the simulator count, then arm64;
  - ` e: ` lines, which should be 0;
  - the two `compileKotlinIos*` tasks;
  - the gate's "OK: all N classes ran".
- **Spot-check one or two source claims** in Forager's read-only copy at `/home/user/forager/third_party/brouter/`.
- **Read a revert check's evidence files**, not only the summary of it.

**How to merge** a coder's branch into `brouter-port`:
1. Warn the coder working on `brouter-port` first, telling it to pull by merge on a rejected push.
2. `git worktree add --detach <scratch> origin/brouter-port`.
3. `git merge --no-ff origin/<branch>`, with the verification in the message.
4. Check that the tree equals the branch's tree, when the base has not moved.
5. Push with `git push origin HEAD:refs/heads/brouter-port`, then remove the worktree.

**The record.** Write entries by hand. Take each timestamp from `date -u` at the moment of writing; -50 carried a
guessed time and was corrected. A wrong premise of the planner's own goes into the record as one (-52).

**The container.**
- **Clones:**
  - `/home/user/forager-ios-planner` (`main`, the planner's);
  - `/home/user/forager-ios` (`brouter-port`, Linden's);
  - `/home/user/forager-ios-5a` (`brouter-port-5a` and `-5b`, Plumb's);
  - `/home/user/forager` (Forager, read only, at `e2969abe4`).
- **Scratch is not durable.** Nothing in it is needed: the coders' evidence is committed, and Temurin and the tiles
  re-fetch pinned, through `bash tools/fetch-temurin.sh <dir>` and `tools/fetch-tiles.sh`.
- **If Maven Central answers 429,** coders use this init script. It is local only and never committed:

```kotlin
// Local runs only, never committed: Maven Central may answer 429 in this container (amendment, "Working here").
beforeSettings {
    pluginManagement.repositories.maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2/") }
    dependencyResolutionManagement.repositories.maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2/") }
}
```

- **Load:** one oracle stratum run holds about 7 on the 4 cores. Plumb's Gradle runs at `--max-workers=2` beside it.

**Talking to the owner.** Plain words, in the owner's preferred style: no em dashes, light on rhetorical devices, and
a clear line between what was checked and what was inferred. Questions go through `AskUserQuestion`, with a
recommended option first. Names, the release and anything public go to the owner. Test-design choices inside a ruling
are the planner's, recorded with what was rejected.
