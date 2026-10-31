"""Reproduce the casing assets from the checked-in palette; no Minecraft launch.

Run: python tools/assets/generate_test_casing.py
Reference: docs/art/subspace_test_casing_reference.png (visual reference only).
"""
from PIL import Image, ImageDraw

from common import (ROOT, ASSETS, DATA, SIZE, canvas, color, rect, line, polygon,
                    save_texture, save_animation, write_json, box, export_editor_palette)

NAME = "subspace_test_casing"


def screw(image, x, y):
    rect(image, (x-1, y-1, x+2, y+2), "void")
    rect(image, (x, y, x+1, y+1), "copper")
    rect(image, (x, y, x+1, y), "brass")
    rect(image, (x+1, y+1, x+1, y+1), "copper_shadow")


def panel():
    image = canvas()
    rect(image, (1, 1, 30, 30), "metal")
    line(image, [(1, 30), (1, 1), (30, 1)], "edge")
    line(image, [(2, 28), (2, 2), (28, 2)], "highlight")
    line(image, [(30, 2), (30, 30), (2, 30)], "recess")
    rect(image, (4, 4, 27, 27), "void")
    rect(image, (5, 5, 26, 26), "recess")
    for x, y in [(2, 2), (28, 2), (2, 28), (28, 28)]:
        screw(image, x, y)
    return image


def side_panel():
    image = panel()
    # Copper rails surround a recessed service panel, matching the reference.
    for y in (6, 24):
        rect(image, (6, y, 25, y+1), "copper_shadow")
        line(image, [(7, y), (24, y)], "copper")
        for x in (7, 23):
            rect(image, (x, y-1, x+1, y+2), "brass")
            rect(image, (x, y-1, x+1, y-1), "brass_glint")
    rect(image, (8, 10, 23, 22), "metal")
    line(image, [(8, 22), (8, 10), (23, 10)], "edge")
    line(image, [(24, 10), (24, 23), (8, 23)], "void")
    for y in (12, 15, 18):
        rect(image, (10, y, 16, y+1), "void")
        line(image, [(10, y+2), (16, y+2)], "edge")
    rect(image, (19, 12, 22, 20), "void")
    for y in (13, 16, 19):
        rect(image, (20, y, 21, y), "cyan")
        rect(image, (20, y, 20, y), "cyan_glint")
    return image


def top_panel():
    image = panel()
    for start, end in [((6, 6), (25, 6)), ((6, 25), (25, 25)), ((6, 6), (6, 25)), ((25, 6), (25, 25))]:
        line(image, [start, end], "copper")
    rect(image, (9, 9, 22, 22), "metal")
    line(image, [(9, 22), (9, 9), (22, 9)], "highlight")
    line(image, [(23, 9), (23, 23), (9, 23)], "void")
    line(image, [(11, 11), (20, 11)], "edge")
    line(image, [(11, 21), (20, 21)], "recess")
    for x, y in [(7, 7), (23, 7), (7, 23), (23, 23)]:
        rect(image, (x-1, y-1, x+1, y+1), "brass")
        rect(image, (x-1, y-1, x, y-1), "brass_glint")
    for x, y in [(11, 12), (19, 19)]:
        rect(image, (x, y, x+1, y), "cyan")
    return image


def front_panel():
    image = panel()
    polygon(image, [(10, 7), (21, 7), (26, 12), (26, 20), (21, 25), (10, 25), (5, 20), (5, 12)], "copper_shadow")
    polygon(image, [(10, 8), (21, 8), (25, 12), (25, 20), (21, 24), (10, 24), (6, 20), (6, 12)], "copper")
    polygon(image, [(11, 9), (20, 9), (24, 13), (24, 19), (20, 23), (11, 23), (7, 19), (7, 13)], "void")
    rect(image, (9, 9, 22, 22), "violet_shadow")
    line(image, [(9, 21), (9, 10), (21, 10)], "violet_dark")
    for x, y in [(7, 8), (23, 8), (7, 23), (23, 23)]:
        rect(image, (x, y, x+1, y+1), "cyan_shadow")
        rect(image, (x, y, x, y), "cyan_glint")
    return image


