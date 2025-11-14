# UAV-MEC CloudSim Simulation Platform

A modular, enterprise-grade simulation framework for UAV-assisted Multi-access Edge Computing (MEC) with advanced task scheduling, cost optimization, and SLA analysis.

---

##  Project Structure

```
uav-mec-sim/
│
├── README.md                              # This file
├── config.yaml                            # Main simulation configuration (edit this!)
├── pom.xml                                # Maven build configuration
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── simulation/
│   │   │       ├── core/                  # Core simulation engine
│   │   │       │   ├── UAVMECSimulation.java              # Main entry point
│   │   │       │   ├── SimulationConfig.java              # Config management
│   │   │       │   └── ConfigurationLoader.java           # YAML parser
│   │   │       │
│   │   │       ├── model/                 # Domain models
│   │   │       │   ├── Task.java                          # IoT task definition
│   │   │       │   ├── MECServer.java                     # MEC server entity
│   │   │       │   ├── MetricEntry.java                   # Metrics data structure
│   │   │       │   └── UAVEntity.java                     # UAV definition
│   │   │       │
│   │   │       ├── optimization/          # Optimization algorithms
│   │   │       │   ├── CostOptimizer.java                 # Cost/SLA optimization
│   │   │       │   ├── CostModel.java                     # Cost calculation model
│   │   │       │   ├── AdaptivePolicy.java                # Adaptive scheduling
│   │   │       │   └── OptimizationPolicy.java            # Policy interface
│   │   │       │
│   │   │       ├── events/                # Event listeners & metrics
│   │   │       │   ├── MetricsCollector.java              # Collects simulation metrics
│   │   │       │   ├── SimulationEventListener.java       # Event listener interface
│   │   │       │   ├── AbstractEventListener.java         # Base listener
│   │   │       │   └── VmCreationListener.java            # VM lifecycle tracking
│   │   │       │
│   │   │       ├── analysis/              # Results analysis & export
│   │   │       │   ├── MetricsExporter.java               # CSV/JSON export
│   │   │       │   ├── StatisticalAnalyzer.java           # Statistical analysis
│   │   │       │   ├── AnalysisReport.java                # Report generation
│   │   │       │   └── PerformanceMonitor.java            # Runtime monitoring
│   │   │       │
│   │   │       └── util/                  # Utilities
│   │   │           └── (logger, helper classes)
│   │   │
│   │   └── resources/
│   │       ├── config.yaml                # Default configuration
│   │       └── logback.xml                # Logging configuration
│   │
│   └── test/
│       └── java/
│           └── simulation/
│               ├── CostOptimizationTest.java
│               ├── MetricsCollectorTest.java
│               └── SimulationIntegrationTest.java
│
├── results/                               # Output directory (auto-created)
│   ├── metrics_1763099902445.csv          # Raw metrics (50 tasks, all details)
│   ├── analysis_1763099902445.json        # Structured analysis results
│   └── ...
│
├── target/                                # Maven build output
│   ├── classes/
│   ├── uav-mec-simulation.jar             # Executable JAR (fat JAR with deps)
│   └── ...
│
└── .gitignore                             # Git ignore rules
```

---

##  Data Flow & Processing Pipeline

```
USER INPUT (config.yaml)
        ↓
   [ConfigurationLoader]
        ↓
   [SimulationConfig] → Parameters injected
        ↓
   [UAVMECSimulation.run()]
        ├─→ [createDatacenter()] → 10 MEC hosts, CloudSim infrastructure
        ├─→ [createVMs()] → 200 VMs across hosts
        ├─→ [createCloudlets()] → 50 IoT tasks (Poisson arrivals)
        │                  ↓
        │        [Task → Cloudlet Mapping]
        │
        ├─→ [K-means Clustering] → Optimal UAV positioning
        │
        └─→ [simulation.start()] → CloudSim Event Engine
                   ↓
            [Event Processing Loop]
                   ├─→ Task submission to VMs
                   ├─→ VM scheduling decisions
                   ├─→ Task execution on resources
                   └─→ Task completion
                   ↓
            [processResults()]
                   ├─→ Query finished cloudlets
                   ├─→ Calculate latency, cost, SLA
                   ├─→ Store metrics
                   ↓
            [createMetricsFromResults()]
                   ├─→ Build MetricEntry for each task
                   ↓
            [exportResults()]
                   ├─→ MetricsExporter.exportToCSV()
                   ├─→ MetricsExporter.exportToJSON()
                   ├─→ StatisticalAnalyzer.analyzeAndPrintMetrics()
                   ↓
CONSOLE OUTPUT + ./results/metrics_*.csv + ./results/analysis_*.json
```

### Data Flow Details

1. **Configuration Phase:**
   - Load `config.yaml` → Parse into `SimulationConfig` object
   - Validate parameters (simulation time, VM count, task count, etc.)

2. **Initialization Phase:**
   - Instantiate CloudSim engine, datacenters, hosts, VMs
   - Pre-allocate resource pools for scalability

3. **Workload Generation:**
   - Generate 50 tasks with Poisson arrivals
   - Each task: compute demand, I/O size, output size, deadline
   - Map tasks to cloudlets for CloudSim

4. **Optimization (Optional):**
   - K-means clustering for UAV positioning
   - Cost model evaluates best resource allocation

5. **Execution:**
   - CloudSim's discrete-event engine drives simulation
   - Tasks submitted to broker → scheduled to VMs
   - VMs execute tasks respecting CPU/RAM/bandwidth constraints

6. **Metrics Collection:**
   - Upon task completion, capture: latency, deadline met, cost, energy
   - Aggregate per-task metrics for overall statistics

