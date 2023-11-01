package net.agent.SchedulingAgent.Behaviour;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

public class GatherProductionData extends OneShotBehaviour {

	SchedulingAgent schedulingAgent;

	public GatherProductionData(SchedulingAgent schedulingAgent) {
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

	public boolean periodScheduled() {
		boolean periodScheduled = false;
		int currentPeriod = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();
		int currentIteration = this.schedulingAgent.getInternalDataModel().getIteration();
		double sumProduction = this.schedulingAgent.getInternalDataModel().getSumProduction();
		double epsilonProduction = this.schedulingAgent.getInternalDataModel().getEpsilonProduction();
		double productionQuantity = this.schedulingAgent.getInternalDataModel()
				.getProductionQuantityForPeriodAndIteration(currentPeriod, currentIteration); // own Production
		double demand = this.schedulingAgent.getInternalDataModel().getDSMInformation()
				.getProductionQuantityForPeriod(currentPeriod);
		double demandDeviation = productionQuantity + sumProduction - demand;
		
		//TODO TEST
		//System.out.println("Agent: " + this.schedulingAgent.getLocalName() + " Period: " + currentPeriod + " Iteration: " + currentIteration + " DemandDeviation: " + demandDeviation);
		
		if (Math.abs(demandDeviation) < epsilonProduction) {
			periodScheduled = true;
			System.out.println("Agent: " + this.schedulingAgent.getLocalName() + " Periode "+ this.schedulingAgent.getInternalDataModel().getCurrentPeriod() + " Iteration: " + this.schedulingAgent.getInternalDataModel().getIteration() + " scheduled " + " Demand Deviation: " + demandDeviation);
		}
		return periodScheduled;
	}

	public void writeMatrixToExcel(String filepath, int AgentID, int Periode, int Iteration, double ownProduction,
			double receivedProductionQuantity, double Demand, double x, double z, double gradient, double lambda) {
		String header;
		String data;

		if (Iteration == 0) {
			// Create the heading line
			header = "Agent;Periode;Iteration;Eigene Produktionsmenge;Empfangene Produktionsmenge;Demand;X;Z;Gradient;Lambda";
		} else {
			header = "";
		}

		// Create the data row
		data = AgentID + ";" + Periode + ";" + Iteration + ";" + ownProduction + ";" + receivedProductionQuantity + ";"
				+ Demand + ";" + x + ";" + z + ";" + gradient + ";" + lambda;

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

	@Override
	public void action() {
		int currentPeriod = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();
		int currentIteration = this.schedulingAgent.getInternalDataModel().getIteration();
		int lastPeriod = this.schedulingAgent.getInternalDataModel().getDSMInformation().getLastPeriod();
		double productionQuantity = this.schedulingAgent.getInternalDataModel().getProductionQuantityForPeriodAndIteration(currentPeriod, currentIteration); // own Production
		double demand = this.schedulingAgent.getInternalDataModel().getDSMInformation().getProductionQuantityForPeriod(currentPeriod);
		double electricityPrice = this.schedulingAgent.getInternalDataModel().getDSMInformation().getElectricityPriceForPeriod(currentPeriod);
		double x = this.schedulingAgent.getInternalDataModel().getX();
		double z = this.schedulingAgent.getInternalDataModel().getZ();
		double mLCOH = this.schedulingAgent.getInternalDataModel().getIterationADMMTable().get(currentIteration).getmLCOH();
		double sumProduction = this.schedulingAgent.getInternalDataModel().getSumProduction();
		double gradient = calculateGradientmLCOH(x);
		double lambda = this.schedulingAgent.getInternalDataModel().getLambda();

		// ---- Write Data to Excel --> Parse Agent-ID to Integer-Value --- 
		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}

		// Create csv.table and write values
		if (agentId == 1) {
			String filepath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\04_Programmierung\\ElectrolyseurScheduling JADE\\ADMM_Agent1.csv";
			writeMatrixToExcel(filepath, agentId, currentPeriod, currentIteration, productionQuantity, sumProduction,
					demand, x, z, gradient, lambda);
		} else if (agentId == 2) {
			String filepath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\04_Programmierung\\ElectrolyseurScheduling JADE\\ADMM_Agent2.csv";
			writeMatrixToExcel(filepath, agentId, currentPeriod, currentIteration, productionQuantity, sumProduction,
					demand, x, z, gradient, lambda);
		} else if (agentId == 3) {
			String filepath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\04_Programmierung\\ElectrolyseurScheduling JADE\\ADMM_Agent3.csv";
			writeMatrixToExcel(filepath, agentId, currentPeriod, currentIteration, productionQuantity, sumProduction,
					demand, x, z, gradient, lambda);
		}

		// check if scheduling for period is done
		if (periodScheduled() == true) {
			this.schedulingAgent.getInternalDataModel().setLambda(0);
			this.schedulingAgent.getInternalDataModel().getSchedulingResults().addResult(currentPeriod,
					electricityPrice, false, false, true, x, mLCOH, productionQuantity, demand);

			if (currentPeriod < lastPeriod) {
				this.schedulingAgent.getInternalDataModel().incrementCurrentPeriod();
				this.schedulingAgent.getInternalDataModel().incrementIteration();
				this.schedulingAgent.getInternalDataModel().setEnableMessageReceive(true);

				// Next Behaviour to be executed
				MinimizeX minimizeX = new MinimizeX(schedulingAgent);
				this.schedulingAgent.addBehaviour(minimizeX);
			}
			if (currentPeriod == lastPeriod) {
				
				// Next Behaviour to be executed
				System.out.println("Agent: " + this.schedulingAgent.getLocalName() + " Periode: " + this.schedulingAgent.getInternalDataModel().getCurrentPeriod() + " Iteration: " + this.schedulingAgent.getInternalDataModel().getIteration() + " Scheduling Done Activated!" );
				
				//Set Scheduling Complete Variable to True
				this.schedulingAgent.getInternalDataModel().setSchedulingComplete(true);

				//Next behaviour to be executed 
				SchedulingDone schedulingDone = new SchedulingDone(schedulingAgent);
				this.schedulingAgent.addBehaviour(schedulingDone);

			}

		} else if (periodScheduled() == false) {
			// Next Behaviour to be executed
			MinimizeZ minimizeZ = new MinimizeZ(schedulingAgent);
			this.schedulingAgent.addBehaviour(minimizeZ);
		}

	}

}
