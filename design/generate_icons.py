"""Builds the Cinemerick app icon: a white cinema ticket with a play button and an Italian tricolour stub, on a purple gradient.

Run from the repo root: python3 design/generate_icons.py [--install]
Outputs: design/out/*.png; --install also copies them where each platform expects them.
"""
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

GRADIENT = ((0x2A, 0x14, 0x45), (0x6A, 0x4C, 0x93))  # the app's purple, dark at the top-left
PLAY_PURPLE = (0x4B, 0x2E, 0x83)
TRICOLOUR = ((0x00, 0x92, 0x46), (0xFF, 0xFF, 0xFF), (0xCE, 0x2B, 0x37))
TICKET_TILT = 12  # degrees, counter-clockwise


def gradient_background(size):
    vertical = Image.linear_gradient("L").resize((size, size))
    mix = ImageChops.add(vertical, vertical.transpose(Image.TRANSPOSE), scale=2)
    return Image.composite(Image.new("RGB", (size, size), GRADIENT[1]), Image.new("RGB", (size, size), GRADIENT[0]), mix)


def ticket_layer(size, scale=1.0):
    """Transparent layer with the tilted ticket (soft shadow), pulled towards the centre by `scale`."""
    big = size * SS
    w, h = int(big * 0.68 * scale), int(big * 0.42 * scale)
    stub = int(w * 0.24)  # the tear-off part on the right
    radius = int(h * 0.10)
    notch = int(h * 0.12)
    ticket = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(ticket)
    d.rounded_rectangle([0, 0, w - 1, h - 1], radius=radius, fill=(255, 255, 255, 255))
    # Half-circle notches where the stub tears off, top and bottom.
    cut = w - stub
    mask = Image.new("L", (w, h), 255)
    md = ImageDraw.Draw(mask)
    md.ellipse([cut - notch, -notch, cut + notch, notch], fill=0)
    md.ellipse([cut - notch, h - notch, cut + notch, h + notch], fill=0)
    ticket.putalpha(ImageChops.multiply(ticket.getchannel("A"), mask))
    d = ImageDraw.Draw(ticket)
    # Perforation between the two parts.
    dash = max(2, int(h * 0.035))
    y = notch + dash * 2
    while y < h - notch - dash * 2:
        d.rounded_rectangle([cut - dash // 2, y, cut + dash // 2, y + dash * 2], radius=dash // 2, fill=(0x9A, 0x8B, 0xB8, 255))
        y += dash * 4
    # Play button on the main part.
    cx, cy, r = (cut) / 2, h / 2, h * 0.27
    d.polygon([(cx - r * 0.62, cy - r), (cx - r * 0.62, cy + r), (cx + r * 1.05, cy)], fill=PLAY_PURPLE + (255,))
    # Tricolour on the stub: three bars (the white one is edged so it stays visible on the white ticket).
    bar_w = int(stub * 0.22)
    bar_h = int(h * 0.62)
    gap = int(bar_w * 0.22)
    total = 3 * bar_w + 2 * gap
    x0 = cut + (stub - total) // 2
    y0 = (h - bar_h) // 2
    for i, colour in enumerate(TRICOLOUR):
        x = x0 + i * (bar_w + gap)
        d.rounded_rectangle([x, y0, x + bar_w, y0 + bar_h], radius=bar_w // 3, fill=colour + (255,),
                            outline=(0x9A, 0x8B, 0xB8, 255) if colour == (255, 255, 255) else None, width=max(2, int(h * 0.012)))
    canvas = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    rotated = ticket.rotate(TICKET_TILT, resample=Image.BICUBIC, expand=True)
    pos = ((big - rotated.width) // 2, (big - rotated.height) // 2)
    shadow = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    shadow.paste(Image.new("RGBA", rotated.size, (0, 0, 0, 120)), (pos[0], pos[1] + int(big * 0.015)), rotated.getchannel("A"))
    shadow = shadow.filter(ImageFilter.GaussianBlur(big * 0.014))
    canvas.paste(rotated, pos, rotated)
    out = Image.alpha_composite(shadow, canvas)
    return out.resize((size, size), Image.LANCZOS)


def compose(size):
    img = gradient_background(size).convert("RGBA")
    return Image.alpha_composite(img, ticket_layer(size)).convert("RGB")  # iOS requires no transparency


def build():
    master = compose(1024)
    master.save(OUT / "icon-1024.png")
    # Android adaptive layers are 108dp with a 66dp safe zone: keep the badges inside the central 66%
    gradient_background(432).save(OUT / "ic_launcher_background.png")
    ticket_layer(432, scale=0.8).save(OUT / "ic_launcher_foreground.png")
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
