package net.agent.SchedulingAgent.Behaviour;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

public class DualUpdate extends OneShotBehaviour {

	SchedulingAgent schedulingAgent;

	public DualUpdate(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}

	public void writeMatrixToExcel(int AgentID, int Periode, int Iteration, double ownProduction,
			double receivedProductionQuantity, double Demand, double x, double z, double gradient, double lambda,
			double demandPercentage, Long currentTimeMs, int shutdownOrderIndex, int shutdownElectrolyzer, double k) {
		String filepath = "D:\\\\Dokumente\\\\OneDrive - Helmut-Schmidt-Universität\\\\04_Programmierung\\\\ElectrolyseurScheduling JADE\\\\DualUpdate.csv";
		String header;
		String data;

		if (Iteration == 0) {
			// Create the heading line
			header = "Agent;Periode;Iteration;Eigene Produktionsmenge;Empfangene Produktionsmenge;Demand;X;Z;Gradient;Lambda;demandPercentage; CurrentTime; ShutdownOrderIndex; ShutdownElectrolyzer; k";
		} else {
			header = "";
		}

		// Create the data row
		data = AgentID + ";" + Periode + ";" + Iteration + ";" + ownProduction + ";" + receivedProductionQuantity + ";"
				+ Demand + ";" + x + ";" + z + ";" + gradient + ";" + lambda + ";" + demandPercentage + ";"
				+ currentTimeMs + ";" + shutdownOrderIndex + ";" + shutdownElectrolyzer + ";" + k;

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
		double electricityPrice = this.schedulingAgent.getInternalDataModel().getDSMInformation()
				.getElectricityPriceForPeriod(currentPeriod);

		double gradientmLCOH = (electricityPrice * PEL) / (100
				* (ProductionCoefficientA * Math.pow(x, 2) + ProductionCoefficientB * x + ProductionCoefficientC))
				- (electricityPrice * PEL * x * (2 * ProductionCoefficientA * x + ProductionCoefficientB))
						/ (100 * (Math.pow(ProductionCoefficientA * Math.pow(x, 2) + ProductionCoefficientB * x
								+ ProductionCoefficientC, 2)));
		return gradientmLCOH;
	}

	@Override
	public void action() {
		// Parameters
		int currentIteration = this.schedulingAgent.getInternalDataModel().getIteration();
		int currentPeriod = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();
		double productionQuantity = this.schedulingAgent.getInternalDataModel()
				.getProductionQuantityForPeriodAndIteration(currentPeriod, currentIteration); // own Production
		double demand = this.schedulingAgent.getInternalDataModel().getDSMInformation()
				.getDemandForPeriod(currentPeriod);
		double sumProduction = this.schedulingAgent.getInternalDataModel().getSumProduction();
		double lambda = this.schedulingAgent.getInternalDataModel().getLambda();
		double penaltyFactor = this.schedulingAgent.getInternalDataModel().getPenaltyFactor();
		double z = this.schedulingAgent.getInternalDataModel().getZ();
		double x = this.schedulingAgent.getInternalDataModel().getX();
		double demandDeviation = productionQuantity + sumProduction - demand;
		double demandPercentage = Math.abs(demandDeviation / demand);
		long currentMilliseconds = System.currentTimeMillis();

		// Get Agent-ID as Integer
		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}

		double scalingfactor = 0;
		if (this.schedulingAgent.getInternalDataModel().isStateProduction()) {

			// Variable k to penalize the deviation between x and z exponentially
			double delta = Math.abs(x - z);
			
			//Scaling factor for Penalty 
			double k;

			// Limit the exponent to a maximum of 3.5, but only for larger deviations
			double limitedExponent;
			if (delta > 2) {
				limitedExponent = Math.min(delta, 3.5);
				//TODO: Penalty-Factor hier manuell angepasst 
				penaltyFactor = 0.15;
			} else {
				// For small deviations (delta <= 2.0), use a function that grows faster
				limitedExponent = 1.0 + Math.pow(delta, 3);
				penaltyFactor = 0.18;
			}

			// Calculate the value of Math.exp with the limited exponent
			k = Math.exp(limitedExponent);
			scalingfactor = k;

			// double Gradient
			double gradient = Math.abs(calculateGradientmLCOH(x)) + 0.00001;

			// Update Lambda
			lambda = lambda + penaltyFactor * gradient * (x - z) * k;

		}

		int shutdownorderIndex = this.schedulingAgent.getInternalDataModel().getRowIndexShutdownOrder();
		int electrolyzershutdown = this.schedulingAgent.getInternalDataModel().getShutdownOrderValue(shutdownorderIndex);
		
		double k;
		// Write Results into .csv-file
					writeMatrixToExcel(agentId, currentPeriod, currentIteration, productionQuantity, sumProduction, demand, x,
							z, calculateGradientmLCOH(x), lambda, demandPercentage, currentMilliseconds, shutdownorderIndex, electrolyzershutdown, scalingfactor);
		
		// Set and reset values
		this.schedulingAgent.getInternalDataModel().setLambda(lambda);
		this.schedulingAgent.getInternalDataModel().incrementIteration();
		this.schedulingAgent.getInternalDataModel().setEnableMessageReceive(true);

		// Next behaviour to be executed
		MinimizeX minimizeX = new MinimizeX(schedulingAgent);
		this.schedulingAgent.addBehaviour(minimizeX);
	}

}
