package simulation.model;

/**
 * Communication Model: SINR, path loss, and data rate calculations
 * Implements LoS/NLoS path loss model and Shannon capacity formula
 */
public class CommunicationModel {
    
    // ===== Physical Layer Constants =====
    private static final double FREQUENCY = 2.4e9; // 2.4 GHz
    private static final double SPEED_OF_LIGHT = 3e8; // m/s
    private static final double NOISE_POWER = 1e-13; // W (thermal noise)
    private static final double TRANSMISSION_POWER = 0.1; // W
    private static final double BANDWIDTH_PER_PRB = 180e3; // 180 kHz per PRB

    // ===== LoS Probability Parameters =====
    private static final double LOS_PARAM_A = 12.0;
    private static final double LOS_PARAM_B = 0.11;

    // ===== Path Loss Exponents =====
    private static final double PATH_LOSS_EXP_LOS = 2.0; // Free space
    private static final double PATH_LOSS_EXP_NLOS = 2.8; // Obstructed

    /**
     * Calculate Line-of-Sight probability based on elevation angle
     * PLoS(θ) = 1 / (1 + A * exp(-B * θ))
     */
    public static double calculateLoSProbability(double elevationAngleDegrees) {
        return 1.0 / (1.0 + LOS_PARAM_A * Math.exp(-LOS_PARAM_B * elevationAngleDegrees));
    }

    /**
     * Calculate free-space path loss at reference distance (1 meter)
     * PL0 = (4π / λ)²
     */
    private static double calculateFreeSpacePathLoss() {
        double wavelength = SPEED_OF_LIGHT / FREQUENCY;
        return Math.pow((4 * Math.PI) / wavelength, 2);
    }

    /**
     * Calculate path loss between IoT and UAV
     * Combines LoS and NLoS models weighted by probability
     */
    public static double calculatePathLoss(double distance3D, double elevationAngle) {
        double pLos = calculateLoSProbability(elevationAngle);
        double pl0 = calculateFreeSpacePathLoss();

        // LoS path loss: PL_LoS = PL0 + 20*log10(d)
        double plLos = pl0 * Math.pow(distance3D, PATH_LOSS_EXP_LOS);

        // NLoS path loss: PL_NLoS = PL0 + 20*log10(d) + additional attenuation
        double plNlos = pl0 * Math.pow(distance3D, PATH_LOSS_EXP_NLOS);

        // Weighted combination
        return pLos * plLos + (1 - pLos) * plNlos;
    }

    /**
     * Calculate SINR (Signal-to-Interference-plus-Noise Ratio)
     * SINR = Pt / (PL * (I + N0))
     */
    public static double calculateSINR(double distance3D, double elevationAngle, 
                                       double interferenceSum) {
        double pathLoss = calculatePathLoss(distance3D, elevationAngle);
        double receivedPower = TRANSMISSION_POWER / pathLoss;
        double denominator = interferenceSum + NOISE_POWER;
        
        if (denominator <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return receivedPower / denominator;
    }

    /**
     * Calculate data rate using Shannon capacity
     * R = B * log2(1 + SINR)
     */
    public static double calculateDataRate(double sinr, int numPRBs) {
        double totalBandwidth = numPRBs * BANDWIDTH_PER_PRB;
        double capacity = totalBandwidth * Math.log(1 + sinr) / Math.log(2); // bits/sec
        return Math.max(0, capacity);
    }

    /**
     * Calculate transmission delay
     * t_tx = Data Size / Data Rate
     */
    public static double calculateTransmissionDelay(long dataSizeKB, double dataRateBps) {
        if (dataRateBps <= 0) return Double.POSITIVE_INFINITY;
        
        double dataSizeBits = dataSizeKB * 1024.0 * 8.0;
        return dataSizeBits / dataRateBps; // seconds
    }

    /**
     * Calculate processing delay at MEC server
     * t_proc = Computation / Server Capacity
     */
    public static double calculateProcessingDelay(long computeMI, int serverMipsCapacity) {
        if (serverMipsCapacity <= 0) return Double.POSITIVE_INFINITY;
        return (double) computeMI / serverMipsCapacity; // seconds
    }

    /**
     * Calculate total end-to-end latency
     * t_total = t_tx + t_proc
     */
    public static double calculateTotalLatency(long computeMI, long dataSizeKB,
                                              int serverMipsCapacity, double sinr) {
        double txDelay = calculateTransmissionDelay(dataSizeKB, calculateDataRate(sinr, 1));
        double procDelay = calculateProcessingDelay(computeMI, serverMipsCapacity);
        return txDelay + procDelay;
    }
}