package io.github.kebab_hub.subspace.common.link;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;

/** Plain Java entrypoint using NBT/value types only: no game bootstrap, world, client, or server. */
public final class LinkerSelectionChecks {
    public static void main(String[] args) throws Exception {
        Endpoint a = endpoint("minecraft:overworld", new BlockPos(-17, -64, 300), Direction.UP);
        Endpoint b = endpoint("minecraft:the_nether", new BlockPos(17, 127, -300), Direction.WEST);
        for (Direction face : Direction.values()) {
            Endpoint value = endpoint("example:custom/world", a.pos(), face);
            check(Endpoint.read(value.save()).orElseThrow().equals(value), "Face/dimension/position did not round-trip");
        }
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(1, 2, 3);
        Endpoint stable = endpoint("minecraft:overworld", mutable, Direction.NORTH);
        mutable.set(5, 6, 7);
        check(stable.pos().equals(new BlockPos(1, 2, 3)), "Endpoint retained a mutable position");

        check(LinkerSelection.read(null).selection().equals(LinkerSelection.EMPTY), "Missing NBT must be empty");
        LinkerSelection first = LinkerSelection.EMPTY.select(a);
        LinkerSelection pair = first.select(b);
        check(first.a().equals(a) && first.b() == null, "First click must select A only");
        check(pair.complete() && pair.a().equals(a) && pair.b().equals(b), "Second click must retain A and select B");
        LinkerSelection restart = pair.select(b);
        check(restart.a().equals(b) && restart.b() == null, "Third click must discard the old pair and select new A");
        check(first.select(endpoint(a.dimension().toString(), a.pos(), Direction.DOWN)).complete(),
                "Different faces on one block must be distinct endpoints");
        rejects(() -> first.select(a), "Duplicate A/B accepted");
        rejects(() -> new LinkerSelection(null, b), "Orphan B accepted");

        CompoundTag itemTag = new CompoundTag();
        itemTag.putString("unrelated", "preserve me");
        pair.writeTo(itemTag);
        CompoundTag savedCopy = itemTag.copy();
        check(LinkerSelection.read(itemTag).selection().equals(pair), "Pair did not round-trip");
        check(itemTag.equals(savedCopy), "Read mutated item NBT");
        check(itemTag.getCompound(LinkerSelection.TAG_KEY).getAllKeys().equals(Set.of("a", "b")), "Unexpected linker state");
        check(a.save().getAllKeys().equals(Set.of("dimension", "pos", "face")), "Endpoint stores more than an address");
        check(a.save().getCompound("pos").getAllKeys().equals(Set.of("x", "y", "z")), "Unexpected position fields");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(itemTag, bytes);
        CompoundTag reloaded = NbtIo.readCompressed(new ByteArrayInputStream(bytes.toByteArray()));
        check(LinkerSelection.read(reloaded).selection().equals(pair), "Disk NBT round-trip changed endpoints");
        LinkerSelection.clear(reloaded);
        check(!reloaded.contains(LinkerSelection.TAG_KEY) && "preserve me".equals(reloaded.getString("unrelated")),
                "Clear must remove only owned NBT");
        LinkerSelection.EMPTY.writeTo(itemTag);
        check(itemTag.equals(reloaded), "Writing empty state must clear only owned NBT");
        LinkerSelection.clear(null);

        for (String field : new String[] {"dimension", "pos", "face"}) {
            CompoundTag bad = a.save();
            bad.remove(field);
            invalidEndpoint(bad);
            bad = a.save();
            bad.putInt(field, 0);
            invalidEndpoint(bad);
        }
        for (String dimension : new String[] {"", "overworld", "Minecraft:overworld", "bad dimension", "a:" + "x".repeat(256)}) {
            CompoundTag bad = a.save();
            bad.putString("dimension", dimension);
            invalidEndpoint(bad);
        }
        for (String face : new String[] {"", "UP", "diagonal", "north ", "north".repeat(1000)}) {
            CompoundTag bad = a.save();
            bad.putString("face", face);
            invalidEndpoint(bad);
        }
        for (String axis : new String[] {"x", "y", "z"}) {
            CompoundTag bad = a.save();
            bad.getCompound("pos").remove(axis);
            invalidEndpoint(bad);
            bad = a.save();
            bad.getCompound("pos").putLong(axis, 0);
            invalidEndpoint(bad);
        }
        CompoundTag malformed = new CompoundTag();
        malformed.putString(LinkerSelection.TAG_KEY, "broken");
        invalidSelection(malformed);
        malformed = new CompoundTag();
        CompoundTag data = new CompoundTag();
        data.put("b", b.save());
        malformed.put(LinkerSelection.TAG_KEY, data);
        invalidSelection(malformed);
        data.put("a", b.save());
        invalidSelection(malformed);
        data.putString("a", "broken");
        invalidSelection(malformed);
        data.put("a", a.save());
        data.putString("b", "broken");
        invalidSelection(malformed);
        LinkerSelection.clear(malformed);
        check(!LinkerSelection.read(malformed).malformed(), "Explicit clear must recover from malformed data");
        System.out.println("Phase Linker checks passed: A/B cycle, six faces, dimensions, immutable addresses, NBT disk round-trip, malformed data and safe clear.");
    }

    private static Endpoint endpoint(String dimension, BlockPos pos, Direction face) {
        return new Endpoint(ResourceLocation.tryParse(dimension), pos, face);
    }

    private static void invalidEndpoint(CompoundTag tag) {
        check(Endpoint.read(tag).isEmpty(), "Malformed endpoint accepted: " + tag);
    }

    private static void invalidSelection(CompoundTag tag) {
        CompoundTag copy = tag.copy();
        check(LinkerSelection.read(tag).malformed(), "Malformed selection accepted");
        check(copy.equals(tag), "Reading malformed data must not delete it");
    }

    private static void rejects(Runnable operation, String message) {
        try {
            operation.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
