# CLAUDE.md — Camera Shift

The rules for the Camera Shift repository. Local sessions usually open from `../guzhenren` (Alex, 2026-09-29), where this file is not autoloaded: the GZR Map points here, and an agent reads this file whole before touching this repository. A session opened in this folder, or a cloud session on its GitHub repository, loads it as its project rules file. Alex's chat instructions beat it.

## Relation to the GZR rules

- Alex's always-on alex-constitution and i-have-adhd skills apply here unchanged. Local sessions get them from `~/.claude`; cloud sessions get the tracked copies in `.claude/skills/`, injected by the hook in `.claude/settings.json`. Those copies come from `../guzhenren/.claude/`, the source, through `python .claude/hooks/session_start.py --deploy` run there; never edit them here.
- The rest of the GZR rules is GZR-only: its Boundaries, Checks and shipping, Map, Workflow router, `rules/`, `reference/`, its other skills and the wiki. Where this file and the GZR rules disagree about this repository, this file wins.

## Project

- Camera Shift, mod id `camera_shift` (`camerashift` until 2026-10-01, Alex), package `net.alex.camerashift`: a client-only add-on for Epic Fight. Mining mode ends in first person, battle mode in the third-person back view, and the camera glides between them. `README.md` is Alex's short player-facing introduction, written by him (2026-10-02), and `README.zh-TW.md` (Traditional Chinese) and `README.en.md` are its translations: change them to follow the Simplified Chinese, never the reverse. Behavior and config details live in the code and this file's Map.
- Minecraft 1.21.1 on NeoForge through ModDevGradle, Java 21. Versions live in `gradle.properties` only.
- `neo_version` is pinned to the oldest NeoForge in use, the GZR dev client (`../guzhenren/gradle.properties`), so the compiler proves no newer API is used; the `neoforge.mods.toml` range stays open-ended. Both moved from `21.1.238` to `21.1.252` on 2026-10-01 at Alex's call, together with Gu World. When the GZR pin moves, ask Alex before moving this one.
- Epic Fight is the one required dependency, pulled from the Modrinth maven (`compileOnly` plus `localRuntime`; nothing goes into `run/mods`). The members used (`LocalPlayerPatch`, `ClientConfig`, `PlayerMode`) are internals, not API: a `LinkageError` switches the controller off with one logged error while the game keeps running. Keep that fail-soft path.
- Better Lock On is optional compat only: `LockOnControlMixin` is `@Pseudo` with `require = 0`, the mixin config is not required, and `CompatMixinPlugin` skips the mixin unless `betterlockon` is loaded. It never becomes a compile or runtime dependency.
- Client-only: `@Mod(dist = Dist.CLIENT)` and `displayTest="IGNORE_ALL_VERSION"`. Nothing may load on a dedicated server or require a server install.

## Boundaries

