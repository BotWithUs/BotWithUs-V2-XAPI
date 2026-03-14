package net.botwithus.xapi.util;

import java.util.Random;
import java.util.function.BooleanSupplier;

public final class BwuTiming {

    private static final Random RANDOM = new Random();
    private static final long TICK_MS = 600L;

    private BwuTiming() {
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void sleepRandom(long min, long max) {
        sleep(min + (long) (RANDOM.nextDouble() * (max - min)));
    }

    public static void sleepTick() {
        sleep(TICK_MS);
    }

    public static void shortDelay() {
        sleepRandom(300, 600);
    }

    public static void mediumDelay() {
        sleepRandom(600, 1200);
    }

    public static void longDelay() {
        sleepRandom(1200, 2400);
    }

    public static boolean waitUntil(BooleanSupplier condition, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            if (condition.getAsBoolean()) {
                return true;
            }
            sleep(50);
        }
        return condition.getAsBoolean();
    }

    public static boolean waitWhile(BooleanSupplier condition, long timeoutMs) {
        return waitUntil(() -> !condition.getAsBoolean(), timeoutMs);
    }

    public static int random(int min, int max) {
        return min + RANDOM.nextInt(max - min + 1);
    }

    public static long gaussianRandom(long mean, long stdDev) {
        return Math.round(mean + RANDOM.nextGaussian() * stdDev);
    }
}
