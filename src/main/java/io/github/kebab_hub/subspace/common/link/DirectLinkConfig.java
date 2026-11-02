package io.github.kebab_hub.subspace.common.link;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.Objects;

/** Persisted intent only. Live availability and capability objects never enter the save format. */
public final class DirectLinkConfig {
    public static final String KEY = "DirectLink";
    public static final DirectLinkConfig EMPTY = new DirectLinkConfig(null, null);
    @Nullable private final Endpoint target;
    @Nullable private final Tag invalidData;

    private DirectLinkConfig(@Nullable Endpoint target, @Nullable Tag invalidData) {
        this.target = target;
        this.invalidData = invalidData;
    }

    public static DirectLinkConfig to(Endpoint target) {
        return new DirectLinkConfig(Objects.requireNonNull(target), null);
    }

    @Nullable public Endpoint target() { return target; }
    public boolean malformed() { return invalidData != null; }

    public static DirectLinkConfig read(CompoundTag root) {
        Tag stored = root.get(KEY);
        if (stored == null) return EMPTY;
        if (stored instanceof CompoundTag data && data.contains("version", Tag.TAG_INT)
                && data.getInt("version") == 1 && data.contains("target", Tag.TAG_COMPOUND)) {
            Endpoint target = Endpoint.read(data.getCompound("target")).orElse(null);
            if (target != null) return to(target);
        }
        return new DirectLinkConfig(null, stored.copy());
    }

    public void writeTo(CompoundTag root) {
        if (invalidData != null) {
            root.put(KEY, invalidData.copy());
        } else if (target == null) {
            root.remove(KEY);
        } else {
            CompoundTag data = new CompoundTag();
            data.putInt("version", 1);
            data.put("target", target.save());
            root.put(KEY, data);
        }
    }
}
