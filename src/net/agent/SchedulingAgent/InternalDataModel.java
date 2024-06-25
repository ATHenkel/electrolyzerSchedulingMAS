package net.agent.SchedulingAgent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import agentgui.core.common.AbstractUserObject;
import jade.core.AID;
import net.agent.DSMInformation.DSMInformation;
import net.agent.DSMInformation.SchedulingResults;

/**
 * This class models the internal data state of a Scheduling Agent within a multi-agent system.
 * It handles configurations, scheduling information, and OPC UA connections.
 */
public class InternalDataModel extends AbstractUserObject {
	private static final long serialVersionUID = -3954236452378160946L;

	// Global configuration and state variables
	private LocalDateTime lastScheduleWriteTime = LocalDateTime.of(1970, 1, 1, 0, 0); //Create variable to measure time and initialize with outdated values
	
	// OPC UA specific attributes for transferring optimized setpoints
	private String endpointURL;
	private OpcUaClient opcUaClient;
	private List<EndpointDescription> endpoints;
	private OpcUaClientConfigBuilder cfg;
	private EndpointDescription configPoint;
	private AddressSpace addressSpace;
	
	// OPC UA specific attributes for monitoring PEA 
	private String peaEndpointURL;
	private OpcUaClient peaOpcUaClient;
	private List<EndpointDescription> peaEndpoints;
	private OpcUaClientConfigBuilder peaCfg;
	private EndpointDescription peaConfigPoint;
	private AddressSpace peaAddressSpace;
	
	//Information regarding the communication with the PLC of the PEA 
	private int schedulingResultNextPeriod = 0; //Defines the period with which the PLC was last communicated the scheduling results
	private boolean headerWritten = true; //Boolean variable to make sure that header will be written only 1x 
	private int counter;
	
	 // Economic and operational parameters
	private double CapEx; // Capital-Costs in €
	private double OMFactor; // Factor for Operation & Maintenance in %
	private int utilizationTime; // Lifetime of Electrolyzer
	private double minPower; // Minimum Power from Electrolyzer
	private double maxPower; // Maximum Power from Electrolyzer
	private double PEL; // Electrical power of the electrolyzer in kW
	private double discountrate; //Discount rate
	private double loadFactor; //Share of full load hours per year
	private double ProductionCoefficientA; // f(x) = A*x^2 + B*x + C
	private double ProductionCoefficientB;
	private double ProductionCoefficientC;
	private int startUpDuration; 
	private String mtpFileName;
	
	// ADMM optimization parameters
	private double lambda = 0.2; // Lagrange-Multiplicator for Demand Constraint (Value 0.0)
	private double penaltyFactor = 0; // Penalty-Term (Value: 0.2)
	private int iteration = 0; // Iteration
	private double epsilonProduction = 0.002; // Tolerable deviation from the required production quantity (Value: 0.0005 (fast convergence))
	private int currentPeriod = 0;
	private int periodShutdown;
	private double demandShutdown;
	private int earliestStartPeriod;
	private boolean stateProduction = true, stateStandby = false, stateIdle = false;
	
	// Information within MAS 
	private int numberofAgents; 
	private double sumProduction;
	private int CountReceivedMessages = 0;
	private boolean enableMessageReceive = false; // is set in Dual Update
	private boolean receiveMessages = true;
	private Map<Integer, Map<Integer, Double>> listReceivedProductionQuantities;
	private Map<Integer, List<Boolean>> listReceivedLowerOperatingLimits;	//List for lowerOperatingLimits
	private Map<Integer, List<Boolean>> listReceivedUpperOperatingLimits;	//List for upperOperatingLimits
	private int rowIndexShutdownOrder = 0;
	private ArrayList<Integer> shutdownOrderList = new ArrayList<Integer>();
	private boolean reschedulingActivated;
	private int reschedulingPeriod;
	private boolean isStartOptimization;

	// Variables
	private double x; // Cost optimal Utilization of the electrolyzer
	private double z; // Demand optimal Utilization of the electrolyzer
	private boolean schedulingComplete; //Boolean variable to indicate whether planning horizon was scheduled completed 

	private DSMInformation dsmInformation; // Manages demand-side management information
	private SchedulingResults schedulingResults; // Stores results from scheduling operations

	/**
	 * Retrieves the DSMInformation instance, creating it if it does not already exist.
	 * @return the singleton DSMInformation instance for this agent
	 */
	public DSMInformation getDSMInformation() {
	    if (dsmInformation == null) {
	        dsmInformation = new DSMInformation();
	    }
	    return dsmInformation;
	}

	/**
	 * Retrieves the scheduling results.
	 * @return the SchedulingResults instance containing current scheduling data
	 */
	public SchedulingResults getSchedulingResults() {
	    return schedulingResults;
	}

	/**
	 * Stores iteration-specific data for the ADMM algorithm.
	 */
	private List<IterationADMM> iterationADMMTable;

	/**
	 * Manages lists of agents' contributions to the scheduling process.
	 */
	private HashMap<Integer, HashMap<AID, Double>> totalListScheduling; // Maps periods to agent contributions
	private HashMap<AID, Double> iterationListScheduling; // Temporary storage for current iteration data
	private List<AID> phoneBook; // Directory of agent identifiers for communication

