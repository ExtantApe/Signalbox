# First public push audit

By Minecraft: Plugins Lead, 2026-11-09, at commit e3d1fe7, before the repo goes public under the owner's yes of 2026-11-09 (a public MIT repo).

| Check | Result |
| --- | --- |
| `scripts/leak-check.sh` locally, with the private terms list | passed |
| Private terms anywhere in `git log -p --all`, commit messages included | none |
| Email addresses in history | only the GitHub noreply commit address and Anthropic's noreply co-author line |
| IP addresses, server addresses | none |
| Home paths (`/Users/`) | none |
| Claude session links in commit text | none |
| Build output in the repo | none (`build/` and `.gradle/` are ignored; the standard `gradle-wrapper.jar` is tracked, as in every plugin repo) |
| Personal names | only git's author field, which `~/Code/CLAUDE.md` allows |
| Leak check in CI | yes, first step of `.github/workflows/ci.yml` |
| LICENSE | MIT, "Copyright (c) 2026 EXTANT APE" |
| README status line | yes: 0.1.0, what the tests cover, not yet used by a plugin, JitPack not yet tried |

Result: clean. Nothing needed rewriting.
