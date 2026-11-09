# Decisions

Dated decisions for this repo, newest last.

- 2026-11-09: created from the ops starter kit, area `minecraft-plugins`.
- 2026-11-09 (owner): the name is Signalbox, one joined spelling everywhere (repo, `rootProject.name`, package `com.extantape.signalbox`, prose).
- 2026-11-09 (owner): its own public MIT repo, consumed through JitPack and shaded, relocated, into each plugin. Public only after the first-public-push audit, with the leak check in CI, `LICENSE` "Copyright (c) 2026 EXTANT APE" and a README status line.
- 2026-11-09: the format follows the ops spec `briefs/plugin-commands-spec.md` (format 1), agreed with Leaf Juice App. A breaking change bumps the format and is logged as a `plugins-leafjuice` crossing before it ships.
- 2026-11-09: no runtime dependencies, not even the Paper API. Answers go out through a `Consumer<String>`, so the library stays plain Java and fully unit-testable.
