package io.github.kebab_hub.subspace.common.transport;

import java.util.Map;
import java.util.Random;

/** Exercises production accounting with value stacks, no game bootstrap or server. */
public final class ItemTransferChecks {
    private record Stack(int count, String item, Map<String, String> tags) { }
    private static final Stack EMPTY = new Stack(0, "air", Map.of());
    private static final Stack INPUT = new Stack(64, "iron", Map.of("name", "Tagged alloy", "custom", "retain"));
    private static final ItemTransfer.Stacks<Stack> STACKS = new ItemTransfer.Stacks<>() {
        public int count(Stack value) { return value.count(); }
        public Stack copy(Stack value, int count) { return new Stack(count, value.item(), value.tags()); }
        public boolean same(Stack a, Stack b) { return a.item().equals(b.item()) && a.tags().equals(b.tags()); }
    };
    private static final class Inventory {
        int count;
        int capacity = 64;
        int calls;
        int lastRequest;
        Stack insert(Stack offered, boolean simulate) {
            calls++;
            lastRequest = offered.count();
            int accepted = Math.min(offered.count(), capacity - count);
            if (!simulate) count += accepted;
            return STACKS.copy(offered, offered.count() - accepted);
        }
        Stack extract(int requested, boolean simulate) {
            calls++;
            lastRequest = requested;
            int extracted = Math.min(requested, count);
            if (!simulate) count -= extracted;
            return STACKS.copy(INPUT, extracted);
        }
    }

    public static void main(String[] args) {
        TransferBudget first = new TransferBudget();
        TransferBudget second = new TransferBudget();
        Inventory inventory = new Inventory();
        java.util.function.IntConsumer charge = count -> { first.consume(count); second.consume(count); };
        int allowance = Math.min(first.remaining(10, 16), second.remaining(10, 16));
        Stack simulated = ItemTransfer.insert(INPUT, true, allowance, STACKS, inventory::insert, charge);
        check(simulated.count() == 48 && inventory.count == 0 && first.used() == 0, "Simulation mutated inventory/budget");
        inventory.capacity = 7; // Execution need not agree with the preceding simulation.
        Stack remainder = ItemTransfer.insert(INPUT, false, allowance, STACKS, inventory::insert, charge);
        check(remainder.count() == 57 && inventory.count == 7 && first.used() == 7 && second.used() == 7, "Partial acceptance accounting");
        check(remainder.tags().equals(INPUT.tags()) && INPUT.count() == 64, "Input metadata/stack changed");
        check(inventory.lastRequest == 16, "Unbounded offer");
        Stack extracted = ItemTransfer.extract(100, false, first.remaining(10, 16), EMPTY, STACKS, inventory::extract, charge);
        check(extracted.count() == 7 && inventory.count == 0 && first.used() == 14, "Actual extraction accounting");
        check(second.remaining(10, 16) == 2, "Opposite endpoint bypassed shared budget");
        TransferBudget restored = new TransferBudget();
        restored.restore(first.tick(), first.used());
        check(restored.remaining(10, 16) == 2 && restored.remaining(10, 5) == 0, "Reload/config reduction reset allowance");
        check(restored.remaining(11, 16) == 16, "Next tick did not replenish");
        restored.consume(16);
        int before = inventory.calls;
        check(ItemTransfer.insert(INPUT, false, restored.remaining(11, 16), STACKS, inventory::insert, restored::consume).count() == 64, "Exhausted insert lost items");
        check(ItemTransfer.extract(-1, false, 16, EMPTY, STACKS, inventory::extract, restored::consume).count() == 0, "Negative extraction");
        check(ItemTransfer.extract(Integer.MAX_VALUE, false, 0, EMPTY, STACKS, inventory::extract, restored::consume).count() == 0, "Disabled transfer");
        check(before == inventory.calls, "Unavailable transfer called handler");

        Random random = new Random(0x5B5ACE);
        for (int iteration = 0; iteration < 25_000; iteration++) {
            inventory.capacity = 1 + random.nextInt(128);
            inventory.count = random.nextInt(inventory.capacity + 1);
            int start = inventory.count;
            int limit = random.nextInt(65);
            int offered = random.nextInt(129);
            boolean simulate = random.nextBoolean();
            int[] charged = {0};
            if (random.nextBoolean()) {
                Stack input = STACKS.copy(INPUT, offered);
                Stack left = ItemTransfer.insert(input, simulate, limit, STACKS, inventory::insert, n -> charged[0] += n);
                int moved = offered - left.count();
                check(moved >= 0 && moved <= limit && moved <= offered, "Insertion violated limit");
                check(simulate ? inventory.count == start && charged[0] == 0
                        : inventory.count + left.count() == start + offered && charged[0] == moved, "Insertion conservation");
                check(left.tags().equals(input.tags()), "Remainder lost tags");
            } else {
                Stack result = ItemTransfer.extract(offered, simulate, limit, EMPTY, STACKS, inventory::extract, n -> charged[0] += n);
                check(result.count() <= Math.min(limit, offered), "Extraction violated limit");
                check(simulate ? inventory.count == start && charged[0] == 0
                        : inventory.count + result.count() == start && charged[0] == result.count(), "Extraction conservation");
            }
        }
        check(ItemCallGuard.call(0, () -> ItemCallGuard.call(0, () -> 99)) == 0, "Recursive proxy not blocked");
        try {
            ItemCallGuard.call(0, () -> { throw new IllegalStateException("provider failure"); });
            throw new AssertionError("Failure swallowed");
        } catch (IllegalStateException expected) { }
        check(ItemCallGuard.call(0, () -> 1) == 1, "Guard not released after failure");
        int[] calls = {0};
        try {
            ItemTransfer.insert(INPUT, false, 8, STACKS, (stack, simulation) -> {
                calls[0]++;
                throw new IllegalStateException("foreign handler threw after a possible commit");
            }, n -> { throw new AssertionError("Unknown result charged"); });
            throw new AssertionError("Mutation failure hidden");
        } catch (IllegalStateException expected) { }
        check(calls[0] == 1, "Unsafe mutation retry");
        System.out.println("Item transfer checks passed: 25,000 conservation cases, simulation/execution divergence, tags, partial acceptance, paired limits, reload, disabled transfers and recursion.");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
