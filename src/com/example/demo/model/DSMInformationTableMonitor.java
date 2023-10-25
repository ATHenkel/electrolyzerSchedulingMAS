package com.example.demo.model;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DSMInformationTableMonitor {

    @Autowired
    private DSMInformationService dsmInformationService;

    private int lastProcessedId = -1; // Initialer Wert
    
    @Scheduled(fixedRate = 5000) // Überwachung alle 60 Sekunden 60000
    public void monitorDSMInformationTable() {
        System.out.println("Überwache die DSMInformation-Tabelle...");
        
        List<dsmdata> dsmInformationList = dsmInformationService.getAllDSMInformation();

        for (dsmdata dsmInformation : dsmInformationList) {
            int currentId = dsmInformation.getId();
            
            if (currentId > lastProcessedId) {
                // Eine neue ID wurde hinzugefügt, führe den "print"-Befehl aus
       //       dsmInformationService.printAllDsmInformation();
                lastProcessedId = currentId; // Aktualisiere die zuletzt verarbeitete ID
            }
        }
    }
}
