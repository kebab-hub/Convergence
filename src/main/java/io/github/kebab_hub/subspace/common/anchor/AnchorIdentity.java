package io.github.kebab_hub.subspace.common.anchor;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** A local address, bound to its saved location to avoid duplicating addresses through copied NBT. */
public record AnchorIdentity(UUID address, ResourceLocation dimension, BlockPos pos) {
    public static final UUID UNSYNCED = new UUID(0, 0);

    public AnchorIdentity {
        if (address == null || address.equals(UNSYNCED) || dimension == null || pos == null) {
            throw new IllegalArgumentException("An anchor needs an address and location");
        }
        pos = pos.immutable();
    }

    public static AnchorIdentity create(ResourceLocation dimension, BlockPos pos) {
        return new AnchorIdentity(UUID.randomUUID(), dimension, pos);
    }

    public static AnchorIdentity restore(CompoundTag tag, ResourceLocation dimension, BlockPos pos) {
        if (tag.contains("version", Tag.TAG_INT) && tag.getInt("version") == 1 && tag.hasUUID("address")
                && !tag.getUUID("address").equals(UNSYNCED) && tag.contains("dimension", Tag.TAG_STRING)
                && tag.getString("dimension").equals(dimension.toString())
                && tag.contains("pos", Tag.TAG_LONG) && tag.getLong("pos") == pos.asLong()) {
            return new AnchorIdentity(tag.getUUID("address"), dimension, pos);
        }
        return create(dimension, pos);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", 1);
        tag.putUUID("address", address);
        tag.putString("dimension", dimension.toString());
        tag.putLong("pos", pos.asLong());
        return tag;
    }
}
