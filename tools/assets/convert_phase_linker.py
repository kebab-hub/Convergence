"""Reproduce the Phase Linker sprite from its saved original image-generation output."""
from PIL import Image, ImageColor, ImageDraw

from common import PALETTE, ROOT, save_texture


def main():
    image = Image.open(ROOT / "docs/art/phase_linker/source.png").convert("RGBA")
    bounds = image.getchannel("A").point(lambda a: 255 if a >= 128 else 0).getbbox()
    if bounds is None:
        raise ValueError("Empty source silhouette")
    image = image.crop(bounds)
    factor = 28 / max(image.size)
    image = image.resize(tuple(max(1, round(size * factor)) for size in image.size), Image.Resampling.NEAREST)
    palette = [ImageColor.getrgb(value) for value in PALETTE.values()]
    pixels = []
    for r, g, b, a in image.get_flattened_data():
        if a < 128:
            pixels.append((0, 0, 0, 0))
        else:
            rgb = min(palette, key=lambda c: (r-c[0])**2 + (g-c[1])**2 + (b-c[2])**2)
            pixels.append(rgb + (255,))
    image.putdata(pixels)
    sprite = Image.new("RGBA", (32, 32))
    sprite.paste(image, ((32-image.width)//2, (32-image.height)//2))
    save_texture(sprite, "item/phase_linker.png")
    preview = Image.new("RGBA", (400, 245), (28, 32, 40, 255))
    for x, background in ((20, (40, 44, 52, 255)), (220, (198, 198, 198, 255))):
        tile = Image.new("RGBA", (160, 160), background)
        tile.alpha_composite(sprite.resize((160, 160), Image.Resampling.NEAREST))
        preview.alpha_composite(tile, (x, 30))
        preview.alpha_composite(sprite, (x+40, 205))
        preview.alpha_composite(sprite.resize((16, 16), Image.Resampling.NEAREST), (x+100, 213))
    ImageDraw.Draw(preview).text((20, 8), "Phase Linker | 32px and 16px inspection", fill="white")
    path = ROOT / "build/phase-linker-preview.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    preview.save(path)
    print(f"Saved 32x32 Phase Linker texture; preview: {path}")


if __name__ == "__main__":
    main()