	/**
	 * Retrieves or initializes the map of total scheduling contributions by period.
	 * @return the map of total contributions
	 */
	public HashMap<Integer, HashMap<AID, Double>> getTotalListScheduling() {
	    if (totalListScheduling == null) {
	        totalListScheduling = new HashMap<>();
	    }
	    return totalListScheduling;
	}

	/**
	 * Sets the total list of scheduling contributions by period.
	 * @param totalListScheduling the new map of contributions
	 */
	public void setTotalListScheduling(HashMap<Integer, HashMap<AID, Double>> totalListScheduling) {
	    this.totalListScheduling = totalListScheduling;
	}

	/**
	 * Retrieves or initializes the map of contributions for the current iteration.
	 * @return the map of current iteration contributions
	 */
	public HashMap<AID, Double> getIterationListScheduling() {
	    if (iterationListScheduling == null) {
	        iterationListScheduling = new HashMap<>();
	    }
	    return iterationListScheduling;
	}

	/**
	 * Sets the iteration-specific list of scheduling contributions.
	 * @param iterationListScheduling the new map of contributions for the current iteration
	 */
	public void setIterationListScheduling(HashMap<AID, Double> iterationListScheduling) {
	    this.iterationListScheduling = iterationListScheduling;
	}

	public void addTotalListSchedling(int periode, HashMap<AID, Double> hashMap) {
		totalListScheduling = getTotalListScheduling();
		totalListScheduling.put(periode, hashMap);
	}

	public void additerationListScheduling(AID aid, double productionQuantity) {
		iterationListScheduling = getIterationListScheduling();
		iterationListScheduling.put(aid, productionQuantity);
	}

	public InternalDataModel() {
		this.dsmInformation = new DSMInformation();
		iterationADMMTable = new ArrayList<>();
		schedulingResults = new SchedulingResults();
	}

	// ---- Getter & Setter ----
	
	/**
	 * Gets boolean, if Optimization should be started
	 * @return the rescheduling period
	 */
	public boolean isStartOptimization() {
		return isStartOptimization;
	}

	
	/**
	 * Sets the isStartOptimization boolean
	 * @param isStartOptimization boolean indicating if PEA-Agent should start Optimization
	 */
	public void setStartOptimization(boolean isStartOptimization) {
		this.isStartOptimization = isStartOptimization;
	}
	

	/**
	 * Gets the period for which rescheduling is activated.
	 * @return the rescheduling period
	 */
	public int getReschedulingPeriod() {
	    return reschedulingPeriod;
	}

	/**
	 * Sets the rescheduling period.
	 * @param reschedulingPeriod the period to be set for rescheduling
	 */
	public void setReschedulingPeriod(int reschedulingPeriod) {
	    this.reschedulingPeriod = reschedulingPeriod;
	}

	/**
	 * Checks if rescheduling is activated.
	 * @return true if rescheduling is activated, false otherwise
	 */
	public boolean isReschedulingActivated() {
	    return reschedulingActivated;
	}

	/**
	 * Sets the rescheduling activation status.
	 * @param reschedulingActivated the new status of rescheduling
	 */
	public void setReschedulingActivated(boolean reschedulingActivated) {
	    this.reschedulingActivated = reschedulingActivated;
	}

	/**
	 * Gets the internal counter value.
	 * @return the current value of the counter
	 */
	public int getCounter() {
	    return counter;
	}

	/**
	 * Sets the internal counter value.
	 * @param counter the new value for the counter
	 */
	public void setCounter(int counter) {
	    this.counter = counter;
	}

	/**
	 * Increments the internal counter by one.
	 */
	public void incrementCounter() {
	    this.counter += 1;
	}

	/**
	 * Gets the file name of the MTP (Module Type Package).
	 * @return the MTP file name
	 */
	public String getMtpFileName() {
	    return mtpFileName;
	}

	/**
	 * Sets the MTP (Module Type Package) file name.
	 * @param mtpFileName the new MTP file name
	 */
	public void setMtpFileName(String mtpFileName) {
	    this.mtpFileName = mtpFileName;
	}

	/**
	 * Gets the startup duration converted to the number of periods.
	 * @return the startup duration in number of periods
	 */
	public int getStartUpDuration() {
	    return startUpDuration;
	}

	/**
	 * Sets the startup duration in real time and converts it to periods.
	 * @param startUpDuration the startup duration in minutes
	 */
	public void setStartUpDuration(double startUpDuration) {
	    double periodLength = 15; // define the length of one period in minutes
	    this.startUpDuration = (int) (startUpDuration / periodLength);
	}

	/**
	 * Sets the index of the current row in the shutdown order list.
	 * @param rowIndexShutdownOrder the new index for the shutdown order
	 */
	public void setRowIndexShutdownOrder(int rowIndexShutdownOrder) {
	    this.rowIndexShutdownOrder = rowIndexShutdownOrder;
	}

