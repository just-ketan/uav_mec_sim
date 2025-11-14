package simulation.events;

import simulation.model.MetricEntry;
import java.util.*;
import java.util.concurrent.*;

public class MetricsCollector {

    private final ConcurrentMap<String, List<MetricEntry>> metrics = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public MetricsCollector(int capacity) {}

    public void recordMetric(String type, long id, boolean success) {
        MetricEntry entry = new MetricEntry(type, id, success, System.currentTimeMillis());

        executor.submit(() -> {
            metrics.computeIfAbsent(type, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(entry);
        });
    }

    public List<MetricEntry> getAllMetrics() {
        List<MetricEntry> list = new ArrayList<>();
        metrics.values().forEach(list::addAll);
        return list;
    }

    public int getTotalMetricsCount() {
        return metrics.values().stream().mapToInt(List::size).sum();
    }

    public void shutdown() {
        executor.shutdown();
    }
}
