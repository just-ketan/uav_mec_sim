package simulation.optimization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.model.UAVEntity;
import java.util.*;

/**
 * UAV Positioning Optimizer using K-means Clustering
 * Implements optimal UAV placement for IoT coverage as per paper
 */
public class UAVPositioningOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(UAVPositioningOptimizer.class);

    /**
     * K-means clustering for UAV positioning
     * @param iotPositions Map of IoT device positions {x, y}
     * @param maxUAVs Maximum number of UAVs available
     * @param altitude UAV flight altitude
     * @param uavCapacity Capacity of each UAV (max IoTs served)
     * @return List of optimally positioned UAV entities
     */
    public static List<UAVEntity> optimizeUAVPositions(Map<String, double[]> iotPositions,
                                                       int maxUAVs,
                                                       double altitude,
                                                       int uavCapacity) {
        if (iotPositions.isEmpty()) {
            logger.warn("No IoT positions provided, returning empty UAV list");
            return Collections.emptyList();
        }

        int M = iotPositions.size();
        
        // Calculate optimal K based on capacity
        int K = Math.max(1, Math.min(maxUAVs, (M + uavCapacity - 1) / uavCapacity));
        logger.debug("K-means clustering: {} IoTs, {} UAVs with capacity {}", M, K, uavCapacity);

        List<double[]> iotPosList = new ArrayList<>(iotPositions.values());

        // Run K-means clustering
        List<double[]> centroids = kMeansClustering(iotPosList, K, 100);

        // Create UAV entities at centroids
        List<UAVEntity> uavs = new ArrayList<>();
        for (int i = 0; i < centroids.size(); i++) {
            double[] centroid = centroids.get(i);
            UAVEntity uav = new UAVEntity("UAV_" + i, centroid[0], centroid[1],
                    altitude, uavCapacity);
            uavs.add(uav);
            logger.debug("UAV {} positioned at ({:.1f}, {:.1f}, {:.1f})",
                    i, centroid[0], centroid[1], altitude);
        }

        logger.info("K-means optimization complete: {} UAVs positioned", uavs.size());
        return uavs;
    }

    /**
     * K-means clustering algorithm
     */
    private static List<double[]> kMeansClustering(List<double[]> points, int k, int maxIterations) {
        if (points.isEmpty() || k <= 0) {
            return Collections.emptyList();
        }

        Random rand = new Random(42); // Fixed seed for reproducibility

        // Step 1: Initialize centroids randomly from points
        List<double[]> centroids = new ArrayList<>();
        Set<Integer> selectedIndices = new HashSet<>();

        while (centroids.size() < k && centroids.size() < points.size()) {
            int idx = rand.nextInt(points.size());
            if (!selectedIndices.contains(idx)) {
                centroids.add(points.get(idx).clone());
                selectedIndices.add(idx);
            }
        }

        // Pad with random points if needed
        while (centroids.size() < k) {
            double[] randomPoint = new double[2];
            randomPoint[0] = rand.nextDouble() * 1000;
            randomPoint[1] = rand.nextDouble() * 1000;
            centroids.add(randomPoint);
        }

        // Step 2: Iterative clustering
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Assign points to nearest centroid
            List<List<double[]>> clusters = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                clusters.add(new ArrayList<>());
            }

            for (double[] point : points) {
                int nearestCluster = findNearestCentroid(point, centroids);
                clusters.get(nearestCluster).add(point);
            }

            // Update centroids
            boolean changed = false;
            for (int i = 0; i < k; i++) {
                if (clusters.get(i).isEmpty()) continue;

                double[] newCentroid = calculateCentroid(clusters.get(i));
                if (!arraysEqual(centroids.get(i), newCentroid)) {
                    centroids.set(i, newCentroid);
                    changed = true;
                }
            }

            // Check convergence
            if (!changed) {
                logger.debug("K-means converged after {} iterations", iteration + 1);
                break;
            }
        }

        return centroids;
    }

    /**
     * Find nearest centroid to a point
     */
    private static int findNearestCentroid(double[] point, List<double[]> centroids) {
        int nearest = 0;
        double minDist = euclideanDistance(point, centroids.get(0));

        for (int i = 1; i < centroids.size(); i++) {
            double dist = euclideanDistance(point, centroids.get(i));
            if (dist < minDist) {
                minDist = dist;
                nearest = i;
            }
        }
        return nearest;
    }

    /**
     * Calculate Euclidean distance between two 2D points
     */
    private static double euclideanDistance(double[] p1, double[] p2) {
        double dx = p1[0] - p2[0];
        double dy = p1[1] - p2[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calculate centroid of a cluster
     */
    private static double[] calculateCentroid(List<double[]> points) {
        double sumX = 0, sumY = 0;
        for (double[] p : points) {
            sumX += p[0];
            sumY += p[1];
        }
        return new double[]{sumX / points.size(), sumY / points.size()};
    }

    /**
     * Check if two arrays are equal
     */
    private static boolean arraysEqual(double[] a, double[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (Math.abs(a[i] - b[i]) > 1e-6) return false;
        }
        return true;
    }
}