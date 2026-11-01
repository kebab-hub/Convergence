"""Offline orthographic inspection of the actual JSON geometry/UVs; not a Minecraft render."""
import json
import math
from PIL import Image, ImageDraw
from common import ASSETS, ROOT


def render(active, model_name="subspace_anchor"):
    model = json.loads((ASSETS / f"models/block/{model_name}_{active}.json").read_text())
    textures = {key: Image.open(ASSETS / ("textures/" + path.split(":")[1] + ".png")).convert("RGBA")
                for key, path in model["textures"].items()}
    faces = []
    camera = (1, 1, -1)
    for element in model["elements"]:
        x0, y0, z0 = element["from"]
        x1, y1, z1 = element["to"]
        quads = {
            "north": ([(x1,y1,z0), (x0,y1,z0), (x0,y0,z0), (x1,y0,z0)], (0,0,-1)),
            "south": ([(x0,y1,z1), (x1,y1,z1), (x1,y0,z1), (x0,y0,z1)], (0,0,1)),
            "east": ([(x1,y1,z1), (x1,y1,z0), (x1,y0,z0), (x1,y0,z1)], (1,0,0)),
            "west": ([(x0,y1,z0), (x0,y1,z1), (x0,y0,z1), (x0,y0,z0)], (-1,0,0)),
            "up": ([(x0,y1,z0), (x1,y1,z0), (x1,y1,z1), (x0,y1,z1)], (0,1,0)),
            "down": ([(x0,y0,z1), (x1,y0,z1), (x1,y0,z0), (x0,y0,z0)], (0,-1,0)),
        }
        def rotate(point, normal=False):
            rotation = element.get("rotation")
            if not rotation:
                return point
            assert rotation["axis"] == "z" and not rotation["rescale"]
            ox, oy, oz = (0,0,0) if normal else rotation["origin"]
            x, y, z = (point[0]-ox, point[1]-oy, point[2]-oz)
            angle = math.radians(rotation["angle"])
            return (ox+x*math.cos(angle)-y*math.sin(angle), oy+x*math.sin(angle)+y*math.cos(angle), oz+z)
        for side, face in element["faces"].items():
            vertices, normal = quads[side]
            normal = rotate(normal, True)
            if sum(a*b for a, b in zip(normal, camera)) <= 0:
                continue
            vertices = [rotate(vertex) for vertex in vertices]
            depth = sum(sum(a*b for a, b in zip(vertex, camera)) for vertex in vertices)/4
            faces.append((depth, vertices, normal, face))
    image = Image.new("RGBA", (400, 400), (28, 32, 40, 255))
    draw = ImageDraw.Draw(image)
    def project(point):
        x, y, z = point
        return (200 + (x+z-16)*0.7071*14, 200 + ((x-z)*0.4082-(y-8)*0.8165)*14)
    for _, vertices, normal, face in sorted(faces, key=lambda value: value[0]):
        texture = textures[face["texture"][1:]]
        u0, v0, u1, v1 = face["uv"]
        steps_u = max(1, math.ceil(abs(u1-u0)*2))
        steps_v = max(1, math.ceil(abs(v1-v0)*2))
        shade = 1 if face.get("emissivity") else 0.65 + 0.25*max(0, normal[1]) + 0.1*max(0, -normal[2])
        def interpolate(u, v):
            return tuple(vertices[0][i] + u*(vertices[1][i]-vertices[0][i]) + v*(vertices[3][i]-vertices[0][i])
                         for i in range(3))
        for row in range(steps_v):
            for column in range(steps_u):
                u, v = column/steps_u, row/steps_v
                px = min(31, max(0, int((u0+(u+0.5/steps_u)*(u1-u0))*2)))
                py = min(31, max(0, int((v0+(v+0.5/steps_v)*(v1-v0))*2)))
                rgb = texture.getpixel((px, py))
                points = [project(interpolate(a,b)) for a,b in ((u,v),(u+1/steps_u,v),
                          (u+1/steps_u,v+1/steps_v),(u,v+1/steps_v))]
                draw.polygon(points, fill=tuple(int(c*shade) for c in rgb[:3])+(255,))
    draw.text((12, 10), active.title() + " | offline geometry preview", fill="white")
    return image


def main():
    preview = Image.new("RGBA", (800, 610), (28, 32, 40, 255))
    preview.alpha_composite(render("active"), (0,0))
    preview.alpha_composite(render("inactive"), (400,0))
    panel = Image.open(ASSETS / "textures/gui/anchor.png").crop((0,0,236,166)).convert("RGBA")
    draw = ImageDraw.Draw(panel)
    for text, x, y, fill in [("Subspace Anchor",16,13,"#FFE19B"), ("Network address",16,37,"#8D9CB5"),
            ("37c7e272-9409-487e",20,52,"#EACDFF"), ("-a8ca-732a110dd47c",20,63,"#EACDFF"),
            ("Status: Active / stable",16,87,"#A4F8FF"), ("Linked endpoints: 0",16,105,"#EACDFF")]:
        draw.text((x,y),text,fill=fill)
    draw.rectangle((48,128,187,147),fill="#465263",outline="#8D9CB5")
    draw.text((88,132),"Deactivate",fill="white")
    preview.alpha_composite(panel,(20,425))
    ImageDraw.Draw(preview).text((280,450), "GUI layout preview (approximate font)\nIn-game rendering remains an owner check.",fill="white")
    output = ROOT / "build/anchor-model-preview.png"
    output.parent.mkdir(parents=True, exist_ok=True)
    preview.save(output)
    print(output)


if __name__ == "__main__":
    main()
