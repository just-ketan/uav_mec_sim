package simulation.model;

public class Task {

    private final String id;
    private final double arrivalTime;
    private final long computeMI;
    private final long dataSizeKB;
    private final double deadline;

    public Task(String id, double arrivalTime, long computeMI, long dataSizeKB, double deadline) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.computeMI = computeMI;
        this.dataSizeKB = dataSizeKB;
        this.deadline = deadline;
    }

    public String getId() { return id; }
    public double getArrivalTime() { return arrivalTime; }
    public long getComputeMI() { return computeMI; }
    public long getDataSizeKB() { return dataSizeKB; }
    public double getDeadline() { return deadline; }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", arrivalTime=" + arrivalTime +
                ", computeMI=" + computeMI +
                ", dataSizeKB=" + dataSizeKB +
                ", deadline=" + deadline +
                '}';
    }
}
