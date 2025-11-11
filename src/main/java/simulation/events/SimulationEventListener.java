package simulation.events;

import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;

public interface SimulationEventListener {
    void onVmCreated(Vm vm);
    void onCloudletProcessing(Cloudlet cloudlet);
    void onCloudletFinished(Cloudlet cloudlet);
    void onSimulationStart();
    void onSimulationEnd();
}
