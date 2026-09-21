#!/usr/bin/env python3
"""Fetch Material Symbols icons as Compose ImageVector sources.

Google Fonts exposes a render endpoint that returns a ready-to-use Compose
ImageVector ("Kotlin") next to SVG/XML:

    https://fonts.gstatic.com/render/v1/<Family>/<size>/<icon>.kt?var=<axes>

The icon set itself lives in `icons.toml` next to this script - that file, not
this script, is what changes when an icon is added, removed or restyled. Every
icon is described there - `[defaults]` plus one `[[icons]]` block per icon with
the axes it should be rendered with - and the script emits, per icon:

  * `<PascalName>.kt` - `internal val <name>: MaterialSymbol`, carrying both the
    axes (as a [SymbolStyle]) and the vector itself, so a single icon can be
    edited/reviewed on its own and its style is readable right there. The default
    unfilled variant gets no suffix (`Security.kt`), a filled one gets `Filled`
    (`SecurityFilled.kt`);
  * `MaterialSymbols.kt` - the public namespace
    (`MaterialSymbols.<Family>.<Name>` for the default unfilled variant,
    `MaterialSymbols.Filled.<Family>.<Name>` for the filled one) plus the `icons`
    registry, always generated from the whole manifest, also for a partial
    refresh.

Usage (this script ships inside the `material-symbols-icons` project skill):
    python3 .agents/skills/material-symbols-icons/fetch_material_icons.py
    python3 .agents/skills/material-symbols-icons/fetch_material_icons.py --icons home,info
    python3 .agents/skills/material-symbols-icons/fetch_material_icons.py --set wght=500
    python3 .agents/skills/material-symbols-icons/fetch_material_icons.py --prune

Notes:
  * Needs Python 3.11+ (`tomllib`) to read the manifest.
  * The endpoint answers gzip-encoded, so the response must be decompressed.
  * The axes are baked into the returned path data: they are a *build* knob,
    there is no runtime control. `opsz`/`wght`/`GRAD`/`FILL` all change the
    output, `ROND` is currently ignored by the endpoint for every family.
  * `Material+Icons*` (the legacy, frozen families) ignore `var=` entirely, so
    they are rejected here.
  * Todo before generating: the manifest `[defaults]` must match the
    hand-written `SymbolStyle.FilledRounded`, which is asserted.
"""

from __future__ import annotations

import argparse
import gzip
import pathlib
import re
import sys
import urllib.request
from dataclasses import dataclass, replace


def find_repo_root(start: pathlib.Path) -> pathlib.Path:
    """The bundle may live in any skills root, so locate the repository instead."""
    for candidate in (start, *start.parents):
        if (candidate / "manager/app/src/main/java").is_dir():
            return candidate
    raise SystemExit("could not locate the repository root (manager/app/src/main/java)")


BUNDLE = pathlib.Path(__file__).resolve().parent
REPO = find_repo_root(BUNDLE)
SOURCE_ROOT = REPO / "manager/app/src/main/java"
OUT_DIR = SOURCE_ROOT / "me/weishu/kernelsu/breezeui/icons"
MANIFEST = BUNDLE / "icons.toml"

# Derived, never spelled out: the package a generated file declares has to match
# where it lands, so moving the icons cannot leave the two disagreeing.
PACKAGE = ".".join(OUT_DIR.relative_to(SOURCE_ROOT).parts)
SIZE = "24dp"
AXIS_ORDER = ("opsz", "wght", "FILL", "GRAD", "ROND")
AXIS_RANGES = {"fill": (0, 1), "opsz": (20, 48), "wght": (100, 700), "grad": (-50, 200), "rond": (0, 100)}
SECTIONS = ("defaults", "icons")
# `fill` is not accepted: it changes file names and the namespace shape.
SETTABLE = ("opsz", "wght", "grad", "rond")

