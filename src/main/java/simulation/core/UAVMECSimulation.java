package simulation.core;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.analysis.AnalysisReport;
import simulation.analysis.MetricsExporter;
import simulation.analysis.StatisticalAnalyzer;
import simulation.events.MetricsCollector;
import simulation.model.*;
import simulation.optimization.*;

import java.util.*;

public class UAVMECSimulation {

    private static final Logger logger = LoggerFactory.getLogger(UAVMECSimulation.class);

    private final CloudSim simulation;
    private final DatacenterBrokerSimple broker;
    private final SimulationConfig config;

    private final CostOptimizer optimizer;
    private final List<Vm> vmList = new ArrayList<>();
    private final List<Cloudlet> cloudlets = new ArrayList<>();
    private final List<MECServer> mecServers = new ArrayList<>();
    private final Map<Cloudlet, Task> cloudletTaskMap = new HashMap<>();

    private long startClock;
    private int tasksCompleted = 0;
    private int tasksWithinDeadline = 0;
    private double totalLatency = 0;
    private double totalCost = 0;

    public UAVMECSimulation(SimulationConfig cfg) {
        this.config = cfg;
        this.simulation = new CloudSim();
        simulation.terminateAt(cfg.getSimulationTime());
        this.broker = new DatacenterBrokerSimple(simulation);

        this.optimizer = new CostOptimizer(
            new CostModel(cfg.getComputeCost(), cfg.getBandwidthCost(), cfg.getLatencyPenalty(), cfg.getEnergyCost()),
            new MetricsCollector(10_000)
        );
    }

    public void run() {
        logger.info("Starting UAV-MEC Simulation…");
        startClock = System.currentTimeMillis();

        Datacenter dc = createDatacenter();
        logger.info("✓ Datacenter created with {} hosts", config.getHostCount());

        createVMs();
        logger.info("✓ Created {} VMs for MEC servers", vmList.size());

        createCloudlets();
        logger.info("✓ Generated {} IoT tasks", cloudlets.size());

        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudlets);
        logger.info("✓ Submitted {} cloudlets to broker", cloudlets.size());

        // ✅ CORRECTED: Use CloudSim native method instead of broker listener
        simulation.start();
        
        logger.info("✓ Simulation completed");

