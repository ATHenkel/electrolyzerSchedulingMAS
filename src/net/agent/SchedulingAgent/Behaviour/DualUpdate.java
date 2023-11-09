package net.agent.SchedulingAgent.Behaviour;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

public class DualUpdate extends OneShotBehaviour {
	
	SchedulingAgent schedulingAgent;
	public DualUpdate(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}
	
	public void writeMatrixToExcel(int AgentID, int Periode, int Iteration, double ownProduction,
			double receivedProductionQuantity, double Demand, double x, double z, double gradient, double lambda, double demandPercentage, Long currentTimeMs) {
		String filepath = "D:\\\\Dokumente\\\\OneDrive - Helmut-Schmidt-Universität\\\\04_Programmierung\\\\ElectrolyseurScheduling JADE\\\\DualUpdate.csv";
		String header;
		String data;

		if (Iteration == 0) {
			// Create the heading line
			header = "Agent;Periode;Iteration;Eigene Produktionsmenge;Empfangene Produktionsmenge;Demand;X;Z;Gradient;Lambda;demandPercentage; CurrentTime";
		} else {
			header = "";
		}

		// Create the data row
		data = AgentID + ";" + Periode + ";" + Iteration + ";" + ownProduction + ";" + receivedProductionQuantity + ";"
				+ Demand + ";" + x + ";" + z + ";" + gradient + ";" + lambda + ";" + demandPercentage + ";" + currentTimeMs;

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
			// If the iteration is 0, write the heading line
			if (Iteration == 0) {
				writer.write(header);
				writer.newLine(); // New line after the headings
			}
			writer.write(data);
			writer.newLine(); // New line after the data
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public double calculateGradientmLCOH(double x) {
		int currentPeriod = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();
		x = this.schedulingAgent.getInternalDataModel().getX();
		double PEL = this.schedulingAgent.getInternalDataModel().getPEL();
		double ProductionCoefficientA = this.schedulingAgent.getInternalDataModel().getProductionCoefficientA();
		double ProductionCoefficientB = this.schedulingAgent.getInternalDataModel().getProductionCoefficientB();
		double ProductionCoefficientC = this.schedulingAgent.getInternalDataModel().getProductionCoefficientC();
		double electricityPrice = this.schedulingAgent.getInternalDataModel().getDSMInformation().getElectricityPriceForPeriod(currentPeriod);
		
		double gradientmLCOH = (electricityPrice * PEL) / (100
				* (ProductionCoefficientA * Math.pow(x, 2) + ProductionCoefficientB * x + ProductionCoefficientC))
				- (electricityPrice * PEL * x * (2 * ProductionCoefficientA * x + ProductionCoefficientB))
						/ (100 * (Math.pow(ProductionCoefficientA * Math.pow(x, 2) + ProductionCoefficientB * x
								+ ProductionCoefficientC, 2)));
		return gradientmLCOH;
	}
	
	@Override
	public void action() {
		//Parameters 
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
		double demandPercentage = Math.abs(demandDeviation / demand);
		long currentMilliseconds = System.currentTimeMillis();

				
    	//Get Agent-ID as Integer
		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}
		
		//Variable k to penalize the deviation between x and z exponentially 
		double k;
		double delta = Math.abs(x-z);
		if (delta > 3.5) {
		    k = Math.exp(3.5);
		} else {
		    k = Math.exp(delta);
		}
		
		//Write Results into .csv-file
		writeMatrixToExcel(agentId, currentPeriod, currentIteration, productionQuantity, sumProduction, demand, x, z, calculateGradientmLCOH(x), lambda, demandPercentage, currentMilliseconds);
		
		// Update Lambda 
		lambda = lambda + penaltyFactor * Math.abs(calculateGradientmLCOH(x))*(x-z)*k;
		
		// Set and reset values
		this.schedulingAgent.getInternalDataModel().setLambda(lambda);
		this.schedulingAgent.getInternalDataModel().incrementIteration();
		this.schedulingAgent.getInternalDataModel().setEnableMessageReceive(true);
		
		//TODO: Simulate electrolyzer failure 
		if (agentId == 2 && currentPeriod == 10 && currentIteration == 1269) {
			System.out.println("Agent 2 simulate Electrolyzer Failure");
			this.schedulingAgent.getInternalDataModel().setStateProduction(false);
		}
		
		//Next behaviour to be executed
		MinimizeX minimizeX = new MinimizeX(schedulingAgent);
		this.schedulingAgent.addBehaviour(minimizeX);
	}

}
