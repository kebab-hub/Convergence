package io.github.kebab_hub.subspace.common.interfaceblock;

import io.github.kebab_hub.subspace.common.block.SubspaceInterfaceBlock;
import io.github.kebab_hub.subspace.common.link.ConnectionSnapshot;
import io.github.kebab_hub.subspace.common.link.ConnectionState;
import io.github.kebab_hub.subspace.common.link.DirectLinkConfig;
import io.github.kebab_hub.subspace.common.link.DirectLinkHost;
import io.github.kebab_hub.subspace.common.link.DirectLinkInspector;
import io.github.kebab_hub.subspace.common.link.Endpoint;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import io.github.kebab_hub.subspace.common.registry.ModParticles;
import io.github.kebab_hub.subspace.common.transport.DirectItemHandler;
import io.github.kebab_hub.subspace.common.transport.DirectItemLink;
import io.github.kebab_hub.subspace.common.transport.TransferBudget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import javax.annotation.Nullable;
import java.util.EnumMap;

/** Thin host: direct pair configuration, revocable item views, accounting and presentation. */
public final class SubspaceInterfaceBlockEntity extends BlockEntity implements DirectLinkHost {
    public static final int CHECK_INTERVAL_TICKS = 20;
    private DirectLinkConfig config = DirectLinkConfig.EMPTY;
    private ConnectionSnapshot snapshot = ConnectionSnapshot.unavailable(ConnectionState.UNBOUND);
    private boolean refreshNeeded = true;
    private final TransferBudget itemBudget = new TransferBudget();
    private final EnumMap<Direction, DirectItemHandler> itemViews = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, LazyOptional<IItemHandler>> itemCaps = new EnumMap<>(Direction.class);
    private boolean capsAvailable = true;
    private long lastParticleTick = Long.MIN_VALUE;
    @Nullable private Direction lastInventoryPort;

    public SubspaceInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.SUBSPACE_INTERFACE_ENTITY.get(), pos, state);
    }

    @Override public DirectLinkConfig linkConfig() { return config; }
    public ConnectionSnapshot snapshot() { return snapshot; }
    public TransferBudget itemBudget() { return itemBudget; }
    public boolean itemAccessEnabled() { return capsAvailable && !isRemoved(); }

    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null || !capsAvailable || isRemoved() || !(level instanceof ServerLevel world)
                    || !world.getServer().isSameThread() || side == DirectItemLink.inventoryPort(this)) return LazyOptional.empty();
            return itemCaps.computeIfAbsent(side, face -> {
                DirectItemHandler view = new DirectItemHandler(this, face);
                itemViews.put(face, view);
                return LazyOptional.of(() -> view);
            }).cast();
        }
        return super.getCapability(capability, side);
    }

    private void revokeItemViews() {
        itemViews.values().forEach(DirectItemHandler::revoke);
        var oldCaps = java.util.List.copyOf(itemCaps.values());
        itemViews.clear();
        itemCaps.clear();
        oldCaps.forEach(LazyOptional::invalidate);
    }

    @Override public void invalidateCaps() {
        capsAvailable = false;
        revokeItemViews();
        super.invalidateCaps();
    }

    @Override public void reviveCaps() {
        super.reviveCaps();
        capsAvailable = true;
        refreshNeeded = true;
    }

    @Override public void onLoad() {
        super.onLoad();
        if (!capsAvailable) reviveCaps();
        refreshNeeded = true;
    }

    public void transferred() {
        if (!(level instanceof ServerLevel world)) return;
        long now = world.getGameTime();
        if (lastParticleTick != Long.MIN_VALUE && now - lastParticleTick < 5) return;
        lastParticleTick = now;
        Direction facing = getBlockState().getValue(SubspaceInterfaceBlock.FACING);
        world.sendParticles(ModParticles.SUBSPACE_SPARK.get(),
                worldPosition.getX() + 0.5 + facing.getStepX() * 0.52, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.52, 2, 0.06, 0.06, 0.06, 0.01);
    }

    public boolean configure(Endpoint target) {
        if (!(level instanceof ServerLevel world) || isRemoved() || !world.getServer().isSameThread()) return false;
        if (target.dimension().equals(world.dimension().location()) && target.pos().equals(worldPosition)) return false;
        DirectLinkConfig candidate = DirectLinkConfig.to(target);
        config = candidate;
        revokeItemViews();
        setChanged();
        refresh();
        return true;
    }

    public void clearLink() {
        if (!(level instanceof ServerLevel world) || isRemoved() || !world.getServer().isSameThread()) return;
        config = DirectLinkConfig.EMPTY;
        revokeItemViews();
        setChanged();
        refresh();
    }

    public void refresh() {
        if (!(level instanceof ServerLevel world) || isRemoved()) return;
        ConnectionSnapshot previous = snapshot;
        snapshot = DirectLinkInspector.inspect(this);
        Direction inventoryPort = DirectItemLink.inventoryPort(this);
        if (!snapshot.equals(previous) || lastInventoryPort != inventoryPort) revokeItemViews();
        lastInventoryPort = inventoryPort;
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
        revokeItemViews();
        itemBudget.restore(tag.contains("ItemBudgetTick", Tag.TAG_LONG) ? tag.getLong("ItemBudgetTick") : Long.MIN_VALUE,
                tag.contains("ItemBudgetUsed", Tag.TAG_INT) ? tag.getInt("ItemBudgetUsed") : 0);
        snapshot = ConnectionSnapshot.unavailable(config.malformed() ? ConnectionState.INVALID_DATA : ConnectionState.UNBOUND);
        refreshNeeded = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        config.writeTo(tag);
        tag.putLong("ItemBudgetTick", itemBudget.tick());
        tag.putInt("ItemBudgetUsed", itemBudget.used());
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
