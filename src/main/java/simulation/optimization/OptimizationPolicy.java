package simulation.optimization;

import java.util.List;

import simulation.model.MECServer;
import simulation.model.Task;

public interface OptimizationPolicy {
    /**
     * Make optimization decision for a task
     */
    CostOptimizer.OffloadingDecision makeDecision(Task task, List<MECServer> servers);

    /**
     * Update policy based on current simulation state
     */
    void update(double simulationTime, List<MECServer> servers);

    /**
     * Get policy name
     */
    String getPolicyName();
}
