package simulation.model;

/**
 * Cost Model: Encapsulates economic parameters for service provider profit calculation
 * Based on paper formulation: Profit = Revenue - Cost
 */
public class CostModel {
    
    // ===== Pricing Parameters ($/unit) =====
    private double computeCostPerCpuHour; // $/CPU-hour - ES leasing cost
    private double bandwidthCostPerGB; // $/GB - transmission cost
    private double latencyPenaltyPerMs; // $/ms - SLA violation penalty
    private double energyCostPerKWh; // $/kWh - power consumption cost

    // ===== Resource Parameters =====
    private double vmCpuCapacity = 1000.0; // MIPS per CPU
    private double vmMemoryCapacity = 4096.0; // MB

    public CostModel(double computeCost, double bandwidthCost,
                     double latencyPenalty, double energyCost) {
        this.computeCostPerCpuHour = computeCost;
        this.bandwidthCostPerGB = bandwidthCost;
        this.latencyPenaltyPerMs = latencyPenalty;
        this.energyCostPerKWh = energyCost;
    }

    /**
     * Cost calculation result structure
     */
    public static class CostCalculation {
        public double computeCost;
        public double bandwidthCost;
        public double latencyPenalty;
        public double energyCost;
        public double totalCost;

        public CostCalculation(double compute, double bandwidth, 
                              double latency, double energy) {
            this.computeCost = compute;
            this.bandwidthCost = bandwidth;
            this.latencyPenalty = latency;
            this.energyCost = energy;
            this.totalCost = compute + bandwidth + latency + energy;
        }

        @Override
        public String toString() {
            return String.format("Cost{Compute:$%.4f, BW:$%.4f, Latency:$%.4f, Energy:$%.4f, Total:$%.4f}",
                    computeCost, bandwidthCost, latencyPenalty, energyCost, totalCost);
        }
    }

    /**
     * Calculate cost for task execution
     */
    public CostCalculation calculateCost(long computeMI, long dataSizeKB, 
                                        double executionTimeSeconds, double powerWatts) {
        
        // Compute cost: (MIPS * $/CPU-hour) / 3600 seconds
        double computeCost = (computeMI / vmCpuCapacity) * computeCostPerCpuHour / 3600.0;

        // Bandwidth cost: (data KB -> GB) * $/GB
        double bandwidthCost = (dataSizeKB / (1024.0 * 1024.0)) * bandwidthCostPerGB;

        // Energy cost: (Power W -> kW) * Time * $/kWh
        double energyCost = (powerWatts / 1000.0) * (executionTimeSeconds / 3600.0) * energyCostPerKWh;

        // Latency penalty: $/ms
        double latencyPenaltyCost = executionTimeSeconds * 1000.0 * latencyPenaltyPerMs;

        return new CostCalculation(computeCost, bandwidthCost, latencyPenaltyCost, energyCost);
    }

    // ===== Getters =====

    public double getComputeCost() { return computeCostPerCpuHour; }
    public double getBandwidthCost() { return bandwidthCostPerGB; }
    public double getLatencyPenalty() { return latencyPenaltyPerMs; }
    public double getEnergyCost() { return energyCostPerKWh; }
    public double getVmCpuCapacity() { return vmCpuCapacity; }
    public double getVmMemoryCapacity() { return vmMemoryCapacity; }

    // ===== Setters =====

    public void setComputeCost(double cost) { computeCostPerCpuHour = cost; }
    public void setBandwidthCost(double cost) { bandwidthCostPerGB = cost; }
    public void setLatencyPenalty(double penalty) { latencyPenaltyPerMs = penalty; }
    public void setEnergyCost(double cost) { energyCostPerKWh = cost; }
    public void setVmCpuCapacity(double capacity) { vmCpuCapacity = capacity; }
    public void setVmMemoryCapacity(double capacity) { vmMemoryCapacity = capacity; }

    @Override
    public String toString() {
        return String.format("CostModel{Compute:$%.6f/hr, BW:$%.6f/GB, Latency:$%.6f/ms, Energy:$%.6f/kWh}",
                computeCostPerCpuHour, bandwidthCostPerGB, latencyPenaltyPerMs, energyCostPerKWh);
    }
}