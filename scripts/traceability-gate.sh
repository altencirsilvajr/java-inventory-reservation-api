#!/usr/bin/env bash
set -euo pipefail

mode="${1:---staged}"
if [[ "$mode" == "--staged" ]]; then
  mapfile_cmd=(git diff --cached --name-only --diff-filter=ACMR)
else
  mapfile_cmd=(git show --pretty=format: --name-only --diff-filter=ACMR "$mode")
fi

files="$(${mapfile_cmd[@]})"
[[ -n "$files" ]] || { echo "traceability: no files to validate"; exit 0; }

journal_count="$(printf '%s\n' "$files" | awk '/^journal\/[0-9]+-.*\.md$/ { count++ } END { print count+0 }')"
if [[ "$journal_count" -ne 1 ]]; then
  echo "traceability: expected exactly one changed journal, found $journal_count" >&2
  exit 1
fi

journal_file="$(printf '%s\n' "$files" | awk '/^journal\/[0-9]+-.*\.md$/ { print; exit }')"
grep -Eq 'Novo ADR criado:|ADR aplicado:|Decisao local sem ADR novo:' "$journal_file" || {
  echo "traceability: $journal_file must declare ADR status" >&2
  exit 1
}

echo "traceability: ok ($journal_file)"