FAMILIES = {
    "Rounded": "Material+Symbols+Rounded",
    "Outlined": "Material+Symbols+Outlined",
    "Sharp": "Material+Symbols+Sharp",
}

ENDPOINT = "https://fonts.gstatic.com/render/v1/{family}/{size}/{icon}.kt"

# Icon names become Kotlin identifiers, so a leading digit is not usable.
NAME_RE = re.compile(r"^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$")


@dataclass(frozen=True)
class IconSpec:
    """One icon and the axes it is rendered with.

    Every field except [auto_mirror] is required: the manifest `[defaults]`
    table is the only place the shared style is written down.
    """

    name: str
    family: str
    fill: int
    opsz: int
    wght: int
    grad: int
    rond: int
    auto_mirror: bool = False

    def axes(self) -> tuple[int, int, int, int, int]:
        return (self.opsz, self.wght, self.fill, self.grad, self.rond)


# Every key an icon entry (and therefore `[defaults]`) may use.
SPEC_FIELDS = ("family", "fill", "opsz", "wght", "grad", "rond", "auto_mirror")
REQUIRED_DEFAULTS = ("family", "fill", "opsz", "wght", "grad", "rond")

# The unfilled variant is the namespace default: `MaterialSymbols.<Family>.<Name>`
# is unfilled, only a non-default fill gets its own level (`MaterialSymbols.Filled.…`).
DEFAULT_FILL = 0
FILL_NAMES = {0: "Unfilled", 1: "Filled"}

ICON_IMPORTS = """import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
"""

PRESET_NAME = "FilledRounded"
PRESET_RE = re.compile(
    r"val FilledRounded = SymbolStyle\(\s*SymbolFamily\.(\w+),\s*SymbolFill\.(\w+),"
    r"\s*(-?\d+),\s*(-?\d+),\s*(-?\d+),\s*(-?\d+),?\s*\)"
)


def pascal(name: str) -> str:
    return "".join(part.capitalize() for part in name.split("_"))


def camel(name: str) -> str:
    head, *rest = name.split("_")
    return head + "".join(part.capitalize() for part in rest)


def variant_suffix(spec: IconSpec) -> str:
    """Only a non-default fill needs a suffix: the unfilled variant is the default."""
    return "" if spec.fill == DEFAULT_FILL else FILL_NAMES[spec.fill]


def value_name(spec: IconSpec) -> str:
    """Top-level property name, e.g. `security` (unfilled) / `securityFilled`."""
    return camel(spec.name) + variant_suffix(spec)


def file_name(spec: IconSpec) -> str:
    return f"{pascal(spec.name)}{variant_suffix(spec)}.kt"


def fill_enum(spec: IconSpec) -> str:
    return FILL_NAMES[spec.fill]


def style_expr(spec: IconSpec, defaults: IconSpec) -> str:
    """The `style = ...` argument: the preset, a copy of it, or a literal.

    [defaults] is the manifest `[defaults]` table - i.e. what the hand-written
    `SymbolStyle.FilledRounded` means - so a `--set` experiment renders as an
    explicit `.copy(...)` instead of silently claiming to be the preset.
    """
    if (spec.family, spec.fill) == (defaults.family, defaults.fill):
        overrides = [
            f"{axis.lower()} = {value}"
            for axis, value in zip(("opsz", "wght", "grad", "rond"), (spec.opsz, spec.wght, spec.grad, spec.rond))
            if value != getattr(defaults, axis)
        ]
        if not overrides:
            return f"SymbolStyle.{PRESET_NAME}"
        return f"SymbolStyle.{PRESET_NAME}.copy({', '.join(overrides)})"
    return (
        f"SymbolStyle(SymbolFamily.{spec.family}, SymbolFill.{fill_enum(spec)}, "
        f"{spec.opsz}, {spec.wght}, {spec.grad}, {spec.rond})"
    )


