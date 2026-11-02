"""Small authoring helpers. Run with Python 3.10+ and Pillow; never imported by the mod."""
import json
from pathlib import Path

from PIL import Image, ImageColor, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/subspace"
DATA = ROOT / "src/main/resources/data"
PALETTE_FILE = Path(__file__).with_name("palette.json")
PALETTE = json.loads(PALETTE_FILE.read_text(encoding="utf-8"))
SIZE = 32


def color(name):
    return ImageColor.getrgb(PALETTE[name]) + (255,)


def canvas(background="void"):
    return Image.new("RGBA", (SIZE, SIZE), color(background) if background else (0, 0, 0, 0))


def rect(image, bounds, shade):
    """Inclusive integer pixel bounds, with no smoothing or implicit scaling."""
    ImageDraw.Draw(image).rectangle(bounds, fill=color(shade))


def line(image, points, shade, width=1):
    ImageDraw.Draw(image).line(points, fill=color(shade), width=width)


def polygon(image, points, shade):
    ImageDraw.Draw(image).polygon(points, fill=color(shade))


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def save_texture(image, relative):
    if image.size != (SIZE, SIZE):
        raise ValueError(f"Static texture must be {SIZE}x{SIZE}: {relative}")
    path = ASSETS / "textures" / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def save_animation(frames, relative, ticks=8, sequence=None):
    if not frames or ticks <= 0 or any(frame.size != (SIZE, SIZE) for frame in frames):
        raise ValueError("Animation requires 32x32 frames and positive timing")
    sequence = list(range(len(frames))) if sequence is None else sequence
    if any(not isinstance(i, int) or not 0 <= i < len(frames) for i in sequence):
        raise ValueError("Animation frame index out of range")
    sheet = Image.new("RGBA", (SIZE, SIZE * len(frames)))
    for index, frame in enumerate(frames):
        sheet.paste(frame, (0, index * SIZE))
    path = ASSETS / "textures" / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(path)
    write_json(Path(str(path) + ".mcmeta"), {"animation": {
        "width": SIZE, "height": SIZE, "frametime": ticks,
        "frames": sequence, "interpolate": False,
    }})


def box(start, end, textures, *, omit=(), shade=True):
    """JSON element with projected 0..16 UVs; cull only full-boundary faces."""
    x0, y0, z0 = start
    x1, y1, z1 = end
    uv = {
        "north": [16-x1, 16-y1, 16-x0, 16-y0],
        "south": [x0, 16-y1, x1, 16-y0],
        "west": [z0, 16-y1, z1, 16-y0],
        "east": [16-z1, 16-y1, 16-z0, 16-y0],
        "up": [x0, z0, x1, z1],
        "down": [x0, 16-z1, x1, 16-z0],
    }
    boundary = {"north": z0 == 0, "south": z1 == 16, "west": x0 == 0,
                "east": x1 == 16, "down": y0 == 0, "up": y1 == 16}
    faces = {}
    for side in uv:
        if side in omit:
            continue
        face = {"uv": uv[side], "texture": "#" + textures.get(side, textures["all"])}
        if boundary[side]:
            face["cullface"] = side
        faces[side] = face
    return {"from": start, "to": end, "shade": shade, "faces": faces}


def export_editor_palette():
    rows = ["GIMP Palette", "Name: Subspace Engineering", "Columns: 7", "# palette.json is authoritative"]
    for name, value in PALETTE.items():
        r, g, b = ImageColor.getrgb(value)
        rows.append(f"{r:3} {g:3} {b:3}\t{name}")
    PALETTE_FILE.with_suffix(".gpl").write_text("\n".join(rows) + "\n", encoding="utf-8")
