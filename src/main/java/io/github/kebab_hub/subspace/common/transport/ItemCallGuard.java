package io.github.kebab_hub.subspace.common.transport;

import java.util.function.Supplier;

/** Stops reentrant capability providers, including foreign wrappers that lead back to an interface. */
public final class ItemCallGuard {
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> false);
    private ItemCallGuard() { }
    public static <T> T call(T unavailable, Supplier<T> operation) {
        if (ACTIVE.get()) return unavailable;
        ACTIVE.set(true);
        try { return operation.get(); }
        finally { ACTIVE.remove(); }
    }
}
