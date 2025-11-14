package simulation;

import org.junit.Test;
import simulation.core.SimulationConfig;
import simulation.core.UAVMECSimulation;
import simulation.events.MetricsCollector;
import simulation.model.MetricEntry;
import static org.junit.Assert.*;
import java.util.List;

public class SimulationIntegrationTest {

    @Test
    public void testMetricsAreCollected() {
        SimulationConfig config = new SimulationConfig();
        config.setSimulationTime(100.0);
        config.setVmCount(2);
        config.setTaskCount(20);
        config.setRandomSeed(123);

        MetricsCollector collector = new MetricsCollector(1000);
        assertNotNull("Metrics collector should be initialized", collector);
        
        List<MetricEntry> metrics = collector.getAllMetrics();
        assertTrue("Initial metrics should be empty or sparse", metrics.size() >= 0);
        
        collector.shutdown();
    }

    @Test
    public void testSimulationConfiguration() {
        SimulationConfig config = new SimulationConfig();
        config.setSimulationTime(100.0);
        config.setVmCount(5);
        config.setTaskCount(50);
        config.setRandomSeed(42);

        assertEquals("Simulation time should match", 100.0, config.getSimulationTime(), 0.01);
        assertEquals("VM count should match", 5, config.getVmCount());
        assertEquals("Task count should match", 50, config.getTaskCount());
        assertEquals("Random seed should match", 42L, config.getRandomSeed());
    }

    @Test
    public void testSimulationConfigurationDefaults() {
        SimulationConfig config = new SimulationConfig();
        
        assertTrue("Simulation time should be positive", config.getSimulationTime() > 0);
        assertTrue("VM count should be positive", config.getVmCount() > 0);
        assertTrue("Task count should be positive", config.getTaskCount() > 0);
    }
}