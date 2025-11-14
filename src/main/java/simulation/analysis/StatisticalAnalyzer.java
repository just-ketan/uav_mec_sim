
package simulation.analysis;

import java.util.*;
import java.util.stream.Collectors;

import simulation.model.MetricEntry;

public class StatisticalAnalyzer {

    public static class Statistics {
        public double mean;
        public double median;
        public double stdDev;
        public double min;
        public double max;
        public int count;

        public Statistics(double mean, double median, double stdDev, 
                         double min, double max, int count) {
            this.mean = mean;
            this.median = median;
            this.stdDev = stdDev;
            this.min = min;
            this.max = max;
            this.count = count;
        }

        @Override
        public String toString() {
            return String.format(
                "Stats{mean=%.4f, median=%.4f, stdDev=%.4f, min=%.4f, max=%.4f, count=%d}",
                mean, median, stdDev, min, max, count
            );
        }
    }

    public static Statistics analyzeMetric(List<Double> values) {
        if (values.isEmpty()) {
            return new Statistics(0, 0, 0, 0, 0, 0);
        }

        // Mean
        double mean = values.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

        // Median
        List<Double> sorted = values.stream()
            .sorted()
            .collect(Collectors.toList());
        double median = sorted.size() % 2 == 0 ?
            (sorted.get(sorted.size() / 2 - 1) + sorted.get(sorted.size() / 2)) / 2.0 :
            sorted.get(sorted.size() / 2);

        // Standard Deviation
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        double stdDev = Math.sqrt(variance);

        // Min and Max
        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        return new Statistics(mean, median, stdDev, min, max, values.size());
    }

    public static void analyzeAndPrintMetrics(List<MetricEntry> metrics) {
        System.out.println("\n=== Statistical Analysis ===\n");

        // Cost analysis
        List<Double> costs = metrics.stream()
            .map(MetricEntry::getCost)
            .collect(Collectors.toList());
        System.out.println("Cost Statistics: " + analyzeMetric(costs));

        // Latency analysis
        List<Double> latencies = metrics.stream()
            .map(MetricEntry::getLatency)
            .collect(Collectors.toList());
        System.out.println("Latency Statistics: " + analyzeMetric(latencies));

        // Energy analysis
        List<Double> energies = metrics.stream()
            .map(MetricEntry::getEnergyConsumption)
            .collect(Collectors.toList());
        System.out.println("Energy Statistics: " + analyzeMetric(energies));

        // Event distribution
        Map<String, Long> eventDist = metrics.stream()
            .collect(Collectors.groupingBy(
                MetricEntry::getEventType,
                Collectors.counting()
            ));
        System.out.println("\nEvent Distribution:");
        eventDist.forEach((event, count) -> 
            System.out.printf("  %s: %d\n", event, count)
        );
    }
}
