"""Convert saved detector art to native pixels and build its layered JSON instrument model."""
from copy import deepcopy

from PIL import Image, ImageColor, ImageDraw

from common import ASSETS, DATA, PALETTE, ROOT, box, save_texture, write_json

NAME = "subspace_detector"
COLORS = [ImageColor.getrgb(value) for value in PALETTE.values()]


def texture(surface):
    source = Image.open(ROOT / f"docs/art/detector/{surface}_source.png").convert("RGB")
    source = source.resize((32, 32), Image.Resampling.NEAREST)
    image = Image.new("RGBA", (32, 32))
    image.putdata([min(COLORS, key=lambda c: sum((a-b)**2 for a, b in zip(rgb, c))) + (255,)
                   for rgb in source.get_flattened_data()])
    save_texture(image, f"block/{NAME}_{surface}.png")
    return image


def geometry():
    elements = []

    def part(name, start, end, *, back=False):
        element = box(start, end, {"all": "side", "north": "front", "up": "top", "down": "top"},
                      omit=() if back else ("south",))
        element["name"] = name
        elements.append(element)

    part("instrument_body", [0, 0, 3], [16, 16, 16], back=True)
    for name, start, end in [
        ("left_rail", [0, 0, 0.5], [3, 16, 3]),
        ("right_rail", [13, 0, 0.5], [16, 16, 3]),
        ("lower_rail", [3, 0, 0.5], [13, 1.5, 3]),
        ("upper_rail", [3, 14.5, 0.5], [13, 16, 3]),
        ("sensor_socket", [6, 4, 1.5], [10, 11.5, 3]),
        ("crystal", [7, 5, 0.75], [9, 10.5, 1.5]),
        ("left_bezel", [3, 5.5, 1.5], [5.5, 10.5, 3]),
        ("right_bezel", [10.5, 5.5, 1.5], [13, 10.5, 3]),
        ("left_screen", [3.5, 6, 1.25], [5, 10, 1.5]),
        ("right_screen", [11, 6, 1.25], [12.5, 10, 1.5]),
        ("status_bezel", [4, 1.5, 1.5], [12, 4, 3]),
        ("status_screen", [4.5, 2.5, 1.25], [11.5, 3.5, 1.5]),
        ("header_plate", [3.5, 12.5, 2], [12.5, 14.5, 3]),
    ]:
        part(name, start, end)
    for x in (1, 13.5):
        for y in (1, 13.5):
            part(f"copper_fastener_{x}_{y}", [x, y, 0], [x+1.5, y+1.5, 0.5])
    return elements


def models():
    elements = geometry()
    textures = {"side": f"subspace:block/{NAME}_side", "top": f"subspace:block/{NAME}_top",
                "front": f"subspace:block/{NAME}_front_inactive", "particle": "#side"}
    write_json(ASSETS / f"models/block/{NAME}.json", {
        "parent": "minecraft:block/block", "textures": textures, "elements": elements,
    })
    active = deepcopy(elements)
    for element in active:
        if element["name"] in ("crystal", "left_screen", "right_screen", "status_screen"):
            element["shade"] = False
            element["faces"]["north"].update({"emissivity": 15, "ambientocclusion": False})
    write_json(ASSETS / f"models/block/{NAME}_active.json", {
        "parent": f"subspace:block/{NAME}",
        "textures": {"front": f"subspace:block/{NAME}_front_active"}, "elements": active,
    })
    variants = {}
    for facing, angle in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for powered in (False, True):
            for scanning in (False, True):
                state = f"facing={facing},powered={str(powered).lower()},scanning={str(scanning).lower()}"
                suffix = "_active" if powered or scanning else ""
                variants[state] = {"model": f"subspace:block/{NAME}{suffix}", "y": angle}
    write_json(ASSETS / f"blockstates/{NAME}.json", {"variants": variants})
    write_json(ASSETS / f"models/item/{NAME}.json", {"parent": f"subspace:block/{NAME}"})
    write_json(DATA / f"subspace/loot_tables/blocks/{NAME}.json", {
        "type": "minecraft:block", "pools": [{"rolls": 1,
            "entries": [{"type": "minecraft:item", "name": f"subspace:{NAME}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}],
    })


def main():
    surfaces = ["front_inactive", "front_active", "side", "top"]
    images = [texture(surface) for surface in surfaces]
    models()
    preview = Image.new("RGBA", (800, 240), (28, 32, 40, 255))
    draw = ImageDraw.Draw(preview)
    for index, (surface, image) in enumerate(zip(surfaces, images)):
        x = index * 200 + 20
        draw.text((x, 8), surface, fill="white")
        preview.alpha_composite(image.resize((160, 160), Image.Resampling.NEAREST), (x, 28))
        preview.alpha_composite(image, (x+64, 200))
    output = ROOT / "build/detector-texture-preview.png"
    output.parent.mkdir(parents=True, exist_ok=True)
    preview.save(output)
    print(f"Generated four 32x32 textures, two 18-element models, 16 states, item model and loot. Preview: {output}")


if __name__ == "__main__":
    main()
