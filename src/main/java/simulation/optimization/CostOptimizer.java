package simulation.optimization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.events.MetricsCollector;
import simulation.model.*;
import java.util.*;

/**
 * Cost Optimizer: Manages offloading decisions and profit optimization
 * Integrates R-TMSC matching and provides CloudSim-compatible API
 */
public class CostOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(CostOptimizer.class);

    private final CostModel costModel;
    private final MetricsCollector metrics;
    private final RTMSCMatcher matcher;
    
    public final List<Task> taskQueue = new ArrayList<>();
    public final List<MECServer> servers = new ArrayList<>();
    public final List<UAVEntity> uavs = new ArrayList<>();
    public final Map<String, double[]> iotPositions = new HashMap<>();

    private int optimizationDecisions = 0;
    private double totalProfit = 0;

    public CostOptimizer(CostModel model, MetricsCollector collector) {
        this.costModel = model;
        this.metrics = collector;
        
        // Initialize R-TMSC matcher with paper parameters
        this.matcher = new RTMSCMatcher(model, 0.1, 0.01, 10.0, 100);
    }

    public void registerTask(Task task) {
        taskQueue.add(task);
    }

    public void registerServer(MECServer server) {
        servers.add(server);
    }

    public void registerUAV(UAVEntity uav) {
        uavs.add(uav);
    }

    public void registerIoTPosition(String taskId, double x, double y) {
        iotPositions.put(taskId, new double[]{x, y});
    }

    /**
     * Find stable matching using R-TMSC algorithm
     */
    public List<RTMSCMatcher.MatchingTriplet> findStableMatching(List<Task> tasks,
                                                                  List<UAVEntity> uavEntities,
                                                                  List<MECServer> mecServers,
                                                                  Map<String, double[]> positions) {
        if (tasks.isEmpty() || uavEntities.isEmpty() || mecServers.isEmpty()) {
            logger.warn("Cannot optimize: missing tasks, UAVs, or servers");
            return Collections.emptyList();
        }

        try {
            List<RTMSCMatcher.MatchingTriplet> matching = 
                matcher.findStableMatching(tasks, uavEntities, mecServers, positions);

            totalProfit = matching.stream()
                .mapToDouble(m -> m.profit)
                .sum();

            optimizationDecisions += matching.size();

            logger.info("R-TMSC optimization complete: {} matches, Total Profit: ${:.2f}",
                    matching.size(), totalProfit);

            return matching;
        } catch (Exception e) {
            logger.error("R-TMSC optimization failed", e);
            return Collections.emptyList();
        }
    }

    /**
     * Single task offloading decision (for AdaptivePolicy compatibility)
     */
    public OffloadingDecision optimizeTaskOffloading(Task task, List<MECServer> availableServers) {
        if (uavs.isEmpty() || availableServers.isEmpty()) {
            return new OffloadingDecision(null, null, 0, Double.MAX_VALUE, false, 0);
        }

        double[] iotPos = iotPositions.getOrDefault(task.getId(), new double[]{0, 0});

        UAVEntity bestUAV = null;
        MECServer bestES = null;
        double bestProfit = Double.NEGATIVE_INFINITY;
        double bestLatency = Double.MAX_VALUE;

        for (UAVEntity uav : uavs) {
            if (!uav.hasCapacity()) continue;

            double dist3D = uav.getDistance3D(iotPos[0], iotPos[1]);
            double elevAngle = uav.getElevationAngle(iotPos[0], iotPos[1]);
            double sinr = CommunicationModel.calculateSINR(dist3D, elevAngle, 0);

            if (sinr < 10.0) continue;

            double dataRate = CommunicationModel.calculateDataRate(sinr, 1);
            double txDelay = CommunicationModel.calculateTransmissionDelay(
                task.getDataSizeKB(), dataRate);

            for (MECServer es : availableServers) {
                double procDelay = CommunicationModel.calculateProcessingDelay(
                    task.getComputeMI(), es.getMipsCapacity());
                double totalLatency = txDelay + procDelay;

                if (totalLatency > task.getDeadline()) continue;

                double revenue = 0.1 * task.getDataSizeKB() * dataRate / 1e6;
                double cost = 0.01 * task.getDataSizeKB() * procDelay;
                double profit = revenue - cost;

                if (profit > bestProfit) {
                    bestProfit = profit;
                    bestUAV = uav;
                    bestES = es;
                    bestLatency = totalLatency;
                }
            }
        }

        boolean deadlineSafe = bestLatency <= task.getDeadline();
        double estimatedCost = bestProfit > 0 ? 0.01 * task.getDataSizeKB() * bestLatency : 0;

        return new OffloadingDecision(bestUAV, bestES, estimatedCost, 
                                     bestLatency, deadlineSafe, bestProfit);
    }

    // ===== Getters =====
    public int getOptimizationDecisionsCount() { return optimizationDecisions; }
    public double getTotalProfit() { return totalProfit; }
    public double getComputeCost() { return costModel.getComputeCost(); }
    public double getBandwidthCost() { return costModel.getBandwidthCost(); }
    public double getLatencyPenalty() { return costModel.getLatencyPenalty(); }
    public double getEnergyCost() { return costModel.getEnergyCost(); }

    /**
     * Offloading decision result
     */
    public static class OffloadingDecision {
        public final UAVEntity selectedUAV;
        public final MECServer selectedServer;
        public final double estimatedCost;
        public final double estimatedLatency;
        public final boolean isDeadlineSafe;
        public final double expectedProfit;

        public OffloadingDecision(UAVEntity uav, MECServer server, double cost,
                                 double latency, boolean safe, double profit) {
            this.selectedUAV = uav;
            this.selectedServer = server;
            this.estimatedCost = cost;
            this.estimatedLatency = latency;
            this.isDeadlineSafe = safe;
            this.expectedProfit = profit;
        }

        public boolean isValid() {
            return selectedUAV != null && selectedServer != null && isDeadlineSafe;
        }

        @Override
        public String toString() {
            return String.format("OffloadingDecision{UAV:%s, Server:%s, Latency:%.2fs, Profit:$%.2f, Safe:%b}",
                    selectedUAV != null ? selectedUAV.getUavId() : "null",
                    selectedServer != null ? selectedServer.getServerId() : "null",
                    estimatedLatency, expectedProfit, isDeadlineSafe);
        }
    }
}