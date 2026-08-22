# SmritiAI — Project Setup Memory

> A log of all major and minor setup steps completed by Ashutosh on August 15, 2026.
> This file is safe to commit; it contains no secrets.

---

## ✅ GitHub Setup

- [x] Verified Git is installed (`git version 2.54.0`)
- [x] Logged into GitHub account (`KumarAshutoshDev`)
- [x] Created public repository: **SmritiAI**
  - URL: `https://github.com/KumarAshutoshDev/SmritiAI`
- [x] Invited collaborators:
  - Rhythm Grover — `RhythmLovesTea` (pending acceptance)
  - Ritvik Jain — `jain-ritvik` (pending acceptance)
  - Maahi Choudhary — not yet added (username needed)
- [x] Chose SSH over HTTPS for push/pull
- [x] Verified existing SSH key pair (`~/.ssh/id_ed25519`, `id_ed25519.pub`)
- [x] Added public SSH key to GitHub (key already existed, re-added/verified as "Arch")
- [x] Tested SSH connection with `ssh -T git@github.com` — success
- [x] Cloned the repository (initially via HTTPS)
- [x] Switched remote URL from HTTPS to SSH (`git remote set-url origin git@github.com:KumarAshutoshDev/SmritiAI.git`)
- [x] Created `.gitignore` with standard Android/secret exclusions
- [x] Configured Git identity:
  - `user.name` = "Kumar Ashutosh"
  - `user.email` = (user's GitHub email)
- [x] Made first commit: `Add .gitignore for Android project`
- [x] Pushed first commit to `main` branch
- [x] Enabled branch protection on `main`:
  - Require a pull request before merging
  - No direct pushes to `main`
  - Status checks deferred (no CI yet)

## ✅ Jira Setup

- [x] Created Jira Cloud account: `ashukm2004.atlassian.net`
- [x] Created Jira project: **SmritiAI** (key: `SMI`)
- [x] Chose Kanban board
- [x] Created `tasks.csv` with 83 tasks (title, assignees, status)
  - Initial import attempt failed due to invalid status value `todo`
- [x] Created simplified `tasks_simple.csv` (title only)
- [x] Successfully imported 83 tasks into Jira
- [x] Confirmed tasks visible after page refresh
- [x] Board columns present: Todo, In Progress, In Review, Done

## 📋 Pending / Next Steps

- [ ] Add Maahi Choudhary as GitHub collaborator (once username received)
- [ ] Assign Jira tasks to teammates (Rhythm, Ritvik, Maahi, Ashutosh)
- [ ] Create Epics in Jira to group tasks by phase (Phase 0–6)
- [ ] Link GitHub repo to Jira for automatic PR/commit tracking
- [ ] Wait for collaborators to accept GitHub invites
- [ ] Start Phase 0 — Task 1 (Android project creation, assigned to Maahi)
- [ ] Decide on "Team Garuda" vs "Team Chromium" for external materials before any pitch

## 🔐 Security Reminders

- Never commit `.env`, `local.properties`, or any file containing API keys.
- The `.gitignore` already excludes `.env`, `*.jks`, `*.keystore`, `local.properties`, etc.
- Public repo: be extra careful with secrets; use GitHub noreply email if privacy desired.
- SSH private key (`~/.ssh/id_ed25519`) must never be shared or uploaded.

---

**Generated:** August 15, 2026  
**Maintained by:** Kumar Ashutosh (Team Lead)
