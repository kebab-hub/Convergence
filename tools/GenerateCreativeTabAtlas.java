import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Run from the repository root with Java 17:
 * java tools/GenerateCreativeTabAtlas.java
 *
 * Forge 1.19.2 samples the native creative-tab atlas in 256x256 logical units.
 * A 512x512 atlas preserves the emblem's 32x32 pixels in a 16x16 logical icon.
 * This tool is not included in the mod jar and does not launch Minecraft.
 */
public final class GenerateCreativeTabAtlas {
    private static final Path TEXTURES = Path.of("src/main/resources/assets/subspace/textures/gui");
    private static final int OUTLINE = 0x101319;
    private static final int PANEL = 0x2C3440;
    private static final int EDGE = 0x465263;
    private static final int HIGHLIGHT = 0x657184;
    private static final int RECESS = 0x1B2028;
    private static final int COPPER = 0xB8753E;
    private static final int BRASS = 0xD6AA59;
    private static final int CYAN = 0x20CEDB;
    private static final int DIM_CYAN = 0x087F98;
    private static final int VIOLET = 0xAE3FFF;

    public static void main(String[] args) throws IOException {
        BufferedImage emblem = ImageIO.read(TEXTURES.resolve("subspace_emblem.png").toFile());
        if (emblem == null || emblem.getWidth() != 32 || emblem.getHeight() != 32) {
            throw new IOException("The creative-tab emblem must be a 32x32 PNG");
        }

        BufferedImage atlas = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = atlas.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            graphics.scale(2, 2);
            // Include all columns: placement changes when other mods add tabs.
            for (int row = 0; row < 4; row++) {
                for (int column = 0; column < 6; column++) {
                    drawTab(graphics, emblem, column * 28, row * 32, row >= 2, row % 2 == 1);
                }
            }
            drawScrollThumb(graphics, 232, true);
            drawScrollThumb(graphics, 244, false);
        } finally {
            graphics.dispose();
        }

        Files.createDirectories(TEXTURES);
        Path output = TEXTURES.resolve("creative_tabs.png");
        if (!ImageIO.write(atlas, "PNG", output.toFile())) {
            throw new IOException("PNG encoder unavailable");
        }
        System.out.println("Wrote " + output + " (512x512, 2x native GUI density)");
    }

    private static void drawTab(Graphics2D graphics, BufferedImage emblem,
                                int x, int y, boolean bottom, boolean selected) {
        Graphics2D tab = (Graphics2D) graphics.create();
        try {
            tab.translate(x, y);
            tab.setColor(new Color(OUTLINE));
            // Chamfer the outside corners, keeping the panel-facing edge square.
            if (bottom) {
                tab.fillPolygon(new int[]{0, 28, 28, 26, 2, 0},
                        new int[]{0, 0, 30, 32, 32, 30}, 6);
            } else {
                tab.fillPolygon(new int[]{2, 26, 28, 28, 0, 0},
                        new int[]{0, 0, 2, 32, 32, 2}, 6);
            }
            rectangle(tab, 2, 2, 24, 28, PANEL);
            rectangle(tab, 1, 3, 1, 27, EDGE);
            rectangle(tab, 3, 1, 22, 1, HIGHLIGHT);
            rectangle(tab, 26, 3, 1, 27, RECESS);
            rectangle(tab, 3, 30, 22, 1, RECESS);

            int iconY = bottom ? 7 : 9;
            rectangle(tab, 5, iconY - 1, 18, 18, OUTLINE);
            rectangle(tab, 6, iconY, 16, 16, RECESS);
            tab.drawImage(emblem, 6, iconY, 16, 16, null);

            int outsideY = bottom ? 27 : 4;
            rectangle(tab, 3, outsideY, 3, 2, COPPER);
            rectangle(tab, 22, outsideY, 3, 2, COPPER);
            rectangle(tab, 3, outsideY, 2, 1, BRASS);
            rectangle(tab, 22, outsideY, 2, 1, BRASS);
            rectangle(tab, 9, outsideY, 10, 2, OUTLINE);
            rectangle(tab, 10, outsideY, 8, 1, selected ? CYAN : DIM_CYAN);

            int insideY = bottom ? 2 : 29;
            rectangle(tab, 7, insideY, 14, 1, selected ? VIOLET : EDGE);
            rectangle(tab, 3, insideY, 2, 1, selected ? BRASS : COPPER);
            rectangle(tab, 23, insideY, 2, 1, selected ? BRASS : COPPER);
        } finally {
            tab.dispose();
        }
    }

    private static void drawScrollThumb(Graphics2D graphics, int x, boolean enabled) {
        rectangle(graphics, x, 0, 12, 15, OUTLINE);
        rectangle(graphics, x + 1, 1, 10, 13, enabled ? PANEL : RECESS);
        rectangle(graphics, x + 1, 1, 9, 1, enabled ? HIGHLIGHT : EDGE);
        rectangle(graphics, x + 1, 2, 1, 11, EDGE);
        for (int y : new int[]{5, 7, 9}) {
            rectangle(graphics, x + 3, y, 6, 1, enabled ? COPPER : EDGE);
        }
        rectangle(graphics, x + 4, 12, 4, 1, enabled ? CYAN : DIM_CYAN);
    }

    private static void rectangle(Graphics2D graphics, int x, int y, int width, int height, int rgb) {
        graphics.setColor(new Color(rgb));
        graphics.fillRect(x, y, width, height);
    }
}
