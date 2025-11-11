package simulation.core;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.listeners.CloudletEventInfo;
import org.cloudsimplus.listeners.VmEventInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.analysis.MetricsExporter;
import simulation.analysis.StatisticalAnalyzer;
import simulation.events.AbstractEventListener;
import simulation.events.MetricsCollector;
import simulation.model.CostModel;
import simulation.model.MECServer;
import simulation.model.MetricEntry;
import simulation.optimization.CostOptimizer;

import java.util.*;

/**
 * UAV-MEC Simulation Core – CloudSim Plus 7.x compatible.
 */
public class UAVMECSimulation {
    private static final Logger logger = LoggerFactory.getLogger(UAVMECSimulation.class);

    private final CloudSim simulation;
    private final DatacenterBroker broker;
    private final CostOptimizer optimizer;
    private final MetricsCollector metricsCollector;
    private final SimulationConfig config;
    private final List<Vm> vmList = new ArrayList<>();
    private final List<Cloudlet> cloudletList = new ArrayList<>();
    private final List<MECServer> mecServers = new ArrayList<>();
    private long startTime;

    public UAVMECSimulation(SimulationConfig config) {
        this.config = config;
        this.simulation = new CloudSim();
        simulation.terminateAt(1000); // ✅ Stop after 1000 seconds of simulated time
        this.metricsCollector = new MetricsCollector(10_000);

        CostModel costModel = new CostModel(
                config.getComputeCost(),
                config.getBandwidthCost(),
                config.getLatencyPenalty(),
                config.getEnergyCost()
        );
        this.optimizer = new CostOptimizer(costModel, metricsCollector);
        this.broker = new DatacenterBrokerSimple(simulation);
    }

    /** Build and run the simulation */
    public void run() {
        logger.info("Starting UAV-MEC Simulation: {}", config);
        startTime = System.currentTimeMillis();

        try {
            Datacenter datacenter = createDatacenter();
            logger.info("Created datacenter with hosts");

            registerEventListeners();
            logger.info("Registered event listeners");

            createVms();
            logger.info("Created {} VMs", vmList.size());

            createCloudlets();
            logger.info("Created {} tasks", cloudletList.size());

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);
            logger.info("Submitted VMs and cloudlets to broker");

            logger.info("Starting simulation execution...");
            simulation.start();
            logger.info("Simulation completed");

            processResults();

        } catch (Exception e) {
            logger.error("Simulation failed", e);
            throw new RuntimeException("Simulation execution failed", e);
        } finally {
            metricsCollector.shutdown();
        }
    }

    /** Create datacenter with hosts */
    private Datacenter createDatacenter() {
        List<Host> hostList = new ArrayList<>();
        for (int i = 0; i < config.getVmCount(); i++) {
            hostList.add(createHost());
            mecServers.add(new MECServer("MEC_" + i, 10_000.0, 4096.0, 100_000.0));
        }
        return new DatacenterSimple(simulation, hostList);
    }

    /** Create individual host */
    private Host createHost() {
        long mips = 10_000, ram = 8192, storage = 1_000_000, bandwidth = 10_000;
        List<Pe> peList = Arrays.asList(new PeSimple(mips), new PeSimple(mips));
        return new HostSimple(ram, bandwidth, storage, peList);
    }

    /** Register event listeners */
    private void registerEventListeners() {
        AbstractEventListener listener = new AbstractEventListener(metricsCollector);

        simulation.addOnEventProcessingListener(event -> {
            Object data = event.getData();
            if (data instanceof CloudletEventInfo) {
                // Extract the Cloudlet object from the event info
                listener.onCloudletProcessing(((CloudletEventInfo) data).getCloudlet());
            } else if (data instanceof VmEventInfo) {
                // Extract the VM object from the event info
                listener.onVmCreated(((VmEventInfo) data).getVm());
            }
        });

        simulation.addOnSimulationStartListener(ev -> listener.onSimulationStart());
// 7.1.2 doesn’t expose a “stop” event; use pause as the nearest hook
        simulation.addOnSimulationPauseListener(ev -> listener.onSimulationEnd());

    }

    /** Create VMs */
    private void createVms() {
        long mips = 20000, ram = 8192;
        int bw = 10000000;
        for (int i = 0; i < config.getVmCount(); i++) {
            Vm vm = new VmSimple(mips, 1).setRam(ram).setBw(bw);
            vmList.add(vm);
        }
    }

    /** Create Cloudlets (tasks) */
    private void createCloudlets() {
        Random rnd = new Random(config.getRandomSeed());
        var util = new UtilizationModelFull();
        for (int i = 0; i < config.getTaskCount(); i++) {
            long length = 1000 + rnd.nextInt(9000);
            long fileSize = 100 + rnd.nextInt(900);
            long outputSize = 50 + rnd.nextInt(450);
            Cloudlet c = new CloudletSimple(length, 1)
                    .setFileSize(fileSize)
                    .setOutputSize(outputSize)
                    .setUtilizationModelCpu(util)
                    .setUtilizationModelRam(util)
                    .setUtilizationModelBw(util);
            cloudletList.add(c);
        }
    }

    /** Process simulation results */
    private void processResults() {
        long execTime = System.currentTimeMillis() - startTime;
        logger.info("\n=== Simulation Results ===");
        logger.info("Execution time: {} ms", execTime);
        logger.info("Metrics collected: {}", metricsCollector.getTotalMetricsCount());
        logger.info("Optimization decisions: {}", optimizer.getOptimizationDecisionsCount());

        List<MetricEntry> all = metricsCollector.getAllMetrics();
        String ts = String.valueOf(System.currentTimeMillis());
        String csv = "results/metrics_" + ts + ".csv";
        String json = "results/analysis_" + ts + ".json";
        String report = "results/report_" + ts + ".txt";

        MetricsExporter.exportToCSV(csv, all);
        MetricsExporter.exportToJSON(json, all);

        String summary = MetricsExporter.generateSummaryReport(all);
        try {
            java.nio.file.Files.write(java.nio.file.Paths.get(report), summary.getBytes());
        } catch (Exception e) {
            logger.error("Failed to write report file", e);
        }
        StatisticalAnalyzer.analyzeAndPrintMetrics(all);
        logger.info("Results exported to results/ directory");
    }

    /** Entry point */
    public static void main(String[] args) {
        SimulationConfig config = new SimulationConfig()
                .withSimulationTime(3600.0)
                .withVmCount(500)
                .withTaskCount(200)
                .withTaskArrivalRate(0.1)
                .withCostModel(0.0001, 0.00001, 0.01, 0.1)
                .withRandomSeed(42);

        new UAVMECSimulation(config).run();

    }
}
