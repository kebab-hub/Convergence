package io.github.kebab_hub.subspace.common.link;

import java.util.Objects;
import java.util.Set;

/** Diagnostic snapshot, not a live capability lease or authorization to transfer. */
public record ConnectionSnapshot(ConnectionState state, Set<CapabilityChannels.Channel<?>> available) {
    public ConnectionSnapshot {
        Objects.requireNonNull(state);
        available = Set.copyOf(available);
        if ((state == ConnectionState.READY) != !available.isEmpty()) {
            throw new IllegalArgumentException("Only a ready snapshot can report available channels");
        }
    }

    public static ConnectionSnapshot unavailable(ConnectionState state) {
        return new ConnectionSnapshot(state, Set.of());
    }
}
