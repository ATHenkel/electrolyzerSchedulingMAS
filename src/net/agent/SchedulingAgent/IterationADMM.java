package net.agent.SchedulingAgent;

/**
 * Represents a single iteration of the ADMM optimization process for scheduling.
 */
public class IterationADMM {
    private int period;
    private int iteration;
    private double productionQuantity;
    private double energyDemand;
    private double x; // decision variable x
    private double z; // decision variable z
    private double mLCOH; // Marginal Levelized Cost of Hydrogen

    /**
     * Constructs an IterationADMM instance with specified parameters.
     *
     * @param period            The scheduling period.
     * @param iteration         The iteration number.
     * @param productionQuantity The amount of production in this iteration.
     * @param energyDemand      The energy demand for this period.
     * @param x                 The decision variable x for optimization.
     * @param z                 The decision variable z for optimization.
     * @param mLCOH             The marginal levelized cost of hydrogen.
     */
    public IterationADMM(int period, int iteration, double productionQuantity, double energyDemand, double x, double z, double mLCOH) {
        this.period = period;
        this.iteration = iteration;
        this.productionQuantity = productionQuantity;
        this.energyDemand = energyDemand;
        this.x = x;
        this.z = z;
        this.mLCOH = mLCOH;
    }

    // Getter methods
    public int getPeriod() {
        return period;
    }

    public int getIteration() {
        return iteration;
    }

    public double getProductionQuantity() {
        return productionQuantity;
    }

    public double getEnergyDemand() {
        return energyDemand;
    }

    public double getX() {
        return x;
    }

    public double getZ() {
        return z;
    }

    public double getmLCOH() {
        return mLCOH;
    }

    // Setter methods
    public void setProductionQuantity(double productionQuantity) {
        this.productionQuantity = productionQuantity;
    }

    public void setmLCOH(double mLCOH) {
        this.mLCOH = mLCOH;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setX(double x) {
        this.x = x;
    }
}
