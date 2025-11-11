
package simulation.model;

public class MECServer {
    private String serverId;
    private double cpuCapacity;       // MIPS
    private double memoryCapacity;    // MB
    private double storageCapacity;   // MB
    private double currentCpuUsage;
    private double currentMemoryUsage;
    private boolean isActive;

    public MECServer(String serverId, double cpu, double memory, double storage) {
        this.serverId = serverId;
        this.cpuCapacity = cpu;
        this.memoryCapacity = memory;
        this.storageCapacity = storage;
        this.currentCpuUsage = 0.0;
        this.currentMemoryUsage = 0.0;
        this.isActive = true;
    }

    public boolean canAccommodateTask(Task task) {
        double availableCpu = cpuCapacity - currentCpuUsage;
        double availableMemory = memoryCapacity - currentMemoryUsage;
        
        return (availableCpu >= task.getComputeRequirement() * 0.1) &&
               (availableMemory >= (task.getDataSize() * 0.5));
    }

    public double getResourceUtilization() {
        double cpuUtil = currentCpuUsage / cpuCapacity;
        double memUtil = currentMemoryUsage / memoryCapacity;
        return (cpuUtil + memUtil) / 2.0;
    }

    // Getters and Setters
    public String getServerId() { return serverId; }
    public double getCpuCapacity() { return cpuCapacity; }
    public double getMemoryCapacity() { return memoryCapacity; }
    public double getCurrentCpuUsage() { return currentCpuUsage; }
    public void setCurrentCpuUsage(double usage) { currentCpuUsage = usage; }
    public double getCurrentMemoryUsage() { return currentMemoryUsage; }
    public void setCurrentMemoryUsage(double usage) { currentMemoryUsage = usage; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return String.format(
            "MECServer{id=%s, cpu=%.0f, mem=%.0f, utilization=%.2f%%}",
            serverId, cpuCapacity, memoryCapacity, getResourceUtilization() * 100
        );
    }
}
