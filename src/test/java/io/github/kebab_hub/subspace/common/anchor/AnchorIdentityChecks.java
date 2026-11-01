package io.github.kebab_hub.subspace.common.anchor;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** NBT/value classes only; no Minecraft world, client, server, or bootstrap. */
public final class AnchorIdentityChecks {
    public static void main(String[] args) throws Exception {
        ResourceLocation dimension = ResourceLocation.tryParse("minecraft:overworld");
        BlockPos pos = new BlockPos(-17, -64, 200);
        AnchorIdentity identity = AnchorIdentity.create(dimension, pos);
        CompoundTag tag = identity.save();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(tag, bytes);
        CompoundTag reloaded = NbtIo.readCompressed(new ByteArrayInputStream(bytes.toByteArray()));
        check(identity.equals(AnchorIdentity.restore(reloaded, dimension, pos)), "Reload changed address");
        check(!identity.address().equals(AnchorIdentity.restore(tag, dimension, pos.east()).address()), "Copy retained address");
        check(!identity.address().equals(AnchorIdentity.restore(tag, ResourceLocation.tryParse("minecraft:the_nether"), pos).address()),
                "Cross-dimension copy retained address");
        check(tag.equals(reloaded), "Restoration mutated input NBT");
        check(tag.getAllKeys().equals(Set.of("version", "address", "dimension", "pos")), "Unexpected owner/gameplay data");
        for (String missing : tag.getAllKeys()) {
            CompoundTag malformed = tag.copy();
            malformed.remove(missing);
            check(!identity.address().equals(AnchorIdentity.restore(malformed, dimension, pos).address()), "Missing field accepted");
            malformed = tag.copy();
            malformed.putString(missing, "invalid");
            check(!identity.address().equals(AnchorIdentity.restore(malformed, dimension, pos).address()), "Wrong field type accepted");
        }
        CompoundTag invalid = tag.copy();
        invalid.putIntArray("address", new int[] {1, 2});
        check(!identity.address().equals(AnchorIdentity.restore(invalid, dimension, pos).address()), "Invalid UUID array accepted");
        invalid.putUUID("address", AnchorIdentity.UNSYNCED);
        check(!AnchorIdentity.UNSYNCED.equals(AnchorIdentity.restore(invalid, dimension, pos).address()), "Zero UUID accepted");
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(1, 2, 3);
        AnchorIdentity stable = AnchorIdentity.create(dimension, mutable);
        mutable.set(5, 6, 7);
        check(stable.pos().equals(new BlockPos(1, 2, 3)), "Identity retains mutable position");
        Set<UUID> unique = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            UUID id = AnchorIdentity.create(dimension, pos).address();
            check(unique.add(id), "New placement reused an address");
            // The wire truncates to a signed short; decoding must recover every high bit.
            check(id.equals(AddressWords.read(index -> (short) AddressWords.get(id, index))), "UUID lost bits in menu packets");
        }
        for (UUID id : new UUID[] {new UUID(-1, -1), new UUID(Long.MIN_VALUE, Long.MAX_VALUE), new UUID(0, 0),
                new UUID(0x8000ffff8000ffffL, 0xffff8000ffff8000L)}) {
            check(id.equals(AddressWords.read(index -> (short) AddressWords.get(id, index))), "UUID boundary bits lost");
        }
        System.out.println("Anchor checks passed: persistent identity, new placement/copy identity, malformed NBT, and 128-bit UUID menu round-trips.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
