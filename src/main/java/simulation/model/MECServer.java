package simulation.model;

public class MECServer {

    private final String serverId;
    private final int mipsCapacity;
    private final int ram;
    private final int storage;

    private double currentUtilization = 0;

    public MECServer(String serverId, int mipsCapacity, int ram, int storage) {
        this.serverId = serverId;
        this.mipsCapacity = mipsCapacity;
        this.ram = ram;
        this.storage = storage;
    }

    public String getServerId() { return serverId; }
    public int getMipsCapacity() { return mipsCapacity; }
    public int getRam() { return ram; }
    public int getStorage() { return storage; }

    public double getResourceUtilization() { return currentUtilization; }

    public void updateUtilization(double usedFraction) {
        this.currentUtilization = Math.max(0, Math.min(1.0, usedFraction));
    }

    @Override
    public String toString() {
        return "MECServer{" +
                "serverId='" + serverId + '\'' +
                ", mipsCapacity=" + mipsCapacity +
                ", ram=" + ram +
                ", storage=" + storage +
                ", utilization=" + currentUtilization +
                '}';
    }
}