def validate_values(where: str, values: dict) -> None:
    """Reject typos and out-of-range axes before anything is fetched or written."""
    unknown = sorted(set(values) - set(SPEC_FIELDS))
    if unknown:
        raise SystemExit(f"{where}: unknown field(s) {unknown}; expected a subset of {list(SPEC_FIELDS)}")
    family = values.get("family")
    if family is not None and family not in FAMILIES:
        raise SystemExit(f"{where}: unknown family {family!r}; expected one of {sorted(FAMILIES)}")
    if "auto_mirror" in values and not isinstance(values["auto_mirror"], bool):
        raise SystemExit(f"{where}: auto_mirror must be true or false, got {values['auto_mirror']!r}")
    for axis, (low, high) in AXIS_RANGES.items():
        if axis not in values:
            continue
        number = values[axis]
        if isinstance(number, bool) or not isinstance(number, int) or not low <= number <= high:
            raise SystemExit(f"{where}: {axis} = {number!r} is invalid, expected an integer in {low}..{high}")


def load_manifest(path: pathlib.Path) -> tuple[IconSpec, list[tuple[str, dict]]]:
    """Read [defaults] and the [icons] table, validating everything up front."""
    try:
        import tomllib
    except ModuleNotFoundError:  # Python < 3.11
        raise SystemExit(f"reading {path.name} needs Python 3.11+ (tomllib is missing here)") from None

    if not path.is_file():
        raise SystemExit(f"manifest not found: {path}")
    try:
        data = tomllib.loads(path.read_text(encoding="utf-8"))
    except tomllib.TOMLDecodeError as error:
        raise SystemExit(f"invalid TOML in {path}: {error}") from None

    unknown = sorted(set(data) - set(SECTIONS))
    if unknown:
        raise SystemExit(f"{path}: unknown section(s) {unknown}; expected {list(SECTIONS)}")
    defaults, icons = data.get("defaults"), data.get("icons")
    if not isinstance(defaults, dict) or not isinstance(icons, list):
        raise SystemExit(f"{path}: [defaults] and [[icons]] are both required")
    if not icons:
        raise SystemExit(f"{path}: [[icons]] is empty; refusing to wipe the generated namespace")

    missing = [field for field in REQUIRED_DEFAULTS if field not in defaults]
    if missing:
        raise SystemExit(f"{path}: [defaults] is missing {missing}")
    validate_values(f"{path}: [defaults]", defaults)

    entries: list[tuple[str, dict]] = []
    for index, entry in enumerate(icons):
        where = f"{path}: [[icons]] #{index + 1}"
        if not isinstance(entry, dict):
            raise SystemExit(f"{where} must be a table with a name, e.g. name = \"home\"")
        name = entry.get("name")
        if not isinstance(name, str) or not NAME_RE.match(name):
            raise SystemExit(
                f"{where}: invalid or missing name {name!r}; expected lowercase snake_case "
                f"starting with a letter (the name becomes a Kotlin identifier)"
            )
        # `name` is the only key that is not an axis override.
        overrides = {key: value for key, value in entry.items() if key != "name"}
        validate_values(f"{where} ({name})", overrides)
        entries.append((name, overrides))

    values = {field: defaults[field] for field in REQUIRED_DEFAULTS}
    values["auto_mirror"] = defaults.get("auto_mirror", False)
    return IconSpec(name="", **values), entries


def spec_from(defaults: IconSpec, name: str, overrides: dict) -> IconSpec:
    """`[defaults]` overlaid with one entry's overrides."""
    values = {field: getattr(defaults, field) for field in SPEC_FIELDS}
    values.update(overrides)
    return IconSpec(name=name, **values)


def build_specs(defaults: IconSpec, entries: list[tuple[str, dict]]) -> list[IconSpec]:
    return [spec_from(defaults, name, overrides) for name, overrides in entries]


def check_unique(specs: list[IconSpec]) -> None:
    """Two names colliding on one Kotlin property/file would silently drop one."""
    seen: dict[str, str] = {}
    for spec in specs:
        key = file_name(spec).lower()
        if key in seen:
            raise SystemExit(f"{spec.name!r} and {seen[key]!r} generate the same file name")
        seen[key] = spec.name


