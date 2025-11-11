package simulation.events;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.model.MetricEntry;

/**
 * Event listener for VM and Cloudlet lifecycle events (CloudSim Plus 7.x).
 */
public class AbstractEventListener implements SimulationEventListener {
    private static final Logger logger = LoggerFactory.getLogger(AbstractEventListener.class);
    protected final MetricsCollector metricsCollector;

    public AbstractEventListener(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    @Override
    public void onVmCreated(Vm vm) {
        String vmId = "VM_" + vm.getId();

        MetricEntry entry = new MetricEntry(System.currentTimeMillis(), "VM_CREATION", vmId);
        entry.addCustomValue("vmCpus", vm.getNumberOfPes());
        entry.addCustomValue("vmRam", vm.getRam().getCapacity());
        entry.addCustomValue("vmBw", vm.getBw().getCapacity());
        metricsCollector.recordMetric(entry);

        logger.info("VM Created: {}", vmId);
    }

    @Override
    public void onCloudletProcessing(Cloudlet cloudlet) {
        String taskId = "TASK_" + cloudlet.getId();
        MetricEntry entry = new MetricEntry(System.currentTimeMillis(), "TASK_PROCESSING", taskId);
        entry.addCustomValue("taskLength", cloudlet.getLength());
        entry.addCustomValue("vmId", cloudlet.getVm().getId());
        metricsCollector.recordMetric(entry);

        logger.debug("Cloudlet Processing: {}", taskId);
    }

    @Override
    public void onCloudletFinished(Cloudlet cloudlet) {
        String taskId = "TASK_" + cloudlet.getId();

        MetricEntry entry = new MetricEntry(System.currentTimeMillis(), "TASK_COMPLETION", taskId);
        entry.setSuccessful(cloudlet.isFinished());
        entry.setLatency(cloudlet.getActualCpuTime());
        metricsCollector.recordMetric(entry);

        logger.info("Task Completed: {} (Success: {})", taskId, entry.isSuccessful());
    }

    @Override
    public void onSimulationStart() {
        logger.info("=== Simulation Started ===");
    }

    @Override
    public void onSimulationEnd() {
        logger.info("=== Simulation Ended ===");
        logger.info("Total Metrics Collected: {}", metricsCollector.getTotalMetricsCount());
    }
}
