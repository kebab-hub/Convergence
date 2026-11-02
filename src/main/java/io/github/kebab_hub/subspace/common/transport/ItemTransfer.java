package io.github.kebab_hub.subspace.common.transport;

import java.util.function.BiFunction;
import java.util.function.IntConsumer;

/** Single-handler item accounting. No source extraction, staging buffer, rollback or retry. */
public final class ItemTransfer {
    private ItemTransfer() { }

    /** Small stack seam lets the same accounting run in headless conservation tests. */
    public interface Stacks<S> {
        int count(S stack);
        S copy(S stack, int count);
        boolean same(S first, S second);
    }

    public static <S> S insert(S input, boolean simulate, int allowance, Stacks<S> stacks,
                               BiFunction<S, Boolean, S> insert, IntConsumer charge) {
        int original = stacks.count(input);
        int offered = Math.min(original, Math.max(0, allowance));
        if (offered == 0) return stacks.copy(input, original);
        S remainder = insert.apply(stacks.copy(input, offered), simulate);
        int left = stacks.count(remainder);
        if (left < 0 || left > offered || (left > 0 && !stacks.same(input, remainder))) {
            // Never hide a broken provider's committed operation behind a fabricated safe return.
            throw new IllegalStateException("Item handler violated insertion remainder contract");
        }
        int accepted = offered - left;
        if (!simulate) charge.accept(accepted);
        return stacks.copy(input, original - accepted);
    }

    public static <S> S extract(int amount, boolean simulate, int allowance, S empty, Stacks<S> stacks,
                                BiFunction<Integer, Boolean, S> extract, IntConsumer charge) {
        int requested = Math.min(Math.max(0, amount), Math.max(0, allowance));
        if (requested == 0) return empty;
        S result = extract.apply(requested, simulate);
        int actual = stacks.count(result);
        if (actual < 0 || actual > requested) throw new IllegalStateException("Item handler exceeded extraction request");
        if (!simulate) charge.accept(actual);
        return stacks.copy(result, actual);
    }
}