def apply_overrides(defaults: IconSpec, overrides: list[str]) -> IconSpec:
    """`--set axis=value` rewrites the shared style for this run only."""
    for override in overrides:
        axis, separator, value = override.partition("=")
        if not separator or axis not in SETTABLE:
            raise SystemExit(f"--set expects {'|'.join(SETTABLE)}=VALUE, got {override!r}")
        try:
            number = int(value)
        except ValueError:
            raise SystemExit(f"--set {axis} expects a number, got {value!r}") from None
        low, high = AXIS_RANGES[axis]
        if not low <= number <= high:
            raise SystemExit(f"--set {axis}={number} is out of range {low}..{high}")
        defaults = replace(defaults, **{axis: number})
    return defaults


def fetch(spec: IconSpec) -> str:
    if spec.family not in FAMILIES:
        raise SystemExit(f"unknown family {spec.family!r}; expected one of {sorted(FAMILIES)}")
    var = ",".join(str(v) for v in spec.axes())
    url = (
        ENDPOINT.format(family=FAMILIES[spec.family], size=SIZE, icon=spec.name)
        + f"?var={','.join(AXIS_ORDER)}@{var}"
    )
    request = urllib.request.Request(
        url, headers={"User-Agent": "kernelsu-icon-fetcher", "Accept-Encoding": "gzip"}
    )
    with urllib.request.urlopen(request, timeout=60) as response:
        body = response.read()
    if body[:2] == b"\x1f\x8b":
        body = gzip.decompress(body)
    return body.decode("utf-8")


def indent_body(expr: str) -> list[str]:
    """Re-indent the fetched expression the way the IDE leaves it.

    Google formats the snippet for its own 2-space style and breaks the
    `Builder(...).apply { … }.build()` chain over separate lines, while the
    project glues each chained call to the token that closes what it is called
    on (`).apply {`, `}.build()`) and indents arguments by 4.

    Rebuilding the indentation from the bracket nesting - instead of scaling
    whatever Google sent - makes the output byte-identical to a reformat, so
    committing an icon never shows up as a formatting diff.
    """
    lines: list[str] = []
    depth = 0
    for raw_line in expr.splitlines():
        line = raw_line.strip()
        if not line:
            continue

        # Closing tokens that open the line pull it back out of the nesting.
        leading = 0
        for char in line:
            if char.isspace():
                continue
            if char in ")}":
                leading += 1
                continue
            break

        if line.startswith("."):
            # A chained call stays glued to the call it follows.
            if not lines:
                raise SystemExit(f"malformed snippet, chain without a receiver: {line!r}")
            lines[-1] += line
        else:
            lines.append(f"    {' ' * (max(depth - leading, 0) * 4)}{line}")

        in_string = False
        for char in line:
            if in_string:
                in_string = char != '"'
            elif char == '"':
                in_string = True
            elif char in "({":
                depth += 1
            elif char in ")}":
                depth -= 1
        if depth < 0:
            raise SystemExit(f"malformed snippet, unbalanced brackets at {line!r}")
    if depth:
        raise SystemExit("malformed snippet, unbalanced brackets")

    if not lines or not lines[0].endswith("ImageVector.Builder(") or lines[-1] != "    }.build()":
        raise SystemExit(f"unexpected snippet shape: {lines[:1]} … {lines[-1:]}")
    return lines


