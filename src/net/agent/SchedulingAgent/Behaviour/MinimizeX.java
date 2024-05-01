package net.agent.SchedulingAgent.Behaviour;

import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

/**
 * A behavior class object used to minimize the variable X.
 * The goal is to find the lowest mLCOH (marginal Levelized Cost of Hydrogen) value by optimizing production.
 */
public class MinimizeX extends OneShotBehaviour {
	private static final long serialVersionUID = -5844996727231977882L;
	
	SchedulingAgent schedulingAgent;
	
	  /**
     * Constructor for MinimizeX behavior.
     * @param schedulingAgent The agent to which this behavior is attached.
     */
	public MinimizeX(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}

	public double getAnnualNominalProduction() {
		double ProductionCoefficientA = this.schedulingAgent.getInternalDataModel().getProductionCoefficientA();
		double ProductionCoefficientB = this.schedulingAgent.getInternalDataModel().getProductionCoefficientB();
		double ProductionCoefficientC = this.schedulingAgent.getInternalDataModel().getProductionCoefficientC();
		double fullLoadHours = this.schedulingAgent.getInternalDataModel().getLoadFactor() * 8760; // 8760 hours per year
		double nominalProductionPerHour; // Nominal Production Quantity per Hour
		double nominalProductionPerYear; // Nominal Production Quantity per Year
		double x = 100;

		// Production Quantity per Hour
		nominalProductionPerHour = ProductionCoefficientA * Math.pow(x, 2) + ProductionCoefficientB * x
				+ ProductionCoefficientC;

		// Production Quantity per Year
		nominalProductionPerYear = nominalProductionPerHour * fullLoadHours;
		return nominalProductionPerYear;
	}

	public double getHourlyProductionQuantity(double x) {
		double ProductionCoefficientA = this.schedulingAgent.getInternalDataModel().getProductionCoefficientA();
		double ProductionCoefficientB = this.schedulingAgent.getInternalDataModel().getProductionCoefficientB();
		double ProductionCoefficientC = this.schedulingAgent.getInternalDataModel().getProductionCoefficientC();
		boolean stateProduction = this.schedulingAgent.getInternalDataModel().isStateProduction();
		double productionQuantity;

		if (stateProduction) {
			productionQuantity = ProductionCoefficientA * Math.pow(x, 2) + ProductionCoefficientB * x
					+ ProductionCoefficientC;

			// Check that the production quantity is always positive
			if (productionQuantity < 0) {
				productionQuantity = 0.000001;
			}
		} else {
			productionQuantity = 0;
		}
		return productionQuantity;
	}



