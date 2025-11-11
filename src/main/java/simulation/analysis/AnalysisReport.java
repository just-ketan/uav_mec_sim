
package simulation.analysis;

import java.util.*;

import simulation.model.MetricEntry;

public class AnalysisReport {
    private List<MetricEntry> metrics;
    private StatisticalAnalyzer.Statistics costStats;
    private StatisticalAnalyzer.Statistics latencyStats;
    private StatisticalAnalyzer.Statistics energyStats;
    private double taskCompletionRate;
    private double slaViolationRate;
    private double costReduction;
    private long totalSimulationTime;

    public AnalysisReport(List<MetricEntry> metrics) {
        this.metrics = metrics;
    }

    public void generateAnalysis() {
        // Cost analysis
        List<Double> costs = new ArrayList<>();
        List<Double> latencies = new ArrayList<>();
        List<Double> energies = new ArrayList<>();

        for (MetricEntry metric : metrics) {
            if (metric.getCost() > 0) costs.add(metric.getCost());
            if (metric.getLatency() > 0) latencies.add(metric.getLatency());
            if (metric.getEnergyConsumption() > 0) energies.add(metric.getEnergyConsumption());
        }

        this.costStats = StatisticalAnalyzer.analyzeMetric(costs);
        this.latencyStats = StatisticalAnalyzer.analyzeMetric(latencies);
        this.energyStats = StatisticalAnalyzer.analyzeMetric(energies);

        // Completion rate
        long successCount = metrics.stream().filter(MetricEntry::isSuccessful).count();
        this.taskCompletionRate = (double) successCount / metrics.size() * 100;

        // SLA violations (placeholder - implement based on your SLA definition)
        this.slaViolationRate = 0.0;
    }

    public String generateHTMLReport() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>UAV-MEC Simulation Report</title></head><body>\n");
        html.append("<h1>Simulation Analysis Report</h1>\n");
        
        html.append("<h2>Cost Analysis</h2>\n");
        html.append(String.format("<p>Mean Cost: $%.4f</p>\n", costStats.mean));
        html.append(String.format("<p>Median Cost: $%.4f</p>\n", costStats.median));
        html.append(String.format("<p>Std Dev: $%.4f</p>\n", costStats.stdDev));

        html.append("<h2>Latency Analysis</h2>\n");
        html.append(String.format("<p>Mean Latency: %.2f ms</p>\n", latencyStats.mean));
        html.append(String.format("<p>Max Latency: %.2f ms</p>\n", latencyStats.max));

        html.append("<h2>Success Rates</h2>\n");
        html.append(String.format("<p>Task Completion: %.2f%%</p>\n", taskCompletionRate));
        html.append(String.format("<p>SLA Compliance: %.2f%%</p>\n", 100 - slaViolationRate));

        html.append("</body></html>\n");
        return html.toString();
    }

    // Getters
    public StatisticalAnalyzer.Statistics getCostStats() { return costStats; }
    public StatisticalAnalyzer.Statistics getLatencyStats() { return latencyStats; }
    public StatisticalAnalyzer.Statistics getEnergyStats() { return energyStats; }
    public double getTaskCompletionRate() { return taskCompletionRate; }
    public double getSLAViolationRate() { return slaViolationRate; }
}
