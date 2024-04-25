package net.agent.DSMInformation;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the storage and retrieval of Demand-Side Management (DSM) information
 * for various time periods.
 */
public class DSMInformation {
    private Map<Integer, DSMData> externalInformation;

    public DSMInformation() {
        externalInformation = new HashMap<>();
    }

    /**
     * Adds DSM data for a specific period identified by an ID.
     */
    public void addExternalDSMInformation(int id, double demand, double electricityPrice, double powerIn,
                                          double productionQuantity, double tanklevel, int timestep) {
        externalInformation.put(id, new DSMData(timestep, demand, electricityPrice, powerIn, productionQuantity, tanklevel));
    }

    /**
     * Retrieves all DSM information stored.
     */
    public Map<Integer, DSMData> getDSMInformation() {
        return externalInformation;
    }

    /**
     * Retrieves DSM information for a specific period based on timestep.
     */
    public DSMData getDSMInformationForPeriod(int timestep) {
        return externalInformation.values().stream()
                                  .filter(data -> data.getTimestep() == timestep)
                                  .findFirst()
                                  .orElse(null);
    }

    /**
     * Retrieves the last entered DSM information.
     */
    public DSMData getLastDSMInformation() {
        return externalInformation.values().stream()
                                  .reduce((first, second) -> second)
                                  .orElse(null);
    }

    /**
     * Retrieves the electricity price for a specific period.
     */
    public Double getElectricityPriceForPeriod(int timestep) {
        DSMData data = getDSMInformationForPeriod(timestep);
        return data != null ? data.getElectricityPrice() : null;
    }

    /**
     * Retrieves the demand for a specific period.
     */
    public Double getDemandForPeriod(int timestep) {
        DSMData data = getDSMInformationForPeriod(timestep);
        return data != null ? data.getDemand() : null;
    }

    /**
     * Retrieves the production quantity for a specific period.
     */
    public Double getProductionQuantityForPeriod(int timestep) {
        DSMData data = getDSMInformationForPeriod(timestep);
        return data != null ? data.getProductionQuantity() : null;
    }

    /**
     * Finds the last timestep recorded.
     */
    public int getLastPeriod() {
        return externalInformation.values().stream()
                                  .mapToInt(DSMData::getTimestep)
                                  .max()
                                  .orElse(-1);
    }

    /**
     * Prints all stored DSM information.
     */
    public void printAllDSMInformation() {
        externalInformation.forEach((id, data) -> {
            System.out.printf("ID: %d, Timestep: %d, Demand: %.2f, ElectricityPrice: %.2f, PowerIn: %.2f, ProductionQuantity: %.2f, Tanklevel: %.2f%n",
                              id, data.getTimestep(), data.getDemand(), data.getElectricityPrice(), data.getPowerIn(),
                              data.getProductionQuantity(), data.getTanklevel());
            System.out.println("-----------------------------");
        });
    }

    /**
     * Inner class to represent DSM data for each period.
     */
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

        // Getters and Setters
        public double getDemand() { return demand; }
        public void setDemand(double demand) { this.demand = demand; }

        public double getElectricityPrice() { return electricityPrice; }
        public void setElectricityPrice(double electricityPrice) { this.electricityPrice = electricityPrice; }

        public double getPowerIn() { return powerIn; }
        public void setPowerIn(double powerIn) { this.powerIn = powerIn; }

        public double getProductionQuantity() { return productionQuantity; }
        public void setProductionQuantity(double productionQuantity) { this.productionQuantity = productionQuantity; }

        public double getTanklevel() { return tanklevel; }
        public void setTanklevel(double tanklevel) { this.tanklevel = tanklevel; }

        public int getTimestep() { return timestep; }
        public void setTimestep(int timestep) { this.timestep = timestep; }
    }
}
