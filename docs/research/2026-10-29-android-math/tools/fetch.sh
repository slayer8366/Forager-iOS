#!/bin/bash
# fetch.sh <outfile> <url>  -- one bounded read, logged. Googlesource ?format=TEXT bodies are base64; decoded here.
# Appends: date-read  bytes  sha256  outfile  url   to $LOG (default: downloads/sources.tsv)
set -u
out="$1"; url="$2"; LOG="${LOG:-$(dirname "$out")/sources.tsv}"
UA="forager-ios-research/0.1 (read-only, bounded; one file per request)"
tmp="$out.raw"
code=$(curl -sS -A "$UA" --max-time 60 -o "$tmp" -w '%{http_code}' "$url") || { echo "FETCH-ERROR $url"; rm -f "$tmp"; exit 2; }
if [ "$code" != "200" ]; then echo "HTTP $code $url"; rm -f "$tmp"; exit 3; fi
case "$url" in
  *"format=TEXT"*) python3 -I -c 'import sys,base64; sys.stdout.buffer.write(base64.b64decode(open(sys.argv[1],"rb").read()))' "$tmp" > "$out" ;;
  *) mv "$tmp" "$out" ;;
esac
rm -f "$tmp"
printf '%s\t%s\t%s\t%s\t%s\n' "$(date -u +%FT%TZ)" "$(stat -c %s "$out")" "$(sha256sum "$out" | cut -c1-64)" "$(basename "$out")" "$url" >> "$LOG"
echo "ok $(stat -c %s "$out") bytes $(basename "$out")"
sleep 1