	/**
	 * Gets the current row index in the shutdown order list.
	 * @return the current row index
	 */
	public int getRowIndexShutdownOrder() {
	    return rowIndexShutdownOrder;
	}

	/**
	 * Updates the row index in the shutdown order list to the next row, cycling back to zero if the end is reached.
	 */
	public void updateShutdownOrderIndex() {
	    shutdownOrderList = getShutdownOrderList();
	    if (shutdownOrderList != null && !shutdownOrderList.isEmpty()) {
	        rowIndexShutdownOrder = (rowIndexShutdownOrder + 1) % shutdownOrderList.size();
	        System.out.println("Updated rowIndexShutdownOrder to: " + rowIndexShutdownOrder);
	    } else {
	        System.err.println("The shutdown order list is empty. Cannot update index.");
	    }
	}

	/**
	 * Gets the list of shutdown orders.
	 * @return the list of shutdown orders
	 */
	public ArrayList<Integer> getShutdownOrderList() {
	    return shutdownOrderList;
	}

	/**
	 * Retrieves the shutdown order value at a specific row index.
	 * @param rowIndex the index to retrieve
	 * @return the shutdown order value at the specified index, or -1 if the index is out of bounds
	 */
	public int getShutdownOrderValue(int rowIndex) {
	    if (shutdownOrderList != null && rowIndex >= 0 && rowIndex < shutdownOrderList.size()) {
	        return shutdownOrderList.get(rowIndex);
	    } else {
	        return -1; // Fallback value if index is out of bounds
	    }
	}

	/**
	 * Sets the list of shutdown orders.
	 * @param shutdownOrderList the new list of shutdown orders
	 */
	public void setShutdownOrderList(ArrayList<Integer> shutdownOrderList) {
	    this.shutdownOrderList = shutdownOrderList;
	}

	/**
	 * Gets the earliest possible start period.
	 * @return the earliest start period
	 */
	public int getEarliestStartPeriod() {
	    return earliestStartPeriod;
	}

	/**
	 * Sets the earliest possible start period.
	 * @param earliestStartPeriod the period to set as the earliest start
	 */
	public void setEarliestStartPeriod(int earliestStartPeriod) {
	    this.earliestStartPeriod = earliestStartPeriod;
	}

	/**
	 * Gets the demand for shutdown.
	 * @return the demand for shutdown
	 */
	public double getDemandShutdown() {
	    return demandShutdown;
	}

	/**
	 * Sets the demand value at which a shutdown should be considered.
	 * @param demandShutdown the shutdown demand threshold
	 */
	public void setDemandShutdown(double demandShutdown) {
	    this.demandShutdown = demandShutdown;
	}

	/**
	 * Gets the period set for a shutdown.
	 * @return the shutdown period
	 */
	public int getPeriodShutdown() {
	    return periodShutdown;
	}

	/**
	 * Sets the period during which a shutdown should occur.
	 * @param periodShutdown the period to set for the shutdown
	 */
	public void setPeriodShutdown(int periodShutdown) {
	    this.periodShutdown = periodShutdown;
	}
	
	/**
	 * Retrieves the complete list of received lower operating limits.
	 * @return a map of iteration indices to lists of Boolean values.
	 */
	public Map<Integer, List<Boolean>> getListReceivedLowerOperatingLimits() {
	    return listReceivedLowerOperatingLimits;
	}

	/**
	 * Sets the list of received lower operating limits.
	 * @param listReceivedLowerOperatingLimits a map of iteration indices to lists of Boolean values.
	 */
	public void setListReceivedLowerOperatingLimits(Map<Integer, List<Boolean>> listReceivedLowerOperatingLimits) {
	    this.listReceivedLowerOperatingLimits = listReceivedLowerOperatingLimits;
	}

	/**
	 * Adds a Boolean value to the list of received lower operating limits for a specific iteration.
	 * @param iteration the iteration index.
	 * @param value the Boolean value indicating if the lower operating limit was reached.
	 */
	   public void addLowerOperatingLimit(int iteration, boolean value) {
		   if (listReceivedLowerOperatingLimits == null ) {
			   listReceivedLowerOperatingLimits = new HashMap<>();
		}
		   
		   // Check whether there is already a list for the iteration
	        if (!listReceivedLowerOperatingLimits.containsKey(iteration)) {
	            listReceivedLowerOperatingLimits.put(iteration, new ArrayList<>());
	        }

	     // Add the value to the list
	        listReceivedLowerOperatingLimits.get(iteration).add(value);
	    }

	/**
	 * Checks if all Boolean values for a specific iteration indicate that the lower operating limit was reached.
	 * @param iteration the iteration index.
	 * @return true if all values are true, otherwise false.
	 */
		public boolean lowerLimitsAllTrueForIteration(int iteration) {
			   if (listReceivedLowerOperatingLimits == null ) {
				   listReceivedLowerOperatingLimits = new HashMap<>();
			}
			List<Boolean> values = listReceivedLowerOperatingLimits.get(iteration);
			if (values == null || values.isEmpty()) {
				return false;
			}

			for (boolean value : values) {
				if (!value) {
					return false;
				}
			}
			return true;
		}

