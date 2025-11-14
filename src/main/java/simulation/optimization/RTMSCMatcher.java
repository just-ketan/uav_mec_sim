package simulation.optimization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.model.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * R-TMSC (Reverse Three-sided Many-to-one with Cyclic Preferences) Matching Algorithm
 * Implements the three-sided matching mechanism from the paper for IoT-UAV-ES association
 */
public class RTMSCMatcher {
    private static final Logger logger = LoggerFactory.getLogger(RTMSCMatcher.class);

    public static class MatchingTriplet {
        public final Task iot;
        public final UAVEntity uav;
        public final MECServer mecServer;
        public final double sinrIotUav;
        public final double profit;
        public final double latency;

        public MatchingTriplet(Task iot, UAVEntity uav, MECServer mec, 
                              double sinr, double latency, double profit) {
            this.iot = iot;
            this.uav = uav;
            this.mecServer = mec;
            this.sinrIotUav = sinr;
            this.latency = latency;
            this.profit = profit;
        }

        @Override
        public String toString() {
            return String.format("Match{IoT:%s, UAV:%s, MEC:%s, SINR:%.2f, Profit:$%.2f}",
                    iot.getId(), uav.getUavId(), mecServer.getServerId(), sinrIotUav, profit);
        }
    }

    private static class PreferenceLists {
        Map<String, List<UAVEntity>> iotPreferences = new HashMap<>();
        Map<String, List<MECServer>> uavPreferences = new HashMap<>();
        Map<String, List<Task>> esPreferences = new HashMap<>();
    }

    private final CostModel costModel;
    private final double revenueParameter;
    private final double costParameter;
    private final double sinrThreshold;
    private final int maxIterations;

    public RTMSCMatcher(CostModel costModel, double revenueParam, double costParam, 
                       double sinrThreshold, int maxIterations) {
        this.costModel = costModel;
        this.revenueParameter = revenueParam;
        this.costParameter = costParam;
        this.sinrThreshold = sinrThreshold;
        this.maxIterations = maxIterations;
    }

    /**
     * Main R-TMSC algorithm: Find stable matching
     */
    public List<MatchingTriplet> findStableMatching(List<Task> tasks,
                                                    List<UAVEntity> uavs,
                                                    List<MECServer> servers,
                                                    Map<String, double[]> iotPositions) {
        logger.debug("Starting R-TMSC with {} tasks, {} UAVs, {} servers",
                tasks.size(), uavs.size(), servers.size());

        // Reset UAV loads
        uavs.forEach(UAVEntity::resetLoad);

        // Step 1: Generate preference lists
        PreferenceLists prefs = generatePreferenceLists(tasks, uavs, servers, iotPositions);

        // Step 2: Initialize matching
        Set<MatchingTriplet> matchingSet = new HashSet<>();
        Map<String, MatchingTriplet> currentMatches = new HashMap<>();

        // Step 3: Iterative matching with convergence detection
        int iteration = 0;
        boolean converged = false;

        while (!converged && iteration < maxIterations) {
            converged = true;
            iteration++;

            for (Task iot : tasks) {
                // Skip if already matched
                if (currentMatches.containsKey(iot.getId())) {
                    continue;
                }

                // Get available UAVs in preference order
                List<UAVEntity> availableUAVs = prefs.iotPreferences.get(iot.getId())
                    .stream()
                    .filter(UAVEntity::hasCapacity)
                    .collect(Collectors.toList());

                if (availableUAVs.isEmpty()) {
                    logger.trace("No available UAVs for task {}", iot.getId());
                    continue;
                }

                // Select top-preferred UAV
                UAVEntity selectedUAV = availableUAVs.get(0);

                // Get available ES for this UAV, feasible for task
                List<MECServer> availableES = prefs.uavPreferences.get(selectedUAV.getUavId())
                    .stream()
                    .filter(es -> canAcceptTask(iot, es, iotPositions))
                    .collect(Collectors.toList());

                if (availableES.isEmpty()) {
                    logger.trace("No available ES for UAV {} and task {}", 
                            selectedUAV.getUavId(), iot.getId());
                    continue;
                }

                // Select top-preferred ES
                MECServer selectedES = availableES.get(0);

                // Calculate metrics
                double[] iotPos = iotPositions.get(iot.getId());
                double dist3D = selectedUAV.getDistance3D(iotPos[0], iotPos[1]);
                double elevAngle = selectedUAV.getElevationAngle(iotPos[0], iotPos[1]);
                double sinr = CommunicationModel.calculateSINR(dist3D, elevAngle, 0);

                // SINR threshold check
                if (sinr < sinrThreshold) {
                    logger.trace("SINR {:.2f} below threshold {:.2f} for task {}", 
                            sinr, sinrThreshold, iot.getId());
                    continue;
                }

                // Calculate latency and profit
                double latency = calculateTotalLatency(iot, selectedUAV, selectedES, iotPos);
                double profit = calculateProfit(iot, selectedUAV, selectedES, iotPos, sinr);

                // Check deadline constraint
                if (latency > iot.getDeadline()) {
                    logger.trace("Latency {:.2f}s exceeds deadline {:.2f}s for task {}",
                            latency, iot.getDeadline(), iot.getId());
                    continue;
                }

                // Create and store match
                MatchingTriplet match = new MatchingTriplet(iot, selectedUAV, selectedES, 
                                                           sinr, latency, profit);
                matchingSet.add(match);
                currentMatches.put(iot.getId(), match);
                selectedUAV.incrementLoad();

                logger.trace("Matched: {}", match);
                converged = false;
            }
        }

        logger.info("R-TMSC converged after {} iterations with {} matches", 
                   iteration, matchingSet.size());
        return new ArrayList<>(matchingSet);
    }

