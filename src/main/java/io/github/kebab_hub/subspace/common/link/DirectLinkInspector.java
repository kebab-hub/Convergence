package io.github.kebab_hub.subspace.common.link;

import io.github.kebab_hub.subspace.common.interfaceblock.SubspaceInterfaceBlockEntity;
import io.github.kebab_hub.subspace.common.transport.DirectItemLink;
import io.github.kebab_hub.subspace.common.transport.ItemCallGuard;
import java.util.Set;

/** Diagnostic snapshot only. Operation-local handlers are discarded immediately. */
public final class DirectLinkInspector {
    private DirectLinkInspector() { }
    public static ConnectionSnapshot inspect(SubspaceInterfaceBlockEntity source) {
        return ItemCallGuard.call(ConnectionSnapshot.unavailable(ConnectionState.CHAIN_UNSUPPORTED), () -> {
            var link = DirectItemLink.resolve(source, null);
            return link.handler() == null ? ConnectionSnapshot.unavailable(link.state())
                    : new ConnectionSnapshot(ConnectionState.READY, Set.of(CapabilityChannels.ITEMS));
        });
    }
}
