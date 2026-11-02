package io.github.kebab_hub.subspace.common.link;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

/** An address only; never retains a world, chunk, block entity, or capability. */
public record Endpoint(ResourceLocation dimension, BlockPos pos, Direction face) {
    public Endpoint {
        Objects.requireNonNull(dimension);
        pos = Objects.requireNonNull(pos).immutable();
        Objects.requireNonNull(face);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", dimension.toString());
        CompoundTag position = new CompoundTag();
        position.putInt("x", pos.getX());
        position.putInt("y", pos.getY());
        position.putInt("z", pos.getZ());
        tag.put("pos", position);
        tag.putString("face", face.getName());
        return tag;
    }

    public static Optional<Endpoint> read(CompoundTag tag) {
        if (!tag.contains("dimension", Tag.TAG_STRING) || !tag.contains("pos", Tag.TAG_COMPOUND)
                || !tag.contains("face", Tag.TAG_STRING)) return Optional.empty();
        String id = tag.getString("dimension");
        if (id.length() > 256) return Optional.empty();
        ResourceLocation dimension = ResourceLocation.tryParse(id);
        if (dimension == null || !dimension.toString().equals(id)) return Optional.empty();
        String side = tag.getString("face");
        if (side.length() > 5) return Optional.empty();
        Direction face = Direction.byName(side);
        if (face == null || !face.getName().equals(side)) return Optional.empty();
        CompoundTag pos = tag.getCompound("pos");
        // Require exact types: NBT numeric coercion must not turn broken data into an origin address.
        if (!pos.contains("x", Tag.TAG_INT) || !pos.contains("y", Tag.TAG_INT)
                || !pos.contains("z", Tag.TAG_INT)) return Optional.empty();
        return Optional.of(new Endpoint(dimension,
                new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z")), face));
    }
}
