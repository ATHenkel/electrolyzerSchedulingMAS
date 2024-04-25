package net.agent.SchedulingAgent.Behaviour;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

public class DualUpdate extends OneShotBehaviour {

	SchedulingAgent schedulingAgent;


	public DualUpdate(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}
	

	//Write results of current iteration into .csv-file
	public void writeMatrixToExcel(int AgentID, int Periode, int Iteration, double ownProduction,
	        double receivedProductionQuantity, double Demand, double x, double z, double gradient, double lambda,
	        double demandPercentage, Long currentTimeMs, int shutdownOrderIndex, int shutdownElectrolyzer, boolean stateProduction, boolean stateStandby) {
		
    	//Get Agent-ID as Integer
		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}
	    
	    // Format for the current date and time as prefix
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
	    String datePrefix = sdf.format(new Date());

	    // File path adjustment to include the date and time prefix and write into 'out' directory
	    String filepath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\04_Programmierung\\ElectrolyseurScheduling JADE\\out\\" + datePrefix + "_DualUpdate.csv";
	    String header;
	    String data;

	    if (Iteration == 0) {
	        //Ensure that headline is only created once
	        header = "Agent;Periode;Iteration;Eigene Produktionsmenge;Empfangene Produktionsmenge;Demand;X;Z;Gradient;Lambda;DemandDeviation %; CurrentTime; ShutdownOrderIndex; ShutdownElectrolyzer; stateProduction; stateStandby";
	    } else {
	        header = "";
	    }

	    // Create the data row
	    data = AgentID + ";" + Periode + ";" + Iteration + ";" + ownProduction + ";" + receivedProductionQuantity + ";"
	            + Demand + ";" + x + ";" + z + ";" + gradient + ";" + lambda + ";" + demandPercentage + ";"
	            + currentTimeMs + ";" + shutdownOrderIndex + ";" + shutdownElectrolyzer + ";" + stateProduction + ";" + stateStandby;

	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
	        // If the iteration is 0, write the heading line
	        if (Iteration == 0 && Periode == 1 && agentId ==1) {
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
		double demand = this.schedulingAgent.getInternalDataModel().getDSMInformation().getProductionQuantityForPeriod(currentPeriod);
		
		double sumProduction = this.schedulingAgent.getInternalDataModel().getSumProduction();
		double lambda = this.schedulingAgent.getInternalDataModel().getLambda();
		double penaltyFactor = this.schedulingAgent.getInternalDataModel().getPenaltyFactor();
		double z = this.schedulingAgent.getInternalDataModel().getZ();
		double x = this.schedulingAgent.getInternalDataModel().getX();
		double demandDeviation = productionQuantity + sumProduction - demand;
		double demandPercentage = Math.abs(demandDeviation / demand + 0.000000000001); //Added 0.000000000001 to exclude division by 0
		long currentMilliseconds = System.currentTimeMillis();
		int shutdownorderIndex = this.schedulingAgent.getInternalDataModel().getRowIndexShutdownOrder();
		int electrolyzershutdown = this.schedulingAgent.getInternalDataModel().getShutdownOrderValue(shutdownorderIndex);

		// Get Agent-ID as Integer
		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}

		if (this.schedulingAgent.getInternalDataModel().isStateProduction()) {
			
			/**
			 * Use this Configuration for Enapter AEM EL4.0 Electrolyzer 
			 * 
			 * 	// Limit the exponent to a maximum of 3.5, but only for larger deviations
			double limitedExponent;
			if (delta > 2) {
				limitedExponent = Math.min(delta, 3.5);
				penaltyFactor = 0.15;
			} else {
				// For small deviations (delta <= 2.0), use a function that grows faster
				limitedExponent = 1.0 + Math.pow(delta, 3);
				penaltyFactor = 0.18;
			}
			
			 */

			// Variable k to penalize the deviation between x and z exponentially
			double delta = Math.abs(x - z);
			
			//Scaling factor for Penalty 
			double k;
			
			double limitedExponent;
			//For larger Deviations
			if (delta > 0.01) {
				limitedExponent = 3.7;
				penaltyFactor = 0.620215;
			} else {
				// For small deviations
				limitedExponent = 1.0;
				penaltyFactor = 0.3;
			}
			
			k = Math.exp(limitedExponent);
			// double Gradient
			double gradient = Math.abs(calculateGradientmLCOH(x)) + 0.00001;

			// Only update Lambda if in state Production
			if (x > 0) {
				lambda = lambda + penaltyFactor * (x - z) * gradient * k ;
			} else {
				lambda = 0;
			}
		}
		
		// Write Results into .csv-file
		writeMatrixToExcel(agentId, currentPeriod, currentIteration, productionQuantity, sumProduction, demand, x,
							z, calculateGradientmLCOH(x), lambda, demandPercentage, currentMilliseconds, shutdownorderIndex, electrolyzershutdown,  this.schedulingAgent.getInternalDataModel().isStateProduction(), this.schedulingAgent.getInternalDataModel().isStateStandby());
		
		// Set and reset values
		this.schedulingAgent.getInternalDataModel().setLambda(lambda);
		this.schedulingAgent.getInternalDataModel().incrementIteration();
		this.schedulingAgent.getInternalDataModel().setEnableMessageReceive(true);

		// Next behaviour to be executed
		MinimizeX minimizeX = new MinimizeX(schedulingAgent);
		this.schedulingAgent.addBehaviour(minimizeX);
	}

}
