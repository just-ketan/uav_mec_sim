
package simulation.optimization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.events.MetricsCollector;
import simulation.model.*;

import java.util.*;

public class CostOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(CostOptimizer.class);
    
    private final CostModel costModel;
    private final MetricsCollector metricsCollector;
    private int optimizationDecisionsCount = 0;
    private double totalCostSaved = 0.0;

    public CostOptimizer(CostModel costModel, MetricsCollector metricsCollector) {
        this.costModel = costModel;
        this.metricsCollector = metricsCollector;
    }

    public static class OffloadingDecision {
        public MECServer selectedServer;
        public double estimatedCost;
        public double estimatedLatency;
        public boolean isDeadlineSafe;

        public OffloadingDecision(MECServer server, double cost, double latency, 
                                 boolean deadlineSafe) {
            this.selectedServer = server;
            this.estimatedCost = cost;
            this.estimatedLatency = latency;
            this.isDeadlineSafe = deadlineSafe;
        }

        @Override
        public String toString() {
            return String.format(
                "Decision{server=%s, cost=%.4f, latency=%.2f, safe=%s}",
                selectedServer != null ? selectedServer.getServerId() : "NULL",
                estimatedCost, estimatedLatency, isDeadlineSafe
            );
        }
    }

    /**
     * Main optimization function: Select best server for task
     */
    public OffloadingDecision optimizeTaskOffloading(Task task, List<MECServer> servers) {
        if (servers == null || servers.isEmpty()) {
            logger.warn("No servers available for task: {}", task.getTaskId());
            return new OffloadingDecision(null, Double.MAX_VALUE, 0, false);
        }

        double minCost = Double.MAX_VALUE;
        MECServer bestServer = null;
        double bestLatency = 0;
        boolean bestIsSafe = false;

        for (MECServer server : servers) {
            // Check if server can accommodate task
            if (!server.isActive() || !server.canAccommodateTask(task)) {
                continue;
            }

            // Estimate latency (simplified: proportional to task size and server capacity)
            double estimatedLatency = estimateLatency(task, server);

            // Check deadline
            boolean isDeadlineSafe = estimatedLatency <= task.getDeadline();

            // Calculate cost
            double estimatedEnergy = estimateEnergy(task, server);
            CostModel.CostCalculation costCalc = costModel.calculateTaskCost(
                task.getComputeRequirement(),
                task.getDataSize(),
                estimatedLatency,
                task.getDeadline(),
                estimatedEnergy
            );

            double totalCost = costCalc.totalCost;

            // Prefer deadline-safe options; among those, pick minimum cost
            if (isDeadlineSafe) {
                if (totalCost < minCost) {
                    minCost = totalCost;
                    bestServer = server;
                    bestLatency = estimatedLatency;
                    bestIsSafe = true;
                }
            } else if (bestServer == null) {
                // Fallback: if no deadline-safe option, pick cheapest
                if (totalCost < minCost) {
                    minCost = totalCost;
                    bestServer = server;
                    bestLatency = estimatedLatency;
                    bestIsSafe = false;
                }
            }
        }

        OffloadingDecision decision = new OffloadingDecision(
            bestServer, minCost, bestLatency, bestIsSafe
        );

        recordOptimizationDecision(task, decision);
        return decision;
    }

    /**
     * Estimate latency for task on server (simplified model)
     */
    private double estimateLatency(Task task, MECServer server) {
        // Latency = (task_compute_req / server_cpu_capacity) + network_delay
        double computeLatency = (task.getComputeRequirement() / server.getCpuCapacity()) * 1000; // ms
        double networkDelay = 5.0; // 5ms network overhead
        double queueDelay = server.getResourceUtilization() * 10; // Queue delay proportional to utilization
        
        return computeLatency + networkDelay + queueDelay;
    }

    /**
     * Estimate energy consumption for task (simplified model)
     */
    private double estimateEnergy(Task task, MECServer server) {
        // Energy = power * time
        double computeTime = task.getComputeRequirement() / server.getCpuCapacity();
        double serverPower = 50.0; // Watts (typical MEC server)
        return serverPower * computeTime; // Joules
    }

    /**
     * Record optimization decision to metrics
     */
    private void recordOptimizationDecision(Task task, OffloadingDecision decision) {
        MetricEntry entry = new MetricEntry(
            System.currentTimeMillis() / 1000.0,
            "OPTIMIZATION_DECISION",
            task.getTaskId()
        );
        entry.setCost(decision.estimatedCost);
        entry.setLatency(decision.estimatedLatency);
        entry.addCustomValue("selectedServer", 
            decision.selectedServer != null ? decision.selectedServer.getServerId() : "NONE");
        entry.addCustomValue("isDeadlineSafe", decision.isDeadlineSafe);
        
        metricsCollector.recordMetric(entry);
        
        optimizationDecisionsCount++;
        logger.info("Optimization Decision #{}: {} -> Cost: $%.4f", 
            optimizationDecisionsCount, task.getTaskId(), decision.estimatedCost);
    }

    /**
     * Periodically optimize VM allocation based on current metrics
     */
    public void optimizeVMAllocation(List<MECServer> servers) {
        logger.info("Running VM allocation optimization across {} servers", servers.size());
        
        for (MECServer server : servers) {
            if (server.getResourceUtilization() > 0.9) {
                logger.warn("Server {} is overutilized ({}%)", 
                    server.getServerId(), server.getResourceUtilization() * 100);
            }
        }
    }

    // Getters
    public int getOptimizationDecisionsCount() { return optimizationDecisionsCount; }
    public double getTotalCostSaved() { return totalCostSaved; }
    public void recordCostSavings(double savings) { totalCostSaved += savings; }
}
