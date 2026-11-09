# Test audit

Kept by this repo's owning session (Minecraft: Plugins Lead), per `~/Code/CLAUDE.md`, "Test self-audits". Newest first.

## 2026-11-09: first audit (at 0.1.0)

| Promise (spec, format 1) | Covered by |
| --- | --- |
| Flags last in either order; free text quoted, `\"` and `\\` only; no value starting with `-` | `RequestTest` |
| Caller ids `[a-z0-9-]{1,36}`; errors still carry the id | `RequestTest` |
| One plain-ASCII line, `\uXXXX` escapes, under 8 KB | `AnswerTest` |
| Errors with a stable code; fixed key order | `AnswerTest` |
| Confirm questions as data; nothing changes until every id is given | `AnswerTest` (`Confirm.missing`) |
| **Anti-spoofing:** prefix straight after `]: `; chat and logger lines rejected; first answer only; unknown ids ignored | `ReaderTest` |
| Wire forms of every argument type | `ArgsTest` |
| **Secrets:** once-file 0600 in a 0700 folder, atomic, swept after 5 minutes, cleared at startup, id can't escape the folder | `OnceFilesTest` |
| `describe` carries the format and typed arguments | `DescribeTest` |
| The console line has no prefix on Paper 26.1.2, 26.2 and 26.3 | checked on throwaway servers 2026-11-09 (spec, "Verified on Paper") |

**Not yet covered (no plugin uses Signalbox yet):** that a plugin answers every request including unknown subcommands, and that `latest.log` never contains a secret. Both need a plugin on a real server; they become bot tests in Latchkey 0.2.0, the first user.
