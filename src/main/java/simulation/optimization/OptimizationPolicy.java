package simulation.optimization;

import simulation.model.MECServer;
import simulation.model.Task;
import simulation.model.UAVEntity;
import java.util.List;

/**
 * Optimization Policy Interface
 * Defines contract for offloading decision policies
 */
public interface OptimizationPolicy {
    
    /**
     * Make offloading decision for a task
     */
    CostOptimizer.OffloadingDecision makeDecision(Task task, List<MECServer> servers);
    
    /**
     * Update policy based on system state
     */
    void update(double simulationTime, List<MECServer> servers);
    
    /**
     * Get policy name
     */
    String getPolicyName();
}