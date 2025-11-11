
package simulation.model;

import java.util.HashMap;
import java.util.Map;

public class MetricEntry {
    private double timestamp;
    private String eventType;
    private String entityId;
    private double cost;
    private double latency;
    private double energyConsumption;
    private Map<String, Object> customValues;
    private boolean successful;

    public MetricEntry(double timestamp, String eventType, String entityId) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.entityId = entityId;
        this.cost = 0.0;
        this.latency = 0.0;
        this.energyConsumption = 0.0;
        this.customValues = new HashMap<>();
        this.successful = true;
    }

    // Getters and Setters
    public double getTimestamp() { return timestamp; }
    public void setTimestamp(double timestamp) { this.timestamp = timestamp; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public double getLatency() { return latency; }
    public void setLatency(double latency) { this.latency = latency; }

    public double getEnergyConsumption() { return energyConsumption; }
    public void setEnergyConsumption(double energyConsumption) { 
        this.energyConsumption = energyConsumption; 
    }

    public Map<String, Object> getCustomValues() { return customValues; }
    public void addCustomValue(String key, Object value) { 
        customValues.put(key, value); 
    }

    public boolean isSuccessful() { return successful; }
    public void setSuccessful(boolean successful) { this.successful = successful; }

    public String[] toCSVRow() {
        return new String[]{
            String.valueOf(timestamp),
            eventType,
            entityId,
            String.valueOf(cost),
            String.valueOf(latency),
            String.valueOf(energyConsumption),
            String.valueOf(successful),
            customValues.toString()
        };
    }

    @Override
    public String toString() {
        return String.format(
            "MetricEntry{ts=%.2f, event=%s, entity=%s, cost=%.4f, lat=%.2f, energy=%.2f}",
            timestamp, eventType, entityId, cost, latency, energyConsumption
        );
    }
}
