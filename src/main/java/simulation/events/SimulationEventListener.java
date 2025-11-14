package simulation.events;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

public interface SimulationEventListener {

    default void onVmCreated(Vm vm) {}
    default void onCloudletProcessing(Cloudlet cloudlet) {}
    default void onCloudletFinished(Cloudlet cloudlet) {}
    default void onSimulationStart() {}
    default void onSimulationEnd() {}
}
