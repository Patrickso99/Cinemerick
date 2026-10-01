"""Builds the Cinemerick app icon: UCI (top-left) and The Space (bottom-right) split by a diagonal.

Run from the repo root: python3 design/generate_icons.py
Sources: design/logos/uci.jpg, design/logos/the_space.jpg (480x480). Outputs: design/out/*.png
"""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).parent
OUT = ROOT / "out"
OUT.mkdir(exist_ok=True)

UCI = Image.open(ROOT / "logos" / "uci.jpg").convert("RGB")
SPACE = Image.open(ROOT / "logos" / "the_space.jpg").convert("RGB")

SS = 2  # supersampling for smooth diagonal edges
LINE = (255, 255, 255)

# Per-logo placement, as fractions of the canvas: scale and offset of the logo centre from the canvas centre.
UCI_SCALE, UCI_OFFSET = 0.80, (-0.13, -0.13)
SPACE_SCALE, SPACE_OFFSET = 0.80, (0.07, 0.15)


def place(logo, size, scale, offset, bg):
    canvas = Image.new("RGB", (size, size), bg)
    s = int(size * scale)
    resized = logo.resize((s, s), Image.LANCZOS)
    x = (size - s) // 2 + int(offset[0] * size)
    y = (size - s) // 2 + int(offset[1] * size)
    canvas.paste(resized, (x, y))
    return canvas


def diagonal_mask(size):
    """White where UCI shows: the area above the line from bottom-left to top-right."""
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).polygon([(0, 0), (size, 0), (0, size)], fill=255)
    return mask


def compose(size, logo_scale=1.0, line_width=0.014):
    big = size * SS
    uci_bg = UCI.getpixel((2, 2))
    space_bg = SPACE.getpixel((2, 2))
    uci = place(UCI, big, UCI_SCALE * logo_scale, tuple(o * logo_scale for o in UCI_OFFSET), uci_bg)
    space = place(SPACE, big, SPACE_SCALE * logo_scale, tuple(o * logo_scale for o in SPACE_OFFSET), space_bg)
    img = Image.composite(uci, space, diagonal_mask(big))
    if line_width:
        ImageDraw.Draw(img).line([(big, 0), (0, big)], fill=LINE, width=int(big * line_width))
    return img.resize((size, size), Image.LANCZOS)


def separator_only(size, line_width=0.014):
    """Transparent layer holding just the diagonal line (Android adaptive foreground)."""
    big = size * SS
    layer = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    ImageDraw.Draw(layer).line([(big, 0), (0, big)], fill=LINE + (255,), width=int(big * line_width))
    return layer.resize((size, size), Image.LANCZOS)


def halves_only(size, logo_scale):
    """Both halves without the separator (Android adaptive background)."""
    return compose(size, logo_scale=logo_scale, line_width=0)


if __name__ == "__main__":
    # Full-bleed master for iOS / desktop / web (iOS requires no transparency)
    compose(1024).save(OUT / "icon-1024.png")
    # Android adaptive layers are 108dp with a 66dp safe zone: keep logos inside the central 66%
    halves_only(432, logo_scale=0.72).save(OUT / "ic_launcher_background.png")
    separator_only(432).save(OUT / "ic_launcher_foreground.png")
    print("ok")
