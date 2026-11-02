"""Convert original material art into 32px textures and a hollow anchor model; draw its native GUI."""
from copy import deepcopy
from PIL import Image, ImageColor, ImageDraw
from common import ASSETS, DATA, PALETTE, ROOT, box, color, save_texture, write_json

NAME = "subspace_anchor"
COLORS = [ImageColor.getrgb(value) for value in PALETTE.values()]


def textures():
    source = Image.open(ROOT / "docs/art/anchor/materials_source.png").convert("RGB")
    w, h = source.size
    images = {}
    for name, bounds in {"frame": (0, 0, w//2, h//2), "copper": (w//2, 0, w, h//2),
                         "core_active": (0, h//2, w//2, h), "lights_active": (w//2, h//2, w, h)}.items():
        image = source.crop(bounds).resize((32, 32), Image.Resampling.NEAREST)
        result = Image.new("RGBA", (32, 32))
        result.putdata([min(COLORS, key=lambda c: sum((a-b)**2 for a, b in zip(rgb, c))) + (255,)
                        for rgb in image.get_flattened_data()])
        images[name] = result
    # State conversion retains the generated facets/circuits but removes their luminous highlights.
    dim = {color("violet"): color("violet_dark"), color("energy"): color("violet_dark"),
           color("magenta"): color("violet"), color("crystal"): color("violet_dark"),
           color("crystal_glint"): color("violet"), color("core_glint"): color("crystal"),
           color("cyan"): color("cyan_shadow"), color("cyan_glint"): color("cyan_shadow")}
    for name in ("core", "lights"):
        inactive = Image.new("RGBA", (32, 32))
        inactive.putdata([dim.get(pixel, pixel) for pixel in images[name + "_active"].get_flattened_data()])
        images[name + "_inactive"] = inactive
    for name, image in images.items():
        save_texture(image, f"block/{NAME}_{name}.png")
    return images


def geometry():
    elements = []

    def part(name, start, end, texture, full_uv=False):
        element = box(start, end, {"all": texture})
        element["name"] = name
        if full_uv:
            for face in element["faces"].values():
                face["uv"] = [0, 0, 16, 16]
        elements.append(element)
        return element

    part("heavy_base", [0, 0, 0], [16, 3, 16], "frame")
    for name, start, end in [
        ("north_upper_beam", [1, 14, 1], [15, 16, 3]),
        ("south_upper_beam", [1, 14, 13], [15, 16, 15]),
        ("west_upper_beam", [1, 14, 3], [3, 16, 13]),
        ("east_upper_beam", [13, 14, 3], [15, 16, 13]),
    ]:
        part(name, start, end, "frame")
    for x in (1, 13):
        for z in (1, 13):
            part(f"pillar_{x}_{z}", [x, 3, z], [x+2, 14, z+2], "frame")
            # Collars occupy a distinct wider shell, rather than coincident pillar faces.
            for y in (3, 12.5):
                part(f"clamp_{x}_{y}_{z}", [x-0.25, y, z-0.25], [x+2.25, y+1, z+2.25], "copper", True)
    part("lower_emitter", [5, 3, 5], [11, 4, 11], "copper", True)
    for name, start, end in [
        ("west_stabilizer", [1, 3, 5], [3, 4, 11]),
        ("east_stabilizer", [13, 3, 5], [15, 4, 11]),
        ("north_stabilizer", [5, 3, 1], [11, 4, 3]),
        ("south_stabilizer", [5, 3, 13], [11, 4, 15]),
    ]:
        part(name, start, end, "lights", True)
    core = part("floating_core", [5.5, 6, 5.5], [10.5, 11, 10.5], "core", True)
    core["rotation"] = {"origin": [8, 8.5, 8], "axis": "z", "angle": 45, "rescale": False}
    return elements


def models():
    elements = geometry()
    for active in (False, True):
        suffix = "active" if active else "inactive"
        parts = deepcopy(elements)
        if active:
            for part in parts:
                if part["name"] == "floating_core" or part["name"].endswith("stabilizer"):
                    part["shade"] = False
                    for face in part["faces"].values():
                        face.update({"emissivity": 15, "ambientocclusion": False})
        write_json(ASSETS / f"models/block/{NAME}_{suffix}.json", {
            "parent": "minecraft:block/block", "textures": {
                "frame": f"subspace:block/{NAME}_frame", "copper": f"subspace:block/{NAME}_copper",
                "core": f"subspace:block/{NAME}_core_{suffix}", "lights": f"subspace:block/{NAME}_lights_{suffix}",
                "particle": f"subspace:block/{NAME}_frame"}, "elements": parts,
        })
    write_json(ASSETS / f"blockstates/{NAME}.json", {"variants": {
        "active=false": {"model": f"subspace:block/{NAME}_inactive"},
        "active=true": {"model": f"subspace:block/{NAME}_active"},
    }})
    write_json(ASSETS / f"models/item/{NAME}.json", {"parent": f"subspace:block/{NAME}_active"})
    write_json(DATA / f"subspace/loot_tables/blocks/{NAME}.json", {"type": "minecraft:block", "pools": [{
        "rolls": 1, "entries": [{"type": "minecraft:item", "name": f"subspace:{NAME}"}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}]})


def gui(images):
    # Native UI geometry extends the existing palette, with text rendered by Minecraft for localization.
    image = Image.new("RGBA", (256, 256))
    draw = ImageDraw.Draw(image)
    def rectangle(bounds, shade):
        draw.rectangle(bounds, fill=color(shade))
    rectangle((0, 0, 235, 165), "void")
    rectangle((1, 1, 234, 164), "edge")
    rectangle((3, 3, 232, 162), "metal")
    rectangle((5, 5, 230, 160), "recess")
    rectangle((11, 10, 224, 26), "void")
    rectangle((12, 28, 223, 29), "copper")
    rectangle((12, 48, 223, 77), "edge")
    rectangle((13, 49, 222, 76), "void")
    rectangle((12, 81, 223, 82), "cyan_shadow")
    rectangle((12, 120, 223, 121), "copper_shadow")
    for x in (6, 225):
        for y in (6, 154):
            rectangle((x, y, x+4, y+4), "copper_shadow")
            rectangle((x, y, x+3, y+2), "brass")
            rectangle((x+1, y+1, x+2, y+1), "void")
    image.alpha_composite(images["core_active"].resize((12, 12), Image.Resampling.NEAREST), (207, 12))
    path = ASSETS / "textures/gui/anchor.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)
    return image


def main():
    images = textures()
    models()
    panel = gui(images)
    preview = Image.new("RGBA", (960, 430), (28, 32, 40, 255))
    draw = ImageDraw.Draw(preview)
    for index, (name, image) in enumerate(images.items()):
        x = 10 + index * 158
        draw.text((x, 10), name, fill="white")
        preview.alpha_composite(image.resize((128, 128), Image.Resampling.NEAREST), (x, 30))
        preview.alpha_composite(image, (x+48, 170))
    preview.alpha_composite(panel.crop((0, 0, 236, 166)), (20, 235))
    draw.text((280, 245), "GUI background: text and control rendered by Minecraft", fill="white")
    path = ROOT / "build/anchor-texture-preview.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    preview.save(path)
    print(f"Saved six 32px textures, GUI, two {len(geometry())}-element models, two states, item model, loot: {path}")


if __name__ == "__main__":
    main()
