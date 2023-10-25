package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class dsmdata {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
	

    @Column(name = "demand") 
    private double demand;
    
    @Column(name = "productionquantity") 
    private double productionquantity;
    
	@Column(name = "tanklevel") 
    private double tanklevel;
    
    @Column(name = "timestep") 
    private int timestep;

    @Column(name = "electricityprice") 
    private double electricityPrice;

    @Column(name = "power_in") 
    private double power_in;

    // Getter and Setter
    public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public double getDemand() {
		return demand;
	}
	public void setDemand(double demand) {
		this.demand = demand;
	}
	public double getelectricityPrice() {
		return electricityPrice;
	}
	public void setelectricityPrice(double electricityPrice) {
		this.electricityPrice = electricityPrice;
	}
	public double getenergyAvailability() {
		return power_in;
	}
	public void setenergyAvailability(double energyAvailability) {
		this.power_in = energyAvailability;
	}
    
    public double getProductionquantity() {
		return productionquantity;
	}
	public void setProductionquantity(double productionquantity) {
		this.productionquantity = productionquantity;
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
	public double getPower_in() {
		return power_in;
	}
	public void setPower_in(double power_in) {
		this.power_in = power_in;
	}
	
}
