package io.github.kebab_hub.subspace.common.detection;

import java.util.HashSet;
import java.util.Set;

/** Dependency-free checks; this entrypoint never initializes Minecraft or Forge. */
public final class BackgroundActivityChecks {
    public static void main(String[] args) {
        Set<Integer> readings = new HashSet<>();
        for (int coordinate = -500; coordinate <= 500; coordinate++) {
            for (long time : new long[] {0, 99, 100, 123456789L, Long.MAX_VALUE}) {
                int reading = BackgroundActivity.sample(42, "minecraft:overworld", coordinate, -64,
                        -coordinate, time);
                check(reading >= 0 && reading <= 100, "Reading out of range");
                readings.add(reading);
                check(reading == BackgroundActivity.sample(42, "minecraft:overworld", coordinate,
                        -64, -coordinate, time), "Reading must be deterministic");
            }
        }
        check(readings.size() == 101, "The ambient signal should cover the advertised scale");
        for (int bucket = 0; bucket < 100; bucket++) {
            long first = (long) bucket * BackgroundActivity.SAMPLE_INTERVAL_TICKS;
            check(sample(first) == sample(first + BackgroundActivity.SAMPLE_INTERVAL_TICKS - 1),
                    "Reading changed within a sample interval");
        }
        Set<Integer> timeSeries = new HashSet<>();
        Set<Integer> dimensions = new HashSet<>();
        Set<Integer> seeds = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            timeSeries.add(sample((long) i * BackgroundActivity.SAMPLE_INTERVAL_TICKS));
            dimensions.add(BackgroundActivity.sample(42, "test:dimension_" + i, 1, 64, 2, 0));
            seeds.add(BackgroundActivity.sample(i, "minecraft:overworld", 1, 64, 2, 0));
        }
        check(timeSeries.size() > 20 && dimensions.size() > 20 && seeds.size() > 20,
                "Time, dimension and seed must influence the signal");
        for (int value = 0; value <= 100; value++) {
            BackgroundActivity.Band expected = value <= 33 ? BackgroundActivity.Band.QUIET
                    : value <= 66 ? BackgroundActivity.Band.STEADY : BackgroundActivity.Band.ELEVATED;
            check(BackgroundActivity.band(value) == expected, "Wrong activity band");
        }
        for (int invalid : new int[] {-1, 101, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            try {
                BackgroundActivity.band(invalid);
                throw new AssertionError("Invalid reading was accepted");
            } catch (IllegalArgumentException expected) {
                // The public classification boundary rejects invalid readings.
            }
        }
        System.out.println("Background activity checks passed: bounds, repeatability, sample timing and bands.");
    }

    private static int sample(long time) {
        return BackgroundActivity.sample(42, "minecraft:overworld", 1, 64, 2, time);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