7. **Export & Analysis:**
   - Export raw metrics to CSV (per-task) and JSON (aggregated)
   - Print SLA compliance, avg latency, total cost to console
   - Generate visualizable datasets for Jupyter/Tableau

---

##  Architecture Overview

### Three-Tier Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  PRESENTATION / OUTPUT LAYER                                │
│  ├─ Console Logging (SLF4J)                                 │
│  ├─ CSV/JSON Exports (MetricsExporter)                      │
│  └─ Statistical Reports (StatisticalAnalyzer)               │
└─────────────────────────────────────────────────────────────┘
                           ↑
┌─────────────────────────────────────────────────────────────┐
│  SIMULATION / LOGIC LAYER                                   │
│  ├─ Core Engine (UAVMECSimulation)                          │
│  ├─ CloudSim+ Integration                                   │
│  ├─ Optimization (CostOptimizer, AdaptivePolicy)            │
│  ├─ Metrics Collection (MetricsCollector)                   │
│  └─ Resource Management (DatacenterBroker, VMs, Hosts)      │
└─────────────────────────────────────────────────────────────┘
                           ↑
┌─────────────────────────────────────────────────────────────┐
│  DATA / MODEL LAYER                                         │
│  ├─ Configuration (SimulationConfig, ConfigurationLoader)   │
│  ├─ Domain Models (Task, MECServer, MetricEntry, UAVEntity) │
│  ├─ Cost Model (CostModel)                                  │
│  └─ Policies (OptimizationPolicy, OffloadingPolicy)         │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

| Component | Role |
|-----------|------|
| **UAVMECSimulation** | Main orchestrator; initializes all components, runs simulation loop |
| **SimulationConfig** | Holds all tunable parameters (loaded from YAML) |
| **CloudSim Plus** | Discrete-event simulation engine (underlying framework) |
| **MetricsCollector** | Captures per-task and aggregate metrics |
| **CostOptimizer** | Evaluates cost/SLA trade-offs, K-means placement |
| **MetricsExporter** | Serializes results to CSV/JSON for analysis |
| **StatisticalAnalyzer** | Aggregates metrics, computes mean/stddev/compliance |

---

##  Quick Start (Execute in 2 Minutes)

### Step 1: Create Fast-Running config.yaml

Save as `src/main/resources/config.yaml`:

```yaml
# FAST CONFIG - Completes in ~2 minutes
simulationTime: 120              # 2 minute simulation
randomSeed: 42
resultsDirectory: results

# Datacenter / Hosts (small scale)
hostCount: 3                    # Just 3 hosts
hostMips: 10000
hostPes: 2
hostRam: 16384
hostBandwidth: 50000
hostStorage: 500000

# VMs (small scale)
vmCount: 20                     # Just 20 VMs
vmMips: 2500
vmRam: 1024
vmBandwidth: 2500
vmSize: 5000

# Workload (small, fast)
taskCount: 25                   # Just 25 tasks
arrivalRate: 0.2                # One every ~5 seconds
taskComputeMin: 500
taskComputeMax: 5000
taskDataMin: 50
taskDataMax: 5000
taskOutputMin: 5
taskOutputMax: 500
deadlineMin: 2.0
deadlineMax: 10.0

# Cost Model
computeCost: 0.0001
bandwidthCost: 0.00001
latencyPenalty: 0.00005
energyCost: 0.00005
```

### Step 2: Build

```bash
mvn clean package -DskipTests
```

**Expected output:**  
```
[INFO] BUILD SUCCESS
[INFO] Total time: 15s
```

### Step 3: Run

```bash
java -jar target/uav-mec-simulation.jar
```

**Expected execution time:** ~1-2 minutes

**Expected console output:**

```
INFO  Configuration loaded: SimulationConfig{simulationTime=120.0, taskCount=25, ...}
INFO  Starting UAV-MEC Simulation…
INFO  ✓ Datacenter created with 3 hosts
INFO  ✓ Created 20 VMs for MEC servers
INFO  ✓ Generated 25 IoT tasks
INFO  ✓ Positioned 1 UAVs using K-means clustering

... [CloudSim event log, hundreds of lines] ...

INFO  ? Simulation completed
INFO  Processing 25 finished cloudlets
INFO  ? Processed: 25/25 tasks, X met deadline
INFO  Exported 25 metrics to ./results/metrics_XXXXX.csv
INFO  Exported 25 metrics to ./results/analysis_XXXXX.json

======================================================================
SIMULATION RESULTS SUMMARY
======================================================================
Total Execution Time: 150 ms
Tasks Completed: 25 / 25
Tasks Meeting Deadline: 20 (SLA Compliance: 80.00%)
Average Latency: 245.67 ms
Average Cost: $0.1234
Total Cost: $3.09
Results exported to: ./results/
======================================================================
```

### Step 4: Check Results

```bash
ls -la results/
cat results/metrics_*.csv
cat results/analysis_*.json
```

---

##  Scaling for Larger Scenarios

For realistic/research scenarios, adjust config.yaml:

```yaml
simulationTime: 3600            # 1 hour
taskCount: 500                  # Large workload
hostCount: 20
vmCount: 200
arrivalRate: 0.5                # Higher arrival rate
```

**Expected runtime:** 30-90 seconds depending on hardware

---

##  Advanced Usage

### Custom Policies

Edit `AdaptivePolicy.java` to implement your own scheduling logic.

### Custom Cost Model

Modify `CostModel.java` coefficients for your pricing scheme.

### Visualization

Use `visualize_metrics.py` to plot results in Jupyter:

```bash
python visualize_metrics.py results/metrics_*.csv
```


##  Authors

[Ketan 252CS006, Stanzin 252CS033]

---