	/**
	 * Prints all values of the lower operating limits for each iteration.
	 */
	public void printAllLowerOperatingLimitValues() {
	    listReceivedLowerOperatingLimits.forEach((iteration, values) ->
	        System.out.println("Iteration: " + iteration + ", Values: " + values));
	}

	// ---- Methods for Managing Upper Operating Limits ----

	/**
	 * Retrieves the complete list of received upper operating limits.
	 * @return a map of iteration indices to lists of Boolean values.
	 */
	public Map<Integer, List<Boolean>> getListReceivedUpperOperatingLimits() {
	    return listReceivedUpperOperatingLimits;
	}

	/**
	 * Sets the list of received upper operating limits.
	 * @param listReceivedUpperOperatingLimits a map of iteration indices to lists of Boolean values.
	 */
	public void setListReceivedUpperOperatingLimits(Map<Integer, List<Boolean>> listReceivedUpperOperatingLimits) {
	    this.listReceivedUpperOperatingLimits = listReceivedUpperOperatingLimits;
	}

	/**
	 * Adds a Boolean value to the list of received upper operating limits for a specific iteration.
	 * @param iteration the iteration index.
	 * @param value the Boolean value indicating if the upper operating limit was reached.
	 */
	   public void addUpperOperatingLimit(int iteration, boolean value) {
		   if (listReceivedUpperOperatingLimits == null ) {
			   listReceivedUpperOperatingLimits = new HashMap<>();
		}
		   
		   // Check whether there is already a list for the iteration
	        if (!listReceivedUpperOperatingLimits.containsKey(iteration)) {
	            listReceivedUpperOperatingLimits.put(iteration, new ArrayList<>());
	        }

	     // Add the value to the list
	        listReceivedUpperOperatingLimits.get(iteration).add(value);
	    }

	/**
	 * Checks if all Boolean values for a specific iteration indicate that the upper operating limit was reached.
	 * @param iteration the iteration index.
	 * @return true if all values are true, otherwise false.
	 */
	public boolean upperLimitsAllTrueForIteration(int iteration) {
	    List<Boolean> values = listReceivedUpperOperatingLimits.getOrDefault(iteration, new ArrayList<>());
	    return !values.contains(false);
	}

	/**
	 * Prints all values of the upper operating limits for each iteration.
	 */
	public void printAllUpperOperatingLimitValues() {
	    listReceivedUpperOperatingLimits.forEach((iteration, values) ->
	        System.out.println("Iteration: " + iteration + ", Values: " + values));
	}

	// ---- Methods for Managing Production Quantities ----

    /**
     * Retrieves the mLCOH (Marginal Levelized Cost of Hydrogen) for a specific period and iteration.
     *
     * @param period    The scheduling period.
     * @param iteration The iteration number.
     * @return The mLCOH for the specified period and iteration, or Double.NaN if not found.
     */
    public double getmLCOHForPeriodAndIteration(int period, int iteration) {
        // Iterate through each IterationADMM instance in the list
        for (IterationADMM admm : iterationADMMTable) {
            // Check if the period and iteration match the provided values
            if (admm.getPeriod() == period && admm.getIteration() == iteration) {
                // Return the mLCOH value for the matching period and iteration
                return admm.getmLCOH();
            }
        }
        // If no matching period and iteration is found, return Double.NaN
        return Double.NaN;
    }
	
	/**
	 * Adds a received production quantity for a specific period and iteration.
	 * @param period the period index.
	 * @param iteration the iteration index.
	 * @param quantity the production quantity received.
	 */
	public void addReceivedProductionQuantity(int period, int iteration, double quantity) {
		if (listReceivedProductionQuantities == null) {
			listReceivedProductionQuantities = new HashMap<>();
		}
		
    	listReceivedProductionQuantities.computeIfAbsent(period, k -> new HashMap<>());
    	listReceivedProductionQuantities.get(period).put(iteration, quantity);
    }

	/**
	 * Retrieves the received production quantity for a specific period and iteration.
	 * @param period the period index.
	 * @param iteration the iteration index.
	 * @return the production quantity or 0.0 if none was recorded.
	 */
	public Double getReceivedProductionQuantity(int period, int iteration) {
	    return listReceivedProductionQuantities.getOrDefault(period, new HashMap<>()).getOrDefault(iteration, 0.0);
	}

	/**
	 * Retrieves the list of IterationADMM objects, ensuring that the list is initialized if it's null.
	 * @return the list of IterationADMM objects.
	 */
	public List<IterationADMM> getIterationADMMTable() {
	    if (iterationADMMTable == null) {
	        iterationADMMTable = new ArrayList<>();
	    }
	    return iterationADMMTable;
	}

	/**
	 * Prints detailed values of each IterationADMM object in the table.
	 */
	public void printIterationADMMValues() {
	    for (IterationADMM iterationADMM : getIterationADMMTable()) {
	        System.out.printf("Period: %d, Iteration: %d, ProductionQuantity: %.2f, EnergyDemand: %.2f, mLCOH: %.2f%n",
	            iterationADMM.getPeriod(), iterationADMM.getIteration(),
	            iterationADMM.getProductionQuantity(), iterationADMM.getEnergyDemand(),
	            iterationADMM.getmLCOH());
	    }
	}

