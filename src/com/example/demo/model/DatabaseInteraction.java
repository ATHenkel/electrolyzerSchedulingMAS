package com.example.demo.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInteraction implements CommandLineRunner {

    @Autowired
    private DSMInformationService dsmInformationService;
    
    @Override
    public void run(String... args) throws Exception {

        System.out.print("Ausgabe der DSM-Information-Daten \n");
        dsmInformationService.printAllDsmInformation();
        
        System.out.println("Daten hinzufügen in der Tabelle DSM-Information");
        
		int period = 4;
		double demand = 600.0;
		double electricityPrice = 0.69;
		double energyAvailability = 690.0;

		dsmInformationService.addDsmInformation(period, demand, electricityPrice,
				energyAvailability);	

		System.out.println("----------");
		System.out.println("PEA-Scheduling");
		
		
    }
}
