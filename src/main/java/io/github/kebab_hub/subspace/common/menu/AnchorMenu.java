package io.github.kebab_hub.subspace.common.menu;

import io.github.kebab_hub.subspace.common.anchor.AddressWords;
import io.github.kebab_hub.subspace.common.anchor.AnchorIdentity;
import io.github.kebab_hub.subspace.common.anchor.SubspaceAnchorBlockEntity;
import io.github.kebab_hub.subspace.common.block.SubspaceAnchorBlock;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;
import java.util.UUID;

/** Slotless screen; vanilla menu synchronization and button packets suffice. */
public final class AnchorMenu extends AbstractContainerMenu {
    public static final int TOGGLE = 0;
    private static final int ACTIVE = AddressWords.COUNT;
    private static final int LINKS = ACTIVE + 1;
    private static final int READY = LINKS + 1;
    private static final int DATA_COUNT = READY + 1;
    @Nullable private final SubspaceAnchorBlockEntity anchor;
    private final ContainerData data;

    public AnchorMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, null, new SimpleContainerData(DATA_COUNT));
        buffer.readBlockPos(); // Opening payload is consumed; clients never resolve or create an address.
    }

    public AnchorMenu(int id, Inventory inventory, SubspaceAnchorBlockEntity anchor) {
        this(id, anchor, new ContainerData() {
            @Override public int get(int index) {
                if (index < AddressWords.COUNT) return AddressWords.get(anchor.address(), index);
                return switch (index) {
                    case ACTIVE -> anchor.isActive() ? 1 : 0;
                    case LINKS -> anchor.linkedEndpointCount();
                    case READY -> 1;
                    default -> throw new IndexOutOfBoundsException(index);
                };
            }
            @Override public void set(int index, int value) { }
            @Override public int getCount() { return DATA_COUNT; }
        });
    }

    private AnchorMenu(int id, @Nullable SubspaceAnchorBlockEntity anchor, ContainerData data) {
        super(ModRegistries.ANCHOR_MENU.get(), id);
        this.anchor = anchor;
        this.data = data;
        addDataSlots(data);
    }

    public UUID address() {
        return synced() ? AddressWords.read(data::get) : AnchorIdentity.UNSYNCED;
    }

    public boolean synced() { return data.get(READY) == 1; }
    public boolean active() { return data.get(ACTIVE) == 1; }
    public int linkedEndpointCount() { return data.get(LINKS); }

    @Override
    public boolean stillValid(Player player) {
        if (anchor == null) return player.level.isClientSide;
        if (!(anchor.getLevel() instanceof ServerLevel world) || player.level != world || anchor.isRemoved()) return false;
        var pos = anchor.getBlockPos();
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64
                || !world.getWorldBorder().isWithinBounds(pos)) return false;
        LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        return chunk != null && chunk.getBlockEntity(pos) == anchor;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button != TOGGLE || anchor == null || player.isSpectator() || !player.mayBuild() || !stillValid(player)) return false;
        var world = anchor.getLevel();
        var pos = anchor.getBlockPos();
        if (!world.mayInteract(player, pos)) return false;
        boolean active = !anchor.isActive();
        world.setBlock(pos, anchor.getBlockState().setValue(SubspaceAnchorBlock.ACTIVE, active), Block.UPDATE_ALL);
        anchor.setChanged();
        world.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.3F, active ? 1.2F : 0.7F);
        broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }
}
