
package simulation.optimization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.model.MECServer;
import simulation.model.Task;

import java.util.List;

public class AdaptivePolicy implements OptimizationPolicy {
    private static final Logger logger = LoggerFactory.getLogger(AdaptivePolicy.class);
    
    private final CostOptimizer costOptimizer;
    private double aggressionLevel = 0.5; // 0.0 = conservative, 1.0 = aggressive cost minimization

    public AdaptivePolicy(CostOptimizer costOptimizer) {
        this.costOptimizer = costOptimizer;
    }

    @Override
    public CostOptimizer.OffloadingDecision makeDecision(Task task, List<MECServer> servers) {
        CostOptimizer.OffloadingDecision decision = costOptimizer.optimizeTaskOffloading(task, servers);
        
        // Adjust decision based on aggression level
        if (!decision.isDeadlineSafe && aggressionLevel < 0.5) {
            // Conservative mode: reject unsafe decisions
            logger.debug("Rejecting deadline-unsafe decision due to conservative policy");
            return new CostOptimizer.OffloadingDecision(null, Double.MAX_VALUE, 0, false);
        }

        return decision;
    }

    @Override
    public void update(double simulationTime, List<MECServer> servers) {
        // Adapt aggression level based on system load
        double avgUtilization = servers.stream()
            .mapToDouble(MECServer::getResourceUtilization)
            .average()
            .orElse(0.0);

        if (avgUtilization > 0.8) {
            // High load: increase aggression (accept more cost optimization)
            aggressionLevel = Math.min(1.0, aggressionLevel + 0.05);
            logger.info("Increased aggression level to {}", aggressionLevel);
        } else if (avgUtilization < 0.4) {
            // Low load: decrease aggression (be more conservative)
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
}
