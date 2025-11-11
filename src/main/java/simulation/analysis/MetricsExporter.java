
package simulation.analysis;

import com.opencsv.CSVWriter;

import simulation.model.MetricEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MetricsExporter {
    private static final Logger logger = LoggerFactory.getLogger(MetricsExporter.class);

    public static void exportToCSV(String filename, List<MetricEntry> metrics) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
            // Write header
            String[] header = {
                "Timestamp", "EventType", "EntityId", 
                "Cost", "Latency", "Energy", "Successful", "CustomValues"
            };
            writer.writeNext(header);

            // Write metrics
            for (MetricEntry metric : metrics) {
                writer.writeNext(metric.toCSVRow());
            }

            logger.info("Exported {} metrics to {}", metrics.size(), filename);
        } catch (IOException e) {
            logger.error("Failed to export metrics to CSV", e);
            throw new RuntimeException("CSV export failed", e);
        }
    }

    public static void exportToJSON(String filename, List<MetricEntry> metrics) {
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(metrics, writer);
            logger.info("Exported {} metrics to {}", metrics.size(), filename);
        } catch (IOException e) {
            logger.error("Failed to export metrics to JSON", e);
            throw new RuntimeException("JSON export failed", e);
        }
    }

    public static String generateSummaryReport(List<MetricEntry> metrics) {
        StringBuilder report = new StringBuilder();
        report.append("=== Simulation Metrics Summary ===\n\n");
        report.append(String.format("Total Metrics: %d\n", metrics.size()));
        
        // Count by event type
        java.util.Map<String, Long> eventCounts = metrics.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                MetricEntry::getEventType, 
                java.util.stream.Collectors.counting()
            ));
        
        report.append("\nEvent Distribution:\n");
        eventCounts.forEach((event, count) -> 
            report.append(String.format("  %s: %d\n", event, count))
        );

        // Cost statistics
        double totalCost = metrics.stream()
            .mapToDouble(MetricEntry::getCost)
            .sum();
        double avgCost = metrics.stream()
            .mapToDouble(MetricEntry::getCost)
            .average()
            .orElse(0.0);

        report.append(String.format("\nCost Analysis:\n"));
        report.append(String.format("  Total Cost: $%.4f\n", totalCost));
        report.append(String.format("  Average Cost: $%.4f\n", avgCost));

        // Latency statistics
        double avgLatency = metrics.stream()
            .mapToDouble(MetricEntry::getLatency)
            .average()
            .orElse(0.0);

        report.append(String.format("\nLatency Analysis:\n"));
        report.append(String.format("  Average Latency: %.2f ms\n", avgLatency));

        // Success rate
        long successCount = metrics.stream()
            .filter(MetricEntry::isSuccessful)
            .count();
        double successRate = (double) successCount / metrics.size() * 100;

        report.append(String.format("\nSuccess Rate: %.2f%%\n", successRate));

        return report.toString();
    }
}
