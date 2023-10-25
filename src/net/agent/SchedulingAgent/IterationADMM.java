package net.agent.SchedulingAgent;

public class IterationADMM {
    private int period;
    private int iteration;
    private double productionQuantity;
    private double energyDemand;
	private double x;
    private double z;
    private double mLCOH;

    public IterationADMM(int period, int iteration, double productionQuantity, double energyDemand, double x, double z, double mLCOH) {
        this.period = period;
        this.iteration = iteration;
        this.productionQuantity = productionQuantity;
        this.energyDemand = energyDemand;
        this.x = x;
        this.z = z;
        this.mLCOH = mLCOH;
    }

    public double getX() {
		return x;
	}

	public double getZ() {
		return z;
	}

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

	public double getmLCOH() {
		return mLCOH;
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