def icon_file(source: str, spec: IconSpec, defaults: IconSpec) -> str:
    prop = pascal(spec.name)
    head = source.index("ImageVector.Builder(")
    expr = source[source.rindex("\n", 0, head) + 1 : source.rindex(".build()") + len(".build()")]
    expr = expr.replace(f'name = "{spec.name}",', f'name = "{prop}",')
    expr = expr.replace("PathFillType.Companion.NonZero", "PathFillType.NonZero")
    if spec.auto_mirror:
        expr, count = re.subn(
            r"(?m)^(\s*)viewportHeight = 24f,\n",
            r"\1viewportHeight = 24f,\n\1autoMirror = true,\n",
            expr,
        )
        if count != 1:
            raise SystemExit(f"failed to enable autoMirror for {spec.name}")

    expected = int(SIZE.removesuffix("dp"))
    if f"viewportWidth = {expected}f" not in expr:
        raise SystemExit(f"unexpected viewport for {spec.name}: size/axis mismatch?")

    body = indent_body(expr)

    return (
        f"package {PACKAGE}\n\n"
        f"{ICON_IMPORTS}\n"
        f'@Suppress("CheckReturnValue")\n'
        f"internal val {value_name(spec)}: MaterialSymbol = MaterialSymbol(\n"
        f'    name = "{spec.name}",\n'
        f"    style = {style_expr(spec, defaults)},\n"
        f") {{\n" + "\n".join(body) + "\n}\n"
    )


def symbols_file(specs: list[IconSpec]) -> str:
    lines = [
        f"package {PACKAGE}",
        "",
        "import androidx.compose.ui.graphics.vector.ImageVector",
        "",
        "object MaterialSymbols {",
    ]
    groups: dict[tuple[int, str], list[IconSpec]] = {}
    for spec in specs:
        groups.setdefault((spec.fill, spec.family), []).append(spec)

    for (fill, family), members in sorted(
        groups.items(), key=lambda item: (item[0][0] != DEFAULT_FILL, item[0][1])
    ):
        axes = {member.axes() for member in members}
        if len(axes) == 1:
            opsz, wght, _, grad, rond = axes.pop()
            note = f"opsz={opsz}, wght={wght}, GRAD={grad}, ROND={rond}"
        else:
            note = "see each icon for its axes"
        # The default fill needs no level: MaterialSymbols.<Family>.<Name>.
        levels = [family] if fill == DEFAULT_FILL else [FILL_NAMES[fill], family]
        lines.append(f"    /** Material Symbols {family}, FILL={fill} - {note}. */")
        for depth, level in enumerate(levels):
            lines.append(f"{'    ' * (depth + 1)}object {level} {{")
        for member in sorted(members, key=lambda m: pascal(m.name)):
            lines.append(
                f"{'    ' * (len(levels) + 1)}val {pascal(member.name)}: ImageVector"
                f" get() = {value_name(member)}.vector"
            )
        for depth in reversed(range(len(levels))):
            lines.append(f"{'    ' * (depth + 1)}}}")
        lines.append("")

    lines.append("    /** Every icon of the set, with the style it was generated with. */")
    lines.append("    val icons: List<MaterialSymbol> = listOf(")
    for spec in sorted(specs, key=lambda s: pascal(s.name)):
        lines.append(f"        {value_name(spec)},")
    lines += ["    )", "}", ""]
    return "\n".join(lines)


# Only files carrying this marker are considered generated, so `--prune` can
# never delete something hand-written that happens to live in the package.
GENERATED_MARKER = '@Suppress("CheckReturnValue")\ninternal val '


def stale_files(specs: list[IconSpec]) -> list[pathlib.Path]:
    """Generated files that the manifest no longer asks for (removed or refilled)."""
    expected = {file_name(spec) for spec in specs}
    stale = []
    for path in sorted(OUT_DIR.glob("*.kt")):
        if path.name in expected:
            continue
        if GENERATED_MARKER in path.read_text(encoding="utf-8"):
            stale.append(path)
    return stale


ENUM_RE = {
    "SymbolFamily": re.compile(r"enum class SymbolFamily[^{]*\{(.*?)\n\}", re.S),
    "SymbolFill": re.compile(r"enum class SymbolFill[^{]*\{(.*?)\n\}", re.S),
}


