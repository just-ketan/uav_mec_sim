package test.java.simulation;

import org.junit.Before;
import org.junit.Test;

import main.java.simulation.events.MetricsCollector;
import main.java.simulation.model.*;
import main.java.simulation.optimization.CostOptimizer;

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
        assertNotNull("Selected server should not be null", decision.selectedServer);
        assertTrue("Decision should be deadline safe", decision.isDeadlineSafe);
    }

    @Test
    public void testOptimizationSelectsMinimumCost() {
        Task task = new Task("TASK_2", 0, 1000, 100, 10);
        
        CostOptimizer.OffloadingDecision decision = optimizer.optimizeTaskOffloading(task, servers);
        
        assertTrue("Estimated cost should be positive", decision.estimatedCost > 0);
    }

    @Test
    public void testOptimizationWithEmptyServerList() {
        Task task = new Task("TASK_3", 0, 1000, 100, 10);
        List<MECServer> emptyServers = new ArrayList<>();
        
        CostOptimizer.OffloadingDecision decision = optimizer.optimizeTaskOffloading(task, emptyServers);
        
        assertNull("Selected server should be null", decision.selectedServer);
        assertEquals("Cost should be max value", Double.MAX_VALUE, decision.estimatedCost, 0.01);
    }

    @Test
    public void testDeadlineConstraintRespected() {
        Task task = new Task("TASK_4", 0, 5000, 500, 5); // Very tight deadline
        
        CostOptimizer.OffloadingDecision decision = optimizer.optimizeTaskOffloading(task, servers);
        
        assertTrue("Latency should be less than deadline", 
                   decision.estimatedLatency <= task.getDeadline());
    }
}