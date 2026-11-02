"""Convert the saved original component art to reproducible 32x32 inventory sprites.

No drawing or smoothing: retain each source silhouette, fit it within 28 pixels,
sample with nearest neighbor, and snap opaque pixels to the shared project palette.
"""
from PIL import Image, ImageColor, ImageDraw

from common import PALETTE, ROOT, save_texture

NAMES = (
    "phase_crystal",
    "stabilized_phase_crystal",
    "resonance_coil",
    "phase_alloy_ingot",
    "phase_alloy_plate",
)
SOURCE = ROOT / "docs/art/components"
COLORS = [ImageColor.getrgb(value) for value in PALETTE.values()]


def convert(name):
    source = Image.open(SOURCE / f"{name}_source.png").convert("RGBA")
    # Ignore near-transparent generation fringes when measuring the silhouette.
    bounds = source.getchannel("A").point(lambda a: 255 if a >= 128 else 0).getbbox()
    if bounds is None:
        raise ValueError(f"Empty source: {name}")
    source = source.crop(bounds)
    factor = 28 / max(source.size)
    source = source.resize(tuple(max(1, round(size * factor)) for size in source.size), Image.Resampling.NEAREST)
    pixels = []
    for r, g, b, a in source.get_flattened_data():
        if a < 128:
            pixels.append((0, 0, 0, 0))
        else:
            nearest = min(COLORS, key=lambda c: (r-c[0])**2 + (g-c[1])**2 + (b-c[2])**2)
            pixels.append(nearest + (255,))
    source.putdata(pixels)
    sprite = Image.new("RGBA", (32, 32))
    sprite.paste(source, ((32-source.width)//2, (32-source.height)//2))
    save_texture(sprite, f"item/{name}.png")
    return sprite


def main():
    sprites = [convert(name) for name in NAMES]
    preview = Image.new("RGBA", (1000, 460), (28, 32, 40, 255))
    draw = ImageDraw.Draw(preview)
    for index, (name, sprite) in enumerate(zip(NAMES, sprites)):
        x = index * 200
        draw.text((x+8, 8), name.replace("_", " "), fill="white")
        for y, background in ((30, (40, 44, 52, 255)), (220, (198, 198, 198, 255))):
            tile = Image.new("RGBA", (160, 160), background)
            tile.alpha_composite(sprite.resize((160, 160), Image.Resampling.NEAREST))
            preview.alpha_composite(tile, (x+20, y))
        # Native 32px and reduced 16px inventory-scale inspections.
        preview.alpha_composite(sprite, (x+60, 405))
        preview.alpha_composite(sprite.resize((16, 16), Image.Resampling.NEAREST), (x+115, 413))
    target = ROOT / "build/component-texture-preview.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    preview.save(target)
    print(f"Converted {len(sprites)} sprites; preview: {target}")


if __name__ == "__main__":
    main()
