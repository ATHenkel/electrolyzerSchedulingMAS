package net.agent.DSMInformation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Manages and stores the results of scheduling operations for each period.
 */
public class SchedulingResults {
    private Map<Integer, Map<String, Object>> results;

    /**
     * Constructor initializes the map to store scheduling results.
     */
    public SchedulingResults() {
        results = new HashMap<>();
    }

    /**
     * Adds scheduling results for a specific period.
     * @param period The scheduling period.
     * @param electricityPrice Price of electricity for the period.
     * @param standbyState Standby state of the system.
     * @param idleState Idle state of the system.
     * @param productionState Production state of the system.
     * @param setpoint The setpoint value for the period.
     * @param mLCOH Marginal Levelized Cost of Hydrogen.
     * @param productionQuantity Quantity of production.
     * @param demand Demand for the period.
     */
    public void addResult(int period, double electricityPrice, boolean standbyState, boolean idleState,
            boolean productionState, double setpoint, double mLCOH, double productionQuantity, double demand) {
        Map<String, Object> result = new HashMap<>();
        result.put("ElectricityPrice", electricityPrice);
        result.put("StandbyState", standbyState);
        result.put("IdleState", idleState);
        result.put("ProductionState", productionState);
        result.put("Setpoint", setpoint);
        result.put("mLCOH", mLCOH);
        result.put("ProductionQuantity", productionQuantity);
        result.put("Demand", demand);

        results.put(period, result);
    }

    /**
     * Retrieves the result map for a specified period.
     * @param period The period for which results are requested.
     * @return The map of results for the period.
     */
    public Map<String, Object> getResult(int period) {
        return results.get(period);
    }

    /**
     * Saves the scheduling results to a CSV file.
     * @param filePath The path of the file to which the results will be written.
     */
    public void saveSchedulingResultsToCSV(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Period;ElectricityPrice;StandbyState;IdleState;ProductionState;Setpoint;mLCOH;ProductionQuantity;Demand");
            writer.newLine();

            for (Map.Entry<Integer, Map<String, Object>> entry : results.entrySet()) {
                Map<String, Object> result = entry.getValue();
                writer.write(String.format(Locale.US, "%d;%.2f;%b;%b;%b;%.1f;%.2f;%.2f;%.2f",
                        entry.getKey(),
                        result.get("ElectricityPrice"),
                        result.get("StandbyState"),
                        result.get("IdleState"),
                        result.get("ProductionState"),
                        result.get("Setpoint"),
                        result.get("mLCOH"),
                        result.get("ProductionQuantity"),
                        result.get("Demand")));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing the CSV file: " + e.getMessage());
        }
    }
    
    /**
     * Gets the First Period 
     */
    public int getFirstPeriod() {
        Iterator<Map.Entry<Integer, Map<String, Object>>> iterator = results.entrySet().iterator();
        if (iterator.hasNext()) {
            return iterator.next().getKey();
        }
        return -1; // Return -1 or handle it differently if the map is empty
    }

    /**
     * Prints the scheduling results for all periods.
     */
    public void printSchedulingResults() {
        if (results.isEmpty()) {
            System.out.println("No optimization results found.");
            return;
        }

        for (Integer period : results.keySet()) {
            Map<String, Object> result = results.get(period);
            System.out.printf("Optimization results for Period %d:%n", period);
            System.out.printf("Electricity Price: %.2f%n", result.get("ElectricityPrice"));
            System.out.println("Standby State: " + (Boolean) result.get("StandbyState"));
            System.out.println("Idle State: " + (Boolean) result.get("IdleState"));
            System.out.println("Production State: " + (Boolean) result.get("ProductionState"));
            System.out.printf("Setpoint: %.2f%n", result.get("Setpoint"));
            System.out.printf("mLCOH: %.2f%n", result.get("mLCOH"));
            System.out.printf("Production Quantity: %.2f%n", result.get("ProductionQuantity"));
            System.out.printf("Demand: %.2f%n%n", result.get("Demand"));
        }
    }

    /**
     * Returns the total number of scheduled periods.
     * @return The number of periods for which results are available.
     */
    public int getNumberScheduledPeriods() {
        return results.size();
    }
}