	/**
	 * Retrieves the production quantity for a specific period and iteration.
	 * @param targetPeriod the target period.
	 * @param targetIteration the target iteration.
	 * @return the production quantity, or -1 if not found.
	 */
	public double getProductionQuantityForPeriodAndIteration(int targetPeriod, int targetIteration) {
	    for (IterationADMM iteration : iterationADMMTable) {
	        if (iteration.getPeriod() == targetPeriod && iteration.getIteration() == targetIteration) {
	            return iteration.getProductionQuantity();
	        }
	    }
	    return -1; // Indicate not found
	}

	/**
	 * Resets all production quantities in the ADMM table to zero.
	 */
	public void resetProductionQuantities() {
	    for (IterationADMM iterationADMM : iterationADMMTable) {
	        iterationADMM.setProductionQuantity(0);
	    }
	}

	/**
	 * Adds a new IterationADMM record to the table.
	 * @param period the period of the iteration.
	 * @param iteration the iteration number.
	 * @param productionQuantity the production quantity.
	 * @param energyDemand the energy demand.
	 * @param x the x value (cost optimal utilization).
	 * @param z the z value (demand optimal utilization).
	 * @param mLCOH the marginal levelized cost of hydrogen.
	 */
	public void addIterationADMMInfo(int period, int iteration, double productionQuantity, double energyDemand, double x, double z, double mLCOH) {
	    IterationADMM info = new IterationADMM(period, iteration, productionQuantity, energyDemand, x, z, mLCOH);
	    iterationADMMTable.add(info);
	}

	/**
	 * Retrieves the x value for a specific period and iteration.
	 * @param period the period number.
	 * @param iteration the iteration number.
	 * @return the x value, or null if not found.
	 */
	public Double getXForIteration(int period, int iteration) {
	    for (IterationADMM admm : iterationADMMTable) {
	        if (admm.getPeriod() == period && admm.getIteration() == iteration) {
	            return admm.getX();
	        }
	    }
	    return null;
	}

	/**
	 * Retrieves the z value for a specific period and iteration.
	 * @param period the period number.
	 * @param iteration the iteration number.
	 * @return the z value, or null if not found.
	 */
	public Double getZForIteration(int period, int iteration) {
	    for (IterationADMM admm : iterationADMMTable) {
	        if (admm.getPeriod() == period && admm.getIteration() == iteration) {
	            return admm.getZ();
	        }
	    }
	    return null;
	}

	/**
	 * Clears all values from the iteration ADMM table.
	 */
	public void clearIterationADMMTable() {
	    iterationADMMTable.clear();
	}

	// ---- Configuration and Operational Parameters ----

	/**
	 * Sets the load factor and converts the percentage input to a decimal.
	 * @param loadFactor the load factor percentage to be set.
	 */
	public void setLoadFactor(double loadFactor) {
	    this.loadFactor = loadFactor / 100;
	}

	/**
	 * Gets the operation and maintenance factor, converted from percentage to decimal.
	 * @return the OM factor as a decimal.
	 */
	public double getOMFactor() {
	    return OMFactor / 100;
	}

	/**
	 * Sets the operation and maintenance factor.
	 * @param oMFactor the OM factor percentage to be set.
	 */
	public void setOMFactor(double oMFactor) {
	    OMFactor = oMFactor;
	}

	/**
	 * Retrieves the utilization time of the equipment.
	 * @return the utilization time in years.
	 */
	public int getUtilizationTime() {
	    return utilizationTime;
	}

	/**
	 * Sets the utilization time based on the equipment's expected lifetime.
	 * @param lifetime the lifetime in years.
	 */
	public void setUtilizationTime(int lifetime) {
	    this.utilizationTime = lifetime;
	}

	/**
	 * Retrieves the discount rate, converted from percentage to decimal.
	 * @return the discount rate as a decimal.
	 */
	public double getDiscountRate() {
	    return discountrate / 100;
	}

	/**
	 * Sets the discount rate after converting from a percentage.
	 * @param discountrate the discount rate as a percentage.
	 */
	public void setDiscountRate(double discountrate) {
	    this.discountrate = discountrate;
	}

	// ---- OPC UA Communication ----
	
	/**
	 * Gets the endpoint URL for the OPC UA connection.
	 * @return the endpoinst URL
	 */
	public String getEndpointURL() {
	    return endpointURL;
	}
	
	public String getPeaEndpointURL() {
	    return peaEndpointURL;
	}

	/**
	 * Sets the endpoint URL for the OPC UA connection.
	 * @param endpointURL the new endpoint URL
	 */
	public void setEndpointURL(String endpointURL) {
	    this.endpointURL = endpointURL;
	}
	
	public void setPeaEndpointURL(String peaEndpointURL) {
	    this.peaEndpointURL = peaEndpointURL;
	}

	/**
	 * Retrieves the OPC UA client instance.
	 * @return the OPC UA client.
	 */
	public OpcUaClient getOpcUaClient() {
	    return opcUaClient;
	}

