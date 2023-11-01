package net.agent.SchedulingAgent.Behaviour;

import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

public class DualUpdate extends OneShotBehaviour {
	
	SchedulingAgent schedulingAgent;
	public DualUpdate(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}
	
	public double calculateGradientmLCOH(double x) {
		int currentPeriod = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();
		x = this.schedulingAgent.getInternalDataModel().getX();
		double PEL = this.schedulingAgent.getInternalDataModel().getPEL();
		double ProductionCoefficientA = this.schedulingAgent.getInternalDataModel().getProductionCoefficientA();
		double ProductionCoefficientB = this.schedulingAgent.getInternalDataModel().getProductionCoefficientB();
		double ProductionCoefficientC = this.schedulingAgent.getInternalDataModel().getProductionCoefficientC();
		double electricityPrice = this.schedulingAgent.getInternalDataModel().getDSMInformation().getElectricityPriceForPeriod(currentPeriod);
			
		// TODO:Lambda ergänzen? + Ableitung prüfen 
		double gradientmLCOH = (electricityPrice * PEL) / (100
				* (ProductionCoefficientA * Math.pow(x, 2) + ProductionCoefficientB * x + ProductionCoefficientC))
				- (electricityPrice * PEL * x * (2 * ProductionCoefficientA * x + ProductionCoefficientB))
						/ (100 * (Math.pow(ProductionCoefficientA * Math.pow(x, 2) + ProductionCoefficientB * x
								+ ProductionCoefficientC, 2)));
		return gradientmLCOH;
	}
	
	@Override
	public void action() {
		int currentIteration = this.schedulingAgent.getInternalDataModel().getIteration();
		int currentPeriod = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();
		double productionQuantity = this.schedulingAgent.getInternalDataModel().getProductionQuantityForPeriodAndIteration(currentPeriod, currentIteration); //own Production
		double demand = this.schedulingAgent.getInternalDataModel().getDSMInformation().getProductionQuantityForPeriod(currentPeriod);
		double sumProduction = this.schedulingAgent.getInternalDataModel().getSumProduction();
		double lambda = this.schedulingAgent.getInternalDataModel().getLambda();
		double penaltyFactor = this.schedulingAgent.getInternalDataModel().getPenaltyFactor();	
		double z = this.schedulingAgent.getInternalDataModel().getZ();
		double x = this.schedulingAgent.getInternalDataModel().getX();
		
		double demandDeviation = productionQuantity + sumProduction - demand;
		double demandPercentage = Math.abs(demandDeviation / demand) * 100; 

		// Update Lambda
		lambda = lambda + penaltyFactor * calculateGradientmLCOH(x) * demandPercentage *(x-z);
		//lambda = lambda + penaltyFactor * (x-z)/x;
		
    	//-----------------
        //Test
		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}
	
		//-----------------
				//  Set and reset values
		this.schedulingAgent.getInternalDataModel().setLambda(lambda);
		this.schedulingAgent.getInternalDataModel().incrementIteration();
		this.schedulingAgent.getInternalDataModel().setEnableMessageReceive(true);
		
		//Next behaviour to be executed
		MinimizeX minimizeX = new MinimizeX(schedulingAgent);
		this.schedulingAgent.addBehaviour(minimizeX);
	}

}
