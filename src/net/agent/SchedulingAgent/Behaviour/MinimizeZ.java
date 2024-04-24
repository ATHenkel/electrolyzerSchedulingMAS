package net.agent.SchedulingAgent.Behaviour;

import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

public class MinimizeZ extends OneShotBehaviour {

	// Instantiate Agent
	public MinimizeZ(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}

	SchedulingAgent schedulingAgent;

	public double calculateProductionQuantity(double x) {
		
		double ProductionCoefficientA = this.schedulingAgent.getInternalDataModel().getProductionCoefficientA();
		double ProductionCoefficientB = this.schedulingAgent.getInternalDataModel().getProductionCoefficientB();
		double ProductionCoefficientC = this.schedulingAgent.getInternalDataModel().getProductionCoefficientC();
		boolean stateProduction = this.schedulingAgent.getInternalDataModel().isStateProduction();
		
		double productionQuantity;

		if (stateProduction) {
			productionQuantity = ProductionCoefficientA * Math.pow(x, 2) + ProductionCoefficientB * x
					+ ProductionCoefficientC;
		} else {
			productionQuantity = 0;
		}
		return productionQuantity;
	}

	public double minimizeLz() {
		
		// Get Information from the InternalDataModel
		int currentPeriod = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();
		double minPower = this.schedulingAgent.getInternalDataModel().getMinPower();
		double maxPower = this.schedulingAgent.getInternalDataModel().getMaxPower();

		boolean stateProduction = this.schedulingAgent.getInternalDataModel().isStateProduction();
		double ProductionCoefficientA = this.schedulingAgent.getInternalDataModel().getProductionCoefficientA();
		double ProductionCoefficientB = this.schedulingAgent.getInternalDataModel().getProductionCoefficientB();
		double ProductionCoefficientC = this.schedulingAgent.getInternalDataModel().getProductionCoefficientC();
		double sumProduction = this.schedulingAgent.getInternalDataModel().getSumProduction();
		double demand = this.schedulingAgent.getInternalDataModel().getDSMInformation().getProductionQuantityForPeriod(currentPeriod);
		double increment = 0.001; 

		// Check whether the electrolyser is in production mode
		double minDiffToZero = Double.POSITIVE_INFINITY; // Initialize minDiffToZero with a high value
		double minZ = minPower; //Initialize minZ with minPower

		if (stateProduction) {
			// Loop over the range of values of the realizable load of the electrolyzer
			for (double z = minPower; z <= maxPower + increment; z += increment) {
				
				if (z > maxPower) {
					// Set z to the maximum value if the increment exceeds the maximum
					z= maxPower;
				}
				
				double dzProduction = sumProduction + ProductionCoefficientA * Math.pow(z, 2) + ProductionCoefficientB * z + ProductionCoefficientC - demand;
				double diffToZero = Math.abs(dzProduction - 0);

				// If the current difference to 0 is smaller than the previous minimum, update the minimum and the Z-value
				if (diffToZero < minDiffToZero) {
					minDiffToZero = diffToZero;
					minZ = z;
				}
				
				// When z has been set to the maximum, end the loop
				if (z == maxPower) {
					break;
				}
			}
		} else {
			minZ = 0;
		}

		this.schedulingAgent.getInternalDataModel().setZ(minZ);
		return minZ;
	}

	@Override
	public void action() {

		// Minimize Lz
		minimizeLz();
		
		// Next Behaviour to be executed
		DualUpdate dualUpdate = new DualUpdate(schedulingAgent);
		this.schedulingAgent.addBehaviour(dualUpdate);
	}

}