	public OpcUaClient getPeaOpcUaClient() {
		return peaOpcUaClient;
	}

	/**
	 * Sets the OPC UA client instance.
	 * @param opcUaClient the OPC UA client to be set.
	 */
	public void setOpcUaClient(OpcUaClient opcUaClient) {
	    this.opcUaClient = opcUaClient;
	}

	public void setPeaOpcUaClient(OpcUaClient client) {
		this.peaOpcUaClient = client;
	}

	/**
	 * Retrieves a list of endpoint descriptions. Initializes the list if it is null.
	 * @return a list of endpoint descriptions.
	 */
	public List<EndpointDescription> getEndpoints() {
	    if (endpoints == null) {
	        endpoints = new ArrayList<>();
	    }
	    return endpoints;
	}
	
	public List<EndpointDescription> getPeaEndpoints() {
	    if (peaEndpoints == null) {
	        peaEndpoints = new ArrayList<>();
	    }
	    return peaEndpoints;
	}

	/**
	 * Sets the list of endpoint descriptions.
	 * @param endpoints the list of endpoint descriptions to be set.
	 */
	public void setEndpoints(List<EndpointDescription> endpoints) {
	    this.endpoints = endpoints;
	}

	public void setPeaEndpoints(List<EndpointDescription> endpoints) {
		this.peaEndpoints = endpoints;
	}

	/**
	 * Retrieves the configuration point for the OPC UA connection.
	 * @return the configuration point.
	 */
	public EndpointDescription getConfigPoint() {
	    return configPoint;
	}
	
	public EndpointDescription getPeaConfigPoint() {
	    return peaConfigPoint;
	}

	/**
	 * Sets the configuration point for the OPC UA connection.
	 * @param configPoint the configuration point to be set.
	 */
	public void setConfigPoint(EndpointDescription configPoint) {
	    this.configPoint = configPoint;
	}
	
	public void setPeaConfigPoint(EndpointDescription peaConfigPoint) {
	    this.peaConfigPoint = peaConfigPoint;
	}

	/**
	 * Retrieves the OPC UA client's address space.
	 * @return the address space.
	 */
	public AddressSpace getAddressSpace() {
	    return addressSpace;
	}
	
	public AddressSpace getPeaAddressSpace() {
	    return peaAddressSpace;
	}

	/**
	 * Sets the OPC UA client's address space.
	 * @param addressSpace the address space to be set.
	 */
	public void setAddressSpace(AddressSpace addressSpace) {
	    this.addressSpace = addressSpace;
	}
	
	public void setPeaAddressSpace(AddressSpace peaAddressSpace) {
	    this.peaAddressSpace = peaAddressSpace;
	}

	/**
	 * Retrieves the OPC UA client configuration builder, initializing it if necessary.
	 * @return the configuration builder.
	 */
	public OpcUaClientConfigBuilder getCfg() {
	    if (cfg == null) {
	        cfg = new OpcUaClientConfigBuilder();
	    }
	    return cfg;
	}
	
	public OpcUaClientConfigBuilder getPeaCfg() {
	    if (peaCfg == null) {
	        peaCfg = new OpcUaClientConfigBuilder();
	    }
	    return peaCfg;
	}

	/**
	 * Sets the OPC UA client configuration builder.
	 * @param cfg the configuration builder to be set.
	 */
	public void setCfg(OpcUaClientConfigBuilder cfg) {
	    this.cfg = cfg;
	}

	public void setPeaCfg(OpcUaClientConfigBuilder peaCfg) {
	    this.peaCfg = peaCfg;
	}
	
	// ---- Message Handling ----

	/**
	 * Checks if message receiving is enabled.
	 * @return true if message receiving is enabled, otherwise false.
	 */
	public boolean isEnableMessageReceive() {
	    return enableMessageReceive;
	}

	/**
	 * Enables or disables message receiving.
	 * @param enableMessageReceive true to enable, false to disable.
	 */
	public void setEnableMessageReceive(boolean enableMessageReceive) {
	    this.enableMessageReceive = enableMessageReceive;
	}

	/**
	 * Checks if messages are currently being received.
	 * @return true if messages are being received, otherwise false.
	 */
	public boolean isReceiveMessages() {
	    return receiveMessages;
	}

	/**
	 * Sets the state of message reception.
	 * @param receiveMessages true to enable reception, false to disable.
	 */
	public void setReceiveMessages(boolean receiveMessages) {
	    this.receiveMessages = receiveMessages;
	}

	/**
	 * Increments the count of received messages by one.
	 */
	public void increaseCountReceivedMessages() {
	    CountReceivedMessages++;
	}

	/**
	 * Retrieves the count of received messages.
	 * @return the count of received messages.
	 */
	public int getCountReceivedMessages() {
	    return CountReceivedMessages;
	}

	/**
	 * Sets the count of received messages.
	 * @param countReceivedMessages the number of received messages to be set.
	 * @return the count of received messages.
	 */
	public int setCountReceivedMessages(int countReceivedMessages) {
	    this.CountReceivedMessages = countReceivedMessages;
	    return countReceivedMessages;
	}

