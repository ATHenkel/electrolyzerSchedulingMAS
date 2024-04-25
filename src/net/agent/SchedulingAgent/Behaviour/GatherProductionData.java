package net.agent.SchedulingAgent.Behaviour;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
		double electricityPrice = this.schedulingAgent.getInternalDataModel().getDSMInformation()
				.getElectricityPriceForPeriod(currentPeriod);

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
		double productionQuantity = this.schedulingAgent.getInternalDataModel().getProductionQuantityForPeriodAndIteration(currentPeriod, currentIteration); // own Production
		double demand = this.schedulingAgent.getInternalDataModel().getDSMInformation().getProductionQuantityForPeriod(currentPeriod);
		double demandDeviation = productionQuantity + sumProduction - demand;
		boolean allUpperOperatingLimit = this.schedulingAgent.getInternalDataModel().upperLimitsAllTrueForIteration(currentIteration-1);
		double x = this.schedulingAgent.getInternalDataModel().getX();
		double maxPower = this.schedulingAgent.getInternalDataModel().getMaxPower();
		
		double demandDeviationPercentage = (demandDeviation/demand +0.000000001) * 100.0; // Conversion in percent
		String formattedDemandDeviation = String.format("%.3f", demandDeviationPercentage); // Formatting to 3 decimal digits
		System.out.println("Agent: " + this.schedulingAgent.getLocalName() + " Periode:" + currentPeriod + " Iteration:" + currentIteration + " DemandDeviation:" + formattedDemandDeviation + "%");

		if (Math.abs(demandDeviation) < epsilonProduction) {
			periodScheduled = true;
			System.out.println("--------");
			System.out.println("Agent: " + this.schedulingAgent.getLocalName() + " Periode "
					+ this.schedulingAgent.getInternalDataModel().getCurrentPeriod() + " Iteration: "
					+ this.schedulingAgent.getInternalDataModel().getIteration() + " scheduled " + " Demand Deviation: "
					+ formattedDemandDeviation + "%");
			//Reset Iteration 
			this.schedulingAgent.getInternalDataModel().setIteration(0);
		}
		
		// Production target not reachable (demandDeviation < 0) and all PEA-agents are producing at the upper limit
		if (demandDeviation < 0 &&  allUpperOperatingLimit && x == maxPower){
	    periodScheduled = true;
	    System.out.println("--------");
 		System.out.println("Agent: " + this.schedulingAgent.getLocalName() + " Periode "
				+ this.schedulingAgent.getInternalDataModel().getCurrentPeriod() + " Iteration: "
				+ this.schedulingAgent.getInternalDataModel().getIteration() + " ProductionTarget Unreachable " + " Demand Deviation: "
				+ demandDeviation + " Demand: " + demand) ;
		//Reset Iteration 
		this.schedulingAgent.getInternalDataModel().setIteration(0);
		}
		return periodScheduled;
	}

	@Override
	public void action() {
		// Get Agent-ID as Integer
		String localName = this.schedulingAgent.getLocalName();
		int agentId;
		try {
			agentId = Integer.parseInt(localName);
		} catch (NumberFormatException e) {
			agentId = -1; // Default value if the conversion fails.
		}
		
		//Get Values from internal knowledge base
		int currentPeriod = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();
		int currentIteration = this.schedulingAgent.getInternalDataModel().getIteration();
		int lastPeriod = this.schedulingAgent.getInternalDataModel().getDSMInformation().getLastPeriod();
		double productionQuantity = this.schedulingAgent.getInternalDataModel().getProductionQuantityForPeriodAndIteration(currentPeriod, currentIteration); // own Production
		double demand = this.schedulingAgent.getInternalDataModel().getDSMInformation().getProductionQuantityForPeriod(currentPeriod);
		double electricityPrice = this.schedulingAgent.getInternalDataModel().getDSMInformation().getElectricityPriceForPeriod(currentPeriod);
		double x = this.schedulingAgent.getInternalDataModel().getX();
		double z = this.schedulingAgent.getInternalDataModel().getZ();
		double mLCOH = this.schedulingAgent.getInternalDataModel().getIterationADMMTable().get(currentIteration).getmLCOH();
		double lambda = this.schedulingAgent.getInternalDataModel().getLambda();
		double sumProduction = this.schedulingAgent.getInternalDataModel().getSumProduction();
		double demandDeviation = productionQuantity + sumProduction - demand;
		double demandPercentage = Math.abs(demandDeviation / demand);
		long currentMilliseconds = System.currentTimeMillis();
		int shutdownorderIndex = this.schedulingAgent.getInternalDataModel().getRowIndexShutdownOrder();
		int electrolyzershutdown = this.schedulingAgent.getInternalDataModel().getShutdownOrderValue(shutdownorderIndex);
		boolean stateProduction = this.schedulingAgent.getInternalDataModel().isStateProduction();
		boolean stateIdle = this.schedulingAgent.getInternalDataModel().isStateIdle();
		boolean stateStandby = this.schedulingAgent.getInternalDataModel().isStateStandby();

		// check if scheduling for period is reached
		if (periodScheduled() == true) {
			this.schedulingAgent.getInternalDataModel().setLambda(0.2);
			this.schedulingAgent.getInternalDataModel().setX(x);
			this.schedulingAgent.getInternalDataModel().setZ(z);
			this.schedulingAgent.getInternalDataModel().getSchedulingResults().addResult(currentPeriod,
					electricityPrice, stateStandby, stateIdle, stateProduction, x, mLCOH, productionQuantity, demand);
			
			// Write Results into .csv-file
			writeMatrixToExcel(agentId, currentPeriod, currentIteration, productionQuantity, sumProduction, demand, x,
					z, calculateGradientmLCOH(x), lambda, demandPercentage, currentMilliseconds, shutdownorderIndex, electrolyzershutdown,  this.schedulingAgent.getInternalDataModel().isStateProduction(), this.schedulingAgent.getInternalDataModel().isStateStandby());

			if (currentPeriod < lastPeriod) {
				this.schedulingAgent.getInternalDataModel().incrementCurrentPeriod();
				this.schedulingAgent.getInternalDataModel().setEnableMessageReceive(true);

				// Next Behaviour to be executed
				MinimizeX minimizeX = new MinimizeX(schedulingAgent);
				this.schedulingAgent.addBehaviour(minimizeX);
			}
			
			if (currentPeriod == lastPeriod) {
				// Set Scheduling Complete Variable to True
				this.schedulingAgent.getInternalDataModel().setSchedulingComplete(true);

				// Next behaviour to be executed
				SchedulingDone schedulingDone = new SchedulingDone(schedulingAgent);
				this.schedulingAgent.addBehaviour(schedulingDone);
			}

		} else {
			// Next Behaviour to be executed
			MinimizeZ minimizeZ = new MinimizeZ(schedulingAgent);
			this.schedulingAgent.addBehaviour(minimizeZ);
		}

	}
	
	//Write results of current iteration into .csv-file
	public void writeMatrixToExcel(int AgentID, int Periode, int Iteration, double ownProduction,
	        double receivedProductionQuantity, double Demand, double x, double z, double gradient, double lambda,
	        double demandPercentage, Long currentTimeMs, int shutdownOrderIndex, int shutdownElectrolyzer, boolean stateProduction, boolean stateStandby) {
		
		
	    // Format for the current date and time as prefix
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
	    String datePrefix = sdf.format(new Date());
	    String filepath = "D:\\Dokumente\\OneDrive - Helmut-Schmidt-Universität\\04_Programmierung\\ElectrolyseurScheduling JADE\\out\\" + datePrefix + "_DualUpdate.csv";
		String data;

		// Create the data row
	    data = AgentID + ";" + Periode + ";" + Iteration + ";" + ownProduction + ";" + receivedProductionQuantity + ";"
	            + Demand + ";" + x + ";" + z + ";" + gradient + ";" + lambda + ";" + demandPercentage + ";"
	            + currentTimeMs + ";" + shutdownOrderIndex + ";" + shutdownElectrolyzer + ";" + stateProduction + ";" + stateStandby;

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
			writer.write(data);
			writer.newLine(); // New line after the data
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
