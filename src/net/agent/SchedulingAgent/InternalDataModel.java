package net.agent.SchedulingAgent;

import static org.hamcrest.CoreMatchers.nullValue;

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

public class InternalDataModel extends AbstractUserObject {
	
	//Global Variables
	private LocalDateTime lastScheduleWriteTime = LocalDateTime.of(1970, 1, 1, 0, 0); //Create variable to measure time and initialize with outdated values
	
	// OPC UA 
	private OpcUaClient opcUaClient;
	private List<EndpointDescription> endpoints;
	private OpcUaClientConfigBuilder cfg;
	private EndpointDescription configPoint;
	private AddressSpace addressSpace;
	private int schedulingResultNextPeriod = 1; //Defines the period with which the PLC was last communicated the scheduling results
	private boolean headerWritten = true; //Boolean variable to make sure that header will be written only 1x 
	
	// External Parameters
	private double CapEx = 8000; // Capital-Costs in €
	private double OMFactor = 1.5; // Factor for Operation & Maintenance in %
	private int lifetime = 20; // Lifetime of Electrolyzer
	private double minPower = 10; // Minimum Power from Electrolyzer
	private double maxPower = 100; // Maximum Power from Electrolyzer
	private double PEL = 2.4; // Elektrische Leistung des Elektrolyseur in kW
	private double discountrate = 9.73; //Discount rate
	private double loadFactor = 0.98; //Share of full load hours per year
	private double ProductionCoefficientA = -0.00000186008377263649;
	private double ProductionCoefficientB = 0.00068054065735310900;
	private double ProductionCoefficientC = -0.00524975504255868000;
	private int startUpDuration = 2; 

	// ADMM - Lagrange Multiplicators
	private double lambda = 0; // Lagrange-Multiplicator for Demand Constraint (Value 0.0)
	private double penaltyFactor = 0.18; // Penalty-Term (Value: 0.2)
	private int iteration = 0; // Iteration
	private double epsilonProduction = 0.001; // Tolerable deviation from the required production quantity (Value: 0.0005 (fast convergence))
	private int currentPeriod = 1;
	private int periodShutdown;
	private double demandShutdown;
	private int earliestStartPeriod;
	private boolean stateProduction = true;
	private boolean stateStandby;
	private boolean stateIdle;
	
	// Information within MAS 
	private int numberofAgents = 3; 
	private double sumProduction;
	private int CountReceivedMessages = 0;
	private boolean enableMessageReceive = false; // is set in Dual Update
	private boolean receiveMessages = true;
	private Map<Integer, Map<Integer, Double>> listReceivedProductionQuantities;
	private Map<Integer, List<Boolean>> listReceivedLowerOperatingLimits;
	private int rowIndexShutdownOrder = 0;
	private ArrayList<Integer> shutdownOrderList = new ArrayList<Integer>();

	// Variables
	private double x; // Cost optimal Utilization of the electrolyzer
	private double z; // Demand optimal Utilization of the electrolyzer
	private boolean schedulingComplete; //Boolean variable to indicate whether planning horizon was scheduled completed 

	// ---- DSMInformationen - from SQL-Database ----
	private DSMInformation dsmInformation;

	// Method for accessing the DSMInformation instance
	public DSMInformation getDSMInformation() {
		if (dsmInformation == null) {
			dsmInformation = new DSMInformation();
		}
		return this.dsmInformation;
	}

	// ---- Results per Iteratiopn 
	private List<IterationADMM> iterationADMMTable; 

	// Scheduling Results
	private SchedulingResults schedulingResults;
    public SchedulingResults getSchedulingResults() {
        return schedulingResults;
    }

	// Hash Map for Information Exchange 
	private HashMap<Integer, HashMap<AID, Double>> totalListScheduling;
	private HashMap<AID, Double> iterationListScheduling;
	List<AID> phoneBook;

	public HashMap<Integer, HashMap<AID, Double>> getTotalListScheduling() {
		if (totalListScheduling == null) {
			totalListScheduling = new HashMap<Integer, HashMap<AID, Double>>();
		}
		return totalListScheduling;
	}

	public void setTotalListScheduling(HashMap<Integer, HashMap<AID, Double>> totalListScheduling) {
		this.totalListScheduling = totalListScheduling;
	}