def check_hand_written(manifest: pathlib.Path, defaults: IconSpec, specs: list[IconSpec]) -> None:
    """Guard the hand-written types against the manifest."""
    hand_written = (OUT_DIR / "MaterialSymbol.kt").read_text(encoding="utf-8")

    match = PRESET_RE.search(hand_written)
    if not match:
        raise SystemExit("could not find SymbolStyle.FilledRounded in MaterialSymbol.kt")
    family, fill, opsz, wght, grad, rond = match.groups()
    actual = (family, 1 if fill == "Filled" else 0, int(opsz), int(wght), int(grad), int(rond))
    expected = (defaults.family, defaults.fill, defaults.opsz, defaults.wght, defaults.grad, defaults.rond)
    if actual != expected:
        raise SystemExit(
            f"{manifest}: [defaults] {expected} does not match the hand-written "
            f"SymbolStyle.FilledRounded {actual} in MaterialSymbol.kt"
        )

    declared = {}
    for name, pattern in ENUM_RE.items():
        body = pattern.search(hand_written)
        if not body:
            raise SystemExit(f"could not find {name} in MaterialSymbol.kt")
        declared[name] = set(re.findall(r"\b([A-Z]\w*)\(", body.group(1)))
    for spec in specs:
        if spec.family not in declared["SymbolFamily"]:
            raise SystemExit(f"SymbolFamily.{spec.family} is not declared in MaterialSymbol.kt")
        if fill_enum(spec) not in declared["SymbolFill"]:
            raise SystemExit(f"SymbolFill.{fill_enum(spec)} is not declared in MaterialSymbol.kt")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--manifest",
        type=pathlib.Path,
        default=MANIFEST,
        help=f"manifest to read (default: {MANIFEST.name} next to this script)",
    )
    parser.add_argument("--icons", help="comma separated Material Symbols names to refresh")
    parser.add_argument(
        "--set",
        action="append",
        default=[],
        metavar="AXIS=VALUE",
        help=f"override an axis of the shared style for this run, e.g. --set wght=500 ({'|'.join(SETTABLE)})",
    )
    parser.add_argument(
        "--prune",
        action="store_true",
        help="delete generated icons that the manifest no longer asks for",
    )
    args = parser.parse_args()
    if args.set and args.icons:
        # A partial run would leave the namespace describing a style most files do not have.
        raise SystemExit("--set restyles the whole set; drop --icons")

    defaults, entries = load_manifest(args.manifest)
    specs = build_specs(defaults, entries)
    check_unique(specs)
    check_hand_written(args.manifest, defaults, specs)

    styles = apply_overrides(defaults, args.set)
    specs = build_specs(styles, entries)

    selected = specs
    if args.icons:
        wanted = {name.strip() for name in args.icons.split(",") if name.strip()}
        unknown = sorted(wanted - {spec.name for spec in specs})
        if unknown:
            known = sorted({spec.name for spec in specs})
            raise SystemExit(f"unknown icon(s) {unknown}; known: {known}")
        selected = [spec for spec in specs if spec.name in wanted]

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for spec in selected:
        target = OUT_DIR / file_name(spec)
        target.write_text(icon_file(fetch(spec), spec, defaults), encoding="utf-8")
        print(f"wrote {target.relative_to(REPO)} [{style_expr(spec, defaults)}]")

    # The namespace always describes the whole set, also for a partial refresh.
    namespace = OUT_DIR / "MaterialSymbols.kt"
    namespace.write_text(symbols_file(specs), encoding="utf-8")
    print(f"wrote {namespace.relative_to(REPO)} ({len(specs)} icons)")

    leftovers = stale_files(specs)
    if leftovers:
        names = ", ".join(path.name for path in leftovers)
        if args.prune:
            for path in leftovers:
                path.unlink()
                print(f"removed {path.relative_to(REPO)} (no longer in the manifest)")
        else:
            print(f"warning: {names} is no longer in the manifest; re-run with --prune", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main())
