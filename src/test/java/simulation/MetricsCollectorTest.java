package simulation;

import org.junit.Before;
import org.junit.Test;
import simulation.events.MetricsCollector;
import simulation.model.MetricEntry;
import static org.junit.Assert.*;
import java.util.List;

public class MetricsCollectorTest {

    private MetricsCollector collector;

    @Before
    public void setUp() {
        collector = new MetricsCollector(1000);
    }

    @Test
    public void testMetricRecording() {
        MetricEntry entry = new MetricEntry("VM_CREATION", 1L, true, System.currentTimeMillis());
        entry.setCost(1.5);
        collector.recordMetric("VM_CREATION", 1L, true);
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<MetricEntry> metrics = collector.getAllMetrics();
        assertFalse("Metrics should not be empty", metrics.isEmpty());
    }

    @Test
    public void testMetricConcurrentRecording() throws InterruptedException {
        int threadCount = 5;
        int metricsPerThread = 50;
        
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                for (int i = 0; i < metricsPerThread; i++) {
                    collector.recordMetric("TASK_COMPLETION", threadId * 100 + i, true);
                }
            }).start();
        }

        Thread.sleep(1000); // Wait for async processing
        int totalMetrics = collector.getTotalMetricsCount();
        assertTrue("Should have collected metrics", totalMetrics > 0);
    }

    @Test
    public void testMetricsGrouping() {
        collector.recordMetric("VM_CREATION", 1L, true);
        collector.recordMetric("VM_CREATION", 2L, true);
        collector.recordMetric("TASK_SUBMISSION", 10L, true);
        
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<MetricEntry> allMetrics = collector.getAllMetrics();
        assertTrue("Should have collected metrics", allMetrics.size() >= 3);
    }
}