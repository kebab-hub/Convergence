package io.github.kebab_hub.subspace.common.anchor;

import java.util.UUID;
import java.util.function.IntUnaryOperator;

/** Vanilla menu data travels as signed shorts: send all 128 UUID bits as eight 16-bit words. */
public final class AddressWords {
    public static final int COUNT = 8;

    private AddressWords() {
    }

    public static int get(UUID address, int index) {
        if (index < 0 || index >= COUNT) throw new IndexOutOfBoundsException(index);
        long half = index < 4 ? address.getMostSignificantBits() : address.getLeastSignificantBits();
        return (int) (half >>> ((index % 4) * 16)) & 0xffff;
    }

    public static UUID read(IntUnaryOperator words) {
        long most = 0;
        long least = 0;
        for (int i = 0; i < 4; i++) {
            most |= (words.applyAsInt(i) & 0xffffL) << (i * 16);
            least |= (words.applyAsInt(i + 4) & 0xffffL) << (i * 16);
        }
        return new UUID(most, least);
    }
}
