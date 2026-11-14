#!/usr/bin/env python3
"""Build Android adaptive icon layers from Public-Assets/app-logo.svg.

Authoritative outputs: Public-Assets/adaptive-icon/
Android sync: android/app/src/main/res/drawable-nodpi/ic_launcher_foreground.png

Spec: planning/branding.md § Adaptive icon (Android) — foreground fits ~66dp safe zone on 108dp layer.
"""
from __future__ import annotations

import argparse
import io
import shutil
import sys
from pathlib import Path

CANVAS_PX = 432
SAFE_ZONE_RATIO = 0.58  # central mark vs 108dp adaptive canvas (~66dp safe diameter)
BRAND_BG = (0x18, 0x1A, 0x25, 0xFF)


def repo_root() -> Path:
    return Path(__file__).resolve().parents[2]


def generate(svg: Path, out_dir: Path, android_fg: Path) -> None:
    try:
        import cairosvg
        from PIL import Image
    except ImportError as err:
        raise SystemExit(
            "Missing deps for icon generation. Run: pip install cairosvg pillow\n"
            f"({err})",
        ) from err

    logo_px = int(CANVAS_PX * SAFE_ZONE_RATIO)
    png_bytes = cairosvg.svg2png(url=str(svg), output_width=logo_px, output_height=logo_px)
    logo = Image.open(io.BytesIO(png_bytes)).convert("RGBA")
    foreground = Image.new("RGBA", (CANVAS_PX, CANVAS_PX), (0, 0, 0, 0))
    ox = (CANVAS_PX - logo.width) // 2
    oy = (CANVAS_PX - logo.height) // 2
    foreground.paste(logo, (ox, oy), logo)

    out_dir.mkdir(parents=True, exist_ok=True)
    fg_path = out_dir / "adaptive-icon-foreground.png"
    foreground.save(fg_path)

    bg = Image.new("RGBA", (CANVAS_PX, CANVAS_PX), BRAND_BG)
    preview = Image.alpha_composite(bg, foreground)
    preview.save(out_dir / "adaptive-icon-canvas.png")

    android_fg.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(fg_path, android_fg)
    print(f"Wrote {fg_path}")
    print(f"Wrote {out_dir / 'adaptive-icon-canvas.png'}")
    print(f"Synced {android_fg}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--check",
        action="store_true",
        help="Regenerate to a temp file and compare to android foreground (exit 1 if mismatch)",
    )
    args = parser.parse_args()
    root = repo_root()
    svg = root / "Public-Assets/app-logo.svg"
    out_dir = root / "Public-Assets/adaptive-icon"
    android_fg = root / "android/app/src/main/res/drawable-nodpi/ic_launcher_foreground.png"
    if not svg.is_file():
        print(f"Missing {svg}", file=sys.stderr)
        return 1
    if args.check:
        import tempfile

        with tempfile.TemporaryDirectory() as tmp:
            tmp_fg = Path(tmp) / "fg.png"
            generate(svg, Path(tmp), tmp_fg)
            if not android_fg.exists() or android_fg.read_bytes() != tmp_fg.read_bytes():
                print(
                    "Android ic_launcher_foreground.png is out of date. "
                    "Run: python3 planning/tools/generate_adaptive_icon.py",
                    file=sys.stderr,
                )
                return 1
        print("Brand icon assets in sync.")
        return 0
    generate(svg, out_dir, android_fg)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
