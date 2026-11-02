package io.github.kebab_hub.subspace.common.anchor;

import io.github.kebab_hub.subspace.common.block.SubspaceAnchorBlock;
import io.github.kebab_hub.subspace.common.menu.AnchorMenu;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/** Persistence and screen access only. No ticker, inventory, capability, routing, or owner. */
public final class SubspaceAnchorBlockEntity extends BlockEntity implements MenuProvider {
    private CompoundTag savedIdentity = new CompoundTag();
    private AnchorIdentity identity;

    public SubspaceAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.SUBSPACE_ANCHOR_ENTITY.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) address();
    }

    public UUID address() {
        if (!(level instanceof ServerLevel server)) throw new IllegalStateException("Address creation is server-only");
        if (identity == null) {
            identity = AnchorIdentity.restore(savedIdentity, server.dimension().location(), worldPosition);
            CompoundTag resolved = identity.save();
            if (!resolved.equals(savedIdentity)) {
                savedIdentity = resolved;
                setChanged();
            }
        }
        return identity.address();
    }

    /** BlockItem NBT must not let a newly placed anchor impersonate another anchor. */
    public void newPlacement() {
        savedIdentity = new CompoundTag();
        identity = null;
        address();
    }

    public boolean isActive() {
        return getBlockState().getValue(SubspaceAnchorBlock.ACTIVE);
    }

    public int linkedEndpointCount() {
        // Address selection does not establish links. Membership belongs to a later feature.
        return 0;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        savedIdentity = tag.getCompound("Identity").copy();
        identity = null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (level instanceof ServerLevel) address();
        tag.put("Identity", savedIdentity.copy());
        // ACTIVE is already persisted and synchronized by the block state.
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.subspace.subspace_anchor");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AnchorMenu(id, inventory, this);
    }
}
