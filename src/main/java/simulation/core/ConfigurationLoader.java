package simulation.core;

import org.yaml.snakeyaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class ConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

    @SuppressWarnings("unchecked")
    public static SimulationConfig loadFromYAML(String path) {
        SimulationConfig cfg = new SimulationConfig();
        try (InputStream in = Files.newInputStream(Paths.get(path))) {
            Map<String, Object> root = new Yaml().load(in);
            if (root == null) return cfg;

            // Top-level simple fields
            cfg.setSimulationTime(getDouble(root, "simulationTime", cfg.getSimulationTime()));
            cfg.setRandomSeed(getLong(root, "randomSeed", cfg.getRandomSeed()));
            cfg.setResultsDirectory(getString(root, "resultsDirectory", cfg.getResultsDirectory()));

            // Datacenter
            Map<String, Object> dc = getMap(root, "datacenter");
            if (dc != null) {
                cfg.setHostCount(getInt(dc, "hostCount", cfg.getHostCount()));
                cfg.setHostMips(getInt(dc, "cpuCapacity", cfg.getHostMips()));
                cfg.setHostPes(getInt(dc, "pes", cfg.getHostPes()));
                cfg.setHostRam(getInt(dc, "ram", cfg.getHostRam()));
                cfg.setHostBandwidth(getInt(dc, "bandwidth", cfg.getHostBandwidth()));
                cfg.setHostStorage(getInt(dc, "storage", cfg.getHostStorage()));
            }

            // VMs
            Map<String, Object> vms = getMap(root, "vms");
            if (vms != null) {
                cfg.setVmCount(getInt(vms, "count", cfg.getVmCount()));
                cfg.setVmMips(getInt(vms, "cpuCapacity", cfg.getVmMips()));
                cfg.setVmRam(getInt(vms, "ramCapacity", cfg.getVmRam()));
                cfg.setVmBandwidth(getInt(vms, "bandwidth", cfg.getVmBandwidth()));
                cfg.setVmSize(getLong(vms, "size", cfg.getVmSize()));
            }

            // Workload
            Map<String, Object> workload = getMap(root, "workload");
            if (workload != null) {
                cfg.setTaskCount(getInt(workload, "taskCount", cfg.getTaskCount()));
                cfg.setArrivalRate(getDouble(workload, "arrivalRate", cfg.getArrivalRate()));

                Map<String, Object> comp = getMap(workload, "taskComputeRange");
                if (comp != null) {
                    cfg.setTaskComputeMin(getInt(comp, "min", cfg.getTaskComputeMin()));
                    cfg.setTaskComputeMax(getInt(comp, "max", cfg.getTaskComputeMax()));
                }

                Map<String, Object> data = getMap(workload, "taskDataRange");
                if (data != null) {
                    cfg.setTaskDataMin(getInt(data, "min", cfg.getTaskDataMin()));
                    cfg.setTaskDataMax(getInt(data, "max", cfg.getTaskDataMax()));
                }

                Map<String, Object> output = getMap(workload, "taskOutputRange");
                if (output != null) {
                    cfg.setTaskOutputMin(getInt(output, "min", cfg.getTaskOutputMin()));
                    cfg.setTaskOutputMax(getInt(output, "max", cfg.getTaskOutputMax()));
                }

                Map<String, Object> deadline = getMap(workload, "deadlineRange");
                if (deadline != null) {
                    cfg.setDeadlineMin(getDouble(deadline, "min", cfg.getDeadlineMin()));
                    cfg.setDeadlineMax(getDouble(deadline, "max", cfg.getDeadlineMax()));
                }
            }

            // Cost model
            Map<String, Object> cost = getMap(root, "costModel");
            if (cost != null) {
                cfg.setComputeCost(getDouble(cost, "compute", cfg.getComputeCost()));
                cfg.setBandwidthCost(getDouble(cost, "bandwidth", cfg.getBandwidthCost()));
                cfg.setLatencyPenalty(getDouble(cost, "latencyPenalty", cfg.getLatencyPenalty()));
                cfg.setEnergyCost(getDouble(cost, "energy", cfg.getEnergyCost()));
            }

            return cfg;
        }
        catch (Exception e) {
            logger.error("Failed to load config, using defaults", e);
            return cfg;
        }
    }

    // ===== Helpers =====

    private static Map<String, Object> getMap(Map<String, Object> root, String key) {
        Object o = root.get(key);
        return (o instanceof Map ? (Map<String, Object>) o : null);
    }

    private static int getInt(Map<String, Object> m, String k, int def) {
        Object o = m.get(k);
        return (o instanceof Number) ? ((Number)o).intValue() : def;
    }

    private static long getLong(Map<String, Object> m, String k, long def) {
        Object o = m.get(k);
        return (o instanceof Number) ? ((Number)o).longValue() : def;
    }

    private static double getDouble(Map<String, Object> m, String k, double def) {
        Object o = m.get(k);
        return (o instanceof Number) ? ((Number)o).doubleValue() : def;
    }

    private static String getString(Map<String, Object> m, String k, String def) {
        Object o = m.get(k);
        return o != null ? o.toString() : def;
    }
}
