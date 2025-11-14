package simulation.optimization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.model.MECServer;
import simulation.model.Task;
import simulation.model.UAVEntity;
import java.util.List;

/**
 * Adaptive Policy: Adjusts behavior based on system load
 */
public class AdaptivePolicy implements OptimizationPolicy {
    private static final Logger logger = LoggerFactory.getLogger(AdaptivePolicy.class);
    
    private final CostOptimizer costOptimizer;
    private double aggressionLevel = 0.5;

    public AdaptivePolicy(CostOptimizer costOptimizer) {
        this.costOptimizer = costOptimizer;
    }

    @Override
    public CostOptimizer.OffloadingDecision makeDecision(Task task, List<MECServer> servers) {
        CostOptimizer.OffloadingDecision decision = costOptimizer.optimizeTaskOffloading(task, servers);
        
        // Adjust decision based on aggression level
        if (!decision.isDeadlineSafe && aggressionLevel < 0.5) {
            logger.debug("Rejecting deadline-unsafe decision due to conservative policy");
            return new CostOptimizer.OffloadingDecision(null, null, 0, Double.MAX_VALUE, false, 0);
        }
        
        return decision;
    }

    @Override
    public void update(double simulationTime, List<MECServer> servers) {
        double avgUtilization = servers.stream()
            .mapToDouble(MECServer::getResourceUtilization)
            .average()
            .orElse(0.0);
        
        if (avgUtilization > 0.8) {
            aggressionLevel = Math.min(1.0, aggressionLevel + 0.05);
            logger.info("Increased aggression level to {}", aggressionLevel);
        } else if (avgUtilization < 0.4) {
            aggressionLevel = Math.max(0.0, aggressionLevel - 0.05);
            logger.info("Decreased aggression level to {}", aggressionLevel);
        }
    }

    @Override
    public String getPolicyName() {
        return "AdaptivePolicy";
    }

    public void setAggressionLevel(double level) {
        this.aggressionLevel = Math.max(0.0, Math.min(1.0, level));
    }

    public double getAggressionLevel() {
        return aggressionLevel;
    }
}