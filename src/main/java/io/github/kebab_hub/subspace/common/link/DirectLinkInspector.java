package io.github.kebab_hub.subspace.common.link;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashSet;
import java.util.Set;

/** One bounded, server-thread inspection. No routes, chunk requests, capability caches or resource calls. */
public final class DirectLinkInspector {
    private DirectLinkInspector() { }

    public static ConnectionSnapshot inspect(ServerLevel sourceWorld, BlockPos sourcePos, DirectLinkConfig config) {
        if (!sourceWorld.getServer().isSameThread()) throw new IllegalStateException("Inspect links on the server thread");
        if (config.malformed()) return unavailable(ConnectionState.INVALID_DATA);
        Endpoint target = config.target();
        if (target == null) return unavailable(ConnectionState.UNBOUND);
        if (target.dimension().equals(sourceWorld.dimension().location()) && target.pos().equals(sourcePos)) {
            return unavailable(ConnectionState.SELF_LINK);
        }
        ServerLevel destination = sourceWorld.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, target.dimension()));
        if (destination == null) return unavailable(ConnectionState.DIMENSION_UNAVAILABLE);
        if (!destination.isInWorldBounds(target.pos()) || !destination.getWorldBorder().isWithinBounds(target.pos())) {
            return unavailable(ConnectionState.OUT_OF_BOUNDS);
        }
        LevelChunk chunk = destination.getChunkSource().getChunkNow(target.pos().getX() >> 4, target.pos().getZ() >> 4);
        if (chunk == null) return unavailable(ConnectionState.UNLOADED);
        if (chunk.getBlockState(target.pos()).isAir()) return unavailable(ConnectionState.BLOCK_MISSING);
        BlockEntity provider = chunk.getBlockEntity(target.pos(), LevelChunk.EntityCreationType.CHECK);
        if (provider == null || provider.isRemoved()) return unavailable(ConnectionState.NO_BLOCK_ENTITY);
        if (provider instanceof DirectLinkHost) return unavailable(ConnectionState.CHAIN_UNSUPPORTED);
        Set<CapabilityChannels.Channel<?>> available = new HashSet<>();
        try {
            for (CapabilityChannels.Channel<?> channel : CapabilityChannels.STANDARD) {
                if (channel.available(provider, target.face())) available.add(channel);
            }
        } catch (RuntimeException faultyProvider) {
            // A foreign provider can fail. Report unavailable; never retain a partially valid handler set.
            return unavailable(ConnectionState.CAPABILITY_ERROR);
        }
        return available.isEmpty() ? unavailable(ConnectionState.UNSUPPORTED)
                : new ConnectionSnapshot(ConnectionState.READY, available);
    }

    private static ConnectionSnapshot unavailable(ConnectionState state) {
        return ConnectionSnapshot.unavailable(state);
    }
}
