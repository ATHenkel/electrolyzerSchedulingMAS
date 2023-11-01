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
		double lambda = this.schedulingAgent.getInternalDataModel().getLambda();
		double demand = this.schedulingAgent.getInternalDataModel().getDSMInformation().getProductionQuantityForPeriod(currentPeriod);
		
		double increment = 0.1;//TODO Inkrement anpassen Z
		double x = this.schedulingAgent.getInternalDataModel().getX();

		// Check whether the electrolyser is in production mode
		double minDiffToZero = Double.POSITIVE_INFINITY; // Initialize minDiffToZero with a high value
		double minZ = minPower; //Initialize minZ with minPower
		
		
		// Test
		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}
		//---------
		

		if (stateProduction) {
			// Loop over the range of values of the realizable load of the electrolyzer
			for (double z = minPower; z <= maxPower; z += increment) {

				//TODO Z-anpasssen?
				//double dzProduction = sumProduction + ProductionCoefficientA * Math.pow(z, 2) + ProductionCoefficientB * z + ProductionCoefficientC - demand + (lambda*(x - z)/100)*0.04494;
				double dzProduction = sumProduction + ProductionCoefficientA * Math.pow(z, 2) + ProductionCoefficientB * z + ProductionCoefficientC - demand;
				double diffToZero = Math.abs(dzProduction - 0);

				// If the current difference to 0 is smaller than the previous minimum, update the minimum and the Z-value
				if (diffToZero < minDiffToZero) {
					minDiffToZero = diffToZero;
					minZ = z;
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
