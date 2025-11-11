
package simulation;

import org.junit.Before;
import org.junit.Test;

import main.java.simulation.events.MetricsCollector;
import main.java.simulation.model.MetricEntry;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MetricsCollectorTest {
    private MetricsCollector collector;

    @Before
    public void setUp() {
        collector = new MetricsCollector(1000);
    }

    @Test
    public void testMetricRecording() {
        MetricEntry entry = new MetricEntry(0.0, "VM_CREATION", "VM_1");
        entry.setCost(1.5);
        
        collector.recordMetric(entry);
        
        try {
            Thread.sleep(100); // Wait for async processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        List<MetricEntry> metrics = collector.getMetricsForEvent("VM_CREATION");
        assertFalse("Metrics should not be empty", metrics.isEmpty());
    }

    @Test
    public void testConcurrentMetricRecording() throws InterruptedException {
        int threadCount = 10;
        int metricsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                for (int i = 0; i < metricsPerThread; i++) {
                    MetricEntry entry = new MetricEntry(
                        System.currentTimeMillis() / 1000.0,
                        "TASK_COMPLETION",
                        "TASK_" + Thread.currentThread().getId() + "_" + i
                    );
                    collector.recordMetric(entry);
                }
                latch.countDown();
            }).start();
        }

        latch.await(10, TimeUnit.SECONDS);
        Thread.sleep(500); // Wait for async processing

        int totalMetrics = collector.getTotalMetricsCount();
        assertTrue("Should have collected metrics", totalMetrics > 0);
    }

    @Test
    public void testMetricEventTypeGrouping() {
        collector.recordMetric(new MetricEntry(0.0, "VM_CREATION", "VM_1"));
        collector.recordMetric(new MetricEntry(1.0, "VM_CREATION", "VM_2"));
        collector.recordMetric(new MetricEntry(2.0, "TASK_SUBMISSION", "TASK_1"));

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<MetricEntry> vmCreations = collector.getMetricsForEvent("VM_CREATION");
        List<MetricEntry> taskSubmissions = collector.getMetricsForEvent("TASK_SUBMISSION");

        assertTrue("Should have 2+ VM creation metrics", vmCreations.size() >= 2);
        assertTrue("Should have 1+ task submission metrics", taskSubmissions.size() >= 1);
    }
}