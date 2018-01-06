package kic.kafka.pipelet.bolts.util;

public class Try {
    public static void ignore(Runnable runnable) {
        ignore(true, runnable);
    }

    public static void ignore(boolean condition, Runnable runnable) {
        try {
            if (condition) runnable.run();
        } catch (Exception ignore) {}
    }
}
