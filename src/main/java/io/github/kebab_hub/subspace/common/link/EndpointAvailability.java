package io.github.kebab_hub.subspace.common.link;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

/** Read-only inspection of already available worlds/chunks. Never requests a chunk load. */
public final class EndpointAvailability {
    public enum Status {
        AVAILABLE, DIMENSION_UNAVAILABLE, OUT_OF_BOUNDS, UNLOADED, BLOCK_MISSING
    }

    private EndpointAvailability() {
    }

    public static Status check(MinecraftServer server, Endpoint endpoint) {
        ServerLevel world = server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, endpoint.dimension()));
        if (world == null) return Status.DIMENSION_UNAVAILABLE;
        if (!world.isInWorldBounds(endpoint.pos()) || !world.getWorldBorder().isWithinBounds(endpoint.pos())) {
            return Status.OUT_OF_BOUNDS;
        }
        LevelChunk chunk = world.getChunkSource().getChunkNow(endpoint.pos().getX() >> 4, endpoint.pos().getZ() >> 4);
        if (chunk == null) return Status.UNLOADED;
        return chunk.getBlockState(endpoint.pos()).isAir() ? Status.BLOCK_MISSING : Status.AVAILABLE;
    }
}
