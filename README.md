# Signalbox

Signalbox is a tiny library for Paper plugins. It lets a plugin's own commands answer tools such as Leaf Juice, with replies a player can't fake and secrets that never reach the server log. Plugins shade it in; it isn't a plugin itself.

**Status:** 0.1.0, the first release. Its 18 unit tests cover every rule the library enforces itself; the ones a plugin has to keep, such as answering every request, get tested in the first plugin that uses it. The console line format was checked on throwaway Paper 26.1.2, 26.2 and 26.3 servers. No plugin uses it yet: Latchkey is the first, then Usher.

## What it does

A command run with `--json <id>` answers with exactly one line:

```
ea-result <id> {"id":"<id>","ok":true,…}
```

- **One answer per request, always.** Unknown subcommands, bad arguments and missing permission answer too, with a stable `code` and plain words: `{"ok":false,"error":{"code":"bad-argument","message":"…"}}`.
- **Hard to fake.** A reader accepts the line only when `ea-result ` comes straight after the log header's `]: `. Player chat is logged as `]: <Name> …`, so typing an answer in chat never counts. Ids are random, only the first answer per id counts, and ids the reader didn't send are ignored. `Reader` implements the rule.
- **One line, plain ASCII.** Control characters and anything non-ASCII are written as `\uXXXX`, and a line is never longer than 8 KB.
- **Confirm questions as data.** A change that needs a yes answers `{"ok":false,"needs":[{"id","question"}]}` and changes nothing. Send it again with `--yes id1,id2`.
- **No secrets in logs.** Paper writes console output to `logs/latest.log`, so a secret, such as an invite code, goes to `plugins/<Plugin>/once/<id>.json` instead. That folder is 0700 and the file is 0600, written atomically, and the answer only points to it. The reader deletes the file; anything left is deleted after 5 minutes.
- **`describe`.** Each plugin can list its commands, their typed arguments, their confirm questions and which ones return a secret, with a format version readers check.

Arguments are plain tokens, never JSON:

| Type | Written as |
| --- | --- |
| bool | `true` or `false` |
| int | base 10, optional leading `-` |
| player | a name (3 to 16 of `A-Z a-z 0-9 _`) or a uuid |
| uuid | lowercase, with hyphens |
| enum | one of the listed values, `a-z 0-9 -` |
| duration | one number and one unit: `30m`, `7d` (`s`, `m`, `h`, `d`) |
| list | comma-separated, no spaces |

Free text, if a command has any, comes last in double quotes, with `\"` and `\\` as its only escapes. Flags come after everything: `note Alice "some text" --yes a,b --json k3x9`. No value may start with `-`.

The full format is in the owner's ops repo as `briefs/plugin-commands-spec.md` (format 1).

## Use it

Add JitPack and shade it, relocated, so two plugins never clash. This hasn't been tried through JitPack yet; it will be with the first plugin that uses it.

```kotlin
repositories { maven("https://jitpack.io") }
dependencies { implementation("com.github.ExtantApe:Signalbox:v0.1.0") }
// with the Shadow plugin:
tasks.shadowJar { relocate("com.extantape.signalbox", "your.plugin.libs.signalbox") }
```

In a command:

```java
Request r = Request.parse(args);
if (r.json()) sender.sendMessage(Answer.ok(r.id(), Map.of("profiles", profiles)));
```

Send answers with `sendMessage` to the sender that ran the command. Never use the plugin's logger, which puts the plugin's name before the prefix.

## Build

```
./gradlew build
```

Java 25. No runtime dependencies.

## License

MIT. See [LICENSE](LICENSE).
