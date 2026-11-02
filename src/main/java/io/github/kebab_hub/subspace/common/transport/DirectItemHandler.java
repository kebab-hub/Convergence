package io.github.kebab_hub.subspace.common.transport;

import io.github.kebab_hub.subspace.common.interfaceblock.SubspaceInterfaceBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Function;

/** Revocable, sided view. Every call reacquires a live target; no remote handler escapes or is retained. */
public final class DirectItemHandler implements IItemHandler {
    private static final ItemTransfer.Stacks<ItemStack> STACKS = new ItemTransfer.Stacks<>() {
        public int count(ItemStack stack) { return stack.getCount(); }
        public ItemStack copy(ItemStack stack, int count) {
            if (count <= 0) return ItemStack.EMPTY;
            ItemStack result = stack.copy();
            result.setCount(count);
            return result;
        }
        public boolean same(ItemStack first, ItemStack second) { return ItemStack.isSameItemSameTags(first, second); }
    };
    private final SubspaceInterfaceBlockEntity source;
    private final Direction side;
    private boolean valid = true;

    public DirectItemHandler(SubspaceInterfaceBlockEntity source, Direction side) {
        this.source = source;
        this.side = side;
    }
    public void revoke() { valid = false; }

    private <T> T access(T unavailable, Function<DirectItemLink.Access, T> operation) {
        if (!valid) return unavailable;
        return ItemCallGuard.call(unavailable, () -> {
            var link = DirectItemLink.resolve(source, side);
            return link.handler() == null ? unavailable : operation.apply(link);
        });
    }

    private static boolean slot(IItemHandler handler, int slot) { return slot >= 0 && slot < handler.getSlots(); }
    @Override public int getSlots() { return access(0, link -> Math.max(0, link.handler().getSlots())); }
    @Override public ItemStack getStackInSlot(int slot) {
        return access(ItemStack.EMPTY, link -> slot(link.handler(), slot) ? link.handler().getStackInSlot(slot).copy() : ItemStack.EMPTY);
    }
    @Override public int getSlotLimit(int slot) {
        return access(0, link -> slot(link.handler(), slot) ? link.handler().getSlotLimit(slot) : 0);
    }
    @Override public boolean isItemValid(int slot, ItemStack stack) {
        return access(false, link -> slot(link.handler(), slot) && link.handler().isItemValid(slot, stack.copy()));
    }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        return access(stack.copy(), link -> !slot(link.handler(), slot) ? stack.copy()
                : ItemTransfer.insert(stack, simulate, link.allowance(), STACKS,
                        (offered, simulation) -> link.handler().insertItem(slot, offered, simulation), link::charge));
    }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;
        return access(ItemStack.EMPTY, link -> !slot(link.handler(), slot) ? ItemStack.EMPTY
                : ItemTransfer.extract(amount, simulate, link.allowance(), ItemStack.EMPTY, STACKS,
                        (requested, simulation) -> link.handler().extractItem(slot, requested, simulation), link::charge));
    }
}
