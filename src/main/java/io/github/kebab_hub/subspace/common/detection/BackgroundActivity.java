package io.github.kebab_hub.subspace.common.detection;

/** A bounded ambient signal, independent of inventories, research, or network topology. */
public final class BackgroundActivity {
    public static final int SAMPLE_INTERVAL_TICKS = 100;

    public enum Band {
        QUIET, STEADY, ELEVATED
    }

    private BackgroundActivity() {
    }

    public static int sample(long worldSeed, String dimension, int x, int y, int z, long gameTime) {
        long location = mix(worldSeed ^ dimension.hashCode());
        location = mix(location ^ x);
        location = mix(location ^ y);
        location = mix(location ^ z);
        return (int) Math.floorMod(mix(location ^ Math.floorDiv(gameTime, SAMPLE_INTERVAL_TICKS)), 101L);
    }

    public static Band band(int reading) {
        if (reading < 0 || reading > 100) {
            throw new IllegalArgumentException("Activity must be between 0 and 100");
        }
        return reading < 34 ? Band.QUIET : reading < 67 ? Band.STEADY : Band.ELEVATED;
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }
}
