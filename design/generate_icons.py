"""Builds the Cinemerick app icon: the five cinema logos as round badges around a play button, on a purple gradient.

Run from the repo root: python3 design/generate_icons.py [--install]
Sources: design/logos/uci.jpg, the_space.jpg, notorious.jpg, cristallo.jpg (square jpgs), cinergia.png (transparent wordmark). Outputs: design/out/*.png;
--install also copies them where each platform expects them.
"""
import math
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path
from PIL import Image, ImageChops, ImageDraw, ImageFilter

ROOT = Path(__file__).parent
REPO = ROOT.parent
OUT = ROOT / "out"
OUT.mkdir(exist_ok=True)

SS = 2  # supersampling for smooth edges

_cinergia = Image.open(ROOT / "logos" / "cinergia.png").convert("RGBA")
CINERGIA_BG = (0x41, 0x4C, 0x4C)  # same as the Cinergia badge color


def square_logo(name):
    return Image.open(ROOT / "logos" / name).convert("RGB")


def cinergia_logo():
    """The transparent wordmark centred on a dark grey square."""
    word = _cinergia.crop(_cinergia.getchannel("A").getbbox())
    canvas = Image.new("RGB", (480, 480), CINERGIA_BG)
    w = 400
    word = word.resize((w, int(word.height * w / word.width)), Image.LANCZOS)
    canvas.paste(word, ((480 - w) // 2, (480 - word.height) // 2), word)
    return canvas


# Clockwise from the top: logo and the zoom that fits it in its circle (the square's corners are cropped away).
BADGES = [
    (square_logo("uci.jpg"), 1.0),
    (square_logo("the_space.jpg"), 0.9),
    (cinergia_logo(), 1.0),
    (square_logo("notorious.jpg"), 1.0),
    (square_logo("cristallo.jpg"), 0.92),
]
RING_RADIUS = 0.285  # of the canvas, centre of a badge to the canvas centre
BADGE_DIAMETER = 0.30
GRADIENT = ((0x2A, 0x14, 0x45), (0x6A, 0x4C, 0x93))  # the app's purple, dark at the top-left


def gradient_background(size):
    vertical = Image.linear_gradient("L").resize((size, size))
    mix = ImageChops.add(vertical, vertical.transpose(Image.TRANSPOSE), scale=2)
    return Image.composite(Image.new("RGB", (size, size), GRADIENT[1]), Image.new("RGB", (size, size), GRADIENT[0]), mix)


def badges_layer(size, scale=1.0):
    """Transparent layer with the five round badges (white rim, soft shadow) and a play button in the middle, pulled towards the centre by `scale`."""
    big = size * SS
    layer = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    shadow = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    d = int(big * BADGE_DIAMETER * scale)
    rim = max(2, int(d * 0.045))
    for i, (logo, zoom) in enumerate(BADGES):
        angle = -math.pi / 2 + i * 2 * math.pi / 5
        cx = big / 2 + math.cos(angle) * big * RING_RADIUS * scale
        cy = big / 2 + math.sin(angle) * big * RING_RADIUS * scale
        inner = d - 2 * rim
        face = Image.new("RGB", (inner, inner), logo.getpixel((2, 2)))
        scaled = logo.resize((int(inner * zoom), int(inner * zoom)), Image.LANCZOS)
        face.paste(scaled, ((inner - scaled.width) // 2, (inner - scaled.height) // 2))
        mask = Image.new("L", (inner * 2, inner * 2), 0)
        ImageDraw.Draw(mask).ellipse([0, 0, inner * 2 - 1, inner * 2 - 1], fill=255)
        mask = mask.resize((inner, inner), Image.LANCZOS)
        pos = (int(cx - d / 2), int(cy - d / 2))
        ring = Image.new("L", (d * 2, d * 2), 0)
        ImageDraw.Draw(ring).ellipse([0, 0, d * 2 - 1, d * 2 - 1], fill=255)
        ring = ring.resize((d, d), Image.LANCZOS)
        ImageDraw.Draw(shadow).ellipse([pos[0], pos[1] + d * 0.05, pos[0] + d, pos[1] + d * 1.05], fill=(0, 0, 0, 110))
        layer.paste(Image.new("RGBA", (d, d), (255, 255, 255, 255)), pos, ring)
        layer.paste(face, (pos[0] + rim, pos[1] + rim), mask)
    # A white rounded play triangle in the middle.
    r = big * 0.075 * scale
    pts = [(big / 2 - r * 0.62, big / 2 - r), (big / 2 - r * 0.62, big / 2 + r), (big / 2 + r * 1.05, big / 2)]
    play = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    ImageDraw.Draw(play).polygon(pts, fill=(255, 255, 255, 235))
    play = play.filter(ImageFilter.GaussianBlur(big * 0.0008))
    shadow = shadow.filter(ImageFilter.GaussianBlur(big * 0.012))
    out = Image.alpha_composite(shadow, layer)
    out = Image.alpha_composite(out, play)
    return out.resize((size, size), Image.LANCZOS)


def compose(size):
    img = gradient_background(size).convert("RGBA")
    return Image.alpha_composite(img, badges_layer(size)).convert("RGB")  # iOS requires no transparency


def build():
    master = compose(1024)
    master.save(OUT / "icon-1024.png")
    # Android adaptive layers are 108dp with a 66dp safe zone: keep the badges inside the central 66%
    gradient_background(432).save(OUT / "ic_launcher_background.png")
    badges_layer(432, scale=0.72).save(OUT / "ic_launcher_foreground.png")
    return master


def install_outputs(master):
    """Copies the generated icons where each platform expects them."""
    res = REPO / "app" / "src"
    shutil.copy(OUT / "ic_launcher_background.png", REPO / "androidApp/src/main/res/drawable-nodpi/ic_launcher_background.png")
    shutil.copy(OUT / "ic_launcher_foreground.png", REPO / "androidApp/src/main/res/drawable-nodpi/ic_launcher_foreground.png")
    shutil.copy(OUT / "icon-1024.png", REPO / "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/icon-1024.png")
    master.resize((512, 512), Image.LANCZOS).save(res / "desktopMain/resources/icon.png")
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
