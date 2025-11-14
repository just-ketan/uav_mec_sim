package simulation.core;

/**
 * Configuration holder for UAV-MEC simulation parameters
 * Supports parameterization via YAML configuration file
 */
public class SimulationConfig {
    
    // ===== Simulation Parameters =====
    private double simulationTime = 3600;
    private long randomSeed = 42;
    private String resultsDirectory = "results";
    
    // ===== Datacenter / Host Parameters =====
    private int hostCount = 10;
    private int hostMips = 20000;
    private int hostPes = 4;
    private int hostRam = 32768;
    private int hostBandwidth = 100000;
    private int hostStorage = 1_000_000;
    
    // ===== VM Parameters =====
    private int vmCount = 200;
    private int vmMips = 5000;
    private int vmRam = 2048;
    private int vmBandwidth = 5000;
    private long vmSize = 10000;
    
    // ===== Task / Workload Parameters =====
    private int taskCount = 200;
    private double arrivalRate = 0.1;
    private int taskComputeMin = 1000;
    private int taskComputeMax = 9000;
    private int taskDataMin = 100;
    private int taskDataMax = 10000;
    private int taskOutputMin = 10;
    private int taskOutputMax = 1000;
    private double deadlineMin = 1.0;
    private double deadlineMax = 30.0;
    
    // ===== Cost Model Parameters =====
    private double computeCost = 0.0001;
    private double bandwidthCost = 0.00001;
    private double latencyPenalty = 0.00005;
    private double energyCost = 0.00005;
    
    // ===== UAV Parameters (NEW) =====
    private int uavCount = 0; // Auto-calculated if 0
    private double uavAltitude = 100.0; // meters
    private int uavCapacityPerNode = 20; // NU - max IoTs per UAV
    private double sinrThreshold = 10.0; // dB
    private double revenueParameter = 0.1; // v from paper
    private double costParameter = 0.01; // w from paper
    private double areaWidth = 1000.0; // meters
    private double areaHeight = 1000.0; // meters
    
    // ===== Optimization Parameters =====
    private double aggressionLevel = 0.5; // AdaptivePolicy
    private int optimizationIterations = 100; // K-means and R-TMSC
    
    public SimulationConfig() {}
    
    // ========== GETTERS ==========
    
    public double getSimulationTime() { return simulationTime; }
    public long getRandomSeed() { return randomSeed; }
    public String getResultsDirectory() { return resultsDirectory; }
    
    public int getHostCount() { return hostCount; }
    public int getHostMips() { return hostMips; }
    public int getHostPes() { return hostPes; }
    public int getHostRam() { return hostRam; }
    public int getHostBandwidth() { return hostBandwidth; }
    public int getHostStorage() { return hostStorage; }
    
    public int getVmCount() { return vmCount; }
    public int getVmMips() { return vmMips; }
    public int getVmRam() { return vmRam; }
    public int getVmBandwidth() { return vmBandwidth; }
    public long getVmSize() { return vmSize; }
    
    public int getTaskCount() { return taskCount; }
    public double getArrivalRate() { return arrivalRate; }
    public int getTaskComputeMin() { return taskComputeMin; }
    public int getTaskComputeMax() { return taskComputeMax; }
    public int getTaskDataMin() { return taskDataMin; }
    public int getTaskDataMax() { return taskDataMax; }
    public int getTaskOutputMin() { return taskOutputMin; }
    public int getTaskOutputMax() { return taskOutputMax; }
    public double getDeadlineMin() { return deadlineMin; }
    public double getDeadlineMax() { return deadlineMax; }
    
    public double getComputeCost() { return computeCost; }
    public double getBandwidthCost() { return bandwidthCost; }
    public double getLatencyPenalty() { return latencyPenalty; }
    public double getEnergyCost() { return energyCost; }
    
    public int getUavCount() { return uavCount == 0 ? (taskCount + uavCapacityPerNode - 1) / uavCapacityPerNode : uavCount; }
    public double getUavAltitude() { return uavAltitude; }
    public int getUavCapacityPerNode() { return uavCapacityPerNode; }
    public double getSinrThreshold() { return sinrThreshold; }
    public double getRevenueParameter() { return revenueParameter; }
    public double getCostParameter() { return costParameter; }
    public double getAreaWidth() { return areaWidth; }
    public double getAreaHeight() { return areaHeight; }
    
    public double getAggressionLevel() { return aggressionLevel; }
    public int getOptimizationIterations() { return optimizationIterations; }
    
    // ========== SETTERS ==========
    
    public void setSimulationTime(double v) { simulationTime = v; }
    public void setRandomSeed(long v) { randomSeed = v; }
    public void setResultsDirectory(String v) { resultsDirectory = v; }
    
    public void setHostCount(int v) { hostCount = v; }
    public void setHostMips(int v) { hostMips = v; }
    public void setHostPes(int v) { hostPes = v; }
    public void setHostRam(int v) { hostRam = v; }
    public void setHostBandwidth(int v) { hostBandwidth = v; }
    public void setHostStorage(int v) { hostStorage = v; }
    
    public void setVmCount(int v) { vmCount = v; }
    public void setVmMips(int v) { vmMips = v; }
    public void setVmRam(int v) { vmRam = v; }
    public void setVmBandwidth(int v) { vmBandwidth = v; }
    public void setVmSize(long v) { vmSize = v; }
    
    public void setTaskCount(int v) { taskCount = v; }
    public void setArrivalRate(double v) { arrivalRate = v; }
    public void setTaskComputeMin(int v) { taskComputeMin = v; }
    public void setTaskComputeMax(int v) { taskComputeMax = v; }
    public void setTaskDataMin(int v) { taskDataMin = v; }
    public void setTaskDataMax(int v) { taskDataMax = v; }
    public void setTaskOutputMin(int v) { taskOutputMin = v; }
    public void setTaskOutputMax(int v) { taskOutputMax = v; }
    public void setDeadlineMin(double v) { deadlineMin = v; }
    public void setDeadlineMax(double v) { deadlineMax = v; }
    
    public void setComputeCost(double v) { computeCost = v; }
    public void setBandwidthCost(double v) { bandwidthCost = v; }
    public void setLatencyPenalty(double v) { latencyPenalty = v; }
    public void setEnergyCost(double v) { energyCost = v; }
    
    public void setUavCount(int v) { uavCount = v; }
    public void setUavAltitude(double v) { uavAltitude = v; }
    public void setUavCapacityPerNode(int v) { uavCapacityPerNode = v; }
    public void setSinrThreshold(double v) { sinrThreshold = v; }
    public void setRevenueParameter(double v) { revenueParameter = v; }
    public void setCostParameter(double v) { costParameter = v; }
    public void setAreaWidth(double v) { areaWidth = v; }
    public void setAreaHeight(double v) { areaHeight = v; }
    
    public void setAggressionLevel(double v) { aggressionLevel = v; }
    public void setOptimizationIterations(int v) { optimizationIterations = v; }
    
    @Override
    public String toString() {
        return "SimulationConfig{" +
                "simulationTime=" + simulationTime +
                ", taskCount=" + taskCount +
                ", uavCount=" + getUavCount() +
                ", hostCount=" + hostCount +
                ", vmCount=" + vmCount +
                '}';
    }
}