package simulation.model;

/**
 * UAV Entity: Represents a UAV relay node in the MEC architecture
 * Manages position, capacity, and load information
 */
public class UAVEntity {
    private final String uavId;
    private final double altitude; // Fixed altitude H
    private double xPosition;
    private double yPosition;
    private final int maxCapacity; // NU - max IoTs this UAV can serve
    private int currentLoad = 0;

    public UAVEntity(String id, double x, double y, double altitude, int capacity) {
        this.uavId = id;
        this.xPosition = x;
        this.yPosition = y;
        this.altitude = altitude;
        this.maxCapacity = capacity;
    }

    // ===== Position Management =====

    public void updatePosition(double x, double y) {
        this.xPosition = x;
        this.yPosition = y;
    }

    /**
     * Calculate 2D distance to IoT
     */
    public double getDistance2D(double iotX, double iotY) {
        double dx = xPosition - iotX;
        double dy = yPosition - iotY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calculate 3D distance to IoT (includes altitude)
     */
    public double getDistance3D(double iotX, double iotY) {
        double dist2D = getDistance2D(iotX, iotY);
        return Math.sqrt(dist2D * dist2D + altitude * altitude);
    }

    /**
     * Calculate elevation angle from IoT to UAV (in degrees)
     */
    public double getElevationAngle(double iotX, double iotY) {
        double dist2D = getDistance2D(iotX, iotY);
        if (dist2D == 0) return 90.0;
        return Math.toDegrees(Math.atan(altitude / dist2D));
    }

    // ===== Capacity Management =====

    public boolean hasCapacity() {
        return currentLoad < maxCapacity;
    }

    public void incrementLoad() {
        if (currentLoad < maxCapacity) currentLoad++;
    }

    public void decrementLoad() {
        if (currentLoad > 0) currentLoad--;
    }

    public void resetLoad() {
        currentLoad = 0;
    }

    /**
     * Get ratio of available capacity
     */
    public double getAvailableCapacityRatio() {
        return (double) (maxCapacity - currentLoad) / maxCapacity;
    }

    // ===== Getters =====
    
    public String getUavId() { return uavId; }
    public double getXPosition() { return xPosition; }
    public double getYPosition() { return yPosition; }
    public double getAltitude() { return altitude; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getCurrentLoad() { return currentLoad; }

    @Override
    public String toString() {
        return String.format("UAV{id='%s', pos=(%.1f,%.1f,%.1f), load=%d/%d}",
                uavId, xPosition, yPosition, altitude, currentLoad, maxCapacity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UAVEntity)) return false;
        UAVEntity that = (UAVEntity) o;
        return uavId.equals(that.uavId);
    }

    @Override
    public int hashCode() {
        return uavId.hashCode();
    }
}