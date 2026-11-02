package io.github.kebab_hub.subspace.common.link;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Plain Java checks of NBT and Forge capability contracts. No Minecraft bootstrap/world/server. */
public final class DirectLinkChecks {
    public static void main(String[] args) throws Exception {
        CompoundTag empty = new CompoundTag();
        check(DirectLinkConfig.read(empty).target() == null && !DirectLinkConfig.read(empty).malformed(), "Missing link not empty");
        for (Direction face : Direction.values()) {
            Endpoint target = new Endpoint(ResourceLocation.tryParse("example:remote_dimension"), new BlockPos(-31, -64, 100), face);
            CompoundTag root = new CompoundTag();
            root.putString("unrelated", "retain");
            DirectLinkConfig.to(target).writeTo(root);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            NbtIo.writeCompressed(root, bytes);
            CompoundTag loaded = NbtIo.readCompressed(new ByteArrayInputStream(bytes.toByteArray()));
            CompoundTag original = loaded.copy();
            check(target.equals(DirectLinkConfig.read(loaded).target()), "Target address/side did not survive disk NBT");
            check(original.equals(loaded), "Reading changed NBT");
            check(root.getCompound(DirectLinkConfig.KEY).getAllKeys().equals(Set.of("version", "target")), "Runtime state leaked into save");
            DirectLinkConfig.EMPTY.writeTo(loaded);
            check(loaded.getAllKeys().equals(Set.of("unrelated")), "Clearing removed unrelated NBT");
            for (String field : new String[] {"dimension", "pos", "face"}) {
                CompoundTag bad = root.copy();
                bad.getCompound(DirectLinkConfig.KEY).getCompound("target").remove(field);
                invalidPreserved(bad);
            }
            CompoundTag future = root.copy();
            future.getCompound(DirectLinkConfig.KEY).putInt("version", 2);
            invalidPreserved(future);
            CompoundTag wrongType = root.copy();
            wrongType.putString(DirectLinkConfig.KEY, "broken");
            invalidPreserved(wrongType);
        }

        Fixture<IItemHandler> items = fixture("items", new RejectItemOperations());
        Fixture<IFluidHandler> fluids = fixture("fluids", new RejectFluidOperations());
        Fixture<IEnergyStorage> energy = fixture("energy", new RejectEnergyOperations());
        for (Fixture<?> fixture : List.of(items, fluids, energy)) {
            for (Direction allowed : Direction.values()) probeLifecycle(fixture, allowed);
        }
        Set<CapabilityChannels.Channel<?>> mutable = new HashSet<>();
        mutable.add(items.channel());
        ConnectionSnapshot snapshot = new ConnectionSnapshot(ConnectionState.READY, mutable);
        mutable.clear();
        check(snapshot.available().equals(Set.of(items.channel())), "Snapshot retained a mutable channel set");
        check(ConnectionSnapshot.unavailable(ConnectionState.UNLOADED).available().isEmpty(), "Unavailable snapshot leaked capabilities");
        System.out.println("Direct-link checks passed: six target sides, NBT persistence/recovery, immutable snapshots, typed probes, invalidation/revival and zero handler operations.");
    }

    private static void invalidPreserved(CompoundTag root) {
        CompoundTag expected = root.copy();
        DirectLinkConfig config = DirectLinkConfig.read(root);
        check(config.malformed() && config.target() == null, "Malformed data was treated as a valid target");
        root.putString(DirectLinkConfig.KEY, "changed after read");
        config.writeTo(root);
        check(root.equals(expected), "Malformed data wasn't independently retained for explicit recovery");
        DirectLinkConfig.EMPTY.writeTo(root);
        check(!DirectLinkConfig.read(root).malformed(), "Explicit clear did not recover");
    }

    private record Fixture<T>(CapabilityChannels.Channel<T> channel, T handler) { }

    @SuppressWarnings("unchecked")
    private static <T> Fixture<T> fixture(String id, T handler) throws ReflectiveOperationException {
        // ForgeCapabilities tokens require ModLauncher transformation. Construct unregistered test-only
        // Capability instances instead; the actual Forge constants are covered by compiled GameTests.
        var constructor = Capability.class.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        Capability<T> capability = (Capability<T>) constructor.newInstance("subspace_test:" + id);
        return new Fixture<>(new CapabilityChannels.Channel<>(id, capability), handler);
    }

    private static <T> void probeLifecycle(Fixture<T> fixture, Direction side) {
        CapabilityChannels.Channel<T> channel = fixture.channel();
        class Provider implements ICapabilityProvider {
            LazyOptional<T> handle = LazyOptional.of(fixture::handler);
            int calls;
            @Override public <U> LazyOptional<U> getCapability(Capability<U> capability, Direction queriedSide) {
                calls++;
                return capability == channel.capability() && queriedSide == side ? handle.cast() : LazyOptional.empty();
            }
        }
        Provider provider = new Provider();
        check(channel.available(provider, side), "Selected side not probed");
        check(!channel.available(provider, side.getOpposite()), "Selected face was ignored");
        provider.handle.invalidate();
        check(!channel.available(provider, side), "Invalidated handle was cached");
        provider.handle = LazyOptional.of(fixture::handler);
        check(channel.available(provider, side), "New handle was not reacquired");
        check(provider.calls == 4, "Probe performed unexpected recursive or duplicate calls");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static AssertionError operation() {
        return new AssertionError("A presence probe invoked a resource handler operation");
    }

    // Concrete fakes avoid dynamic proxy reflection, which initializes Minecraft registries through
    // ItemStack/FluidStack return types. Every method still fails if the production probe touches it.
    private static final class RejectItemOperations implements IItemHandler {
        public int getSlots() { throw operation(); }
        public ItemStack getStackInSlot(int slot) { throw operation(); }
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { throw operation(); }
        public ItemStack extractItem(int slot, int amount, boolean simulate) { throw operation(); }
        public int getSlotLimit(int slot) { throw operation(); }
        public boolean isItemValid(int slot, ItemStack stack) { throw operation(); }
    }

    private static final class RejectFluidOperations implements IFluidHandler {
        public int getTanks() { throw operation(); }
        public FluidStack getFluidInTank(int tank) { throw operation(); }
        public int getTankCapacity(int tank) { throw operation(); }
        public boolean isFluidValid(int tank, FluidStack stack) { throw operation(); }
        public int fill(FluidStack stack, FluidAction action) { throw operation(); }
        public FluidStack drain(FluidStack stack, FluidAction action) { throw operation(); }
        public FluidStack drain(int amount, FluidAction action) { throw operation(); }
    }

    private static final class RejectEnergyOperations implements IEnergyStorage {
        public int receiveEnergy(int amount, boolean simulate) { throw operation(); }
        public int extractEnergy(int amount, boolean simulate) { throw operation(); }
        public int getEnergyStored() { throw operation(); }
        public int getMaxEnergyStored() { throw operation(); }
        public boolean canExtract() { throw operation(); }
        public boolean canReceive() { throw operation(); }
    }
}
