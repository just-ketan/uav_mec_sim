
package simulation.model;

public class Task {
    private String taskId;
    private double arrivalTime;
    private double computeRequirement;  // MIPS
    private double dataSize;            // MB
    private double deadline;            // seconds
    private boolean isCompleted;
    private double completionTime;

    public Task(String taskId, double arrivalTime, double computeReq, 
                double dataSize, double deadline) {
        this.taskId = taskId;
        this.arrivalTime = arrivalTime;
        this.computeRequirement = computeReq;
        this.dataSize = dataSize;
        this.deadline = deadline;
        this.isCompleted = false;
        this.completionTime = 0.0;
    }

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public double getArrivalTime() { return arrivalTime; }
    public double getComputeRequirement() { return computeRequirement; }
    public double getDataSize() { return dataSize; }
    public double getDeadline() { return deadline; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    
    public double getCompletionTime() { return completionTime; }
    public void setCompletionTime(double time) { completionTime = time; }

    public boolean isSLAViolated() {
        return (completionTime - arrivalTime) > deadline;
    }

    @Override
    public String toString() {
        return String.format(
            "Task{id=%s, compute=%.0f, data=%.0f, deadline=%.2f, completed=%s}",
            taskId, computeRequirement, dataSize, deadline, isCompleted
        );
    }
}
