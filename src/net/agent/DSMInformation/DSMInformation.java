package net.agent.DSMInformation;

import java.util.HashMap;
import java.util.Map;

public class DSMInformation {
	
	final String demand = "Demand";
	final String electricityPrice = "ElectricityPrice";
	
	private Map<Integer, Map<String, Double>> externalInformation;

    public DSMInformation() {
        // Initialisieren Sie die externe Information
        externalInformation = new HashMap<>();
    }

    // Methode zum Hinzufügen von Informationen für eine Periode
    public void addExternalDSMInformation(int period, double demand, double electricityPrice) {
        // Prüfen, ob es bereits Informationen für diese Periode gibt
        if (!externalInformation.containsKey(period)) {
            externalInformation.put(period, new HashMap<>());
        }

        // Die Informationen für die Periode in die Map einfügen
        Map<String, Double> DSMInfo = externalInformation.get(period);
        DSMInfo.put("Demand", demand);
        DSMInfo.put("ElectricityPrice", electricityPrice);
    }

    // Methode zum Abrufen der DSM-Informationen
    public Map<Integer, Map<String, Double>> getDSMInformation() {
        return externalInformation;
    }
    
    public Double getProductionQuantityForPeriod(int period) {
        Map<String, Double> periodInfo = externalInformation.get(period);
        if (periodInfo != null) {
            Double productionQuantity = periodInfo.get(demand);
            return productionQuantity;
        } else {
            return null; // Periode nicht gefunden
        }
    }
    
    public Double getElectricityPriceForPeriod(int period) {
        Map<String, Double> periodInfo = externalInformation.get(period);
        if (periodInfo != null) {
            Double electricityPrice = periodInfo.get("ElectricityPrice");
            return electricityPrice;
        } else {
            return null; // Periode nicht gefunden
        }
    }
    
	public int getLastPeriod() {
		int lastPeriod = -1; // Initialisieren Sie lastPeriod mit einem ungültigen Wert

		for (int period : externalInformation.keySet()) {
			if (period > lastPeriod) {
				lastPeriod = period;
			}
		}

		return lastPeriod;
	}
    
    
}
