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
