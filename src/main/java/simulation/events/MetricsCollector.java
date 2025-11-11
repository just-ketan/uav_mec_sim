
package simulation.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.model.MetricEntry;

import java.util.*;
import java.util.concurrent.*;

public class MetricsCollector {
    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);
    
    private final ConcurrentHashMap<String, List<MetricEntry>> metrics;
    private final BlockingQueue<MetricEntry> eventQueue;
    private final ScheduledExecutorService executor;
    private volatile boolean running = true;

    public MetricsCollector(int queueCapacity) {
        this.metrics = new ConcurrentHashMap<>();
        this.eventQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.executor = Executors.newScheduledThreadPool(2);
        initializeMetricsStorage();
        startAsyncProcessor();
    }

    private void initializeMetricsStorage() {
        metrics.put("VM_CREATION", Collections.synchronizedList(new ArrayList<>()));
        metrics.put("VM_DELETION", Collections.synchronizedList(new ArrayList<>()));
        metrics.put("TASK_SUBMISSION", Collections.synchronizedList(new ArrayList<>()));
        metrics.put("TASK_COMPLETION", Collections.synchronizedList(new ArrayList<>()));
        metrics.put("TASK_FAILED", Collections.synchronizedList(new ArrayList<>()));
        metrics.put("OPTIMIZATION_DECISION", Collections.synchronizedList(new ArrayList<>()));
    }

    private void startAsyncProcessor() {
        executor.submit(() -> {
            while (running) {
                try {
                    MetricEntry entry = eventQueue.poll(1, TimeUnit.SECONDS);
                    if (entry != null) {
                        storeMetric(entry);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public void recordMetric(MetricEntry entry) {
        try {
            eventQueue.put(entry);
        } catch (InterruptedException e) {
            logger.warn("Failed to queue metric: {}", entry);
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void storeMetric(MetricEntry entry) {
        String eventType = entry.getEventType();
        if (metrics.containsKey(eventType)) {
            metrics.get(eventType).add(entry);
        } else {
            List<MetricEntry> newList = Collections.synchronizedList(new ArrayList<>());
            newList.add(entry);
            metrics.put(eventType, newList);
        }
    }

    public List<MetricEntry> getAllMetrics() {
        List<MetricEntry> allMetrics = new ArrayList<>();
        for (List<MetricEntry> list : metrics.values()) {
            allMetrics.addAll(list);
        }
        return allMetrics;
    }

    public List<MetricEntry> getMetricsForEvent(String eventType) {
        return new ArrayList<>(metrics.getOrDefault(eventType, new ArrayList<>()));
    }

    public void shutdown() {
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public int getTotalMetricsCount() {
        return metrics.values().stream().mapToInt(List::size).sum();
    }
}
