package io.github.kebab_hub.subspace.common.transport;

/** Per-endpoint accounting survives retargeting; charging both endpoints shares a pair's allowance. */
public final class TransferBudget {
    private long tick = Long.MIN_VALUE;
    private int used;

    public int remaining(long now, int limit) {
        if (tick != now) { tick = now; used = 0; }
        return Math.max(0, limit - used);
    }

    public void consume(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Negative transfer");
        used = (int) Math.min(Integer.MAX_VALUE, (long) used + amount);
    }

    public long tick() { return tick; }
    public int used() { return used; }
    public void restore(long savedTick, int savedUsed) {
        tick = savedTick;
        used = savedUsed < 0 ? Integer.MAX_VALUE : savedUsed;
    }
}