    /**
     * Generate preference lists for all three agent types
     */
    private PreferenceLists generatePreferenceLists(List<Task> tasks,
                                                    List<UAVEntity> uavs,
                                                    List<MECServer> servers,
                                                    Map<String, double[]> iotPositions) {
        PreferenceLists prefs = new PreferenceLists();

        // IoT→UAV preference: descending SINR
        for (Task iot : tasks) {
            double[] pos = iotPositions.get(iot.getId());
            List<UAVEntity> sortedUAVs = uavs.stream()
                .sorted((u1, u2) -> {
                    double sinr1 = calculateSINR(u1, pos[0], pos[1]);
                    double sinr2 = calculateSINR(u2, pos[0], pos[1]);
                    return Double.compare(sinr2, sinr1); // Descending SINR
                })
                .collect(Collectors.toList());
            prefs.iotPreferences.put(iot.getId(), sortedUAVs);
            logger.trace("IoT {} preferences: top UAV has SINR {:.2f}",
                    iot.getId(), calculateSINR(sortedUAVs.get(0), pos[0], pos[1]));
        }

        // UAV→ES preference: ascending MIPS (lower cost for SP)
        for (UAVEntity uav : uavs) {
            List<MECServer> sortedES = servers.stream()
                .sorted(Comparator.comparingInt(MECServer::getMipsCapacity))
                .collect(Collectors.toList());
            prefs.uavPreferences.put(uav.getUavId(), sortedES);
        }

        // ES→IoT preference: ascending task complexity (Dm * Cm)
        for (MECServer es : servers) {
            List<Task> sortedTasks = tasks.stream()
                .sorted((t1, t2) -> {
                    double complexity1 = t1.getDataSizeKB() * t1.getComputeMI();
                    double complexity2 = t2.getDataSizeKB() * t2.getComputeMI();
                    return Double.compare(complexity1, complexity2); // Ascending complexity
                })
                .collect(Collectors.toList());
            prefs.esPreferences.put(es.getServerId(), sortedTasks);
        }

        return prefs;
    }

    /**
     * Calculate SINR between IoT and UAV
     */
    private double calculateSINR(UAVEntity uav, double iotX, double iotY) {
        double dist3D = uav.getDistance3D(iotX, iotY);
        double elevAngle = uav.getElevationAngle(iotX, iotY);
        return CommunicationModel.calculateSINR(dist3D, elevAngle, 0);
    }

    /**
     * Check if ES can accept task within deadline
     */
    private boolean canAcceptTask(Task task, MECServer es, Map<String, double[]> iotPositions) {
        double procDelay = CommunicationModel.calculateProcessingDelay(
                task.getComputeMI(), es.getMipsCapacity());
        return procDelay < task.getDeadline();
    }

    /**
     * Calculate total latency: transmission + processing
     */
    private double calculateTotalLatency(Task task, UAVEntity uav, MECServer es, double[] iotPos) {
        double dist3D = uav.getDistance3D(iotPos[0], iotPos[1]);
        double elevAngle = uav.getElevationAngle(iotPos[0], iotPos[1]);
        double sinr = CommunicationModel.calculateSINR(dist3D, elevAngle, 0);
        double dataRate = CommunicationModel.calculateDataRate(sinr, 1); // 1 PRB

        double txDelay = CommunicationModel.calculateTransmissionDelay(task.getDataSizeKB(), dataRate);
        double procDelay = CommunicationModel.calculateProcessingDelay(
                task.getComputeMI(), es.getMipsCapacity());

        return txDelay + procDelay;
    }

    /**
     * Calculate SP profit: Revenue - Cost
     * Profit = v*Dm*ρm - w*Dm*t_proc (from paper)
     */
    private double calculateProfit(Task iot, UAVEntity uav, MECServer es, 
                                   double[] iotPos, double sinr) {
        double dataRate = CommunicationModel.calculateDataRate(sinr, 1); // bits/sec
        double procTime = CommunicationModel.calculateProcessingDelay(
                iot.getComputeMI(), es.getMipsCapacity());

        double revenue = revenueParameter * iot.getDataSizeKB() * dataRate / 1e6; // Mbps
        double cost = costParameter * iot.getDataSizeKB() * procTime;

        return revenue - cost;
    }
}