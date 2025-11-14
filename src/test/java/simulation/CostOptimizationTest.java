package simulation;

import org.junit.Before;
import org.junit.Test;
import simulation.events.MetricsCollector;
import simulation.model.*;
import simulation.optimization.CostOptimizer;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

public class CostOptimizationTest {

    private CostOptimizer optimizer;
    private CostModel costModel;
    private MetricsCollector metricsCollector;
    private List<MECServer> servers;

    @Before
    public void setUp() {
        metricsCollector = new MetricsCollector(1000);
        costModel = new CostModel(0.0001, 0.00001, 0.01, 0.1);
        optimizer = new CostOptimizer(costModel, metricsCollector);

        // Create test servers
        servers = new ArrayList<>();
        servers.add(new MECServer("SERVER_1", 10000, 4096, 100000));
        servers.add(new MECServer("SERVER_2", 5000, 2048, 50000));
    }

    @Test
    public void testOptimizationDecisionWithValidServers() {
        Task task = new Task("TASK_1", 0, 1000, 100, 10);
        CostOptimizer.OffloadingDecision decision = optimizer.optimizeTaskOffloading(task, servers);

        assertNotNull("Decision should not be null", decision);
        // Decision may have null server if no valid match
        assertTrue("Decision should either have valid server or be marked unsafe", 
                   decision.selectedServer != null || !decision.isDeadlineSafe);
    }

    @Test
    public void testOptimizationWithValidTask() {
        Task task = new Task("TASK_2", 0, 500, 50, 5);
        CostOptimizer.OffloadingDecision decision = optimizer.optimizeTaskOffloading(task, servers);

        assertNotNull("Decision should not be null", decision);
        assertTrue("Estimated latency should be positive or zero", decision.estimatedLatency >= 0);
    }

    @Test
    public void testOptimizationWithEmptyServerList() {
        Task task = new Task("TASK_3", 0, 1000, 100, 10);
        List<MECServer> emptyServers = new ArrayList<>();

        CostOptimizer.OffloadingDecision decision = optimizer.optimizeTaskOffloading(task, emptyServers);

        assertNotNull("Decision should not be null", decision);
        assertNull("Selected server should be null with empty list", decision.selectedServer);
        assertFalse("Decision should not be deadline safe with no servers", decision.isDeadlineSafe);
    }

    @Test
    public void testDeadlineConstraintRespected() {
        Task task = new Task("TASK_4", 0, 1000, 100, 10);
        CostOptimizer.OffloadingDecision decision = optimizer.optimizeTaskOffloading(task, servers);

        if (decision.selectedServer != null) {
            assertTrue("Latency should not exceed deadline when decision is marked safe",
                    !decision.isDeadlineSafe || decision.estimatedLatency <= task.getDeadline());
        }
    }

    @Test
    public void testCostModelParameters() {
        assertEquals("Compute cost should match", 0.0001, costModel.getComputeCost(), 0.00001);
        assertEquals("Bandwidth cost should match", 0.00001, costModel.getBandwidthCost(), 0.000001);
        assertEquals("Latency penalty should match", 0.01, costModel.getLatencyPenalty(), 0.001);
        assertEquals("Energy cost should match", 0.1, costModel.getEnergyCost(), 0.01);
    }
}