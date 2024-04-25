package net.agent.DSMInformation;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SchedulingResults {
    private Map<Integer, Map<String, Object>> results;

    public SchedulingResults() {
        results = new HashMap<>();
    }

    public void addResult(int period, double electricityPrice, boolean standbyState, boolean idleState,
            boolean productionState, double setpoint, double mLCOH, double productionQuantity,
            double demand) {
    	
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

    public Map<String, Object> getResult(int period) {
        return results.get(period);
    }
    
    public int getFirstPeriod() {
        Iterator<Map.Entry<Integer, Map<String, Object>>> iterator = results.entrySet().iterator();
        if (iterator.hasNext()) {
            return iterator.next().getKey();
        }
        return -1; // Return -1 or handle it differently if the map is empty
    }
    
    public void printSchedulingResults() {
    	// Check whether optimization results are available
        if (results.isEmpty()) {
            System.out.println("No optimization results found.");
            return;
        }

     // Iterate over all periods in the optimization results
        for (Integer period : results.keySet()) {
            System.out.println("Optimierungsergebnisse für Periode " + period + ":");

         // Get results for this period
            Map<String, Object> result = results.get(period);

            System.out.printf("Strompreis: %.2f%n", result.get("ElectricityPrice"));
            System.out.println("Standby State: " + (result.get("StandbyState").equals(true) ? "An" : "Aus"));
            System.out.println("Idle State: " + (result.get("IdleState").equals(true) ? "An" : "Aus"));
            System.out.println("Production State: " + (result.get("ProductionState").equals(true) ? "An" : "Aus"));
            System.out.printf("Setpoint: %.2f%n", result.get("Setpoint"));
            System.out.printf("mLCOH: %.2f%n", result.get("mLCOH"));
            System.out.printf("Production Quantity: %.2f%n", result.get("ProductionQuantity"));
            System.out.printf("Demand: %.2f%n", result.get("Demand"));

            System.out.println(); // Blank line to separate results for different periods
        }
    }
    
    public void saveSchedulingResultsToCSV(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
        	// Writing the header
            writer.write("Period;ElectricityPrice;StandbyState;IdleState;ProductionState;Setpoint;mLCOH;ProductionQuantity;Demand");
            writer.newLine();

            // Schreiben der Daten jeder Periode
            for (Map.Entry<Integer, Map<String, Object>> entry : results.entrySet()) {
                int period = entry.getKey();
                Map<String, Object> result = entry.getValue();
                writer.write(String.format("%d;%.2f;%b;%b;%b;%.2f;%.2f;%.2f;%.2f",
                        period,
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
    
    public Map<Integer, Map<String, Object>> getResults() {
        return results;
    }
    
    public int getNumberScheduledPeriods() {
        return results.size(); //Returns the number of results (periods)
    }

    
}




