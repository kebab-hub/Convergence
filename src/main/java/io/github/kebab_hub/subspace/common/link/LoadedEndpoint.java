package io.github.kebab_hub.subspace.common.link;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

/** An operation-local resolution result. Never requests chunks or creates block entities. */
public record LoadedEndpoint(ConnectionState state, BlockEntity entity) {
    public static LoadedEndpoint find(MinecraftServer server, Endpoint target) {
        if (!server.isSameThread()) return new LoadedEndpoint(ConnectionState.UNLOADED, null);
        var world = server.getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, target.dimension()));
        if (world == null) return new LoadedEndpoint(ConnectionState.DIMENSION_UNAVAILABLE, null);
        if (!world.isInWorldBounds(target.pos()) || !world.getWorldBorder().isWithinBounds(target.pos())) {
            return new LoadedEndpoint(ConnectionState.OUT_OF_BOUNDS, null);
        }
        var chunk = world.getChunkSource().getChunkNow(target.pos().getX() >> 4, target.pos().getZ() >> 4);
        if (chunk == null) return new LoadedEndpoint(ConnectionState.UNLOADED, null);
        if (chunk.getBlockState(target.pos()).isAir()) return new LoadedEndpoint(ConnectionState.BLOCK_MISSING, null);
        var entity = chunk.getBlockEntity(target.pos(), LevelChunk.EntityCreationType.CHECK);
        return entity == null || entity.isRemoved() ? new LoadedEndpoint(ConnectionState.NO_BLOCK_ENTITY, null)
                : new LoadedEndpoint(ConnectionState.READY, entity);
    }
}
