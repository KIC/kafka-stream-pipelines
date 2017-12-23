package kic.kafka.pipeliet.bolts.demo;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.function.BiConsumer;

public class RandomNumberGenerator implements Runnable {
    private final NormalDistribution distribution = new NormalDistribution(0, 0.02);
    private final BiConsumer<Long, Double> callback;

    public RandomNumberGenerator(BiConsumer<Long, Double> callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        while (true) {
            try {
                callback.accept(System.currentTimeMillis(), distribution.sample());
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

}
