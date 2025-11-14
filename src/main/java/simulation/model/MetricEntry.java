package simulation.model;

/**
 * Metric Entry: Records simulation events and metrics
 * Enhanced with cost, latency, and energy fields
 */
public class MetricEntry {
    private final String type;
    private final long targetId;
    private final boolean success;
    private final long timestamp;
    private double cost = 0.0;
    private double latency = 0.0;
    private double energyConsumption = 0.0;

    public MetricEntry(String type, long targetId, boolean success, long timestamp) {
        this.type = type;
        this.targetId = targetId;
        this.success = success;
        this.timestamp = timestamp;
    }

    // ===== Setters =====
    public void setCost(double cost) { this.cost = cost; }
    public void setLatency(double latency) { this.latency = latency; }
    public void setEnergyConsumption(double energy) { this.energyConsumption = energy; }

    // ===== Getters =====
    public String getType() { return type; }
    public String getEventType() { return type; }
    public long getTargetId() { return targetId; }
    public boolean isSuccess() { return success; }
    public boolean isSuccessful() { return success; }
    public long getTimestamp() { return timestamp; }
    public double getCost() { return cost; }
    public double getLatency() { return latency; }
    public double getEnergyConsumption() { return energyConsumption; }

    /**
     * Convert to CSV row format
     */
    public String[] toCSVRow() {
        return new String[]{
                String.valueOf(timestamp),
                type,
                String.valueOf(targetId),
                String.valueOf(cost),
                String.valueOf(latency),
                String.valueOf(energyConsumption),
                String.valueOf(success),
                ""
        };
    }

    @Override
    public String toString() {
        return "MetricEntry{" +
                "type='" + type + '\'' +
                ", targetId=" + targetId +
                ", success=" + success +
                ", timestamp=" + timestamp +
                ", cost=" + cost +
                ", latency=" + latency +
                ", energy=" + energyConsumption +
                '}';
    }
}