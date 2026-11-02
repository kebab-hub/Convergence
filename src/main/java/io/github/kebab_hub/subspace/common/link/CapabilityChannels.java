package io.github.kebab_hub.subspace.common.link;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.Objects;

/** Typed standard Forge channels, independent of machine classes and optional mods. */
public final class CapabilityChannels {
    public record Channel<T>(String id, Capability<T> capability) {
        public Channel {
            Objects.requireNonNull(id);
            Objects.requireNonNull(capability);
        }
        public boolean available(ICapabilityProvider provider, Direction side) {
            // No LazyOptional or mutable handler escapes this query or survives it.
            return provider.getCapability(capability, Objects.requireNonNull(side)).isPresent();
        }
    }

    public static final Channel<IItemHandler> ITEMS = new Channel<>("items", ForgeCapabilities.ITEM_HANDLER);
    public static final Channel<IFluidHandler> FLUIDS = new Channel<>("fluids", ForgeCapabilities.FLUID_HANDLER);
    public static final Channel<IEnergyStorage> ENERGY = new Channel<>("energy", ForgeCapabilities.ENERGY);
    public static final List<Channel<?>> STANDARD = List.of(ITEMS, FLUIDS, ENERGY);

    private CapabilityChannels() { }
}