def frame_panel():
    image = canvas("metal")
    for x in (0, 26):
        rect(image, (x, 0, x+5, 31), "recess")
        line(image, [(x+1, 1), (x+1, 30)], "edge")
        line(image, [(x+2, 6), (x+2, 25)], "highlight")
        line(image, [(x+3, 7), (x+3, 24)], "copper_shadow")
    for y in (0, 26):
        rect(image, (6, y, 25, y+5), "metal")
        line(image, [(6, y+1), (25, y+1)], "highlight")
        line(image, [(7, y+3), (24, y+3)], "edge")
        line(image, [(7, y+5), (24, y+5)], "void")
    # Four raised copper retainers are projected into the model's clamp faces.
    for bounds in [(13, 4, 18, 9), (13, 22, 18, 27), (4, 13, 9, 18), (22, 13, 27, 18)]:
        x0, y0, x1, y1 = bounds
        rect(image, bounds, "copper_shadow")
        rect(image, (x0+1, y0, x1-1, y1-1), "copper")
        line(image, [(x0+1, y0), (x1-1, y0)], "brass_glint")
        line(image, [(x0+1, y0+1), (x0+1, y1-1)], "brass")
        rect(image, (x1-1, y1-1, x1, y1), "void")
    for x, y in [(2, 2), (28, 2), (2, 28), (28, 28)]:
        screw(image, x, y)
    return image


def crystal_frame(phase):
    image = canvas("violet_shadow")
    polygon(image, [(15, 1), (27, 10), (30, 17), (23, 27), (15, 30), (4, 23), (1, 15), (7, 6)], "void")
    polygon(image, [(15, 3), (25, 10), (28, 17), (22, 25), (15, 28), (6, 22), (3, 15), (8, 7)], "violet_dark")
    polygon(image, [(15, 4), (24, 11), (26, 17), (21, 24), (15, 27), (7, 21), (5, 15), (9, 8)], "violet")
    polygon(image, [(15, 4), (15, 14), (5, 15), (9, 8)], "crystal")
    polygon(image, [(15, 4), (24, 11), (18, 14), (15, 14)], "energy")
    polygon(image, [(5, 15), (15, 14), (12, 22), (7, 21)], "magenta")
    polygon(image, [(18, 14), (24, 11), (26, 17), (21, 24)], "violet_dark")
    polygon(image, [(12, 22), (18, 14), (21, 24), (15, 27)], "energy")
    pulse = ["energy", "crystal", "magenta", "crystal_glint"][phase]
    polygon(image, [(15, 9), (20, 15), (16, 22), (10, 16)], pulse)
    line(image, [(15, 6), (15, 10), (11, 14)], "crystal_glint")
    line(image, [(9, 17), (12, 20)], "crystal")
    if phase >= 1:
        rect(image, (14, 13, 16, 15), "crystal_glint")
    if phase >= 2:
        line(image, [(15, 11), (15, 17)], "core_glint")
        line(image, [(13, 14), (17, 14)], "core_glint")
    return image


def spark_texture():
    image = canvas(None)
    polygon(image, [(15, 3), (19, 12), (28, 16), (19, 19), (15, 28), (12, 19), (3, 16), (12, 12)], "violet_dark")
    polygon(image, [(15, 6), (18, 13), (25, 16), (18, 18), (15, 25), (13, 18), (6, 16), (13, 13)], "energy")
    polygon(image, [(15, 10), (20, 16), (15, 21), (10, 16)], "magenta")
    polygon(image, [(15, 12), (18, 16), (15, 19), (12, 16)], "crystal_glint")
    line(image, [(15, 13), (15, 17)], "core_glint")
    return image