	// ---- Scheduling and Production Parameters ----

	/** Returns the tolerable deviation from the required production quantity. */
	public double getEpsilonProduction() {
	    return epsilonProduction;
	}

	/** Sets the tolerable deviation from the required production quantity. */
	public void setEpsilonProduction(double epsilonProduction) {
	    this.epsilonProduction = epsilonProduction;
	}

	/** Returns the total sum of production from all agents. */
	public double getSumProduction() {
	    return sumProduction;
	}

	/** Sets the total sum of production from all agents. */
	public void setSumProduction(double sumProduction) {
	    this.sumProduction = sumProduction;
	}

	/** Returns the penalty factor used in optimization algorithms. */
	public double getPenaltyFactor() {
	    return penaltyFactor;
	}

	/** Sets the penalty factor used in optimization algorithms. */
	public void setPenaltyFactor(double penaltyFactor) {
	    this.penaltyFactor = penaltyFactor;
	}

	// ---- Iteration and Period Management ----

	/** Returns the current iteration number. */
	public int getIteration() {
	    return iteration;
	}

	/** Sets the current iteration number. */
	public void setIteration(int iteration) {
	    this.iteration = iteration;
	}

	/** Increments the iteration number by one. */
	public void incrementIteration() {
	    iteration++;
	}

	/** Returns the current scheduling period. */
	public int getCurrentPeriod() {
	    return currentPeriod;
	}

	/** Sets the current scheduling period. */
	public void setCurrentPeriod(int currentPeriod) {
	    this.currentPeriod = currentPeriod;
	}

	/** Increments the current scheduling period by one. */
	public void incrementCurrentPeriod() {
	    currentPeriod++;
	}

	// ---- Electrolyzer Configuration Parameters ----

	/** Returns the current utilization of the electrolyzer in cost-optimized scenarios. */
	public double getX() {
	    return x;
	}

	/** Sets the current utilization of the electrolyzer in cost-optimized scenarios. */
	public void setX(double x) {
	    this.x = x;
	}

	/** Returns the current utilization of the electrolyzer in demand-optimized scenarios. */
	public double getZ() {
	    return z;
	}

	/** Sets the current utilization of the electrolyzer in demand-optimized scenarios. */
	public void setZ(double z) {
	    this.z = z;
	}

	/** Returns the minimum power limit of the electrolyzer. */
	public double getMinPower() {
	    return minPower;
	}

	/** Sets the minimum power limit of the electrolyzer. */
	public void setMinPower(double minPower) {
	    this.minPower = minPower;
	}

	/** Returns the maximum power limit of the electrolyzer. */
	public double getMaxPower() {
	    return maxPower;
	}

	/** Sets the maximum power limit of the electrolyzer. */
	public void setMaxPower(double maxPower) {
	    this.maxPower = maxPower;
	}
	
	/**
	 * Sets the number of agents in the system.
	 *
	 * @param numberofAgents The number of agents to set.
	 */
	public void setNumberofAgents(int numberofAgents) {
	    this.numberofAgents = numberofAgents;
	}

	/**
	 * Gets the number of agents in the system.
	 *
	 * @return The number of agents.
	 */
	public int getNumberofAgents() {
	    return numberofAgents;
	}

	/**
	 * Gets the index of the next period for which scheduling results will be generated.
	 *
	 * @return The index of the next period.
	 */
	public int getSchedulingResultNextPeriod() {
	    return schedulingResultNextPeriod;
	}

	/**
	 * Checks if the header has been written for the scheduling results.
	 *
	 * @return True if the header has been written, false otherwise.
	 */
	public boolean isHeaderWritten() {
	    return headerWritten;
	}

	/**
	 * Sets the flag indicating whether the header has been written for the scheduling results.
	 *
	 * @param headerWritten True to indicate that the header has been written, false otherwise.
	 */
	public void setHeaderWritten(boolean headerWritten) {
	    this.headerWritten = headerWritten;
	}

	/**
	 * Checks if scheduling is complete.
	 *
	 * @return True if scheduling is complete, false otherwise.
	 */
	public boolean isSchedulingComplete() {
	    return schedulingComplete;
	}

	/**
	 * Sets the flag indicating whether scheduling is complete.
	 *
	 * @param schedulingComplete True to indicate that scheduling is complete, false otherwise.
	 */
	public void setSchedulingComplete(boolean schedulingComplete) {
	    this.schedulingComplete = schedulingComplete;
	}

	/**
	 * Gets the timestamp of the last schedule write operation.
	 *
	 * @return The timestamp of the last schedule write operation.
	 */
	public LocalDateTime getLastScheduleWriteTime() {
	    return lastScheduleWriteTime;
	}

	/**
	 * Sets the timestamp of the last schedule write operation.
	 *
	 * @param lastScheduleWriteTime The timestamp of the last schedule write operation.
	 */
	public void setLastScheduleWriteTime(LocalDateTime lastScheduleWriteTime) {
	    this.lastScheduleWriteTime = lastScheduleWriteTime;
	}

