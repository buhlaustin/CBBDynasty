#!/usr/bin/env python3
"""
MEGA build queue — ordered **implementation** work from docs/MEGA_BUILD_CHECKLIST_AUDIT/*.md.

Excluded from this queue (audit rows remain): `MEGA-QA-*` (manual/device QA), `MEGA-NOTES-*` (planning traceability), `MEGA-POST-*` (postseason burn-down after in-season queue drains).

Build:   python3 planning/tools/mega_queue.py build
Current: python3 planning/tools/mega_queue.py current
Complete: python3 planning/tools/mega_queue.py complete --id MEGA-RECR-005
Stats:   python3 planning/tools/mega_queue.py stats
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import dataclass
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
AUDIT_DIR = REPO / "docs" / "MEGA_BUILD_CHECKLIST_AUDIT"
QUEUE_PATH = REPO / "docs" / "MEGA_BUILD_QUEUE.json"
STATE_PATH = REPO / "docs/MEGA_BUILD_QUEUE_STATE.json"
SUBSCRIPTION_NAME = "mega-build-queue"
INTEGRATION_BRANCH = "cursor/mega-build-executor-cb09"
BASE_BRANCH = "main"

STATUS_TOKENS = frozenset(
    {
        "PASS",
        "SHIP",
        "DONE",
        "FAIL",
        "GAP",
        "PARTIAL",
        "STUB",
        "MISSING",
        "WRONG",
        "RISK",
        "WIRE",
        "DEV",
        "EMU",
        "BOT",
        "REL",
        "OPEN",
        "N/A",
        "RULE",
        "META",
    }
)

# | MEGA-RECR-001 | P0 | FAIL | …
ROW_PRI_STATUS = re.compile(
    r"^\|\s*(MEGA-[A-Z]+-\d+)\s*\|\s*(P[0-3])\s*\|\s*([A-Za-z/]+)\s*\|",
)
# | MEGA-QA-001 | P0 | DEV | OPEN | …
ROW_QA = re.compile(
    r"^\|\s*(MEGA-QA-\d+)\s*\|\s*(P[0-3])\s*\|\s*(?:DEV|EMU|BOT|REL)\s*\|\s*([A-Z]+)\s*\|",
)
# | MEGA-FRAME-001 | SHIP | topic | …  (no Pri column)
ROW_STATUS_ONLY = re.compile(
    r"^\|\s*(MEGA-[A-Z]+-\d+)\s*\|\s*(" + "|".join(STATUS_TOKENS) + r")\s*\|",
    re.IGNORECASE,
)

DONE_STATUSES = frozenset({"PASS", "SHIP", "DONE"})
SKIP_STATUSES = frozenset({"N/A", "RULE", "META"})
# Executor queue skips (shards unchanged — QA is manual; NOTES are traceability, not build rows).
EXECUTOR_SKIP_PREFIXES = (
    "MEGA-QA-",
    "MEGA-NOTES-",
    "MEGA-POST-",
)
SKIP_ID_PREFIXES = ("MEGA-NOTES-META-",)
OPEN_STATUSES = frozenset(
    {
        "FAIL",
        "GAP",
        "PARTIAL",
        "STUB",
        "MISSING",
        "WRONG",
        "RISK",
        "WIRE",
        "DEV",
        "EMU",
        "BOT",
        "REL",
        "OPEN",
    }
)

PRI_RANK = {"P0": 0, "P1": 1, "P2": 2, "P3": 3}
STATUS_RANK = {
    "FAIL": 0,
    "MISSING": 0,
    "WRONG": 0,
    "STUB": 0,
    "GAP": 1,
    "PARTIAL": 1,
    "RISK": 2,
    "WIRE": 2,
    "OPEN": 1,
    "DEV": 1,
    "EMU": 1,
    "BOT": 1,
    "REL": 1,
}


@dataclass
class MegaRow:
    item_id: str
    pri: str
    status: str
    shard: str
    path: str


def infer_pri(status: str, explicit: str | None) -> str:
    if explicit:
        return explicit
    if status in {"FAIL", "MISSING", "WRONG", "STUB"}:
        return "P0"
    if status in {"GAP", "PARTIAL", "RISK", "OPEN", "DEV", "EMU", "BOT", "REL"}:
        return "P1"
    return "P2"


def parse_table_row(line: str) -> tuple[str, str, str] | None:
    m = ROW_QA.match(line)
    if m:
        return m.group(1), m.group(2), m.group(3).strip().upper()

    m = ROW_PRI_STATUS.match(line)
    if m:
        item_id, pri, col3 = m.group(1), m.group(2), m.group(3).strip().upper()
        if col3 in STATUS_TOKENS:
            return item_id, pri, col3
        return None

    m = ROW_STATUS_ONLY.match(line)
    if m:
        item_id, status = m.group(1), m.group(2).strip().upper()
        pri = infer_pri(status, None)
        return item_id, pri, status

    return None


def parse_shards() -> dict[str, MegaRow]:
    by_id: dict[str, MegaRow] = {}
    for md in sorted(AUDIT_DIR.glob("*.md")):
        rel = md.relative_to(REPO).as_posix()
        for line in md.read_text(encoding="utf-8").splitlines():
            parsed = parse_table_row(line)
            if not parsed:
                continue
            item_id, pri, status = parsed
            if any(item_id.startswith(p) for p in SKIP_ID_PREFIXES):
                continue
            if status in SKIP_STATUSES:
                continue
            if item_id in by_id:
                continue
            by_id[item_id] = MegaRow(
                item_id=item_id,
                pri=pri,
                status=status,
                shard=md.name,
                path=rel,
            )
    return by_id


def sort_key(row: MegaRow) -> tuple:
    return (
        PRI_RANK.get(row.pri, 9),
        STATUS_RANK.get(row.status, 5),
        row.item_id,
    )


def executor_skips(item_id: str) -> bool:
    return any(item_id.startswith(p) for p in EXECUTOR_SKIP_PREFIXES)


def build_queue(rows: dict[str, MegaRow]) -> list[dict]:
    open_rows = [
        r
        for r in rows.values()
        if r.status not in DONE_STATUSES
        and r.status not in SKIP_STATUSES
        and not executor_skips(r.item_id)
    ]
    open_rows.sort(key=sort_key)
    return [
        {
            "id": r.item_id,
            "pri": r.pri,
            "status": r.status,
            "shard": r.shard,
            "path": r.path,
        }
        for r in open_rows
    ]


def default_state() -> dict:
    return {
        "version": 2,
        "index": 0,
        "completedIds": [],
        "subscriptionName": SUBSCRIPTION_NAME,
        "integrationBranch": INTEGRATION_BRANCH,
        "baseBranch": BASE_BRANCH,
        "pullRequestNumber": None,
        "pullRequestUrl": None,
    }


def load_state() -> dict:
    if not STATE_PATH.exists():
        return default_state()
    state = json.loads(STATE_PATH.read_text(encoding="utf-8"))
    state.setdefault("integrationBranch", INTEGRATION_BRANCH)
    state.setdefault("baseBranch", BASE_BRANCH)
    state.setdefault("pullRequestNumber", None)
    state.setdefault("pullRequestUrl", None)
    state.setdefault("subscriptionName", SUBSCRIPTION_NAME)
    return state


def save_state(state: dict) -> None:
    STATE_PATH.write_text(json.dumps(state, indent=2) + "\n", encoding="utf-8")


def load_queue() -> list[dict]:
    if not QUEUE_PATH.exists():
        return []
    return json.loads(QUEUE_PATH.read_text(encoding="utf-8"))


def shard_done_count(rows: dict[str, MegaRow]) -> int:
    return sum(1 for r in rows.values() if r.status in DONE_STATUSES)


def persist_queue_metrics(state: dict, queue: list[dict]) -> None:
    rows = parse_shards()
    remaining = max(0, len(queue) - state["index"])
    state["queueMetrics"] = {
        "openImplementation": len(queue),
        "remaining": remaining,
        "shardPassShipDone": shard_done_count(rows),
        "completedIdsRecorded": len(state.get("completedIds", [])),
    }
    save_state(state)


def cmd_build(_: argparse.Namespace) -> int:
    rows = parse_shards()
    queue = build_queue(rows)
    QUEUE_PATH.write_text(json.dumps(queue, indent=2) + "\n", encoding="utf-8")
    state = load_state()
    # clamp index if queue shrank
    if state["index"] > len(queue):
        state["index"] = len(queue)
    persist_queue_metrics(state, queue)
    open_count = len(queue)
    done_count = shard_done_count(rows)
    print(f"Wrote {QUEUE_PATH.name}: {open_count} open, {done_count} already PASS/SHIP/DONE")
    return 0


def cmd_stats(_: argparse.Namespace) -> int:
    queue = load_queue()
    state = load_state()
    remaining = max(0, len(queue) - state["index"])
    rows = parse_shards()
    payload = {
        "totalOpen": len(queue),
        "index": state["index"],
        "remaining": remaining,
        "shardPassShipDone": shard_done_count(rows),
        "completedIdsRecorded": len(state.get("completedIds", [])),
    }
    state["queueMetrics"] = {
        "openImplementation": len(queue),
        "remaining": remaining,
        "shardPassShipDone": payload["shardPassShipDone"],
        "completedIdsRecorded": payload["completedIdsRecorded"],
    }
    save_state(state)
    print(json.dumps(payload, indent=2))
    if remaining == 0:
        print("QUEUE_EMPTY")
    return 0


def cmd_current(_: argparse.Namespace) -> int:
    queue = load_queue()
    state = load_state()
    if not queue:
        print("QUEUE_EMPTY")
        return 0
    idx = state["index"]
    if idx >= len(queue):
        print("QUEUE_EMPTY")
        return 0
    item = queue[idx]
    print(json.dumps(item, indent=2))
    return 0


def align_index_to_first_open(state: dict, queue: list[dict], rows: dict[str, MegaRow]) -> None:
    """Ensure index points at the first still-open queue item (handles out-of-order complete)."""
    for i, item in enumerate(queue):
        live = rows.get(item["id"])
        if live and live.status not in DONE_STATUSES:
            state["index"] = i
            save_state(state)
            return
    state["index"] = len(queue)
    save_state(state)


def sync_index_with_shard(state: dict, queue: list[dict]) -> bool:
    """Advance index while current items are already DONE in markdown."""
    rows = parse_shards()
    advanced = False
    while state["index"] < len(queue):
        cur = queue[state["index"]]
        live = rows.get(cur["id"])
        if live and live.status in DONE_STATUSES:
            if cur["id"] not in state["completedIds"]:
                state["completedIds"].append(cur["id"])
            state["index"] += 1
            advanced = True
        else:
            break
    if advanced:
        save_state(state)
    return advanced


def cmd_complete(args: argparse.Namespace) -> int:
    rows = parse_shards()
    row = rows.get(args.id)
    if not row:
        print(f"Unknown id: {args.id}", file=sys.stderr)
        return 1
    if row.status not in DONE_STATUSES:
        print(
            f"Refuse complete: {args.id} still {row.status}. "
            f"Edit {row.path} Status → PASS/SHIP/DONE first.",
            file=sys.stderr,
        )
        return 1

    queue = load_queue()
    state = load_state()
    if args.id not in state["completedIds"]:
        state["completedIds"].append(args.id)

    cur = queue[state["index"]] if state["index"] < len(queue) else None
    if cur and cur["id"] == args.id:
        state["index"] += 1
    elif args.id in [q["id"] for q in queue]:
        # Item completed out of order — rebuild and resync
        cmd_build(argparse.Namespace())
        queue = load_queue()
        sync_index_with_shard(state, queue)

    save_state(state)
    sync_index_with_shard(state, queue)

    remaining = max(0, len(queue) - state["index"])
    print(f"Completed {args.id}. Remaining: {remaining}")
    if remaining == 0:
        print("QUEUE_EMPTY")
    return 0


def cmd_resync(_: argparse.Namespace) -> int:
    cmd_build(argparse.Namespace())
    queue = load_queue()
    state = load_state()
    rows = parse_shards()
    sync_index_with_shard(state, queue)
    align_index_to_first_open(state, queue, rows)
    return cmd_current(_)


def cmd_integration(_: argparse.Namespace) -> int:
    state = load_state()
    payload = {
        "integrationBranch": state["integrationBranch"],
        "baseBranch": state["baseBranch"],
        "pullRequestNumber": state.get("pullRequestNumber"),
        "pullRequestUrl": state.get("pullRequestUrl"),
        "policy": "All MEGA queue commits push ONLY to integrationBranch; use ManagePullRequest update_pr on this PR — never create_pr per item.",
    }
    print(json.dumps(payload, indent=2))
    return 0


def cmd_set_pr(args: argparse.Namespace) -> int:
    state = load_state()
    state["pullRequestNumber"] = args.number
    state["pullRequestUrl"] = args.url
    save_state(state)
    print(f"Recorded PR #{args.number}")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="MEGA build queue")
    sub = parser.add_subparsers(dest="cmd", required=True)
    sub.add_parser("build", help="Regenerate queue from audit shards").set_defaults(func=cmd_build)
    sub.add_parser("stats", help="Queue statistics").set_defaults(func=cmd_stats)
    sub.add_parser("current", help="Current item JSON").set_defaults(func=cmd_current)
    sub.add_parser("resync", help="Rebuild + skip completed in shards").set_defaults(func=cmd_resync)
    sub.add_parser(
        "integration",
        help="Print single-PR / integration branch policy",
    ).set_defaults(func=cmd_integration)
    p_pr = sub.add_parser("set-pr", help="Record integration pull request")
    p_pr.add_argument("--number", type=int, required=True)
    p_pr.add_argument("--url", required=True)
    p_pr.set_defaults(func=cmd_set_pr)
    p = sub.add_parser("complete", help="Mark id done (shard must be PASS/SHIP/DONE)")
    p.add_argument("--id", required=True)
    p.set_defaults(func=cmd_complete)
    args = parser.parse_args()
    return args.func(args)


if __name__ == "__main__":
    raise SystemExit(main())