	public HashMap<AID, Double> getIterationListScheduling() {
		if (iterationListScheduling == null) {
			iterationListScheduling = new HashMap<AID, Double>();
		}

		return iterationListScheduling;
	}

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
	public int getStartUpDuration() {
		return startUpDuration;
	}

	public void setStartUpDuration(int startUpDuration) {
		this.startUpDuration = startUpDuration;
	}
	
	public void setRowIndexShutdownOrder(int rowIndexShutdownOrder) {
		this.rowIndexShutdownOrder = rowIndexShutdownOrder;
	}
	
	public int getRowIndexShutdownOrder() {
		return rowIndexShutdownOrder;
	}
	
	public void updateShutdownOrderIndex() {
		shutdownOrderList = getShutdownOrderList();
	    if (shutdownOrderList != null && !shutdownOrderList.isEmpty()) {
	        rowIndexShutdownOrder = (rowIndexShutdownOrder + 1) % shutdownOrderList.size();
	    } else {
	        System.err.println("The shutdown order list is zero or empty.");
	    }
	}
	
	
	public ArrayList<Integer> getShutdownOrderList() {
		return shutdownOrderList;
	}
	
	public int getShutdownOrderValue(int rowIndex) {
		if (shutdownOrderList == null) {
			shutdownOrderList = new ArrayList<>();
		}
		
	    if (shutdownOrderList != null && rowIndex >= 0 && rowIndex < shutdownOrderList.size()) {
	        return shutdownOrderList.get(rowIndex);
	    } else {
	    	// Insert your desired fallback value or error handling code here
	        return -1; // Example fallback value
	    }
	}


	public void setShutdownOrderList(ArrayList<Integer> shutdownOrderList) {
		this.shutdownOrderList = shutdownOrderList;
	}
	
	
	public int getEarliestStartPeriod() {
		return earliestStartPeriod;
	}

	public void setEarliestStartPeriod(int earliestStartPeriod) {
		this.earliestStartPeriod = earliestStartPeriod;
	}

	public double getDemandShutdown() {
		return demandShutdown;
	}

	public void setDemandShutdown(double demandShutdown) {
		this.demandShutdown = demandShutdown;
	}
	
	public int getPeriodShutdown() {
		return periodShutdown;
	}

	public void setPeriodShutdown(int periodShutdown) {
		this.periodShutdown = periodShutdown;
	}
	
	public Map<Integer, List<Boolean>> getListReceivedLowerOperatingLimits() {
		return listReceivedLowerOperatingLimits;
	}

	public void setListReceivedLowerOperatingLimits(Map<Integer, List<Boolean>> listReceivedLowerOperatingLimits) {
		this.listReceivedLowerOperatingLimits = listReceivedLowerOperatingLimits;
	}
	
	
	// Method for entering a Boolean value for a specific iteration
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
    
	// Method to check whether all Boolean values for an iteration are true
		public boolean checkAllTrueForIteration(int iteration) {
			   if (listReceivedLowerOperatingLimits == null ) {
				   listReceivedLowerOperatingLimits = new HashMap<>();
			}
			List<Boolean> values = listReceivedLowerOperatingLimits.get(iteration);
			// Falls es keine Liste für die Iteration gibt oder die Liste leer ist
			if (values == null || values.isEmpty()) {
				return false;
			}

			// Überprüfe, ob alle Werte in der Liste true sind
			for (boolean value : values) {
				if (!value) {
					return false;
				}
			}

			return true;
		}
    
	 // Method for outputting all values of lower operating values
	    public void printAllLowerOperatingLimitValues() {
	        for (Map.Entry<Integer, List<Boolean>> entry : listReceivedLowerOperatingLimits.entrySet()) {
	            int iteration = entry.getKey();
	            List<Boolean> values = entry.getValue();

	            System.out.println("Iteration: " + iteration + ", Values: " + values);
	        }
	    }
	
	// Method to add received production quantity for a specific period and iteration
	public void addReceivedProductionQuantity(int period, int iteration, double quantity) {
        // Check if the map for the period exists, create it if not
		if (listReceivedProductionQuantities == null) {
			listReceivedProductionQuantities = new HashMap<>();
		}
		
    	listReceivedProductionQuantities.computeIfAbsent(period, k -> new HashMap<>());

        // Add the quantity to the map
    	listReceivedProductionQuantities.get(period).put(iteration, quantity);
    }

