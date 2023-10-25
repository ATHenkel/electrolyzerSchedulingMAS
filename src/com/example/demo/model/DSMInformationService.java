package com.example.demo.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DSMInformationService {

	@Autowired
	DSMInformationRepository dsmInformationRepository;

	public void printAllDsmInformation() {
		List<dsmdata> dsmInformationList = dsmInformationRepository.findAll();

		for (dsmdata dsmInformation : dsmInformationList) {
			System.out.println("ID: " + dsmInformation.getId());
			System.out.println("TimeStep: " + dsmInformation.getTimestep());
			System.out.println("Demand: " + dsmInformation.getDemand());
			System.out.println("ElectricityPrice: " + dsmInformation.getelectricityPrice());
			System.out.println("EnergyAvailability: " + dsmInformation.getenergyAvailability());
			System.out.println("-----------------------------");
		}
	}

	public dsmdata addDsmInformation(int timestep, double demand, double electricityPrice,
			double energyAvailability) {
		dsmdata dsmInformation = new dsmdata();
		dsmInformation.setTimestep(timestep);
		dsmInformation.setDemand(demand);
		dsmInformation.setelectricityPrice(electricityPrice);
		dsmInformation.setenergyAvailability(energyAvailability);

		// Die save-Methode fügt die Entität in die Datenbank ein und aktualisiert die ID automatisch
		return dsmInformationRepository.save(dsmInformation);
	}

	public void printDSMInformationById(int id) {
		Optional<dsmdata> dsmInformationOptional = dsmInformationRepository.findById((long) id);

		if (dsmInformationOptional.isPresent()) {
			dsmdata dsmInformation = dsmInformationOptional.get();
			System.out.println("ID: " + dsmInformation.getId());
			System.out.println("Periode: " + dsmInformation.getTimestep());
			System.out.println("Demand: " + dsmInformation.getDemand());
			System.out.println("ElectricityPrice: " + dsmInformation.getelectricityPrice());
			System.out.println("EnergyAvailability: " + dsmInformation.getenergyAvailability());
			System.out.println("-----------------------------");
		} else {
			System.out.println("Keine DSMInformation mit ID " + id + " gefunden.");
		}
	}

	public List<dsmdata> getAllDSMInformation() {
		return dsmInformationRepository.findAll();
	}
}