	public double minimizeLx() {
		
		/**
		 * Get Parameters from the InternalDataModel
		 */
		double CapEx = this.schedulingAgent.getInternalDataModel().getCapEx();
		double PEL = this.schedulingAgent.getInternalDataModel().getPEL();
		int currentPeriod = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();
		int currentIteration = this.schedulingAgent.getInternalDataModel().getIteration();
		double electricityPrice = this.schedulingAgent.getInternalDataModel().getDSMInformation().getElectricityPriceForPeriod(currentPeriod);
		double min_x_value = this.schedulingAgent.getInternalDataModel().getMinPower();
		double minPower = this.schedulingAgent.getInternalDataModel().getMinPower();
		double maxPower = this.schedulingAgent.getInternalDataModel().getMaxPower();
		boolean stateStandby = this.schedulingAgent.getInternalDataModel().isStateStandby();
		double lambda = this.schedulingAgent.getInternalDataModel().getLambda();
		int lifetime = this.schedulingAgent.getInternalDataModel().getUtilizaziontime();
		double discountrate = this.schedulingAgent.getInternalDataModel().getDiscountrate();
		double fullLoadHours = this.schedulingAgent.getInternalDataModel().getLoadFactor() * 8760; // 8760 hours per year
		double OMFactor = this.schedulingAgent.getInternalDataModel().getOMFactor();
		double z = this.schedulingAgent.getInternalDataModel().getZ();
		double demand = this.schedulingAgent.getInternalDataModel().getDSMInformation().getProductionQuantityForPeriod(currentPeriod);
		int rowIndexShutdownOrder = this.schedulingAgent.getInternalDataModel().getRowIndexShutdownOrder();
		int nextShutdownElectrolyzer = this.schedulingAgent.getInternalDataModel().getShutdownOrderValue(rowIndexShutdownOrder);
		int startUpDuration = this.schedulingAgent.getInternalDataModel().getStartUpDuration();
		boolean stateProduction = this.schedulingAgent.getInternalDataModel().isStateProduction();

		/**
		 * Parameters for Solving 
		 */
		double min_mLCOHLambda = Double.POSITIVE_INFINITY;
		double min_mLCOH = Double.POSITIVE_INFINITY;
		double mH2_hour;
		double increment = 0.001;

		// Calculate Present Worth Factor (PWF) and hourly Costs
		double annuityCostPerYear = (CapEx * (discountrate * Math.pow((1 + discountrate), lifetime)))
				/ (Math.pow(1 + discountrate, lifetime) - 1);

		// Equation for Annuity Cost per Hour = AnnuityCostPerYear / fullloadhours;
		double annuityCostPerHour = annuityCostPerYear / fullLoadHours;

		// Calculate Costs for Operation and Maintenance as a Percentage of CapEx
		double OMCost = OMFactor * CapEx / getAnnualNominalProduction();

		// --- Standby Check ---
		// Check, if all Electrolyzers are working at lower operating Limit
		if (this.schedulingAgent.getInternalDataModel().lowerLimitsAllTrueForIteration(currentIteration - 1)) {

			// Check, if this electrolyzer should be set to standby
			if (extractAgentNumber(this.schedulingAgent.getLocalName()) == nextShutdownElectrolyzer) {
				int number = extractAgentNumber(this.schedulingAgent.getLocalName());
				System.err.println("Agent: " + this.schedulingAgent.getLocalName() + " Standby-Activated!");
				
				// Activate Standby
				this.schedulingAgent.getInternalDataModel().setStateProduction(false);
				this.schedulingAgent.getInternalDataModel().setStateStandby(true);
				this.schedulingAgent.getInternalDataModel().setX(0);
				min_x_value = 0;
				
				// Set Shutdown Period
				this.schedulingAgent.getInternalDataModel().setPeriodShutdown(currentPeriod);
				this.schedulingAgent.getInternalDataModel().setEarliestStartPeriod(currentPeriod + 1);
				this.schedulingAgent.getInternalDataModel().setDemandShutdown(demand);

				// Update Shutdown Order-List
				this.schedulingAgent.getInternalDataModel().updateShutdownOrderIndex();
			}
			
			//TODO: Hier Unterscheidung ausschalten vs. Standby implementieren 
			// Check, if this electrolyzer should be set to standby
		}

		// -- Start Up Check ---
		// TODO Implement Start up Strategy
		if (stateStandby && currentPeriod >= this.schedulingAgent.getInternalDataModel().getEarliestStartPeriod()
				&& demand > this.schedulingAgent.getInternalDataModel().getDemandShutdown()) {
			this.schedulingAgent.getInternalDataModel().setStateProduction(true);
			this.schedulingAgent.getInternalDataModel().setStateStandby(false);
		}

		// Check whether the electrolyzer is in production mode
		if (stateProduction) {
			// Loop over the range of values of the realizable load of the electrolyzer
			for (double x = minPower; x <= maxPower + increment; x += increment) {
				
				// Set x to the maximum value if the increment exceeds the maximum
				if (x > maxPower) {
					x = maxPower;
				}

				mH2_hour = getHourlyProductionQuantity(x);
				double OpEx = PEL * (x / 100) * electricityPrice;
				double mLCOH = OMCost + (annuityCostPerHour + OpEx) / mH2_hour + lambda * ((x - z) / 100);

				// If the current mLCOH value is less than the previous minimum, update the
				// minimum and the X value.
				if (mLCOH < min_mLCOHLambda) {
					min_mLCOHLambda = OMCost + (annuityCostPerHour + OpEx) / mH2_hour + lambda * (x - z) / 100; // set min mLCOH with lambda-Term 
					min_mLCOH = OMCost + (annuityCostPerHour + OpEx) / mH2_hour;
					min_x_value = x;
				}

				// When x has been set to the maximum, end the loop
				if (x == maxPower) {
					break;
				}
			}
		} else {
			// The electrolyzer is not in production mode, therefore the production is 0
			min_x_value = 0.0;
		}

		// Returns the X value at which mLCOH is minimum.
		this.schedulingAgent.getInternalDataModel().setX(min_x_value);
		this.schedulingAgent.getInternalDataModel().addIterationADMMInfo(
		this.schedulingAgent.getInternalDataModel().getCurrentPeriod(),
		this.schedulingAgent.getInternalDataModel().getIteration(), getHourlyProductionQuantity(min_x_value), 0, min_x_value, this.schedulingAgent.getInternalDataModel().getZ(), min_mLCOH);
		
		return min_x_value;
	}
	
	/**
	 * Extracts the agent number from the agent name.
	 * Assumes that the agent name follows the format "<instanceName>:PEA_Agent<number>".
	 * @param agentName the name of the agent
	 * @return the agent number
	 */
	public static int extractAgentNumber(String agentName) {
	    // Split the agent name by ":PEA_Agent" to get the part containing the agent number
	    String[] parts = agentName.split("--PEAAgent");

	    // Check if the agent name follows the expected format
	    if (parts.length == 2) {
	        // Extract the agent number from the second part and convert it to an integer
	        try {
	            return Integer.parseInt(parts[1]);
	        } catch (NumberFormatException e) {
	            // If the agent number is not a valid integer, return -1 to indicate an error
	            return -1;
	        }
	    } else {
	        // If the agent name does not follow the expected format, return -1 to indicate an error
	        return -1;
	    }
	}


	
	 /**
     * This method is called when the behavior is executed.
     * It performs the actions to minimize X.
     */
	@Override
	public void action() {

		// Minimize Lx
		minimizeLx();

		// Messages can be received again
		if (this.schedulingAgent.getInternalDataModel().isEnableMessageReceive()) {
			this.schedulingAgent.getInternalDataModel().setReceiveMessages(true);
		}

		// Next Behaviour to be executed
		ProductionDataBroadcaster productionDataBroadcaster = new ProductionDataBroadcaster(schedulingAgent);
		this.schedulingAgent.addBehaviour(productionDataBroadcaster);
	}

}
