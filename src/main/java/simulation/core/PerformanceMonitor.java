
package simulation.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceMonitor {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);

    private long simulationStartTime;
    private long simulationEndTime;
    private double simulationTime; // Virtual time
    private long metricsCollected = 0;

    public void startSimulationTimer() {
        simulationStartTime = System.nanoTime();
        logger.info("Performance monitoring started");
    }

    public void endSimulationTimer(double virtualTime) {
        simulationEndTime = System.nanoTime();
        simulationTime = virtualTime;
    }

    public void recordMetricsCollected(long count) {
        metricsCollected = count;
    }

    public void printPerformanceReport() {
        long durationNanos = simulationEndTime - simulationStartTime;
        long durationMs = durationNanos / 1_000_000;
        long durationSeconds = durationMs / 1_000;

        double speedupRatio = simulationTime / Math.max(durationSeconds, 0.001);

        System.out.println("\n=== Performance Report ===");
        System.out.printf("Wall-clock time: %d ms (%.2f s)\n", durationMs, durationSeconds);
        System.out.printf("Simulation time: %.2f seconds\n", simulationTime);
        System.out.printf("Speed-up ratio: %.2fx\n", speedupRatio);
        System.out.printf("Metrics collected: %d\n", metricsCollected);
        System.out.printf("Metrics/second: %.0f\n", metricsCollected / Math.max(durationSeconds, 0.001));
        System.out.println("========================\n");

        logger.info("Performance: {}x speedup, {} metrics collected", speedupRatio, metricsCollected);
    }
}
