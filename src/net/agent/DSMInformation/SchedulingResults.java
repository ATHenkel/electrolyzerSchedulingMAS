package net.agent.DSMInformation;
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
        // Überprüfen Sie, ob Optimierungsergebnisse vorhanden sind
        if (results.isEmpty()) {
            System.out.println("Keine Optimierungsergebnisse gefunden.");
            return;
        }

        // Iterieren Sie über alle Perioden in den Optimierungsergebnissen
        for (Integer period : results.keySet()) {
            System.out.println("Optimierungsergebnisse für Periode " + period + ":");

            // Holen Sie die Ergebnisse für diese Periode
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
    
    public Map<Integer, Map<String, Object>> getResults() {
        return results;
    }
    
    public int getNumberScheduledPeriods() {
        return results.size(); //Returns the number of results (periods)
    }

    
}




