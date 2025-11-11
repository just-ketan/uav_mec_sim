
package simulation.core;

import java.util.HashMap;
import java.util.Map;

public class SimulationConfig {
    private double simulationTime;
    private int vmCount;
    private int taskCount;
    private double taskArrivalRate;
    private double computeCost;
    private double bandwidthCost;
    private double latencyPenalty;
    private double energyCost;
    private long randomSeed;
    private Map<String, Object> customParams;

    public SimulationConfig() {
        // Default values
        this.simulationTime = 3600.0; // 1 hour
        this.vmCount = 5;
        this.taskCount = 1000;
        this.taskArrivalRate = 0.1; // tasks per second
        this.computeCost = 0.0001;
        this.bandwidthCost = 0.00001;
        this.latencyPenalty = 0.01;
        this.energyCost = 0.1;
        this.randomSeed = System.currentTimeMillis();
        this.customParams = new HashMap<>();
    }

    // Builder pattern for easy configuration
    public SimulationConfig withSimulationTime(double time) {
        this.simulationTime = time;
        return this;
    }

    public SimulationConfig withVmCount(int count) {
        this.vmCount = count;
        return this;
    }

    public SimulationConfig withTaskCount(int count) {
        this.taskCount = count;
        return this;
    }

    public SimulationConfig withTaskArrivalRate(double rate) {
        this.taskArrivalRate = rate;
        return this;
    }

    public SimulationConfig withCostModel(double compute, double bandwidth, 
                                         double latency, double energy) {
        this.computeCost = compute;
        this.bandwidthCost = bandwidth;
        this.latencyPenalty = latency;
        this.energyCost = energy;
        return this;
    }

    public SimulationConfig withRandomSeed(long seed) {
        this.randomSeed = seed;
        return this;
    }

    // Getters
    public double getSimulationTime() { return simulationTime; }
    public int getVmCount() { return vmCount; }
    public int getTaskCount() { return taskCount; }
    public double getTaskArrivalRate() { return taskArrivalRate; }
    public double getComputeCost() { return computeCost; }
    public double getBandwidthCost() { return bandwidthCost; }
    public double getLatencyPenalty() { return latencyPenalty; }
    public double getEnergyCost() { return energyCost; }
    public long getRandomSeed() { return randomSeed; }

    @Override
    public String toString() {
        return String.format(
            "SimConfig{time=%.0f, vms=%d, tasks=%d, rate=%.2f, seed=%d}",
            simulationTime, vmCount, taskCount, taskArrivalRate, randomSeed
        );
    }
}
