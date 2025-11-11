
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

    public static SimulationConfig loadFromYAML(String filePath) {
        try (InputStream input = Files.newInputStream(Paths.get(filePath))) {
            Yaml yaml = new Yaml();
            Map<String, Map<String, Object>> config = yaml.load(input);

            SimulationConfig simConfig = new SimulationConfig();

            // Load simulation parameters
            Map<String, Object> simParams = config.get("simulation");
            if (simParams != null) {
                Number time = (Number) simParams.get("simulationTime");
                if (time != null) simConfig.withSimulationTime(time.doubleValue());

                Number seed = (Number) simParams.get("randomSeed");
                if (seed != null) simConfig.withRandomSeed(seed.longValue());
            }

            // Load infrastructure
            Map<String, Object> infra = config.get("infrastructure");
            if (infra != null) {
                Map<String, Object> vms = (Map<String, Object>) infra.get("vms");
                if (vms != null) {
                    Number vmCount = (Number) vms.get("count");
                    if (vmCount != null) simConfig.withVmCount(vmCount.intValue());
                }
            }

            // Load workload
            Map<String, Object> workload = config.get("workload");
            if (workload != null) {
                Number taskCount = (Number) workload.get("taskCount");
                if (taskCount != null) simConfig.withTaskCount(taskCount.intValue());

                Number arrivalRate = (Number) workload.get("arrivalRate");
                if (arrivalRate != null) simConfig.withTaskArrivalRate(arrivalRate.doubleValue());
            }

            // Load cost model
            Map<String, Object> costModel = config.get("costModel");
            if (costModel != null) {
                Number compute = (Number) costModel.get("computeCostPerCpuHour");
                Number bandwidth = (Number) costModel.get("bandwidthCostPerGB");
                Number latency = (Number) costModel.get("latencyPenaltyPerMs");
                Number energy = (Number) costModel.get("energyCostPerKWh");

                if (compute != null && bandwidth != null && latency != null && energy != null) {
                    simConfig.withCostModel(
                        compute.doubleValue(),
                        bandwidth.doubleValue(),
                        latency.doubleValue(),
                        energy.doubleValue()
                    );
                }
            }

            logger.info("Loaded configuration from {}", filePath);
            logger.info("Config: {}", simConfig);

            return simConfig;

        } catch (Exception e) {
            logger.error("Failed to load configuration from {}", filePath, e);
            throw new RuntimeException("Configuration loading failed", e);
        }
    }

    public static SimulationConfig loadDefaults() {
        return new SimulationConfig();
    }
}
