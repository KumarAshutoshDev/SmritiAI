# Memory Update Guide

Each teammate has a file: `memory-<your-name>.md`.

## When to Update

After every work session. Keep it short.

## Format

### Completed
- [x] YYYY-MM-DD: what you did

### In Progress
- [ ] YYYY-MM-DD: what you are doing

### Blockers
- [ ] what is blocking you

### Debug Notes
- fixes, gotchas, important decisions

## Rules

- No secrets, API keys, tokens, or patient data.
- Keep it brief.
- Commit and push after updating:

```bash
git add memory-<your-name>.md
git commit -m "Update memory log for <your-name>"
git push origin main
