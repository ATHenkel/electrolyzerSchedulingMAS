package net.agent.SchedulingAgent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	private double minPower = 5; // Minimum Power from Electrolyzer
	private double maxPower = 100; // Maximum Power from Electrolyzer
	private double PEL = 2.4; // Elektrische Leistung des Elektrolyseur in kW
	private double discountrate = 9.73; //Discount rate
	private double loadFactor = 0.98; //Share of full load hours per year
	
	private double ProductionCoefficientA = -0.0000003819;
	private double ProductionCoefficientB = 0.0005029463;
	private double ProductionCoefficientC = -0.0008;

	// ADMM - Lagrange Multiplicators
	private double lambda = 0; // Lagrange-Multiplicator for Demand Constraint (Value 0.0)
	private double penaltyFactor = 0.5; // Penalty-Term (Value: 0.5)
	private int iteration = 0; // Iteration
	private double epsilonProduction = 0.0005; // Tolerable deviation from the required production quantity (Value: 0.001 (fast convergence))
	private int currentPeriod = 1;
	private boolean stateProduction = true;
	private boolean stateStandby;
	private boolean stateIdle;
	
	// Gather-Information
	private double sumProduction;
	private double sumProduction_temp;
	private int CountReceivedMessages = 0;
	private boolean enableMessageReceive = false; // is set in Dual Update
	private boolean receiveMessages = true;

	// Variables
	private double x; // Leistung von Elektrolyseur/Agent
	private double z; // Hilfsvariable für z
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

	// ---- Ergebnisse pro Iteration ----
	private List<IterationADMM> iterationADMMTable; // Tabelle für IterationADMM-Informationen

	// Methode zum Abrufen von iterationADMMTable
	public List<IterationADMM> getIterationADMMTable() {
		if (iterationADMMTable == null) {
			iterationADMMTable = new ArrayList<IterationADMM>();
		}
		return iterationADMMTable;
	}
	
	public void printIterationADMMValues() {
	    // Schleife durch die iterationADMMTable und gib die Werte aus
	    for (IterationADMM iterationADMM : getIterationADMMTable()) {
	        System.out.printf("Period: %d, Iteration: %d, ProductionQuantity: %.2f, EnergyDemand: %.2f, mLCOH: %.2f%n",
	                iterationADMM.getPeriod(), iterationADMM.getIteration(),
	                iterationADMM.getProductionQuantity(), iterationADMM.getEnergyDemand(),
	                iterationADMM.getmLCOH());
	    }
	}

	// Methode zum Abrufen der Produktionsmenge für eine bestimmte Periode und Iteration
	public double getProductionQuantityForPeriodAndIteration(int targetPeriod, int targetIteration) {
		for (IterationADMM iteration : iterationADMMTable) {
			if (iteration.getPeriod() == targetPeriod && iteration.getIteration() == targetIteration) {
				return iteration.getProductionQuantity();
			}
		}
		return -1; 
	}

	public void addIterationADMMInfo(int period, int iteration, double productionQuantity, double energyDemand, double x, double z, double mLCOH) {
		IterationADMM info = new IterationADMM(period, iteration, productionQuantity, energyDemand, x, z, mLCOH);
		iterationADMMTable.add(info);
	}

	// SchedulingResults
	private SchedulingResults schedulingResults;
    public SchedulingResults getSchedulingResults() {
        return schedulingResults;
    }

	// Hashmap für Informationsaustausch
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
	
	public double getSumProduction_temp() {
		return sumProduction_temp;
	}

	public void setSumProduction_temp(double sumProduction_temp) {
		this.sumProduction_temp = sumProduction_temp;
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
