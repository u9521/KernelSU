---
name: material-symbols-icons
description: Use when adding, replacing, or regenerating the Material Symbols Compose ImageVector icons of the manager app (breezeui/icons).
---

# Material Symbols icons

The manager app's icons are generated from Google Fonts **Material Symbols** into
`manager/app/src/main/java/me/weishu/kernelsu/breezeui/icons/` as
Compose `ImageVector`s. The icon set itself is data in `icons.toml`; never
hand-edit the generated Kotlin files.

| file | role |
| --- | --- |
| `icons.toml` | the icon set: shared style + one entry per icon |
| `fetch_material_icons.py` | generator; reads the manifest, writes the Kotlin |
| `SKILL.md` | this file |

## Run it

```bash
cd <repo root>
python3 .agents/skills/material-symbols-icons/fetch_material_icons.py                    # whole set
python3 .agents/skills/material-symbols-icons/fetch_material_icons.py --icons system_update,info
python3 .agents/skills/material-symbols-icons/fetch_material_icons.py --set wght=500     # experiment
python3 .agents/skills/material-symbols-icons/fetch_material_icons.py --prune            # drop orphans
python3 .agents/skills/material-symbols-icons/fetch_material_icons.py --manifest other.toml
```

Needs Python 3.11+ (stdlib `tomllib`). `--icons` narrows what is fetched, but
`MaterialSymbols.kt` is **always** rebuilt from the whole manifest. `--set`
restyles everything, so it refuses to run together with `--icons`.

## The manifest

```toml
[defaults]                  # the shared style; must equal SymbolStyle.FilledRounded
family = "Rounded"
fill = 1
opsz = 24
wght = 400
grad = 0
rond = 50

[[icons]]                   # one block per icon, `name` = Material Symbols name
name = "system_update"

[[icons]]
name = "article"
auto_mirror = true

[[icons]]
name = "cottage"            # -> Cottage.kt, MaterialSymbols.Rounded.Cottage
fill = 0

[[icons]]
name = "cottage"            # -> CottageFilled.kt, MaterialSymbols.Filled.Rounded.Cottage
fill = 1

[[icons]]
name = "big"
wght = 500                  # -> SymbolStyle.FilledRounded.copy(wght = 500)
```

An entry may override any of `family`, `fill`, `opsz`, `wght`, `grad`, `rond`,
`auto_mirror`; everything else is inherited from `[defaults]`. The same `name` may
appear twice with a different `fill` (that is how the filled/unfilled variants of
Cottage, Extension, Settings and Security are declared), but two entries that
would produce the same file are rejected.

Rejected up front, before anything is fetched or written: unknown fields (typos),
unknown families, out-of-range axes (`fill` 0|1, `opsz` 20..48, `wght` 100..700,
`grad` -50..200, `rond` 0..100), an empty `[[icons]]` list, missing/invalid icon
names (they become Kotlin identifiers, so they must start with a letter), and
`--set` on an axis that is not `opsz`/`wght`/`grad`/`rond`.

## Endpoint and axes

`https://fonts.gstatic.com/render/v1/<family>/<size>/<icon>.kt?var=opsz,wght,FILL,GRAD,ROND@...`

- The axes are **baked into the returned path data**: they are a build-time knob,
  there is no runtime control.
- `opsz`, `wght`, `GRAD` and `FILL` all change the output. **`ROND` is currently
  ignored** by the endpoint (verified across the Rounded, Outlined and Sharp
  families); it is still sent and pinned so the output cannot drift once that
  axis is implemented.
- The `24dp` path segment sets `viewportWidth` (`40dp` -> 40), so keep 24dp and
  scale with Compose modifiers.
- Legacy `Material+Icons*` families ignore `var=` entirely, so they are rejected.
- Values in use: **Rounded + FILL=1 + opsz 24 + wght 400 + GRAD 0 + ROND 50**.

## What it generates

- one file per icon, carrying its own axes. The unfilled variant is the default and
  gets no suffix, a filled one gets `Filled`:

  ```kotlin
  internal val security: MaterialSymbol = MaterialSymbol(        // Security.kt
      name = "security",
      style = SymbolStyle(SymbolFamily.Rounded, SymbolFill.Unfilled, 24, 400, 0, 50),
  ) { /* ImageVector.Builder(...) */ }
  ```

- `MaterialSymbols.kt`: the public namespace (each an `ImageVector`) plus the
  `icons` registry, so a style can be looked up with
  `MaterialSymbols.icons.first { it.name == "system_update" }.style`.
  **Unfilled is the default and needs no level** - `MaterialSymbols.<Family>.<Name>`
  is the unfilled variant, `MaterialSymbols.Filled.<Family>.<Name>` the filled one
  (e.g. `MaterialSymbols.Rounded.Security` / `MaterialSymbols.Filled.Rounded.Security`).
- generated files carry **no header comments**
- generated files are already in the project's Kotlin style, so the reformat the IDE
  runs on commit leaves them untouched: arguments indent by 4 and every chained call
  is glued to the token that closes what it is called on (`).apply {`, `}.build()`).
  `indent_body` rebuilds the indentation from the bracket nesting instead of scaling
  what Google sent - do not go back to a relative-indent transform, it is what makes
  every commit show a formatting diff.
- the package a generated file declares is derived from `OUT_DIR`, so the icons can
  be moved without the script writing a stale package.

## Add, restyle or remove an icon

1. **Add**: put a `[[icons]]` block in the manifest, then run with `--icons <name>`
   (or run the whole set). A new family or fill must first be declared in
   `MaterialSymbol.kt` (`SymbolFamily` / `SymbolFill`); the script fails with a
   clear message otherwise.
2. **Restyle**: add the axis to that icon's block, e.g. `wght = 500`.
3. **Remove**: delete the block and re-run with `--prune`. Without `--prune` the
   script only warns about the leftover file - it never deletes a file that does
   not carry the generated marker.
4. Icons that androidx ships under `Icons.AutoMirrored` need `auto_mirror = true`.

## Guards baked into the script

- `[defaults]` must equal the hand-written `SymbolStyle.FilledRounded`
- every family/fill in use must be declared in `MaterialSymbol.kt`
- `viewportWidth` must match `SIZE`
- icon names must be unique after Pascal-casing, and `[[icons]]` must not be empty

## Verify

1. run the script twice - the output must be identical (no drift)
2. `git diff` after a run must show only the icons that were actually added,
   restyled or removed - a whole-file rewrite means the generated style and the one
   the IDE commits have drifted apart
3. make sure every `[[icons]]` entry is still referenced (grep for its
   `MaterialSymbols.<Family>.<Name>` path outside the icons package) - an icon
   nothing uses belongs out of the manifest
4. IDE inspections clean on the icons package and any touched screen
5. build the manager (`./gradlew :app:assembleDebug`, needs
   `jniLibs/arm64-v8a/libksud.so` first) or build from the IDE
