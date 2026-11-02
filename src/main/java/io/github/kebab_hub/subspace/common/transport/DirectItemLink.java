package io.github.kebab_hub.subspace.common.transport;

import io.github.kebab_hub.subspace.common.config.SubspaceConfig;
import io.github.kebab_hub.subspace.common.interfaceblock.SubspaceInterfaceBlockEntity;
import io.github.kebab_hub.subspace.common.link.ConnectionState;
import io.github.kebab_hub.subspace.common.link.DirectLinkHost;
import io.github.kebab_hub.subspace.common.link.Endpoint;
import io.github.kebab_hub.subspace.common.link.LoadedEndpoint;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/** Exactly one reciprocal peer, then its adjacent inventory. Never follows a peer's capability proxy. */
public final class DirectItemLink {
    private DirectItemLink() { }

    public record Access(ConnectionState state, SubspaceInterfaceBlockEntity source,
                         SubspaceInterfaceBlockEntity peer, IItemHandler handler, long tick) {
        public int allowance() {
            int limit = SubspaceConfig.ITEMS_PER_TICK.get();
            return Math.min(source.itemBudget().remaining(tick, limit), peer.itemBudget().remaining(tick, limit));
        }
        public void charge(int count) {
            if (count == 0) return;
            source.itemBudget().consume(count);
            peer.itemBudget().consume(count);
            source.setChanged();
            peer.setChanged();
            source.transferred();
            peer.transferred();
        }
    }

    private static Access unavailable(ConnectionState state) { return new Access(state, null, null, null, 0); }

    @Nullable
    public static Direction inventoryPort(SubspaceInterfaceBlockEntity source) {
        if (!(source.getLevel() instanceof ServerLevel world)) return null;
        Endpoint target = source.linkConfig().target();
        if (target == null) return null;
        if (!(LoadedEndpoint.find(world.getServer(), target).entity() instanceof SubspaceInterfaceBlockEntity peer)) return null;
        Endpoint back = peer.linkConfig().target();
        return back != null && back.dimension().equals(world.dimension().location()) && back.pos().equals(source.getBlockPos())
                ? back.face() : null;
    }

    /** Call only inside ItemCallGuard; null side is for internal diagnostics, never external access. */
    public static Access resolve(SubspaceInterfaceBlockEntity source, @Nullable Direction queriedSide) {
        if (!(source.getLevel() instanceof ServerLevel world) || !source.itemAccessEnabled() || !world.getServer().isSameThread()) {
            return unavailable(ConnectionState.UNLOADED);
        }
        Endpoint own = new Endpoint(world.dimension().location(), source.getBlockPos(), Direction.UP);
        if (LoadedEndpoint.find(world.getServer(), own).entity() != source) return unavailable(ConnectionState.UNLOADED);
        if (source.linkConfig().malformed()) return unavailable(ConnectionState.INVALID_DATA);
        Endpoint target = source.linkConfig().target();
        if (target == null) return unavailable(ConnectionState.UNBOUND);
        if (sameLocation(own, target)) return unavailable(ConnectionState.SELF_LINK);
        var resolved = LoadedEndpoint.find(world.getServer(), target);
        if (resolved.entity() == null) return unavailable(resolved.state());
        if (!(resolved.entity() instanceof SubspaceInterfaceBlockEntity peer)) return unavailable(ConnectionState.EXPECTED_INTERFACE);
        if (!peer.itemAccessEnabled()) return unavailable(ConnectionState.UNLOADED);
        Endpoint back = peer.linkConfig().target();
        if (peer.linkConfig().malformed() || back == null || !sameLocation(own, back)) return unavailable(ConnectionState.UNPAIRED);
        // The selected face on each interface is its local inventory port. Other faces expose the peer.
        if (queriedSide == back.face()) return unavailable(ConnectionState.LOCAL_PORT);
        Endpoint inventory = new Endpoint(target.dimension(), target.pos().relative(target.face()), target.face().getOpposite());
        var terminal = LoadedEndpoint.find(world.getServer(), inventory);
        if (terminal.entity() == null) return unavailable(terminal.state());
        if (terminal.entity() instanceof DirectLinkHost) return unavailable(ConnectionState.CHAIN_UNSUPPORTED);
        try {
            IItemHandler handler = terminal.entity().getCapability(ForgeCapabilities.ITEM_HANDLER, inventory.face()).orElse(null);
            if (handler == null || handler instanceof DirectItemHandler) return unavailable(ConnectionState.UNSUPPORTED);
            return new Access(ConnectionState.READY, source, peer, handler, world.getServer().overworld().getGameTime());
        } catch (RuntimeException failure) {
            return unavailable(ConnectionState.CAPABILITY_ERROR);
        }
    }

    public static boolean sameLocation(Endpoint first, Endpoint second) {
        return first.dimension().equals(second.dimension()) && first.pos().equals(second.pos());
    }
}
