package io.github.kebab_hub.subspace.common.interfaceblock;

import io.github.kebab_hub.subspace.common.block.SubspaceInterfaceBlock;
import io.github.kebab_hub.subspace.common.link.ConnectionSnapshot;
import io.github.kebab_hub.subspace.common.link.ConnectionState;
import io.github.kebab_hub.subspace.common.link.DirectLinkConfig;
import io.github.kebab_hub.subspace.common.link.DirectLinkHost;
import io.github.kebab_hub.subspace.common.link.DirectLinkInspector;
import io.github.kebab_hub.subspace.common.link.Endpoint;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Thin host: persistent configuration plus diagnostic presentation. Exposes no remote capabilities. */
public final class SubspaceInterfaceBlockEntity extends BlockEntity implements DirectLinkHost {
    public static final int CHECK_INTERVAL_TICKS = 20;
    private DirectLinkConfig config = DirectLinkConfig.EMPTY;
    private ConnectionSnapshot snapshot = ConnectionSnapshot.unavailable(ConnectionState.UNBOUND);
    private boolean refreshNeeded = true;

    public SubspaceInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.SUBSPACE_INTERFACE_ENTITY.get(), pos, state);
    }

    @Override public DirectLinkConfig linkConfig() { return config; }
    public ConnectionSnapshot snapshot() { return snapshot; }

    public boolean configure(Endpoint target) {
        if (!(level instanceof ServerLevel world) || isRemoved()) return false;
        if (target.dimension().equals(world.dimension().location()) && target.pos().equals(worldPosition)) return false;
        DirectLinkConfig candidate = DirectLinkConfig.to(target);
        if (DirectLinkInspector.inspect(world, worldPosition, candidate).state() == ConnectionState.CHAIN_UNSUPPORTED) return false;
        config = candidate;
        setChanged();
        refresh();
        return true;
    }

    public void clearLink() {
        if (!(level instanceof ServerLevel) || isRemoved()) return;
        config = DirectLinkConfig.EMPTY;
        setChanged();
        refresh();
    }

    public void refresh() {
        if (!(level instanceof ServerLevel world) || isRemoved()) return;
        snapshot = DirectLinkInspector.inspect(world, worldPosition, config);
        refreshNeeded = false;
        boolean connected = snapshot.state() == ConnectionState.READY;
        if (getBlockState().getValue(SubspaceInterfaceBlock.CONNECTED) != connected) {
            world.setBlock(worldPosition, getBlockState().setValue(SubspaceInterfaceBlock.CONNECTED, connected), Block.UPDATE_CLIENTS);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SubspaceInterfaceBlockEntity entity) {
        if (entity.refreshNeeded || (level.getGameTime() + pos.asLong()) % CHECK_INTERVAL_TICKS == 0) entity.refresh();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        config = DirectLinkConfig.read(tag);
        snapshot = ConnectionSnapshot.unavailable(config.malformed() ? ConnectionState.INVALID_DATA : ConnectionState.UNBOUND);
        refreshNeeded = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        config.writeTo(tag);
    }

    @Override
    public void onChunkUnloaded() {
        snapshot = ConnectionSnapshot.unavailable(ConnectionState.UNLOADED);
        refreshNeeded = true;
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        snapshot = ConnectionSnapshot.unavailable(ConnectionState.UNLOADED);
        super.setRemoved();
    }
}