        // After simulation completes, process results from finished cloudlets
        processResults();
        exportResults();
    }

    private Datacenter createDatacenter() {
        List<Host> hosts = new ArrayList<>();

        for (int i = 0; i < config.getHostCount(); i++) {
            List<Pe> peList = new ArrayList<>();
            for (int j = 0; j < config.getHostPes(); j++)
                peList.add(new PeSimple(config.getHostMips()));

            Host h = new HostSimple(config.getHostRam(),
                    config.getHostBandwidth(),
                    config.getHostStorage(),
                    peList);
            hosts.add(h);

            MECServer server = new MECServer("MEC_" + i,
                    config.getHostMips(),
                    config.getHostRam(),
                    config.getHostStorage());
            mecServers.add(server);
            optimizer.registerServer(server);
        }

        return new DatacenterSimple(simulation, hosts);
    }

    private void createVMs() {
        for (int i = 0; i < config.getVmCount(); i++) {
            Vm vm = new VmSimple(config.getVmMips(), 1)
                    .setRam(config.getVmRam())
                    .setSize(config.getVmSize())
                    .setBw(config.getVmBandwidth());
            vmList.add(vm);
        }
    }

    private void createCloudlets() {
        Random rnd = new Random(config.getRandomSeed());
        UtilizationModelFull util = new UtilizationModelFull();

        double arrivalTime = 0;

        for (int i = 0; i < config.getTaskCount(); i++) {
            arrivalTime += -Math.log(1 - rnd.nextDouble()) / config.getArrivalRate();

            long compute = config.getTaskComputeMin()
                    + rnd.nextInt((int)(config.getTaskComputeMax() - config.getTaskComputeMin()));
            long data = config.getTaskDataMin()
                    + rnd.nextInt((int)(config.getTaskDataMax() - config.getTaskDataMin()));
            long outSize = config.getTaskOutputMin()
                    + rnd.nextInt((int)(config.getTaskOutputMax() - config.getTaskOutputMin()));
            double deadline = config.getDeadlineMin()
                    + rnd.nextDouble() * (config.getDeadlineMax() - config.getDeadlineMin());

            Task t = new Task("TASK_" + i, arrivalTime, compute, data, deadline);

            Cloudlet c = new CloudletSimple(compute, 1)
                    .setFileSize(data)
                    .setOutputSize(outSize)
                    .setUtilizationModelCpu(util)
                    .setUtilizationModelRam(util)
                    .setUtilizationModelBw(util);

            c.setSubmissionDelay(arrivalTime);
            c.setBroker(broker);

            cloudlets.add(c);
            cloudletTaskMap.put(c, t);
            optimizer.registerTask(t);
        }
    }

    private void processResults() {
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        logger.info("Processing {} finished cloudlets", finishedCloudlets.size());

        for (Cloudlet cloudlet : finishedCloudlets) {
            if (cloudlet == null || !cloudlet.isFinished()) continue;
            
            Task task = cloudletTaskMap.get(cloudlet);
            
            // Count ALL finished cloudlets, even if no task mapping
            double executionTime = cloudlet.getFinishTime() - cloudlet.getSubmissionDelay();
            tasksCompleted++;
            totalLatency += executionTime;
            
            if (task != null) {
                double cost = calculateTaskCost(cloudlet, task);
                totalCost += cost;
                
                if (executionTime <= task.getDeadline()) {
                    tasksWithinDeadline++;
                }
            } else {
                totalCost += 0.1; // Default cost for unmapped
            }
        }
        
        logger.info("✓ Processed: {}/{} tasks, {} met deadline",
            tasksCompleted, config.getTaskCount(), tasksWithinDeadline);
    }


    private double calculateTaskCost(Cloudlet cloudlet, Task task) {
        double executionTime = cloudlet.getFinishTime() - cloudlet.getSubmissionDelay();
        double computeTime = (double) cloudlet.getLength() / (double) cloudlet.getVm().getMips();
        double dataCost = (cloudlet.getFileSize() + cloudlet.getOutputSize()) * 0.001; // per MB
        double computeCost = computeTime * 0.1; // per ms
        double latencyPenalty = (executionTime > task.getDeadline()) ? 
                               (executionTime - task.getDeadline()) * 0.01 : 0;
        
        return dataCost + computeCost + latencyPenalty;
    }

    private void exportResults() {
        String ts = String.valueOf(System.currentTimeMillis());
        long duration = System.currentTimeMillis() - startClock;

        try {
            // Create metric entries from results
            List<MetricEntry> allMetrics = createMetricsFromResults();

            // Export to files
            MetricsExporter.exportToCSV(
                config.getResultsDirectory() + "/metrics_" + ts + ".csv", 
                allMetrics
            );
            MetricsExporter.exportToJSON(
                config.getResultsDirectory() + "/analysis_" + ts + ".json", 
                allMetrics
            );

            // Print summary
            logger.info("\n" + "=".repeat(70));
            logger.info("SIMULATION RESULTS SUMMARY");
            logger.info("=".repeat(70));
            logger.info("Total Execution Time: {} ms", duration);
            logger.info("Tasks Completed: {} / {}", tasksCompleted, config.getTaskCount());
            
            if (tasksCompleted > 0) {
                double avgLatency = totalLatency / tasksCompleted;
                double avgCost = totalCost / tasksCompleted;
                double slaCompliance = (100.0 * tasksWithinDeadline / tasksCompleted);
                
                logger.info(String.format("Tasks Meeting Deadline: %d (SLA Compliance: %.2f%%)", tasksWithinDeadline, slaCompliance));
                logger.info(String.format("Average Latency: %.2f ms", avgLatency));
                logger.info(String.format("Average Cost: $%.4f", avgCost));
                logger.info(String.format("Total Cost: $%.2f", totalCost));

            }

            
            logger.info("Results exported to: {}/", config.getResultsDirectory());
            logger.info("=".repeat(70) + "\n");

        } catch (Exception e) {
            logger.error("Failed to export results", e);
        }
    }

    private List<MetricEntry> createMetricsFromResults() {
        List<MetricEntry> metrics = new ArrayList<>();
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();

        for (Cloudlet cloudlet : finishedCloudlets) {
            double executionTime = cloudlet.getFinishTime() - cloudlet.getSubmissionDelay();
            boolean metDeadline = false;
            double cost = 0.1;
            Task task = cloudletTaskMap.get(cloudlet);

            if (task != null) {
                metDeadline = executionTime <= task.getDeadline();
                cost = calculateTaskCost(cloudlet, task);
            }

            MetricEntry metric = new MetricEntry(
                "CLOUDLET_FINISHED",
                cloudlet.getId(),
                metDeadline,
                (long) cloudlet.getFinishTime()
            );
            metric.setLatency(executionTime);
            metric.setCost(cost);
            metrics.add(metric);
        }
        return metrics;
    }

    public static void main(String[] args) {
        try {
            String configPath = args.length > 0 ? args[0] : "src/main/resources/config.yaml";
            SimulationConfig config = ConfigurationLoader.loadFromYAML(configPath);
            logger.info("Configuration loaded: {}", config);
            
            new UAVMECSimulation(config).run();
        } catch (Exception e) {
            logger.error("Fatal error", e);
            System.exit(1);
        }
    }
}