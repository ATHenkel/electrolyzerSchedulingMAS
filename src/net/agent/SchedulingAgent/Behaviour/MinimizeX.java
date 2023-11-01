package net.agent.SchedulingAgent.Behaviour;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import jade.core.behaviours.OneShotBehaviour;
import net.agent.SchedulingAgent.SchedulingAgent;

public class MinimizeX extends OneShotBehaviour {

	SchedulingAgent schedulingAgent;

	public MinimizeX(SchedulingAgent schedulingAgent) {
		this.schedulingAgent = schedulingAgent;
	}
	
    public void writeOpcUaDataToExcel(int period, int iteration, double x, double mLCOH, double lambda, double minMLCOH, double z) {
    	 String filepath = "D:\\\\Dokumente\\\\OneDrive - Helmut-Schmidt-Universität\\\\04_Programmierung\\\\ElectrolyseurScheduling JADE\\\\MinX_Agent1.csv";
    	
        String data;

		// Create Data
		data = period + ";" + iteration + ";" + x + ";" + mLCOH + ";" + lambda + ";" + minMLCOH + ";" + z;
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
			{
				writer.write(data);
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	
	public double getAnnualNominalProduction() {
		double ProductionCoefficientA = this.schedulingAgent.getInternalDataModel().getProductionCoefficientA();
		double ProductionCoefficientB = this.schedulingAgent.getInternalDataModel().getProductionCoefficientB();
		double ProductionCoefficientC = this.schedulingAgent.getInternalDataModel().getProductionCoefficientC();
		double fullLoadHours = this.schedulingAgent.getInternalDataModel().getLoadFactor()*8760; //8760 hours per year
		double nominalProductionPerHour; //Nominal Production Quantity per Hour
		double nominalProductionPerYear; //Nominal Production Quantity per Year
		double x = 100;

		//Production Quantity per Hour
		nominalProductionPerHour = ProductionCoefficientA * Math.pow(x, 2) + ProductionCoefficientB * x
					+ ProductionCoefficientC;

		//Production Quantity per Year	
		nominalProductionPerYear = nominalProductionPerHour*fullLoadHours;
			
		return nominalProductionPerYear;
	}

	public double getHourlyProductionQuantity(double x) {
		double ProductionCoefficientA = this.schedulingAgent.getInternalDataModel().getProductionCoefficientA();
		double ProductionCoefficientB = this.schedulingAgent.getInternalDataModel().getProductionCoefficientB();
		double ProductionCoefficientC = this.schedulingAgent.getInternalDataModel().getProductionCoefficientC();
		boolean stateProduction = this.schedulingAgent.getInternalDataModel().isStateProduction();
		double productionQuantity;

		if (stateProduction) {
			productionQuantity = ProductionCoefficientA * Math.pow(x, 2) + ProductionCoefficientB * x
					+ ProductionCoefficientC;
		} else {
			productionQuantity = 0;
		}
		return productionQuantity;
	}

	public double minimizeLx() {
	
		// Get Information from the InternalDataModel
		double CapEx = this.schedulingAgent.getInternalDataModel().getCapEx();
		double PEL = this.schedulingAgent.getInternalDataModel().getPEL();
		int currentPeriod = this.schedulingAgent.getInternalDataModel().getCurrentPeriod();
		double electricityPrice = this.schedulingAgent.getInternalDataModel().getDSMInformation()
				.getElectricityPriceForPeriod(currentPeriod);
		double min_x_value = this.schedulingAgent.getInternalDataModel().getMinPower();
		double minPower = this.schedulingAgent.getInternalDataModel().getMinPower();
		boolean stateProduction = this.schedulingAgent.getInternalDataModel().isStateProduction();
		double lambda = this.schedulingAgent.getInternalDataModel().getLambda();
		int lifetime = this.schedulingAgent.getInternalDataModel().getLifetime();
		double discountrate = this.schedulingAgent.getInternalDataModel().getDiscountrate();
		double fullLoadHours = this.schedulingAgent.getInternalDataModel().getLoadFactor()*8760; //8760 hours per year
		double OMFactor = this.schedulingAgent.getInternalDataModel().getOMFactor();
		double z = this.schedulingAgent.getInternalDataModel().getZ();
		
		// Parameters for Solving
		double min_mLCOHLambda = Double.POSITIVE_INFINITY;
		double min_mLCOH = Double.POSITIVE_INFINITY;
		double mH2_hour;
		double increment = 	1/this.schedulingAgent.getInternalDataModel().getMaxPower() ; //TODO Inkrement anpassen X, aktuelle für 1W Leistung 
		double toleranceMinPower = 0.01; // Tolerance threshold, set to 0.01 (1%)
		
		// Calculate Present Worth Factor (PWF) and hourly Costs
		double annuityCostPerYear = (CapEx * (discountrate * Math.pow((1 + discountrate), lifetime))) / (Math.pow(1 + discountrate, lifetime) - 1);

		// Equation for Annuity Cost per Hour = AnnuityCostPerYear / fullloadhours;
		double annuityCostPerHour = annuityCostPerYear / fullLoadHours;
		
		//Calculate Costs for Operation and Maintenance as a Percentage of CapEx
		double OMCost = OMFactor*CapEx/getAnnualNominalProduction();
		
		// Check whether the electrolyser is in production mode
		if (stateProduction) {
			// Loop over the range of values of the realizable load of the electrolyzer
			for (double x = minPower; x < this.schedulingAgent.getInternalDataModel().getMaxPower(); x += increment) {
				mH2_hour = getHourlyProductionQuantity(x);
				double OpEx = PEL * (x/100) * electricityPrice;
				double mLCOH = OMCost + (annuityCostPerHour + OpEx)/mH2_hour + lambda*((x - z)/100);
				
				// If the current mLCOH value is less than the previous minimum, update the minimum and the X value.
				if (mLCOH < min_mLCOHLambda) {
					min_mLCOHLambda = OMCost + (annuityCostPerHour + OpEx)/mH2_hour + lambda*(x-z)/100; // set min mLCOH with lambda-Term
					min_mLCOH = OMCost + (annuityCostPerHour + OpEx)/mH2_hour;
					min_x_value = x;
				}
			}
		} 
		else {
			// The electrolyzer is not in production mode, therefore the production is 0
			min_x_value = 0.0;
		}
		

		/*//TODO Noch korrigieren 
		 * if (Math.abs(min_x_value - minPower) < toleranceMinPower && false) {
		 * //Erstmal nicht aktiv System.out.println("StandBy activated");
		 * this.schedulingAgent.getInternalDataModel().setStateStandby(true);
		 * this.schedulingAgent.getInternalDataModel().setStateProduction(false); }
		 */
		
		// Returns the X value at which mLCOH is minimum.
		this.schedulingAgent.getInternalDataModel().setX(min_x_value);
		this.schedulingAgent.getInternalDataModel().addIterationADMMInfo(
				this.schedulingAgent.getInternalDataModel().getCurrentPeriod(),
				this.schedulingAgent.getInternalDataModel().getIteration(), getHourlyProductionQuantity(min_x_value), 0,
				min_x_value, this.schedulingAgent.getInternalDataModel().getZ(), min_mLCOH);
		return min_x_value;
	}

	@Override
	public void action() {

		// Minimize Lx
		minimizeLx();

		//Messages can be received again 
		if (this.schedulingAgent.getInternalDataModel().isEnableMessageReceive()) {
			this.schedulingAgent.getInternalDataModel().setReceiveMessages(true);
		}

		// Next Behaviour to be executed
		BroadcastProductionData broadcastProductionData = new BroadcastProductionData(schedulingAgent);
		this.schedulingAgent.addBehaviour(broadcastProductionData);
	}

}
