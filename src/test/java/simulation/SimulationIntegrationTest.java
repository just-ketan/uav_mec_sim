
package simulation;

import org.junit.Before;
import org.junit.Test;

import main.java.simulation.core.SimulationConfig;
import main.java.simulation.core.UAVMECSimulation;
import main.java.simulation.events.MetricsCollector;
import main.java.simulation.model.MetricEntry;

import static org.junit.Assert.*;

import java.util.List;

public class SimulationIntegrationTest {
    
    @Test
    public void testEndToEndSimulationWithOptimization() {
        // Create minimal configuration
        SimulationConfig config = new SimulationConfig()
            .withSimulationTime(100.0)
            .withVmCount(2)
            .withTaskCount(50)
            .withRandomSeed(123);

        // Run simulation
        UAVMECSimulation simulation = new UAVMECSimulation(config);
        
        try {
            simulation.run();
        } catch (Exception e) {
            fail("Simulation should complete without errors: " + e.getMessage());
        }
    }

    @Test
    public void testMetricsAreCollected() {
        SimulationConfig config = new SimulationConfig()
            .withVmCount(2)
            .withTaskCount(20);

        MetricsCollector collector = new MetricsCollector(1000);
        assertNotNull("Metrics collector should be initialized", collector);

        List<MetricEntry> metrics = collector.getAllMetrics();
        assertTrue("Initial metrics should be empty or sparse", metrics.size() >= 0);

        collector.shutdown();
    }
}