def models_and_data():
    elements = [box([0, 0, 2], [16, 16, 16], {"all": "side", "up": "top", "down": "top", "north": "front"})]
    # Four frame rails and four clamps. No hidden back faces against the body.
    for start, end in [([0, 0, 0], [3, 16, 2]), ([13, 0, 0], [16, 16, 2]),
                       ([3, 0, 0], [13, 3, 2]), ([3, 13, 0], [13, 16, 2]),
                       ([6.5, 11, 0.5], [9.5, 14, 2]), ([6.5, 2, 0.5], [9.5, 5, 2]),
                       ([2, 6.5, 0.5], [5, 9.5, 2]), ([11, 6.5, 0.5], [14, 9.5, 2])]:
        elements.append(box(start, end, {"all": "side", "north": "frame", "up": "top", "down": "top"}, omit=("south",)))
    core = box([4.5, 4.5, 1.5], [11.5, 11.5, 2], {"all": "front", "north": "core"}, omit=("south",), shade=False)
    core["faces"]["north"].update({"uv": [0, 0, 16, 16], "emissivity": 15, "ambientocclusion": False})
    elements.append(core)
    write_json(ASSETS / "models/block/template/recessed_casing.json", {
        "parent": "minecraft:block/block", "textures": {"particle": "#side"}, "elements": elements,
    })
    write_json(ASSETS / f"models/block/{NAME}.json", {
        "parent": "subspace:block/template/recessed_casing",
        "textures": {slot: f"subspace:block/{NAME}_{slot}" for slot in ("side", "top", "front", "frame", "core")},
    })
    write_json(ASSETS / f"models/item/{NAME}.json", {"parent": f"subspace:block/{NAME}"})
    write_json(ASSETS / f"blockstates/{NAME}.json", {"variants": {
        f"facing={facing}": {"model": f"subspace:block/{NAME}", "y": rotation}
        for facing, rotation in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]
    }})
    write_json(ASSETS / "particles/subspace_spark.json", {"textures": ["subspace:subspace_spark"]})
    write_json(DATA / f"subspace/loot_tables/blocks/{NAME}.json", {
        "type": "minecraft:block", "pools": [{"rolls": 1,
            "entries": [{"type": "minecraft:item", "name": f"subspace:{NAME}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}],
    })
    write_json(DATA / "minecraft/tags/blocks/mineable/pickaxe.json", {"replace": False, "values": [f"subspace:{NAME}"]})


def main():
    export_editor_palette()
    stills = {"side": side_panel(), "top": top_panel(), "front": front_panel(), "frame": frame_panel()}
    for suffix, image in stills.items():
        save_texture(image, f"block/{NAME}_{suffix}.png")
    frames = [crystal_frame(phase) for phase in range(4)]
    save_animation(frames, f"block/{NAME}_core.png", ticks=8, sequence=[0, 1, 2, 3, 2, 1])
    spark = spark_texture()
    save_texture(spark, "particle/subspace_spark.png")
    models_and_data()
    # Authoring contact sheet is a build artifact, not a packaged game resource.
    tiles = list(stills.items()) + [(f"core {i}", frame) for i, frame in enumerate(frames)] + [("spark", spark)]
    preview = Image.new("RGBA", (5*180, 2*205), color("recess"))
    draw = ImageDraw.Draw(preview)
    for i, (name, tile) in enumerate(tiles):
        x, y = (i % 5)*180+10, (i // 5)*205+25
        preview.alpha_composite(tile.resize((160, 160), Image.Resampling.NEAREST), (x, y))
        draw.text((x, y-16), name, fill=color("steel_glint"))
    output = ROOT / "build/asset-preview.png"
    output.parent.mkdir(parents=True, exist_ok=True)
    preview.save(output)
    print("Generated casing textures/models/data, particle sprite, editor palette, and " + str(output))


if __name__ == "__main__":
    main()
