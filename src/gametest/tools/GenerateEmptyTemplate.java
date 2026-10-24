import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

/**
 * Regenerate from the repository root with Java 17:
 * java src/gametest/tools/GenerateEmptyTemplate.java
 *
 * Writes a Minecraft 1.19.2 structure: size [5,5,5], one air palette entry,
 * 125 air blocks, and no entities. NBT is big-endian inside a gzip stream.
 * This standalone tool is outside the gametest Java source directory.
 */
public final class GenerateEmptyTemplate {
    private static final int SIZE = 5;
    private static final int DATA_VERSION = 3120;

    public static void main(String[] args) throws IOException {
        Path output = Path.of("src/gametest/resources/data/convergence/structures/empty.nbt");
        Files.createDirectories(output.getParent());
        try (DataOutputStream nbt = new DataOutputStream(
                new GZIPOutputStream(Files.newOutputStream(output)))) {
            namedTag(nbt, 10, "");
            namedTag(nbt, 3, "DataVersion");
            nbt.writeInt(DATA_VERSION);
            intList(nbt, "size", SIZE, SIZE, SIZE);
            list(nbt, "palette", 10, 1);
            namedTag(nbt, 8, "Name");
            nbt.writeUTF("minecraft:air");
            nbt.writeByte(0);
            list(nbt, "blocks", 10, SIZE * SIZE * SIZE);
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    for (int z = 0; z < SIZE; z++) {
                        intList(nbt, "pos", x, y, z);
                        namedTag(nbt, 3, "state");
                        nbt.writeInt(0);
                        nbt.writeByte(0);
                    }
                }
            }
            list(nbt, "entities", 10, 0);
            nbt.writeByte(0);
        }
    }

    private static void namedTag(DataOutputStream nbt, int type, String name) throws IOException {
        nbt.writeByte(type);
        nbt.writeUTF(name);
    }

    private static void list(DataOutputStream nbt, String name, int type, int count) throws IOException {
        namedTag(nbt, 9, name);
        nbt.writeByte(type);
        nbt.writeInt(count);
    }

    private static void intList(DataOutputStream nbt, String name, int... values) throws IOException {
        list(nbt, name, 3, values.length);
        for (int value : values) {
            nbt.writeInt(value);
        }
    }
}
