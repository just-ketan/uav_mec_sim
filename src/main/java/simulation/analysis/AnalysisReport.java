package simulation.analysis;

import simulation.model.MetricEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Analysis Report: Generates comprehensive analysis from metrics
 */
public class AnalysisReport {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisReport.class);

    private final List<MetricEntry> metrics;
    private double costStats_mean = 0;
    private double costStats_median = 0;
    private double costStats_stdDev = 0;
    private double latencyStats_mean = 0;
    private double latencyStats_max = 0;
    private double energyStats_mean = 0;
    private double taskCompletionRate = 0;
    private double slaViolationRate = 0;

    public AnalysisReport(List<MetricEntry> metrics) {
        this.metrics = metrics;
        analyzeMetrics();
    }

    private void analyzeMetrics() {
        if (metrics.isEmpty()) return;

        // Cost analysis
        double totalCost = 0;
        int costCount = 0;
        for (MetricEntry metric : metrics) {
            if (metric.getCost() > 0) {
                totalCost += metric.getCost();
                costCount++;
            }
        }
        if (costCount > 0) costStats_mean = totalCost / costCount;

        // Latency analysis
        double totalLatency = 0;
        double maxLatency = 0;
        int latencyCount = 0;
        for (MetricEntry metric : metrics) {
            if (metric.getLatency() > 0) {
                totalLatency += metric.getLatency();
                maxLatency = Math.max(maxLatency, metric.getLatency());
                latencyCount++;
            }
        }
        if (latencyCount > 0) {
            latencyStats_mean = totalLatency / latencyCount;
            latencyStats_max = maxLatency;
        }

        // Energy analysis
        double totalEnergy = 0;
        int energyCount = 0;
        for (MetricEntry metric : metrics) {
            if (metric.getEnergyConsumption() > 0) {
                totalEnergy += metric.getEnergyConsumption();
                energyCount++;
            }
        }
        if (energyCount > 0) energyStats_mean = totalEnergy / energyCount;

        // Success rate
        long successCount = metrics.stream().filter(MetricEntry::isSuccessful).count();
        taskCompletionRate = ((double) successCount / metrics.size()) * 100;
        slaViolationRate = 100 - taskCompletionRate;

        logger.info("Analysis Report: Cost ${:.2f}, Latency {:.2f}ms, Success {:.2f}%",
                costStats_mean, latencyStats_mean, taskCompletionRate);
    }

    public String generateHTMLReport() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body><h1>UAV-MEC Simulation Report</h1>");
        html.append(String.format("<p><b>Cost Analysis</b></p>"));
        html.append(String.format("<p>Mean Cost: $%.4f</p>", costStats_mean));
        html.append(String.format("<p>Median Cost: $%.4f</p>", costStats_median));
        html.append(String.format("<p>Std Dev: $%.4f</p>", costStats_stdDev));
        html.append(String.format("<p><b>Latency Analysis</b></p>"));
        html.append(String.format("<p>Mean Latency: %.2f ms</p>", latencyStats_mean));
        html.append(String.format("<p>Max Latency: %.2f ms</p>", latencyStats_max));
        html.append(String.format("<p><b>Performance</b></p>"));
        html.append(String.format("<p>Task Completion: %.2f%%</p>", taskCompletionRate));
        html.append(String.format("<p>SLA Compliance: %.2f%%</p>", 100 - slaViolationRate));
        html.append("</body></html>");
        return html.toString();
    }

    // ===== Getters =====
    public double getCostMean() { return costStats_mean; }
    public double getLatencyMean() { return latencyStats_mean; }
    public double getLatencyMax() { return latencyStats_max; }
    public double getEnergyMean() { return energyStats_mean; }
    public double getTaskCompletionRate() { return taskCompletionRate; }
    public double getSLAViolationRate() { return slaViolationRate; }
}