	/**
	 * Increments the index of the next period for which scheduling results will be generated.
	 */
	public void incrementSchedulingResultNextPeriod(){
	    this.schedulingResultNextPeriod = schedulingResultNextPeriod + 1;
	}

	// ---- Electrolyzer State Flags ----

	/** Checks if the electrolyzer is currently in production state. */
	public boolean isStateProduction() {
	    return stateProduction;
	}

	/** Sets the production state of the electrolyzer. */
	public void setStateProduction(boolean stateProduction) {
	    this.stateProduction = stateProduction;
	}

	/** Checks if the electrolyzer is currently in standby state. */
	public boolean isStateStandby() {
	    return stateStandby;
	}

	/** Sets the standby state of the electrolyzer. */
	public void setStateStandby(boolean stateStandby) {
	    this.stateStandby = stateStandby;
	}

	/** Checks if the electrolyzer is currently idle. */
	public boolean isStateIdle() {
	    return stateIdle;
	}

	/** Sets the idle state of the electrolyzer. */
	public void setStateIdle(boolean stateIdle) {
	    this.stateIdle = stateIdle;
	}
	
	/**
	 * Gets the capital expenditure (CapEx).
	 *
	 * @return The capital expenditure.
	 */
	public double getCapEx() {
	    return CapEx;
	}

	/**
	 * Sets the capital expenditure (CapEx).
	 *
	 * @param capEx The capital expenditure to set.
	 */
	public void setCapEx(double capEx) {
	    CapEx = capEx;
	}

	/**
	 * Gets the utilization time.
	 *
	 * @return The utilization time.
	 */
	public int getUtilizaziontime() {
	    return utilizationTime;
	}

	/**
	 * Sets the utilization time.
	 *
	 * @param lifetime The utilization time to set.
	 */
	public void setUtilizationtime(int lifetime) {
	    this.utilizationTime = lifetime;
	}

	/**
	 * Gets the discount rate.
	 *
	 * @return The discount rate (as a fraction, not percentage).
	 */
	public double getDiscountrate() {
	    double rate = discountrate / 100; // Convert percentage to fraction
	    return rate;
	}

	/**
	 * Sets the discount rate.
	 *
	 * @param discountrate The discount rate to set (in fraction, not percentage).
	 */
	public void setDiscountrate(double discountrate) {
	    this.discountrate = discountrate;
	}

	/**
	 * Gets the load factor.
	 *
	 * @return The load factor.
	 */
	public double getLoadFactor() {
	    return loadFactor;
	}

	// ---- Electrolyzer Performance Parameters ----

	/** Returns the electrical power level of the electrolyzer. */
	public double getPEL() {
	    return PEL;
	}

	/** Sets the electrical power level of the electrolyzer. */
	public void setPEL(double PEL) {
	    this.PEL = PEL;
	}

	/** Returns the Lagrange multiplier used in optimization. */
	public double getLambda() {
	    return lambda;
	}

	/** Sets the Lagrange multiplier used in optimization. */
	public void setLambda(double lambda) {
	    this.lambda = lambda;
	}

	/** Returns coefficient A for the production cost function. */
	public double getProductionCoefficientA() {
	    return ProductionCoefficientA;
	}

	/** Sets coefficient A for the production cost function. */
	public void setProductionCoefficientA(double productionCoefficientA) {
	    ProductionCoefficientA = productionCoefficientA;
	}

	/** Returns coefficient B for the production cost function. */
	public double getProductionCoefficientB() {
	    return ProductionCoefficientB;
	}

	/** Sets coefficient B for the production cost function. */
	public void setProductionCoefficientB(double productionCoefficientB) {
	    ProductionCoefficientB = productionCoefficientB;
	}

	/** Returns coefficient C for the production cost function. */
	public double getProductionCoefficientC() {
	    return ProductionCoefficientC;
	}

	/** Sets coefficient C for the production cost function. */
	public void setProductionCoefficientC(double productionCoefficientC) {
	    ProductionCoefficientC = productionCoefficientC;
	}

	// ---- PHONE-BOOK MANAGEMENT ----

	/**
	 * Adds an Agent Identifier (AID) to the phone book. This method ensures that the
	 * phone book is initialized before adding the AID.
	 *
	 * @param agentAID The AID of the agent to add to the phone book.
	 */
	public void addAID2PhoneBook(AID agentAID) {
	    List<AID> phoneBook = getPhoneBook(); // Ensure the phone book is initialized
	    phoneBook.add(agentAID);
	}

	/**
	 * Retrieves the phone book containing all AIDs. If the phone book has not been initialized,
	 * it initializes a new ArrayList to store the AIDs.
	 *
	 * @return The list of AIDs in the phone book.
	 */
	public List<AID> getPhoneBook() {
	    if (phoneBook == null) {
	        phoneBook = new ArrayList<>();
	    }
	    return phoneBook;
	}

	/**
	 * Sets the phone book to a specific list of AIDs. This method allows directly setting
	 * the list of AIDs from an external source or for initialization purposes.
	 *
	 * @param phoneBook The list of AIDs to set as the phone book.
	 */
	public void setPhoneBook(List<AID> phoneBook) {
	    this.phoneBook = phoneBook;
	}


}