    // Method to get received production quantity for a specific period and iteration
    public Double getReceivedProductionQuantity(int period, int iteration) {
        return listReceivedProductionQuantities.getOrDefault(period, new HashMap<>()).getOrDefault(iteration, 0.0);
    }
	
    
	//Method of retrieving iterationADMMTable
	public List<IterationADMM> getIterationADMMTable() {
		if (iterationADMMTable == null) {
			iterationADMMTable = new ArrayList<IterationADMM>();
		}
		return iterationADMMTable;
	}
	
	
	// Print all Values for every Iteration 
	public void printIterationADMMValues() {
	    for (IterationADMM iterationADMM : getIterationADMMTable()) {
	        System.out.printf("Period: %d, Iteration: %d, ProductionQuantity: %.2f, EnergyDemand: %.2f, mLCOH: %.2f%n",
	                iterationADMM.getPeriod(), iterationADMM.getIteration(),
	                iterationADMM.getProductionQuantity(), iterationADMM.getEnergyDemand(),
	                iterationADMM.getmLCOH());
	    }
	}

	// Get Values for a specific Iteration and Period 
	public double getProductionQuantityForPeriodAndIteration(int targetPeriod, int targetIteration) {
		for (IterationADMM iteration : iterationADMMTable) {
			if (iteration.getPeriod() == targetPeriod && iteration.getIteration() == targetIteration) {
				return iteration.getProductionQuantity();
			}
		}
		return -1; 
	}

	//Method for adding values to ADMMTable 
	public void addIterationADMMInfo(int period, int iteration, double productionQuantity, double energyDemand, double x, double z, double mLCOH) {
		IterationADMM info = new IterationADMM(period, iteration, productionQuantity, energyDemand, x, z, mLCOH);
		iterationADMMTable.add(info);
	}
	
	public int getNumberofAgents() {
		return numberofAgents;
	}

	public void setNumberofAgents(int numberofAgents) {
		this.numberofAgents = numberofAgents;
	}
	
	public boolean isHeaderWritten() {
		return headerWritten;
	}

	public void setHeaderWritten(boolean headerWritten) {
		this.headerWritten = headerWritten;
	}
	
	public LocalDateTime getLastScheduleWriteTime() {
		return lastScheduleWriteTime;
	}

	public void setLastScheduleWriteTime(LocalDateTime lastScheduleWriteTime) {
		this.lastScheduleWriteTime = lastScheduleWriteTime;
	}
	
	public int getSchedulingResultNextPeriod() {
		return schedulingResultNextPeriod;
	}

	public void setSchedulingResultNextPeriod(int writeScheduleCount) {
		this.schedulingResultNextPeriod = writeScheduleCount;
	}
	
	public void incrementSchedulingResultNextPeriod(){
		this.schedulingResultNextPeriod = schedulingResultNextPeriod + 1;
	}
	
	public boolean isSchedulingComplete() {
		return schedulingComplete;
	}

	public void setSchedulingComplete(boolean schedulingComplete) {
		this.schedulingComplete = schedulingComplete;
	}
	
	public double getCapEx() {
		return CapEx;
	}

	public void setCapEx(double capEx) {
		CapEx = capEx;
	}
	
	public double getLoadFactor() {
		return loadFactor;
	}

	public void setLoadFactor(double loadFactor) {
		this.loadFactor = loadFactor;
	}
	
	public double getOMFactor() {
		double factor = OMFactor / 100; //remove percentage
		return factor;
	}

	public void setOMFactor(double oMFactor) {
		OMFactor = oMFactor;
	}
	