- This repository, `C:\workspace\Dev\Projects\Minecraft-ModDev\camera-shift` (remote `alex-0v0-328/mcmod-camera-shift`, renamed 2026-10-01; the earlier names `camera-shift` and `Immersive-Camera-Shift` redirect; branch `main`), is a separate Git repo from GZR. From a GZR-rooted session, run every git command as `git -C ../camera-shift` and every Gradle command from this folder, never against GZR. Check its `status`/`diff` at task start and preserve uncommitted work.
- Never add Claude as a contributor: no `Co-Authored-By` or generated-with line and no Claude identity in any commit (Alex, 2026-10-01).
- Nothing gets backed up (Alex, 2026-09-26): Git history is the only rollback. `.idea/`, `run/`, `src/test/` and `.claude/settings.local.json` sit outside Git.
- Alex edits live in IDEA. Re-read a file right before editing it; his wording changes stay as-is.
- Temporary files follow GZR › Tools and context (Temporary files) under `C:\workspace\Dev\Projects\_Temp\camerashift\`, one subfolder per task.

## Checks and shipping

- GZR's tools drive this repository with `--project camerashift` (Alex, 2026-10-01): `python ../guzhenren/tools/check.py --project camerashift` runs `build`, which compiles, packages and runs the local JUnit suite; `gradlew.bat runClient` starts the dev client with Epic Fight on the classpath and `run/` as the game folder. Visual and feel acceptance in `runClient` stays Alex's.
- In the guzhenren IDEA window, which links this project, its run configurations are named `Camera Shift Client` and so on, in a `Camera Shift` folder (`ideName` in `build.gradle`), so they never overwrite GZR's plain `Client`.
- The tests are local-only, as in GZR (Alex, 2026-10-01): `src/test/` is gitignored and holds plain JUnit over the glide math (`CameraGlideTest`) and nothing that boots Minecraft. They have no remote copy and no version history from that date on.
- GitHub Actions (`.github/workflows/build.yml`) runs `./gradlew build` on every push and pull request, which compiles and packages.
- Commits go through GZR's `commit_push.py` with `--project camerashift` (Alex, 2026-10-01): the plan at `C:\workspace\Dev\Projects\_Temp\camerashift\commit-push-plan.json`; its mirror runs `build` on the exact tree as CI does, then again with the local `src/test` copied in; then the contributor gate, push and the Actions wait. The flow, commit-message picking included, is GZR's `commit-push` skill.
- A cloud session has neither GZR's tools nor the local tests: it runs `./gradlew build` and leaves shipping through `commit_push.py` to a local session.

## Map

| Path                                                  | What it is                                                                                                                                          |
|-------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `src/main/java/net/alex/camerashift/CameraShift.java` | Client-only mod entry (`MOD_ID`, `LOGGER`); registers the client config                                                                             |
| `…/CameraShiftConfig.java`                            | `config/camera_shift-client.toml`: `transitionSeconds`, default 0.4 s, range 0–2, 0 switches instantly                                              |
| `…/PerspectiveController.java`                        | Per-frame driver: follows Epic Fight's player mode, runs the glide, turns off Epic Fight's own perspective auto-switch, fail-soft on `LinkageError` |
| `…/CameraGlide.java`                                  | Pure glide math: smoothstep easing, mid-glide reversal from the current position                                                                    |
| `…/mixin/`                                            | `CompatMixinPlugin` and `LockOnControlMixin`, the optional Better Lock On compat                                                                    |
| `src/main/resources/camera_shift.mixins.json`         | Mixin config, named after the mod id (`neoforge.mods.toml` derives it)                                                                              |
| `src/main/templates/META-INF/neoforge.mods.toml`      | Mod metadata, expanded from `gradle.properties` by `generateModMetadata`                                                                            |
| `src/test/`                                           | JUnit tests; local-only, gitignored                                                                                                                 |
| `run/`                                                | Dev game folder: options, configs, the test world `新的世界`; gitignored                                                                            |
| `.idea/`                                              | IDEA project settings; gitignored                                                                                                                   |
| `.claude/`                                            | Cloud-session hook and the always-on skills, copies synced from GZR; `settings.local.json` stays local                                              |
| `CLAUDE.md`                                           | This rules file; tracked, so cloud sessions load it (Alex, 2026-10-01)                                                                              |

## Language and style

- Chat in Chinese (zh-CN); code, comments and commit messages in American English; this file in English.
- The GZR code conventions apply here (Alex, 2026-10-01). `.editorconfig`, a copy of GZR's, keeps the automatable part; the rest, condensed from GZR reference《写作与命名》:
  - Comments live only in the top-level class Javadoc: no `//` in method bodies and no Javadoc on fields, methods, constructors or nested types; the class Javadoc points at a member with `{@link #member}`. Only `//region` labels and TODO anchors stay in place, and a `//region` label must match its code. Explain non-obvious reasons, contracts and limits; never restate the code. The Javadoc ends with `@author Alex`, `@version 1.0.0`, optional `@see`, `@since 1.0.0`.
  - Blank lines: one after a type's opening brace, none before its closing brace, none at the top of a method or block body; exactly one between members, placed above Javadoc and annotations; consecutive fields form one block with no blank lines; one between the top-level class Javadoc and the declaration; one before `//region` and after `//endregion`, none inside them; at most one between logical steps inside a method.
  - Braces: an empty body is `{}` on one line (`private X() {}`); a one-line brace pair with content keeps a space inside (`{ return x; }`, `new int[] { 1, 2 }`).
  - Imports form one block in plain ASCII order (`ModContainer` before `common.Mod`), static imports in their own block first, no wildcards. Constants are `UPPER_SNAKE` (`MOD_ID`). No hand-aligned argument columns; lines stay within 120 characters.
- New code matches its neighbors' comment density, naming and idiom. Markdown tables stay aligned: after touching one, `python ../guzhenren/tools/md_tables.py <file>` must report nothing.
