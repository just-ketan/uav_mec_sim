package simulation.events;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractEventListener implements SimulationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AbstractEventListener.class);
    protected final MetricsCollector metricsCollector;

    public AbstractEventListener(MetricsCollector collector) {
        this.metricsCollector = collector;
    }

    @Override
    public void onVmCreated(Vm vm) {
        logger.info("VM Created: {}", vm.getId());
        metricsCollector.recordMetric("vm_created", vm.getId(), true);
    }

    @Override
    public void onCloudletProcessing(Cloudlet cloudlet) {
        metricsCollector.recordMetric("cloudlet_started", cloudlet.getId(), true);
    }

    @Override
    public void onCloudletFinished(Cloudlet cloudlet) {
        metricsCollector.recordMetric("cloudlet_finished", cloudlet.getId(), cloudlet.getStatus() == Cloudlet.Status.SUCCESS);
    }

    @Override
    public void onSimulationStart() {
        logger.info("=== Simulation Started ===");
    }

    @Override
    public void onSimulationEnd() {
        logger.info("=== Simulation Ended ===");
    }
}
