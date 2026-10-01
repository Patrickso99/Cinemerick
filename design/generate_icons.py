"""Builds the Cinemerick app icon: UCI, The Space, Cinergia and Notorious from the top-left to the bottom-right, split by three parallel diagonals.

Run from the repo root: python3 design/generate_icons.py [--install]
Sources: design/logos/uci.jpg, the_space.jpg, notorious.jpg (480x480), cinergia.png (transparent wordmark). Outputs: design/out/*.png;
--install also copies them where each platform expects them.
"""
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path
from PIL import Image, ImageChops, ImageDraw

ROOT = Path(__file__).parent
REPO = ROOT.parent
OUT = ROOT / "out"
OUT.mkdir(exist_ok=True)

UCI = Image.open(ROOT / "logos" / "uci.jpg").convert("RGB")
SPACE = Image.open(ROOT / "logos" / "the_space.jpg").convert("RGB")
NOTORIOUS = Image.open(ROOT / "logos" / "notorious.jpg").convert("RGB")
_cinergia = Image.open(ROOT / "logos" / "cinergia.png").convert("RGBA")
CINERGIA = _cinergia.crop(_cinergia.getchannel("A").getbbox())
CINERGIA_BG = (0x41, 0x4C, 0x4C)  # same as the Cinergia badge color

SS = 2  # supersampling for smooth diagonal edges
LINE = (255, 255, 255)

# The separators are the lines x + y = cut * size (they run bottom-left to top-right), one between each pair of neighbouring areas.
CUTS = (0.52, 1.0, 1.48)

# Per-logo placement, as fractions of the canvas: logo width and centre of the logo (UCI, The Space, Notorious: the square jpg).
UCI_WIDTH, UCI_CENTER = 0.32, (0.165, 0.165)
SPACE_WIDTH, SPACE_CENTER = 0.38, (0.38, 0.38)
CINERGIA_WIDTH, CINERGIA_CENTER = 0.40, (0.62, 0.62)
NOTORIOUS_WIDTH, NOTORIOUS_CENTER = 0.30, (0.84, 0.84)


def place(logo, size, width, center, bg, scale):
    """Pastes `logo` (width as a fraction of the canvas, aspect kept, centred on `center`), both pulled towards the canvas centre by `scale`."""
    canvas = bg.copy() if isinstance(bg, Image.Image) else Image.new("RGB", (size, size), bg)
    w = int(size * width * scale)
    resized = logo.resize((w, int(logo.height * w / logo.width)), Image.LANCZOS)
    cx = size * (0.5 + (center[0] - 0.5) * scale)
    cy = size * (0.5 + (center[1] - 0.5) * scale)
    pos = (int(cx - resized.width / 2), int(cy - resized.height / 2))
    if resized.mode == "RGBA":
        canvas.paste(resized, pos, resized)
    else:
        canvas.paste(resized, pos)
    return canvas


def notorious_layer(size):
    """Notorious: the blue NC lettering and sketch lines of the logo, keyed out of its silver square and drawn on our own silver gradient,
    so the logo can be sized freely without its square edge showing."""
    blue = NOTORIOUS.getpixel((150, 240))
    r, _, b = NOTORIOUS.split()
    alpha = ImageChops.subtract(b, r).point(lambda v: min(255, max(0, (v - 20) * 4)))
    ink = Image.new("RGB", NOTORIOUS.size, blue)
    ink.putalpha(alpha)
    vertical = Image.linear_gradient("L").resize((size, size))
    gradient = ImageChops.add(vertical, vertical.transpose(Image.TRANSPOSE), scale=2)  # light at the top-left, dark at the bottom-right
    light, dark = (206, 207, 208), (0xA4, 0xA5, 0xA6)
    return Image.composite(Image.new("RGB", (size, size), dark), Image.new("RGB", (size, size), light), gradient), ink


def cuts(logo_scale):
    """Separator positions, pulled towards the centre together with the logos."""
    return tuple(1 + (cut - 1) * logo_scale for cut in CUTS)


