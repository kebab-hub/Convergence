"""Build the Subspace Interface's finished textures/models from its saved original material art."""
from copy import deepcopy
from PIL import Image, ImageColor, ImageDraw
from common import ASSETS, DATA, PALETTE, ROOT, box, color, save_texture, write_json
from preview_anchor import render

NAME = "subspace_interface"
COLORS = [ImageColor.getrgb(value) for value in PALETTE.values()]


def textures():
    source = Image.open(ROOT / "docs/art/interface/materials_source.png").convert("RGB")
    w, h = source.size
    # The saved source's horizontal tile seam is at 48%, not exactly halfway down the canvas.
    split_y = round(h * 0.48)
    images = {}
    for name, bounds in {"frame": (0,0,w//2,split_y), "coupler": (w//2,0,w,split_y),
                         "core_connected": (0,split_y,w//2,h), "display_connected": (w//2,split_y,w,h)}.items():
        tile = source.crop(bounds).resize((32,32), Image.Resampling.NEAREST)
        image = Image.new("RGBA", (32,32))
        image.putdata([min(COLORS, key=lambda c: sum((a-b)**2 for a,b in zip(rgb,c))) + (255,)
                       for rgb in tile.get_flattened_data()])
        images[name] = image
    dim = {color("violet"): color("violet_dark"), color("energy"): color("violet_dark"),
           color("magenta"): color("violet"), color("crystal"): color("violet_dark"),
           color("crystal_glint"): color("violet"), color("core_glint"): color("crystal"),
           color("cyan"): color("cyan_shadow"), color("cyan_glint"): color("cyan_shadow")}
    for name in ("core", "display"):
        image = Image.new("RGBA", (32,32))
        image.putdata([dim.get(p,p) for p in images[name+"_connected"].get_flattened_data()])
        images[name+"_disconnected"] = image
    for name, image in images.items():
        save_texture(image, f"block/{NAME}_{name}.png")
    return images


def geometry():
    elements = []
    def part(name, start, end, texture="frame", full_uv=False):
        element = box(start,end,{"all":texture})
        element["name"] = name
        if full_uv:
            for face in element["faces"].values():
                face["uv"] = [0,0,16,16]
        elements.append(element)
    part("body",[0,0,3],[16,16,16])
    part("left_rail",[0,0,0],[2,16,3])
    part("right_rail",[14,0,0],[16,16,3])
    part("upper_rail",[2,14,0],[14,16,3])
    part("lower_rail",[2,0,0],[14,2,3])
    part("recessed_socket",[3,3,2],[13,13,3],"coupler",True)
    part("upper_collar",[4,11,1],[12,13,2],"coupler")
    part("lower_collar",[4,3,1],[12,5,2],"coupler")
    part("left_collar",[3,5,1],[5,11,2],"coupler")
    part("right_collar",[11,5,1],[13,11,2],"coupler")
    part("core",[6,6,1.5],[10,10,2],"core",True)
    part("left_status",[2,5,0.75],[3,11,1.5],"display",True)
    part("right_status",[13,5,0.75],[14,11,1.5],"display",True)
    return elements


def models():
    for state in ("connected","disconnected"):
        elements = deepcopy(geometry())
        if state == "connected":
            for element in elements:
                if element["name"] in ("core","left_status","right_status"):
                    element["shade"] = False
                    element["faces"]["north"].update({"emissivity":15,"ambientocclusion":False})
        write_json(ASSETS / f"models/block/{NAME}_{state}.json", {
            "parent":"minecraft:block/block", "textures": {
                "frame":f"subspace:block/{NAME}_frame", "coupler":f"subspace:block/{NAME}_coupler",
                "core":f"subspace:block/{NAME}_core_{state}", "display":f"subspace:block/{NAME}_display_{state}",
                "particle":f"subspace:block/{NAME}_frame"}, "elements":elements})
    variants = {}
    for facing, angle in (("north",0),("east",90),("south",180),("west",270)):
        for connected in (False,True):
            state = "connected" if connected else "disconnected"
            variants[f"connected={str(connected).lower()},facing={facing}"] = {"model":f"subspace:block/{NAME}_{state}","y":angle}
    write_json(ASSETS / f"blockstates/{NAME}.json", {"variants":variants})
    write_json(ASSETS / f"models/item/{NAME}.json", {"parent":f"subspace:block/{NAME}_disconnected"})
    write_json(DATA / f"subspace/loot_tables/blocks/{NAME}.json", {"type":"minecraft:block","pools":[{
        "rolls":1,"entries":[{"type":"minecraft:item","name":f"subspace:{NAME}"}],
        "conditions":[{"condition":"minecraft:survives_explosion"}]}]})


def main():
    images = textures()
    models()
    preview = Image.new("RGBA", (960,650), (28,32,40,255))
    draw = ImageDraw.Draw(preview)
    for index, (name, image) in enumerate(images.items()):
        x = 10+index*158
        draw.text((x,8),name,fill="white")
        preview.alpha_composite(image.resize((128,128),Image.Resampling.NEAREST),(x,28))
        preview.alpha_composite(image,(x+48,165))
    preview.alpha_composite(render("connected", NAME),(40,220))
    preview.alpha_composite(render("disconnected", NAME),(500,220))
    output = ROOT / "build/interface-preview.png"
    output.parent.mkdir(parents=True,exist_ok=True)
    preview.save(output)
    print(f"Saved six 32px textures, two 13-element models, eight states, item model and loot. Preview: {output}")


if __name__ == "__main__":
    main()
