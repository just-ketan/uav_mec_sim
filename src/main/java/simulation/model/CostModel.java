
package simulation.model;

public class CostModel {
    // Cost parameters (in $/unit)
    private double computeCostPerCpuHour;      // $/CPU-hour
    private double bandwidthCostPerGB;         // $/GB
    private double latencyPenaltyPerMs;        // $/ms SLA breach
    private double energyCostPerKWh;           // $/kWh

    // Resource parameters
    private double vmCpuCapacity;              // MIPS per CPU
    private double vmMemoryCapacity;           // MB

    public CostModel(double computeCost, double bandwidthCost, 
                     double latencyPenalty, double energyCost) {
        this.computeCostPerCpuHour = computeCost;
        this.bandwidthCostPerGB = bandwidthCost;
        this.latencyPenaltyPerMs = latencyPenalty;
        this.energyCostPerKWh = energyCost;
        this.vmCpuCapacity = 1000.0;   // Default: 1000 MIPS
        this.vmMemoryCapacity = 4096.0; // Default: 4GB
    }

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
            return String.format(
                "Cost{compute=%.4f, bw=%.4f, latency=%.4f, energy=%.4f, total=%.4f}",
                computeCost, bandwidthCost, latencyPenalty, energyCost, totalCost
            );
        }
    }

    // Calculate cost for executing a task on a VM
    public CostCalculation calculateTaskCost(double taskLength, 
                                             double dataSize,
                                             double estimatedLatency,
                                             double deadline,
                                             double estimatedEnergy) {
        // Compute cost: (CPU-hours) * rate
        double computeHours = (taskLength / vmCpuCapacity) / 3600.0;
        double computeCost = computeHours * computeCostPerCpuHour;

        // Bandwidth cost: (GB transferred) * rate
        double dataGB = dataSize / 1024.0;
        double bwCost = dataGB * bandwidthCostPerGB;

        // Latency penalty: (excess latency over deadline) * penalty rate
        double excessLatency = Math.max(0, estimatedLatency - deadline);
        double latencyCost = excessLatency * latencyPenaltyPerMs;

        // Energy cost: (kWh consumed) * rate
        double energyKWh = estimatedEnergy / 3600000.0; // Convert Joules to kWh
        double energyCost = energyKWh * energyCostPerKWh;

        return new CostCalculation(computeCost, bwCost, latencyCost, energyCost);
    }

    // Simple cost for comparison
    public double estimateTaskCost(double taskLength, double dataSize) {
        double computeHours = (taskLength / vmCpuCapacity) / 3600.0;
        double dataGB = dataSize / 1024.0;
        return (computeHours * computeCostPerCpuHour) + (dataGB * bandwidthCostPerGB);
    }

    // Getters and Setters
    public void setComputeCostPerCpuHour(double cost) { 
        this.computeCostPerCpuHour = cost; 
    }
    public double getComputeCostPerCpuHour() { 
        return computeCostPerCpuHour; 
    }

    public void setBandwidthCostPerGB(double cost) { 
        this.bandwidthCostPerGB = cost; 
    }
    public double getBandwidthCostPerGB() { 
        return bandwidthCostPerGB; 
    }

    public void setLatencyPenaltyPerMs(double penalty) { 
        this.latencyPenaltyPerMs = penalty; 
    }
    public double getLatencyPenaltyPerMs() { 
        return latencyPenaltyPerMs; 
    }

    public void setEnergyCostPerKWh(double cost) { 
        this.energyCostPerKWh = cost; 
    }
    public double getEnergyCostPerKWh() { 
        return energyCostPerKWh; 
    }

    public void setVmCpuCapacity(double mips) { 
        this.vmCpuCapacity = mips; 
    }

    public void setVmMemoryCapacity(double mb) { 
        this.vmMemoryCapacity = mb; 
    }
}