def diagonal_mask(size, cut, above):
    """White on the side of the line x + y = cut*size that holds the top-left corner (`above`) or the bottom-right one."""
    mask = Image.new("L", (size, size), 0)
    c = cut * size
    far = 3 * size
    if above:
        ImageDraw.Draw(mask).polygon([(-far, -far), (c + far, -far), (-far, c + far)], fill=255)
    else:
        ImageDraw.Draw(mask).polygon([(c + far, -far), (c + far, c + far), (-far, c + far)], fill=255)
    return mask


def compose(size, logo_scale=1.0, line_width=0.014):
    big = size * SS
    silver, ink = notorious_layer(big)
    areas = [
        place(UCI, big, UCI_WIDTH, UCI_CENTER, UCI.getpixel((2, 2)), logo_scale),
        place(SPACE, big, SPACE_WIDTH, SPACE_CENTER, SPACE.getpixel((2, 2)), logo_scale),
        place(CINERGIA, big, CINERGIA_WIDTH, CINERGIA_CENTER, CINERGIA_BG, logo_scale),
        place(ink, big, NOTORIOUS_WIDTH, NOTORIOUS_CENTER, silver, logo_scale),
    ]
    img = areas[0]
    for area, cut in zip(areas[1:], cuts(logo_scale)):
        img = Image.composite(area, img, diagonal_mask(big, cut, above=False))
    if line_width:
        draw = ImageDraw.Draw(img)
        for cut in cuts(logo_scale):
            draw.line([(cut * big, 0), (0, cut * big)], fill=LINE, width=int(big * line_width))
    return img.resize((size, size), Image.LANCZOS)


def separators_only(size, logo_scale=1.0, line_width=0.014):
    """Transparent layer holding just the diagonal lines (Android adaptive foreground)."""
    big = size * SS
    layer = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    draw = ImageDraw.Draw(layer)
    for cut in cuts(logo_scale):
        draw.line([(cut * big, 0), (0, cut * big)], fill=LINE + (255,), width=int(big * line_width))
    return layer.resize((size, size), Image.LANCZOS)


def build():
    master = compose(1024)  # iOS requires no transparency
    master.save(OUT / "icon-1024.png")
    # Android adaptive layers are 108dp with a 66dp safe zone: keep logos inside the central 66%
    compose(432, logo_scale=0.72, line_width=0).save(OUT / "ic_launcher_background.png")
    separators_only(432, logo_scale=0.72).save(OUT / "ic_launcher_foreground.png")
    return master


def install_outputs(master):
    """Copies the generated icons where each platform expects them."""
    res = REPO / "app" / "src"
    shutil.copy(OUT / "ic_launcher_background.png", res / "androidMain/res/drawable-nodpi/ic_launcher_background.png")
    shutil.copy(OUT / "ic_launcher_foreground.png", res / "androidMain/res/drawable-nodpi/ic_launcher_foreground.png")
    shutil.copy(OUT / "icon-1024.png", REPO / "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/icon-1024.png")
    master.resize((512, 512), Image.LANCZOS).save(res / "desktopMain/resources/icon.png")
    master.resize((192, 192), Image.LANCZOS).save(res / "wasmJsMain/resources/favicon.png")
    master.save(res / "desktopMain/resources/icon.ico", sizes=[(s, s) for s in (16, 32, 48, 64, 128, 256)])
    with tempfile.TemporaryDirectory() as tmp:
        iconset = Path(tmp) / "icon.iconset"
        iconset.mkdir()
        for base in (16, 32, 128, 256, 512):
            master.resize((base, base), Image.LANCZOS).save(iconset / f"icon_{base}x{base}.png")
            master.resize((base * 2, base * 2), Image.LANCZOS).save(iconset / f"icon_{base}x{base}@2x.png")
        subprocess.run(["iconutil", "-c", "icns", str(iconset), "-o", str(res / "desktopMain/resources/icon.icns")], check=True)


if __name__ == "__main__":
    master = build()
    if "--install" in sys.argv:
        install_outputs(master)
    print("ok")
