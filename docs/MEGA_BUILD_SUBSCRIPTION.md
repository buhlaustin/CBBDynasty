# MEGA build queue subscription

**Purpose:** One Cloud Agent turn per **open** `MEGA-*` row until the queue is empty, then **stop** the timer subscription.

**Integration rule:** **Every implementation commit lands on one branch and one pull request.** Do not open per-item PRs.

| Constant | Value |
|----------|--------|
| Integration branch | `cursor/mega-build-executor-cb09` |
| Base branch | `main` |
| Integration PR | See `MEGA_BUILD_QUEUE_STATE.json` → `pullRequestUrl` |

## Artifacts

| File | Role |
|------|------|
| [`MEGA_BUILD_QUEUE.json`](MEGA_BUILD_QUEUE.json) | Ordered **open implementation** items (excludes `MEGA-QA-*`, `MEGA-NOTES-*`, `MEGA-POST-*`; shards unchanged) |
| [`MEGA_BUILD_QUEUE_STATE.json`](MEGA_BUILD_QUEUE_STATE.json) | Queue cursor + **integration branch / PR** |
| [`planning/tools/mega_queue.py`](../planning/tools/mega_queue.py) | Build / current / complete / **`integration`** |
| Timer name | `mega-build-queue` |

## Git & PR (mandatory every wake)

1. `git fetch origin`
2. **`git checkout cursor/mega-build-executor-cb09`** and `git pull origin cursor/mega-build-executor-cb09`  
   - If the branch does not exist locally: `git checkout -b cursor/mega-build-executor-cb09 origin/cursor/mega-build-executor-cb09`
3. Merge `origin/main` only when you need docs/tooling from main; **never** push implementation to `main` directly.
4. `python3 planning/tools/mega_queue.py integration` — confirm branch + PR URL.
5. After each item: **commit and `git push -u origin cursor/mega-build-executor-cb09` only.
6. **`ManagePullRequest` `update_pr`** on the integration PR (`branch_name: cursor/mega-build-executor-cb09`) — append completed `MEGA-*` id and summary.  
   - **`create_pr` is forbidden** for queue items unless the integration PR was deleted and must be recreated once.

## On each timer wake (mandatory)

1. Complete **Git & PR** steps above.
2. `python3 planning/tools/mega_queue.py resync`
3. If output is **`QUEUE_EMPTY`**: `cursor-subscriptions-list_subscriptions` → `unsubscribe` **`mega-build-queue`** → end turn.
4. Else implement **only** the current item (`mega_queue.py current`):
   - **Full product — nothing deferred.**
   - Test; set shard **Status** → `PASS` or `SHIP`.
5. `python3 planning/tools/mega_queue.py complete --id <MEGA-…>`
6. Commit shard + code on **`cursor/mega-build-executor-cb09`**, push, **`update_pr`** on the integration PR.
7. If **`QUEUE_EMPTY`** → unsubscribe **`mega-build-queue`** and stop.
8. Else re-subscribe timer (`once: true`, `delaySeconds: 60`) with the prompt below.

## Timer prompt (copy for `subscribe_timer`)

```text
MEGA BUILD QUEUE — next item. SINGLE PR ONLY.

Follow docs/MEGA_BUILD_SUBSCRIPTION.md.
Checkout and pull cursor/mega-build-executor-cb09 only (never push MEGA work to main).
python3 planning/tools/mega_queue.py integration && resync
If QUEUE_EMPTY: unsubscribe mega-build-queue and STOP.
Else implement current MEGA item (full product), shard Status PASS/SHIP, complete --id, test.
Commit push origin cursor/mega-build-executor-cb09 ONLY.
ManagePullRequest update_pr branch_name cursor/mega-build-executor-cb09 (never create_pr per item).
Re-subscribe mega-build-queue once delaySeconds 60 if work remains; unsubscribe if QUEUE_EMPTY.
```

## Refresh queue after manual shard edits

```bash
python3 planning/tools/mega_queue.py build
python3 planning/tools/mega_queue.py resync
```

## Policy

- **One PR** carries the entire MEGA burn-down until the user merges or splits explicitly.
- Do **not** batch multiple MEGA ids in one wake unless the user overrides.
- Do **not** `complete` until the shard row is honestly `PASS`/`SHIP`/`DONE`.