	public int getLifetime() {
		return lifetime;
	}

	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}

	public double getDiscountrate() {
		double rate = discountrate/100; //remove percentage
		return rate;
	}

	public void setDiscountrate(double discountrate) {
		this.discountrate = discountrate;
	}
	
	public OpcUaClient getOpcUaClient() {
		return opcUaClient;
	}

	public void setOpcUaClient(OpcUaClient opcUaClient) {
		this.opcUaClient = opcUaClient;
	}

	public List<EndpointDescription> getEndpoints() {
	    if (endpoints == null) {
	        endpoints = new ArrayList<>(); 
	    }
	    return endpoints;
	}

	public void setEndpoints(List<EndpointDescription> endpoints) {
		this.endpoints = endpoints;
	}

	public EndpointDescription getConfigPoint() {
		return configPoint;
	}

	public void setConfigPoint(EndpointDescription configPoint) {
		this.configPoint = configPoint;
	}

	public AddressSpace getAddressSpace() {
		return addressSpace;
	}

	public void setAddressSpace(AddressSpace addressSpace) {
		this.addressSpace = addressSpace;
	}
	
	public OpcUaClientConfigBuilder getCfg() {
		if (cfg == null) {
			cfg = new OpcUaClientConfigBuilder();
		}
		return cfg;
	}

	public void setCfg(OpcUaClientConfigBuilder cfg) {
		this.cfg = cfg;
	}
	
	public boolean isEnableMessageReceive() {
		return enableMessageReceive;
	}

	public void setEnableMessageReceive(boolean enableMessageReceive) {
		this.enableMessageReceive = enableMessageReceive;
	}
	
	public boolean isReceiveMessages() {
		return receiveMessages;
	}

	public void setReceiveMessages(boolean receiveMessages) {
		this.receiveMessages = receiveMessages;
	}
	
	public void increaseCountReceivedMessages() {
		CountReceivedMessages =  CountReceivedMessages + 1;
	}
	
	public int getCountReceivedMessages() {
		return CountReceivedMessages;
	}

	public int setCountReceivedMessages(int countReceivedMessages) {
		this.CountReceivedMessages = countReceivedMessages;
		return countReceivedMessages;
	}

	public double getEpsilonProduction() {
		return epsilonProduction;
	}

	public void setEpsilonProduction(double epsilonProduction) {
		this.epsilonProduction = epsilonProduction;
	}

	public double getSumProduction() {
		return sumProduction;
	}

	public double getPenaltyFactor() {
		return penaltyFactor;
	}

	public void setPenaltyFactor(double penaltyFactor) {
		this.penaltyFactor = penaltyFactor;
	}

	public void setSumProduction(double sumProduction) {
		this.sumProduction = sumProduction;
	}

	public int getIteration() {
		return iteration;
	}

	public void setIteration(int iteration) {
		this.iteration = iteration;
	}
	
	public void incrementIteration() {
		this.iteration = iteration+1;
	}

	public int getCurrentPeriod() {
		return currentPeriod;
	}

	public void setCurrentPeriod(int currentPeriod) {
		this.currentPeriod = currentPeriod;
	}
	
	public void incrementCurrentPeriod () {
		currentPeriod =  currentPeriod + 1;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public double getMinPower() {
		return minPower;
	}

	public void setMinPower(double minPower) {
		this.minPower = minPower;
	}

	public double getMaxPower() {
		return maxPower;
	}

	public void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	public boolean isStateProduction() {
		return stateProduction;
	}

	public void setStateProduction(boolean stateProduction) {
		this.stateProduction = stateProduction;
	}

	public boolean isStateStandby() {
		return stateStandby;
	}

	public void setStateStandby(boolean stateStandby) {
		this.stateStandby = stateStandby;
	}

	public boolean isStateIdle() {
		return stateIdle;
	}

	public void setStateIdle(boolean stateIdle) {
		this.stateIdle = stateIdle;
	}

	public double getPEL() {
		return PEL;
	}

	public void setPEL(double pEL) {
		PEL = pEL;
	}

	public double getLambda() {
		return lambda;
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	public double getProductionCoefficientA() {
		return ProductionCoefficientA;
	}

	public void setProductionCoefficientA(double productionCoefficientA) {
		ProductionCoefficientA = productionCoefficientA;
	}

	public double getProductionCoefficientB() {
		return ProductionCoefficientB;
	}

	public void setProductionCoefficientB(double productionCoefficientB) {
		ProductionCoefficientB = productionCoefficientB;
	}

	public double getProductionCoefficientC() {
		return ProductionCoefficientC;
	}

	public void setProductionCoefficientC(double productionCoefficientC) {
		ProductionCoefficientC = productionCoefficientC;
	}

	// ---- PHONE-BOOK ----
	public void addAID2PhoneBook(AID agentAID) {
		phoneBook = getPhoneBook();
		phoneBook.add(agentAID);
	}

	public List<AID> getPhoneBook() {
		if (phoneBook == null) {
			phoneBook = new ArrayList<AID>();
		}

		return phoneBook;
	}

	public void setPhoneBook(List<AID> phoneBook) {
		this.phoneBook = phoneBook;
	}

}
