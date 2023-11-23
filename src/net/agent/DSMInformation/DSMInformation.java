package net.agent.DSMInformation;

import java.util.HashMap;
import java.util.Map;

public class DSMInformation {

	private Map<Integer, DSMData> externalInformation;

	public DSMInformation() {

		// Initialize the external information
		externalInformation = new HashMap<>();
	}

	// Method for adding information for a period
	public void addExternalDSMInformation(int id, double demand, double electricityPrice, double powerIn,
			double productionQuantity, double tanklevel, int timestep) {
		externalInformation.put(id,
				new DSMData(timestep, demand, electricityPrice, powerIn, productionQuantity, tanklevel));
	}

	// Method for retrieving DSM information
	public Map<Integer, DSMData> getDSMInformation() {
		return externalInformation;
	}

	// Method for retrieving DSM information for a specific period
	public DSMData getDSMInformationForPeriod(int timestep) {
		return externalInformation.get(timestep);
	}

	// Method for retrieving the DSM information for the last period
	public DSMData getLastDSMInformation() {
		int lastId = externalInformation.keySet().stream().max(Integer::compare).orElse(-1);
		return externalInformation.get(lastId);
	}

	// Method to get demand for a specific period
	public Double getDemandForPeriod(int timestep) {
		DSMData dsmData = externalInformation.get(timestep);
		if (dsmData != null) {
			return dsmData.getDemand();
		} else {
			return null; // Period not found
		}
	}

	// Method to get electricityPrice for a specific period
	public Double getElectricityPriceForPeriod(int timestep) {
		DSMData dsmData = externalInformation.get(timestep);
		if (dsmData != null) {
			return dsmData.getElectricityPrice();
		} else {
			return null; // Period not found
		}
	}

	// Method to get the last timestep for all values
	public int getLastPeriod() {
		int lastTimestep = -1;

		for (DSMData dsmData : externalInformation.values()) {
			if (dsmData.getTimestep() > lastTimestep) {
				lastTimestep = dsmData.getTimestep();
			}
		}

		return lastTimestep;
	}

	// Method to print all DSM information to the console
	public void printAllDSMInformation() {
		for (Map.Entry<Integer, DSMData> entry : externalInformation.entrySet()) {
			int id = entry.getKey();
			DSMData dsmData = entry.getValue();

			System.out.println("ID: " + id);
			System.out.println("Timestep: " + dsmData.getTimestep());
			System.out.println("Demand: " + dsmData.getDemand());
			System.out.println("ElectricityPrice: " + dsmData.getElectricityPrice());
			System.out.println("PowerIn: " + dsmData.getPowerIn());
			System.out.println("ProductionQuantity: " + dsmData.getProductionQuantity());
			System.out.println("Tanklevel: " + dsmData.getTanklevel());
			System.out.println("-----------------------------");
		}
	}

	// Inner class for representing the DSM data for each period
	public static class DSMData {
		private double demand;
		private double electricityPrice;
		private double powerIn;
		private double productionQuantity;
		private double tanklevel;
		private int timestep;

		public DSMData(int timestep, double demand, double electricityPrice, double powerIn, double productionQuantity,
				double tanklevel) {
			this.timestep = timestep;
			this.demand = demand;
			this.electricityPrice = electricityPrice;
			this.powerIn = powerIn;
			this.productionQuantity = productionQuantity;
			this.tanklevel = tanklevel;
		}

		// Getter & Setter

		public double getDemand() {
			return demand;
		}

		public void setDemand(double demand) {
			this.demand = demand;
		}

		public double getElectricityPrice() {
			return electricityPrice;
		}

		public void setElectricityPrice(double electricityPrice) {
			this.electricityPrice = electricityPrice;
		}

		public double getPowerIn() {
			return powerIn;
		}

		public void setPowerIn(double powerIn) {
			this.powerIn = powerIn;
		}

		public double getProductionQuantity() {
			return productionQuantity;
		}

		public void setProductionQuantity(double productionQuantity) {
			this.productionQuantity = productionQuantity;
		}

		public double getTanklevel() {
			return tanklevel;
		}

		public void setTanklevel(double tanklevel) {
			this.tanklevel = tanklevel;
		}

		public int getTimestep() {
			return timestep;
		}

		public void setTimestep(int timestep) {
			this.timestep = timestep;
		}
	}
}
