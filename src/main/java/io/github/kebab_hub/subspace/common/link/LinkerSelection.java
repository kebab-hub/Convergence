package io.github.kebab_hub.subspace.common.link;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.Objects;

/** Per-stack A/B state, inferred from the endpoints rather than a separate saved mode. */
public record LinkerSelection(@Nullable Endpoint a, @Nullable Endpoint b) {
    public static final String TAG_KEY = "subspace_linker";
    public static final LinkerSelection EMPTY = new LinkerSelection(null, null);

    public LinkerSelection {
        if (b != null && (a == null || a.equals(b))) {
            throw new IllegalArgumentException("B requires a distinct endpoint A");
        }
    }

    public boolean complete() {
        return b != null;
    }

    public LinkerSelection select(Endpoint endpoint) {
        Objects.requireNonNull(endpoint);
        return a == null || complete() ? new LinkerSelection(endpoint, null) : new LinkerSelection(a, endpoint);
    }

    public void writeTo(CompoundTag itemTag) {
        CompoundTag data = new CompoundTag();
        if (a != null) data.put("a", a.save());
        if (b != null) data.put("b", b.save());
        if (a == null) itemTag.remove(TAG_KEY);
        else itemTag.put(TAG_KEY, data);
    }

    public static void clear(@Nullable CompoundTag itemTag) {
        if (itemTag != null) itemTag.remove(TAG_KEY);
    }

    public static ReadResult read(@Nullable CompoundTag itemTag) {
        if (itemTag == null || !itemTag.contains(TAG_KEY)) return new ReadResult(EMPTY, false);
        if (!itemTag.contains(TAG_KEY, Tag.TAG_COMPOUND)) return malformed();
        CompoundTag data = itemTag.getCompound(TAG_KEY);
        Endpoint a = readEndpoint(data, "a");
        Endpoint b = readEndpoint(data, "b");
        if ((data.contains("a") && a == null) || (data.contains("b") && b == null)
                || (b != null && (a == null || a.equals(b)))) return malformed();
        return new ReadResult(new LinkerSelection(a, b), false);
    }

    @Nullable
    private static Endpoint readEndpoint(CompoundTag data, String name) {
        return data.contains(name, Tag.TAG_COMPOUND) ? Endpoint.read(data.getCompound(name)).orElse(null) : null;
    }

    private static ReadResult malformed() {
        return new ReadResult(EMPTY, true);
    }

    public record ReadResult(LinkerSelection selection, boolean malformed) {
    }
}
